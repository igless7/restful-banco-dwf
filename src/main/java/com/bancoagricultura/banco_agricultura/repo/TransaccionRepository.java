package com.bancoagricultura.banco_agricultura.repo;

import com.bancoagricultura.banco_agricultura.model.Cuenta;
import com.bancoagricultura.banco_agricultura.model.Transaccion;
import com.bancoagricultura.banco_agricultura.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    // Buscar transacciones por tipo (ej. DEPOSITO, RETIRO, etc.)
    List<Transaccion> findByTipo(Transaccion.Tipo tipo);

    // Buscar transacciones por cuenta origen
    List<Transaccion> findByCuentaOrigen(Cuenta cuentaOrigen);

    // Buscar transacciones por cuenta destino
    List<Transaccion> findByCuentaDestino(Cuenta cuentaDestino);

    // Buscar transacciones por cuenta general (cuenta_id)
    List<Transaccion> findByCuenta(Cuenta cuenta);

    // Buscar transacciones ejecutadas por un usuario específico
    List<Transaccion> findByUsuarioEjecutor(Usuario usuarioEjecutor);

    // Buscar transacciones en un rango de fechas
    List<Transaccion> findByFechaHoraBetween(Instant inicio, Instant fin);

    // Buscar transacciones por referencia
    List<Transaccion> findByReferenciaContainingIgnoreCase(String referencia);

    // Todas las transacciones donde la cuenta aparezca como directa, origen o destino
    List<Transaccion> findByCuentaOrCuentaOrigenOrCuentaDestinoOrderByFechaHoraDesc(Cuenta cuenta, Cuenta cuentaOrigen, Cuenta cuentaDestino);

    // Movimientos por lista de cuentas (para usuarios)
    @Query("select t from Transaccion t where t.cuenta in :cuentas or t.cuentaOrigen in :cuentas or t.cuentaDestino in :cuentas order by t.fechaHora desc")
    List<Transaccion> findByCuentasInOrderByFechaHoraDesc(List<Cuenta> cuentas);

    List<Transaccion> findAllByOrderByFechaHoraDesc();
}
