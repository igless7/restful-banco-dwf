package com.bancoagricultura.banco_agricultura.repo;

import com.bancoagricultura.banco_agricultura.model.Cuenta;
import com.bancoagricultura.banco_agricultura.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

    // Buscar por número de cuenta (único)
    Optional<Cuenta> findByNumeroCuenta(String numeroCuenta);

    // Buscar todas las cuentas de un cliente
    List<Cuenta> findByClienteUsuario(Usuario clienteUsuario);

    // Buscar por tipo de cuenta
    List<Cuenta> findByTipoCuenta(Cuenta.TipoCuenta tipoCuenta);

    // Buscar cuentas activas/inactivas
    List<Cuenta> findByActiva(Boolean activa);

    // Buscar cuentas por sucursal
    List<Cuenta> findBySucursalId(Long sucursalId);

    // Buscar cuentas por cliente y estado activo
    List<Cuenta> findByClienteUsuarioAndActiva(Usuario clienteUsuario, Boolean activa);

    long countByClienteUsuario(Usuario usuario);

    boolean existsByNumeroCuenta(String numeroCuenta);
}
