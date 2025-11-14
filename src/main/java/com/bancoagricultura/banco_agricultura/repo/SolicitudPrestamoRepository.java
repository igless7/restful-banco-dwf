package com.bancoagricultura.banco_agricultura.repo;

import com.bancoagricultura.banco_agricultura.model.Cuenta;
import com.bancoagricultura.banco_agricultura.model.SolicitudPrestamo;
import com.bancoagricultura.banco_agricultura.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitudPrestamoRepository extends JpaRepository<SolicitudPrestamo, Long> {

    // Buscar solicitudes por cliente
    List<SolicitudPrestamo> findByUsuarioCliente(Usuario usuarioCliente);

    // Buscar solicitudes por estado del caso
    List<SolicitudPrestamo> findByEstadoCaso(SolicitudPrestamo.EstadoCaso estadoCaso);

    // Buscar solicitudes por cuenta destino
    List<SolicitudPrestamo> findByCuentaDestino(Cuenta cuentaDestino);

    // Buscar solicitudes gestionadas por un usuario específico
    List<SolicitudPrestamo> findByGestionadoPorUsuario(Usuario gestionadoPorUsuario);

    // Buscar solicitudes solicitadas por un usuario específico
    List<SolicitudPrestamo> findBySolicitadoPorUsuario(Usuario solicitadoPorUsuario);

    // Buscar solicitudes por cliente y estado
    List<SolicitudPrestamo> findByUsuarioClienteAndEstadoCaso(Usuario usuarioCliente, SolicitudPrestamo.EstadoCaso estadoCaso);
    List<SolicitudPrestamo> findByUsuarioClienteOrderByCreadoEnDesc(Usuario usuarioCliente);
    List<SolicitudPrestamo> findByEstadoCasoOrderByCreadoEnDesc(SolicitudPrestamo.EstadoCaso estadoCaso);
    List<SolicitudPrestamo> findAllByOrderByCreadoEnDesc();
}
