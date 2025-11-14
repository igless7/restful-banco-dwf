package com.bancoagricultura.banco_agricultura.service;

import com.bancoagricultura.banco_agricultura.model.Cuenta;
import com.bancoagricultura.banco_agricultura.model.Usuario;
import com.bancoagricultura.banco_agricultura.repo.CuentaRepository;
import com.bancoagricultura.banco_agricultura.repo.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CuentaService {

    private final CuentaRepository cuentaRepository;
    private final UsuarioRepository usuarioRepository;

    // Crear nueva cuenta con reglas de negocio
    public Cuenta crearCuenta(Long usuarioId, Cuenta.TipoCuenta tipoCuenta, Long creadorId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + usuarioId));

        Usuario creador = null;
        if (creadorId != null) {
            creador = usuarioRepository.findById(creadorId)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario creador no encontrado: " + creadorId));
        }

        // Validar reglas según rol del usuario
        if (usuario.getRol() == Usuario.Rol.CLIENTE) {
            // Cliente puede crear cuenta él mismo o un cajero
            if (creador != null && creador.getRol() != Usuario.Rol.CAJERO && !creador.getId().equals(usuario.getId())) {
                throw new SecurityException("Solo el propio cliente o un cajero pueden crear cuentas de cliente");
            }

            long cuentasCliente = cuentaRepository.countByClienteUsuario(usuario);
            if (cuentasCliente >= 3) {
                throw new IllegalStateException("El cliente ya tiene el máximo de 3 cuentas");
            }

        } else if (usuario.getRol() == Usuario.Rol.COLABORADOR) {
            // Colaborador solo puede ser creado por un cajero
            if (creador == null || creador.getRol() != Usuario.Rol.CAJERO) {
                throw new SecurityException("Solo un cajero puede crear cuentas de colaborador");
            }

            long cuentasColaborador = cuentaRepository.countByClienteUsuario(usuario);
            if (cuentasColaborador >= 1) {
                throw new IllegalStateException("El colaborador solo puede tener 1 cuenta");
            }

        } else {
            throw new SecurityException("Solo clientes o colaboradores pueden tener cuentas");
        }

        // Generar número de cuenta aleatorio único de 12 dígitos
        String numeroCuenta = generarNumeroCuentaUnico();

        // Crear cuenta
        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta(numeroCuenta);
        cuenta.setClienteUsuario(usuario);
        cuenta.setTipoCuenta(tipoCuenta);
        cuenta.setSaldo(BigDecimal.ZERO);
        cuenta.setSaldoDisponible(BigDecimal.ZERO);
        cuenta.setActiva(true);
        cuenta.setCreadaPorUsuario(creador);

        return cuentaRepository.save(cuenta);
    }

    // Listar cuentas de un usuario
    public List<Cuenta> listarCuentasPorUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + usuarioId));
        return cuentaRepository.findByClienteUsuario(usuario);
    }

    // Dar de baja cuenta (inactivar)
    public void desactivarCuenta(Long cuentaId) {
        Cuenta cuenta = cuentaRepository.findById(cuentaId)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada: " + cuentaId));
        cuenta.setActiva(false);
        cuentaRepository.save(cuenta);
    }

    // ============================
    // Helpers
    // ============================

    private String generarNumeroCuentaUnico() {
        SecureRandom random = new SecureRandom();
        String numeroCuenta;
        do {
            numeroCuenta = String.format("%012d", Math.abs(random.nextLong()) % 1_000_000_000_000L); // 12 dígitos
        } while (cuentaRepository.existsByNumeroCuenta(numeroCuenta));
        return numeroCuenta;
    }
}
