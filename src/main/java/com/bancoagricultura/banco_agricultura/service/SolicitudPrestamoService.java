package com.bancoagricultura.banco_agricultura.service;

import com.bancoagricultura.banco_agricultura.model.Cuenta;
import com.bancoagricultura.banco_agricultura.model.Persona;
import com.bancoagricultura.banco_agricultura.model.SolicitudPrestamo;
import com.bancoagricultura.banco_agricultura.model.Usuario;
import com.bancoagricultura.banco_agricultura.repo.CuentaRepository;
import com.bancoagricultura.banco_agricultura.repo.PersonaRepository;
import com.bancoagricultura.banco_agricultura.repo.SolicitudPrestamoRepository;
import com.bancoagricultura.banco_agricultura.repo.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SolicitudPrestamoService {

    private final SolicitudPrestamoRepository solicitudPrestamoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PersonaRepository personaRepository;
    private final CuentaRepository cuentaRepository;

    private static final BigDecimal TREINTA_PORCIENTO = new BigDecimal("0.30");
    private static final MathContext MC = new MathContext(20, RoundingMode.HALF_UP);

    // ============================
    // Creación de solicitudes
    // ============================

    // Cliente solicita préstamo para su propia cuenta
    public SolicitudPrestamo crearSolicitudPorCliente(Long clienteId, Long cuentaDestinoId, BigDecimal montoSolicitado, String observaciones) {
        Usuario cliente = requireUsuario(clienteId);
        requireRol(cliente, Usuario.Rol.CLIENTE);

        Cuenta cuentaDestino = requireCuenta(cuentaDestinoId);
        requireTitular(cuentaDestino, cliente);

        ValidacionPrestamo vp = validarReglasPrestamo(cliente, montoSolicitado);

        SolicitudPrestamo solicitud = new SolicitudPrestamo();
        solicitud.setUsuarioCliente(cliente);
        solicitud.setCuentaDestino(cuentaDestino);
        solicitud.setMontoSolicitado(montoSolicitado);
        solicitud.setSolicitadoPorUsuario(cliente);
        solicitud.setEstadoCaso(SolicitudPrestamo.EstadoCaso.EN_ESPERA);
        solicitud.setObservaciones((observaciones != null ? observaciones + " | " : "") +
                String.format("tasa=%s, cuotaMensual=%s, plazoAnios=%s",
                        vp.tasa.toPlainString(),
                        vp.cuotaMensual.toPlainString(),
                        vp.plazoAnios.toPlainString()));
        solicitud.setCreadoEn(Instant.now());

        return solicitudPrestamoRepository.save(solicitud);
    }

    // Cajero solicita préstamo en nombre de un cliente, validando DUI–cuenta
    public SolicitudPrestamo crearSolicitudPorCajeroConDui(Long cajeroId, String duiCliente, Long cuentaDestinoId, BigDecimal montoSolicitado, String observaciones) {
        Usuario cajero = requireUsuario(cajeroId);
        requireRol(cajero, Usuario.Rol.CAJERO);

        Persona personaCliente = requirePersonaPorDui(duiCliente);
        Usuario cliente = requireUsuarioPorPersona(personaCliente);

        Cuenta cuentaDestino = requireCuenta(cuentaDestinoId);
        requireTitular(cuentaDestino, cliente);

        ValidacionPrestamo vp = validarReglasPrestamo(cliente, montoSolicitado);

        SolicitudPrestamo solicitud = new SolicitudPrestamo();
        solicitud.setUsuarioCliente(cliente);
        solicitud.setCuentaDestino(cuentaDestino);
        solicitud.setMontoSolicitado(montoSolicitado);
        solicitud.setSolicitadoPorUsuario(cajero);
        solicitud.setEstadoCaso(SolicitudPrestamo.EstadoCaso.EN_ESPERA);
        solicitud.setObservaciones((observaciones != null ? observaciones + " | " : "") +
                String.format("tasa=%s, cuotaMensual=%s, plazoAnios=%s (cliente DUI=%s)",
                        vp.tasa.toPlainString(),
                        vp.cuotaMensual.toPlainString(),
                        vp.plazoAnios.toPlainString(),
                        duiCliente));
        solicitud.setCreadoEn(Instant.now());

        return solicitudPrestamoRepository.save(solicitud);
    }

    // ============================
    // Gestión por Gerente de Sucursal
    // ============================

    public void aprobarSolicitud(Long gerenteSucursalId, Long solicitudId) {
        Usuario gerente = requireUsuario(gerenteSucursalId);
        requireRol(gerente, Usuario.Rol.GERENTE_SUCURSAL);

        SolicitudPrestamo solicitud = requireSolicitud(solicitudId);
        requireEstadoEnEspera(solicitud);

        solicitud.setEstadoCaso(SolicitudPrestamo.EstadoCaso.APROBADO);
        solicitud.setGestionadoPorUsuario(gerente);
        solicitud.setActualizadoEn(Instant.now());
        solicitudPrestamoRepository.save(solicitud);

        // Nota: la apertura del préstamo y depósito del monto aprobado
        // se haría en PrestamoService y TransaccionService respectivamente.
    }

    public void rechazarSolicitud(Long gerenteSucursalId, Long solicitudId, String observaciones) {
        Usuario gerente = requireUsuario(gerenteSucursalId);
        requireRol(gerente, Usuario.Rol.GERENTE_SUCURSAL);

        SolicitudPrestamo solicitud = requireSolicitud(solicitudId);
        requireEstadoEnEspera(solicitud);

        solicitud.setEstadoCaso(SolicitudPrestamo.EstadoCaso.RECHAZADO);
        solicitud.setGestionadoPorUsuario(gerente);
        solicitud.setObservaciones((observaciones != null ? observaciones : ""))
        ;
        solicitud.setActualizadoEn(Instant.now());
        solicitudPrestamoRepository.save(solicitud);
    }

    // ============================
    // Consultas
    // ============================

    public List<SolicitudPrestamo> listarSolicitudesPorCliente(Long clienteId) {
        Usuario cliente = requireUsuario(clienteId);
        return solicitudPrestamoRepository.findByUsuarioClienteOrderByCreadoEnDesc(cliente);
    }

    public List<SolicitudPrestamo> listarSolicitudesPorEstado(SolicitudPrestamo.EstadoCaso estado) {
        return solicitudPrestamoRepository.findByEstadoCasoOrderByCreadoEnDesc(estado);
    }

    public List<SolicitudPrestamo> listarTodasParaGerenteSucursal(Long gerenteSucursalId) {
        Usuario gerente = requireUsuario(gerenteSucursalId);
        requireRol(gerente, Usuario.Rol.GERENTE_SUCURSAL);
        return solicitudPrestamoRepository.findAllByOrderByCreadoEnDesc();
    }

    // ============================
    // Validaciones y cálculo de préstamo
    // ============================

    private ValidacionPrestamo validarReglasPrestamo(Usuario cliente, BigDecimal montoSolicitado) {
        Persona persona = cliente.getPersona();
        if (persona == null) {
            throw new IllegalStateException("El cliente no tiene persona asociada");
        }
        BigDecimal salario = persona.getSalario();
        if (salario == null || salario.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El salario del cliente es inválido");
        }

        // Determinar tope y tasa según salario
        ReglaPrestamo regla = determinarReglaPorSalario(salario);

        if (montoSolicitado.compareTo(regla.montoMaximo) > 0) {
            throw new IllegalArgumentException(String.format(
                    "El monto solicitado (%s) excede el máximo permitido (%s) para su rango salarial",
                    montoSolicitado.toPlainString(), regla.montoMaximo.toPlainString()
            ));
        }

        // Calcular cuota mensual con tasa anual -> tasa mensual
        BigDecimal tasaMensual = regla.tasaAnual.divide(new BigDecimal("12"), MC);
        BigDecimal cuota = calcularCuotaMensual(montoSolicitado, tasaMensual, null); // n se calculará luego

        // La cuota no debe exceder 30% del salario
        BigDecimal maxCuota = salario.multiply(TREINTA_PORCIENTO).setScale(2, RoundingMode.HALF_UP);

        // Si cuota con un n preliminar no es válida, calcula el plazo mínimo tal que cuota <= 30%
        BigDecimal nMeses = calcularPlazoMinimoMeses(montoSolicitado, tasaMensual, maxCuota);
        BigDecimal cuotaFinal = calcularCuotaMensual(montoSolicitado, tasaMensual, nMeses);

        if (cuotaFinal.compareTo(maxCuota) > 0) {
            throw new IllegalStateException("No es posible ajustar un plazo con cuota ≤ 30% del salario");
        }

        BigDecimal anios = nMeses.divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP);

        return new ValidacionPrestamo(regla.tasaAnual, cuotaFinal, anios);
    }

    private ReglaPrestamo determinarReglaPorSalario(BigDecimal salario) {
        // Rangos según especificación
        if (salario.compareTo(new BigDecimal("365")) < 0) {
            return new ReglaPrestamo(new BigDecimal("10000"), new BigDecimal("0.03"));
        } else if (salario.compareTo(new BigDecimal("600")) < 0) {
            return new ReglaPrestamo(new BigDecimal("25000"), new BigDecimal("0.03"));
        } else if (salario.compareTo(new BigDecimal("900")) < 0) {
            return new ReglaPrestamo(new BigDecimal("35000"), new BigDecimal("0.04"));
        } else if (salario.compareTo(new BigDecimal("1000")) > 0) {
            return new ReglaPrestamo(new BigDecimal("50000"), new BigDecimal("0.05"));
        } else {
            // salario entre 900 y 1000 inclusive → asumir regla del tramo anterior (900–1000: 4%)
            return new ReglaPrestamo(new BigDecimal("35000"), new BigDecimal("0.04"));
        }
    }

    // Fórmula de anualidad: cuota = P * i / (1 - (1+i)^-n)
    private BigDecimal calcularCuotaMensual(BigDecimal principal, BigDecimal tasaMensual, BigDecimal nMesesNullable) {
        if (tasaMensual.compareTo(BigDecimal.ZERO) == 0) {
            // Sin interés: cuota = principal / n; si n es null, por defecto 12 meses
            BigDecimal n = nMesesNullable != null ? nMesesNullable : new BigDecimal("12");
            return principal.divide(n, 2, RoundingMode.HALF_UP);
        }
        BigDecimal n = nMesesNullable != null ? nMesesNullable : new BigDecimal("12"); // default si no se pasa n
        // Usar double para la potencia y reconvertir (por simplicidad)
        double i = tasaMensual.doubleValue();
        double nd = n.doubleValue();
        double factor = 1 - Math.pow(1 + i, -nd);
        BigDecimal cuota = principal.multiply(tasaMensual, MC)
                .divide(new BigDecimal(factor, MC), 2, RoundingMode.HALF_UP);
        return cuota;
    }

    // Resolver n mínimo tal que cuota(P, i, n) ≤ maxCuota
    // n = - ln(1 - P*i/maxCuota) / ln(1+i)
    private BigDecimal calcularPlazoMinimoMeses(BigDecimal principal, BigDecimal tasaMensual, BigDecimal maxCuota) {
        if (tasaMensual.compareTo(BigDecimal.ZERO) == 0) {
            // Sin interés: n = ceil(P / maxCuota)
            BigDecimal n = principal.divide(maxCuota, 0, RoundingMode.CEILING);
            return n.max(new BigDecimal("1"));
        }
        double P = principal.doubleValue();
        double i = tasaMensual.doubleValue();
        double C = maxCuota.doubleValue();

        double term = 1 - (P * i) / C;
        if (term <= 0) {
            // Necesitarías n → infinito: no viable
            throw new IllegalStateException("La cuota mínima supera el 30% del salario incluso con plazos extendidos");
        }
        double n = -Math.log(term) / Math.log(1 + i);
        // Redondear hacia arriba a meses enteros
        int nInt = (int) Math.ceil(n);
        return new BigDecimal(nInt);
    }

    // ============================
    // Helpers de acceso y reglas
    // ============================

    private Usuario requireUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
    }

    private Persona requirePersonaPorDui(String dui) {
        return personaRepository.findByDui(dui)
                .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada con DUI: " + dui));
    }

    private Usuario requireUsuarioPorPersona(Persona persona) {
        return usuarioRepository.findByPersona(persona)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado para persona: " + persona.getId()));
    }

    private Cuenta requireCuenta(Long id) {
        return cuentaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada: " + id));
    }

    private void requireTitular(Cuenta cuenta, Usuario usuarioEsperado) {
        if (!cuenta.getClienteUsuario().getId().equals(usuarioEsperado.getId())) {
            throw new SecurityException("La cuenta no pertenece al cliente indicado");
        }
    }

    private void requireRol(Usuario actor, Usuario.Rol rolRequerido) {
        if (actor.getRol() != rolRequerido) {
            throw new SecurityException("Acción permitida solo para rol: " + rolRequerido);
        }
    }

    private SolicitudPrestamo requireSolicitud(Long id) {
        return solicitudPrestamoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud de préstamo no encontrada: " + id));
    }

    private void requireEstadoEnEspera(SolicitudPrestamo solicitud) {
        if (solicitud.getEstadoCaso() != SolicitudPrestamo.EstadoCaso.EN_ESPERA) {
            throw new IllegalStateException("El caso ya fue gestionado");
        }
    }

    // ============================
    // Tipos auxiliares
    // ============================

    private record ReglaPrestamo(BigDecimal montoMaximo, BigDecimal tasaAnual) { }
    private record ValidacionPrestamo(BigDecimal tasa, BigDecimal cuotaMensual, BigDecimal plazoAnios) { }
}
