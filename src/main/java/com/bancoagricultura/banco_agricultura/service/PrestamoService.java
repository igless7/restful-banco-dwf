package com.bancoagricultura.banco_agricultura.service;

import com.bancoagricultura.banco_agricultura.model.*;
import com.bancoagricultura.banco_agricultura.repo.CuentaRepository;
import com.bancoagricultura.banco_agricultura.repo.PrestamoRepository;
import com.bancoagricultura.banco_agricultura.repo.SolicitudPrestamoRepository;
import com.bancoagricultura.banco_agricultura.repo.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PrestamoService {

    private final PrestamoRepository prestamoRepository;
    private final SolicitudPrestamoRepository solicitudPrestamoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CuentaRepository cuentaRepository;
    private final TransaccionService transaccionService; // para depositar el préstamo y registrar pagos

    private static final BigDecimal TREINTA_PORCIENTO = new BigDecimal("0.30");
    private static final MathContext MC = new MathContext(20, RoundingMode.HALF_UP);

    // ============================
    // Aprobación y creación de préstamo
    // ============================

    /**
     * Crea el préstamo a partir de una solicitud APROBADA.
     * - Solo GERENTE_SUCURSAL puede aprobar y crear el préstamo.
     * - Calcula tasa, plazo y cuota para cumplir cuota ≤ 30% del salario.
     * - Deposita el monto en la cuenta destino (Transaccion tipo DEPOSITO_PRESTAMO).
     */
    public Prestamo crearDesdeSolicitud(Long gerenteSucursalId, Long solicitudId) {
        Usuario gerente = requireUsuario(gerenteSucursalId);
        requireRol(gerente, Usuario.Rol.GERENTE_SUCURSAL);

        SolicitudPrestamo solicitud = requireSolicitud(solicitudId);
        if (solicitud.getEstadoCaso() != SolicitudPrestamo.EstadoCaso.APROBADO) {
            throw new IllegalStateException("La solicitud debe estar APROBADA para crear el préstamo");
        }

        Usuario cliente = solicitud.getUsuarioCliente();
        if (cliente.getRol() != Usuario.Rol.CLIENTE) {
            throw new IllegalStateException("El préstamo solo puede asignarse a usuarios con rol CLIENTE");
        }

        Cuenta cuentaDestino = requireCuenta(solicitud.getCuentaDestino().getId());
        requireTitular(cuentaDestino, cliente);

        BigDecimal montoAprobado = solicitud.getMontoSolicitado();
        Validacion v = validarTasaCuotaPlazo(cliente, montoAprobado);

        Prestamo prestamo = new Prestamo();
        prestamo.setSolicitudPrestamo(solicitud);
        prestamo.setUsuarioCliente(cliente);
        prestamo.setMontoAprobado(montoAprobado);
        prestamo.setTasaInteres(v.tasaAnual);       // anual (ej. 0.03)
        prestamo.setSaldoPendiente(montoAprobado);
        prestamo.setPlazoAnios(v.plazoAnios.intValue());
        prestamo.setCuotaMensual(v.cuotaMensual);
        prestamo.setFechaAprobacion(Instant.now());
        prestamo.setFechaVencimiento(LocalDate.now().plusYears(v.plazoAnios.intValue()));
        prestamo.setProximoPago(siguienteMesPrimerDia(LocalDate.now()));
        prestamo.setCuentaDestino(cuentaDestino);
        prestamo.setAprobadoPorUsuario(gerente);
        prestamo.setEstado(Prestamo.Estado.ACTIVO);
        prestamo.setCreadoEn(Instant.now());

        Prestamo guardado = prestamoRepository.save(prestamo);

        // Depositar el monto aprobado en la cuenta destino
        transaccionService.registrarTransaccionBasica(
                Transaccion.Tipo.DEPOSITO_PRESTAMO,
                montoAprobado,
                BigDecimal.ZERO,
                null,
                cuentaDestino,
                cuentaDestino,
                gerente,
                "Depósito de préstamo desde solicitud " + solicitud.getId(),
                null
        );

        return guardado;
    }

    // ============================
    // Pagos y cancelación
    // ============================

    /**
     * Registra el pago mensual del préstamo:
     * - Valida que el ejecutor sea el CLIENTE o un CAJERO.
     * - Aplica el pago disminuyendo el saldo pendiente.
     * - Registra transacción tipo PAGO_PRESTAMO debitando desde la cuenta del cliente.
     */
    public Prestamo pagarCuota(Long ejecutorId, Long prestamoId, Long cuentaOrigenId, BigDecimal monto, String referencia) {
        Usuario ejecutor = requireUsuario(ejecutorId);
        Prestamo prestamo = requirePrestamo(prestamoId);

        // Solo el cliente titular o un cajero puede registrar pagos
        boolean esClienteTitular = ejecutor.getId().equals(prestamo.getUsuarioCliente().getId()) && ejecutor.getRol() == Usuario.Rol.CLIENTE;
        boolean esCajero = ejecutor.getRol() == Usuario.Rol.CAJERO;
        if (!esClienteTitular && !esCajero) {
            throw new SecurityException("Solo el cliente titular o un cajero pueden registrar pagos de préstamo");
        }

        Cuenta cuentaOrigen = requireCuenta(cuentaOrigenId);
        requireTitular(cuentaOrigen, prestamo.getUsuarioCliente());
        requireCuentaActiva(cuentaOrigen);

        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto del pago debe ser mayor a cero");
        }
        if (cuentaOrigen.getSaldoDisponible().compareTo(monto) < 0) {
            throw new IllegalStateException("Saldo insuficiente en la cuenta origen");
        }

        // Registrar transacción de pago (debitando la cuenta del cliente)
        transaccionService.registrarTransaccionBasica(
                Transaccion.Tipo.PAGO_PRESTAMO,
                monto,
                BigDecimal.ZERO,
                cuentaOrigen,
                null,
                cuentaOrigen,
                ejecutor,
                referencia != null ? referencia : "Pago de préstamo #" + prestamo.getId(),
                null
        );

        // Actualizar saldos de la cuenta (retiro)
        cuentaOrigen.setSaldo(cuentaOrigen.getSaldo().subtract(monto));
        cuentaOrigen.setSaldoDisponible(cuentaOrigen.getSaldoDisponible().subtract(monto));
        cuentaRepository.save(cuentaOrigen);

        // Disminuir saldo pendiente del préstamo
        BigDecimal nuevoSaldo = prestamo.getSaldoPendiente().subtract(monto).setScale(2, RoundingMode.HALF_UP);
        prestamo.setSaldoPendiente(nuevoSaldo);

        // Actualizar próximo pago (mes siguiente)
        prestamo.setProximoPago(siguienteMesPrimerDia(prestamo.getProximoPago() != null ? prestamo.getProximoPago() : LocalDate.now()));

        // Cancelar si saldo llega a 0 o menos
        if (nuevoSaldo.compareTo(BigDecimal.ZERO) <= 0) {
            prestamo.setEstado(Prestamo.Estado.CANCELADO);
        }

        prestamo.setActualizadoEn(Instant.now());
        return prestamoRepository.save(prestamo);
    }

    // ============================
    // Consultas
    // ============================

    public Prestamo obtenerPrestamo(Long id) {
        return prestamoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Préstamo no encontrado: " + id));
    }

    public List<Prestamo> listarPrestamosPorCliente(Long clienteId) {
        Usuario cliente = requireUsuario(clienteId);
        return prestamoRepository.findByUsuarioClienteOrderByFechaAprobacionDesc(cliente);
    }

    // ============================
    // Validación de reglas (tasa, cuota, plazo)
    // ============================

    private Validacion validarTasaCuotaPlazo(Usuario cliente, BigDecimal monto) {
        Persona persona = cliente.getPersona();
        if (persona == null) throw new IllegalStateException("El cliente no tiene persona asociada");
        BigDecimal salario = persona.getSalario();
        if (salario == null || salario.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El salario del cliente es inválido");
        }

        Regla regla = determinarReglaPorSalario(salario);
        if (monto.compareTo(regla.montoMax) > 0) {
            throw new IllegalArgumentException("El monto solicitado excede el máximo permitido para su rango salarial");
        }

        BigDecimal maxCuota = salario.multiply(TREINTA_PORCIENTO).setScale(2, RoundingMode.HALF_UP);
        BigDecimal tasaMensual = regla.tasaAnual.divide(new BigDecimal("12"), MC);

        BigDecimal nMeses = calcularPlazoMinimoMeses(monto, tasaMensual, maxCuota);
        BigDecimal cuota = calcularCuotaMensual(monto, tasaMensual, nMeses);
        BigDecimal anios = nMeses.divide(new BigDecimal("12"), 0, RoundingMode.CEILING);

        if (cuota.compareTo(maxCuota) > 0) {
            throw new IllegalStateException("No es posible ajustar un plazo con cuota ≤ 30% del salario");
        }

        return new Validacion(regla.tasaAnual, cuota, anios);
    }

    private Regla determinarReglaPorSalario(BigDecimal salario) {
        if (salario.compareTo(new BigDecimal("365")) < 0) {
            return new Regla(new BigDecimal("10000"), new BigDecimal("0.03"));
        } else if (salario.compareTo(new BigDecimal("600")) < 0) {
            return new Regla(new BigDecimal("25000"), new BigDecimal("0.03"));
        } else if (salario.compareTo(new BigDecimal("900")) < 0) {
            return new Regla(new BigDecimal("35000"), new BigDecimal("0.04"));
        } else if (salario.compareTo(new BigDecimal("1000")) > 0) {
            return new Regla(new BigDecimal("50000"), new BigDecimal("0.05"));
        } else {
            return new Regla(new BigDecimal("35000"), new BigDecimal("0.04"));
        }
    }

    // Fórmula anualidad: cuota = P * i / (1 - (1+i)^-n)
    private BigDecimal calcularCuotaMensual(BigDecimal principal, BigDecimal tasaMensual, BigDecimal nMeses) {
        if (tasaMensual.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(nMeses, 2, RoundingMode.HALF_UP);
        }
        double P = principal.doubleValue();
        double i = tasaMensual.doubleValue();
        double n = nMeses.doubleValue();
        double factor = 1 - Math.pow(1 + i, -n);
        BigDecimal cuota = principal.multiply(tasaMensual, MC)
                .divide(new BigDecimal(factor, MC), 2, RoundingMode.HALF_UP);
        return cuota;
    }

    // n mínimo tal que cuota(P,i,n) ≤ maxCuota
    private BigDecimal calcularPlazoMinimoMeses(BigDecimal principal, BigDecimal tasaMensual, BigDecimal maxCuota) {
        if (tasaMensual.compareTo(BigDecimal.ZERO) == 0) {
            BigDecimal n = principal.divide(maxCuota, 0, RoundingMode.CEILING);
            return n.max(new BigDecimal("1"));
        }
        double P = principal.doubleValue();
        double i = tasaMensual.doubleValue();
        double C = maxCuota.doubleValue();
        double term = 1 - (P * i) / C;
        if (term <= 0) throw new IllegalStateException("La cuota mínima supera el 30% del salario incluso con plazos extendidos");
        int nInt = (int) Math.ceil(-Math.log(term) / Math.log(1 + i));
        return new BigDecimal(nInt);
    }

    private LocalDate siguienteMesPrimerDia(LocalDate base) {
        return base.with(TemporalAdjusters.firstDayOfNextMonth());
    }

    // ============================
    // Helpers de acceso y reglas
    // ============================

    private Prestamo requirePrestamo(Long id) {
        return prestamoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Préstamo no encontrado: " + id));
    }

    private SolicitudPrestamo requireSolicitud(Long id) {
        return solicitudPrestamoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud de préstamo no encontrada: " + id));
    }

    private Usuario requireUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
    }

    private Cuenta requireCuenta(Long id) {
        return cuentaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada: " + id));
    }

    private void requireTitular(Cuenta cuenta, Usuario usuarioEsperado) {
        if (!cuenta.getClienteUsuario().getId().equals(usuarioEsperado.getId())) {
            throw new SecurityException("La cuenta destino no pertenece al cliente del préstamo");
        }
    }

    private void requireRol(Usuario actor, Usuario.Rol rol) {
        if (actor.getRol() != rol) {
            throw new SecurityException("Acción permitida solo para rol: " + rol);
        }
    }
}
