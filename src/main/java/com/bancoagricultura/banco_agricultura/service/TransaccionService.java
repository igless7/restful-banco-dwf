package com.bancoagricultura.banco_agricultura.service;

import com.bancoagricultura.banco_agricultura.model.*;
import com.bancoagricultura.banco_agricultura.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TransaccionService {

    private final TransaccionRepository transaccionRepository;
    private final CuentaRepository cuentaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PersonaRepository personaRepository;
    private final ComisionColaboradorRepository comisionColaboradorRepository;

    private static final BigDecimal COMISION_DEPENDIENTE = new BigDecimal("0.05");

    // ============================
    // Depósitos
    // ============================

    // Cliente deposita en su propia cuenta (sin comisión)
    public Transaccion depositarPorCliente(Long clienteId, Long cuentaId, BigDecimal monto, String referencia) {
        Usuario cliente = requireUsuario(clienteId);
        requireRol(cliente, Usuario.Rol.CLIENTE);

        Cuenta cuenta = requireCuenta(cuentaId);
        requireCuentaActiva(cuenta);
        requireTitular(cuenta, cliente);

        aplicarDeposito(cuenta, monto);

        return registrarTransaccionBasica(
                Transaccion.Tipo.DEPOSITO,
                monto,
                BigDecimal.ZERO,
                null,
                cuenta,
                cuenta,
                cliente,
                referencia,
                Map.of("origen", "CLIENTE")
        );
    }

    // Dependiente deposita con DUI del cliente (cobra comisión 5%)
    public Transaccion depositarPorDependienteConDui(Long dependienteId, String duiCliente, Long cuentaId, BigDecimal monto, String referencia) {
        Usuario dependiente = requireUsuario(dependienteId);
        requireRol(dependiente, Usuario.Rol.COLABORADOR); // Dependiente del banco mapeado a COLABORADOR

        Persona personaCliente = requirePersonaPorDui(duiCliente);
        Usuario cliente = requireUsuarioPorPersona(personaCliente);

        Cuenta cuenta = requireCuenta(cuentaId);
        requireCuentaActiva(cuenta);
        requireTitular(cuenta, cliente);

        BigDecimal comision = monto.multiply(COMISION_DEPENDIENTE).setScale(2, BigDecimal.ROUND_HALF_UP);

        aplicarDeposito(cuenta, monto);

        Transaccion tx = registrarTransaccionBasica(
                Transaccion.Tipo.DEPOSITO,
                monto,
                comision,
                null,
                cuenta,
                cuenta,
                dependiente,
                referencia,
                Map.of("origen", "DEPENDIENTE", "duiCliente", duiCliente)
        );

        registrarComisionDependiente(dependiente, tx, comision, COMISION_DEPENDIENTE);
        return tx;
    }

    // Cajero deposita validando DUI–cuenta
    public Transaccion depositarPorCajeroConDui(Long cajeroId, String duiCliente, Long cuentaId, BigDecimal monto, String referencia) {
        Usuario cajero = requireUsuario(cajeroId);
        requireRol(cajero, Usuario.Rol.CAJERO);

        Persona personaCliente = requirePersonaPorDui(duiCliente);
        Usuario cliente = requireUsuarioPorPersona(personaCliente);

        Cuenta cuenta = requireCuenta(cuentaId);
        requireCuentaActiva(cuenta);
        requireTitular(cuenta, cliente);

        aplicarDeposito(cuenta, monto);

        return registrarTransaccionBasica(
                Transaccion.Tipo.DEPOSITO,
                monto,
                BigDecimal.ZERO,
                null,
                cuenta,
                cuenta,
                cajero,
                referencia,
                Map.of("origen", "CAJERO", "duiCliente", duiCliente)
        );
    }

    // ============================
    // Retiros
    // ============================

    // Cliente retira de su propia cuenta (sin comisión)
    public Transaccion retirarPorCliente(Long clienteId, Long cuentaId, BigDecimal monto, String referencia) {
        Usuario cliente = requireUsuario(clienteId);
        requireRol(cliente, Usuario.Rol.CLIENTE);

        Cuenta cuenta = requireCuenta(cuentaId);
        requireCuentaActiva(cuenta);
        requireTitular(cuenta, cliente);

        aplicarRetiro(cuenta, monto);

        return registrarTransaccionBasica(
                Transaccion.Tipo.RETIRO,
                monto,
                BigDecimal.ZERO,
                cuenta,
                null,
                cuenta,
                cliente,
                referencia,
                Map.of("origen", "CLIENTE")
        );
    }

    // Dependiente retira con DUI del cliente (cobra comisión 5%)
    public Transaccion retirarPorDependienteConDui(Long dependienteId, String duiCliente, Long cuentaId, BigDecimal monto, String referencia) {
        Usuario dependiente = requireUsuario(dependienteId);
        requireRol(dependiente, Usuario.Rol.COLABORADOR);

        Persona personaCliente = requirePersonaPorDui(duiCliente);
        Usuario cliente = requireUsuarioPorPersona(personaCliente);

        Cuenta cuenta = requireCuenta(cuentaId);
        requireCuentaActiva(cuenta);
        requireTitular(cuenta, cliente);

        BigDecimal comision = monto.multiply(COMISION_DEPENDIENTE).setScale(2, BigDecimal.ROUND_HALF_UP);

        aplicarRetiro(cuenta, monto);

        Transaccion tx = registrarTransaccionBasica(
                Transaccion.Tipo.RETIRO,
                monto,
                comision,
                cuenta,
                null,
                cuenta,
                dependiente,
                referencia,
                Map.of("origen", "DEPENDIENTE", "duiCliente", duiCliente)
        );

        registrarComisionDependiente(dependiente, tx, comision, COMISION_DEPENDIENTE);
        return tx;
    }

    // Cajero retira validando DUI–cuenta
    public Transaccion retirarPorCajeroConDui(Long cajeroId, String duiCliente, Long cuentaId, BigDecimal monto, String referencia) {
        Usuario cajero = requireUsuario(cajeroId);
        requireRol(cajero, Usuario.Rol.CAJERO);

        Persona personaCliente = requirePersonaPorDui(duiCliente);
        Usuario cliente = requireUsuarioPorPersona(personaCliente);

        Cuenta cuenta = requireCuenta(cuentaId);
        requireCuentaActiva(cuenta);
        requireTitular(cuenta, cliente);

        aplicarRetiro(cuenta, monto);

        return registrarTransaccionBasica(
                Transaccion.Tipo.RETIRO,
                monto,
                BigDecimal.ZERO,
                cuenta,
                null,
                cuenta,
                cajero,
                referencia,
                Map.of("origen", "CAJERO", "duiCliente", duiCliente)
        );
    }

    // ============================
    // Transferencias
    // ============================

    // Cliente transfiere entre cuentas del banco
    public Transaccion transferirPorCliente(Long clienteId, Long cuentaOrigenId, Long cuentaDestinoId, BigDecimal monto, String referencia) {
        Usuario cliente = requireUsuario(clienteId);
        requireRol(cliente, Usuario.Rol.CLIENTE);

        Cuenta origen = requireCuenta(cuentaOrigenId);
        Cuenta destino = requireCuenta(cuentaDestinoId);
        requireCuentaActiva(origen);
        requireCuentaActiva(destino);
        requireTitular(origen, cliente); // el cliente debe ser titular de la cuenta origen

        aplicarTransferencia(origen, destino, monto);

        return registrarTransaccionBasica(
                Transaccion.Tipo.TRANSFERENCIA,
                monto,
                BigDecimal.ZERO,
                origen,
                destino,
                null,
                cliente,
                referencia,
                Map.of("origen", "CLIENTE")
        );
    }

    // Cajero transfiere validando DUI–cuenta origen (destino puede ser de cualquier usuario del banco)
    public Transaccion transferirPorCajeroConDui(Long cajeroId, String duiClienteOrigen, Long cuentaOrigenId, Long cuentaDestinoId, BigDecimal monto, String referencia) {
        Usuario cajero = requireUsuario(cajeroId);
        requireRol(cajero, Usuario.Rol.CAJERO);

        Persona personaOrigen = requirePersonaPorDui(duiClienteOrigen);
        Usuario clienteOrigen = requireUsuarioPorPersona(personaOrigen);

        Cuenta origen = requireCuenta(cuentaOrigenId);
        Cuenta destino = requireCuenta(cuentaDestinoId);
        requireCuentaActiva(origen);
        requireCuentaActiva(destino);
        requireTitular(origen, clienteOrigen);

        aplicarTransferencia(origen, destino, monto);

        return registrarTransaccionBasica(
                Transaccion.Tipo.TRANSFERENCIA,
                monto,
                BigDecimal.ZERO,
                origen,
                destino,
                null,
                cajero,
                referencia,
                Map.of("origen", "CAJERO", "duiClienteOrigen", duiClienteOrigen)
        );
    }

    // ============================
    // Consultas de movimientos
    // ============================

    public List<Transaccion> listarMovimientosPorCuenta(Long cuentaId) {
        Cuenta cuenta = requireCuenta(cuentaId);
        return transaccionRepository.findByCuentaOrCuentaOrigenOrCuentaDestinoOrderByFechaHoraDesc(cuenta, cuenta, cuenta);
    }

    public List<Transaccion> listarMovimientosPorUsuario(Long usuarioId) {
        Usuario usuario = requireUsuario(usuarioId);
        List<Cuenta> cuentas = cuentaRepository.findByClienteUsuario(usuario);
        return transaccionRepository.findByCuentasInOrderByFechaHoraDesc(cuentas);
    }

    // Gerente general: ver todos los movimientos del banco
    public List<Transaccion> listarMovimientosGlobal(Long gerenteGeneralId) {
        Usuario gg = requireUsuario(gerenteGeneralId);
        requireRol(gg, Usuario.Rol.GERENTE_GENERAL);
        return transaccionRepository.findAllByOrderByFechaHoraDesc();
    }

    // ============================
    // Helpers de negocio y persistencia
    // ============================

    private void aplicarDeposito(Cuenta cuenta, BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero");
        }
        cuenta.setSaldo(cuenta.getSaldo().add(monto));
        cuenta.setSaldoDisponible(cuenta.getSaldoDisponible().add(monto));
        cuentaRepository.save(cuenta);
    }

    private void aplicarRetiro(Cuenta cuenta, BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero");
        }
        if (cuenta.getSaldoDisponible().compareTo(monto) < 0) {
            throw new IllegalStateException("Saldo insuficiente");
        }
        cuenta.setSaldo(cuenta.getSaldo().subtract(monto));
        cuenta.setSaldoDisponible(cuenta.getSaldoDisponible().subtract(monto));
        cuentaRepository.save(cuenta);
    }

    private void aplicarTransferencia(Cuenta origen, Cuenta destino, BigDecimal monto) {
        aplicarRetiro(origen, monto);
        aplicarDeposito(destino, monto);
    }

    private Transaccion registrarTransaccionBasica(
            Transaccion.Tipo tipo,
            BigDecimal monto,
            BigDecimal comision,
            Cuenta cuentaOrigen,
            Cuenta cuentaDestino,
            Cuenta cuenta,
            Usuario ejecutor,
            String referencia,
            Map<String, Object> metadata
    ) {
        Transaccion tx = new Transaccion();
        tx.setTipo(tipo);
        tx.setMonto(monto);
        tx.setComision(comision != null ? comision : BigDecimal.ZERO);
        tx.setCuentaOrigen(cuentaOrigen);
        tx.setCuentaDestino(cuentaDestino);
        tx.setCuenta(cuenta); // para depósitos/retiros directos
        tx.setUsuarioEjecutor(ejecutor);
        tx.setFechaHora(Instant.now());
        tx.setReferencia(referencia);
        tx.setMetadata(metadata);
        return transaccionRepository.save(tx);
    }

    private void registrarComisionDependiente(Usuario dependiente, Transaccion tx, BigDecimal montoComision, BigDecimal porcentaje) {
        ComisionColaborador cc = new ComisionColaborador();
        cc.setColaboradorUsuario(dependiente);
        cc.setTransaccion(tx);
        cc.setMontoComision(montoComision);
        cc.setPorcentajeComision(porcentaje.multiply(new BigDecimal("100"))); // almacenar 5.00 si tu columna es % base 100
        cc.setFechaComision(Instant.now());
        cc.setEstado(ComisionColaborador.Estado.PENDIENTE);
        comisionColaboradorRepository.save(cc);
    }

    // ============================
    // Helpers de validación y acceso
    // ============================

    private Usuario requireUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
    }

    private Usuario requireUsuarioPorPersona(Persona persona) {
        return usuarioRepository.findByPersona(persona)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado para la persona: " + persona.getId()));
    }

    private Persona requirePersonaPorDui(String dui) {
        return personaRepository.findByDui(dui)
                .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada con DUI: " + dui));
    }

    private Cuenta requireCuenta(Long id) {
        return cuentaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada: " + id));
    }

    private void requireCuentaActiva(Cuenta cuenta) {
        if (cuenta.getActiva() == null || !cuenta.getActiva()) {
            throw new IllegalStateException("La cuenta no está activa");
        }
    }

    private void requireTitular(Cuenta cuenta, Usuario usuarioEsperado) {
        if (!cuenta.getClienteUsuario().getId().equals(usuarioEsperado.getId())) {
            throw new SecurityException("La cuenta no pertenece al usuario indicado");
        }
    }

    private void requireRol(Usuario actor, Usuario.Rol rolRequerido) {
        if (actor.getRol() != rolRequerido) {
            throw new SecurityException("Acción permitida solo para rol: " + rolRequerido);
        }
    }
}
