package com.bancoagricultura.banco_agricultura.repo;

import com.bancoagricultura.banco_agricultura.model.ComisionColaborador;
import com.bancoagricultura.banco_agricultura.model.Transaccion;
import com.bancoagricultura.banco_agricultura.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ComisionColaboradorRepository extends JpaRepository<ComisionColaborador, Long> {

    // Buscar comisiones por colaborador
    List<ComisionColaborador> findByColaboradorUsuario(Usuario colaboradorUsuario);

    // Buscar comisiones por transacción
    List<ComisionColaborador> findByTransaccion(Transaccion transaccion);

    // Buscar comisiones por estado (PENDIENTE o PAGADA)
    List<ComisionColaborador> findByEstado(ComisionColaborador.Estado estado);

    // Buscar comisiones por rango de fechas
    List<ComisionColaborador> findByFechaComisionBetween(Instant inicio, Instant fin);

    // Buscar comisiones pendientes de un colaborador específico
    List<ComisionColaborador> findByColaboradorUsuarioAndEstado(Usuario colaboradorUsuario, ComisionColaborador.Estado estado);
}
