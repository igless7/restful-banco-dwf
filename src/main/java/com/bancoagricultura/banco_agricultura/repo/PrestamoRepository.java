package com.bancoagricultura.banco_agricultura.repo;

import com.bancoagricultura.banco_agricultura.model.Cuenta;
import com.bancoagricultura.banco_agricultura.model.Prestamo;
import com.bancoagricultura.banco_agricultura.model.SolicitudPrestamo;
import com.bancoagricultura.banco_agricultura.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    // Buscar préstamos por cliente
    List<Prestamo> findByUsuarioCliente(Usuario usuarioCliente);

    // Buscar préstamos por estado (ACTIVO o CANCELADO)
    List<Prestamo> findByEstado(Prestamo.Estado estado);

    // Buscar préstamos por cuenta destino
    List<Prestamo> findByCuentaDestino(Cuenta cuentaDestino);

    // Buscar préstamos aprobados por un usuario específico
    List<Prestamo> findByAprobadoPorUsuario(Usuario aprobadoPorUsuario);

    // Buscar préstamos vinculados a una solicitud de préstamo
    List<Prestamo> findBySolicitudPrestamo(SolicitudPrestamo solicitudPrestamo);

    // Buscar préstamos con fecha de vencimiento antes de una fecha dada
    List<Prestamo> findByFechaVencimientoBefore(LocalDate fecha);

    // Buscar préstamos con próximo pago en una fecha específica
    List<Prestamo> findByProximoPago(LocalDate fecha);
}