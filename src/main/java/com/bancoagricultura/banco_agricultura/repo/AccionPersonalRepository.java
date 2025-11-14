package com.bancoagricultura.banco_agricultura.repo;

import com.bancoagricultura.banco_agricultura.model.AccionPersonal;
import com.bancoagricultura.banco_agricultura.model.Sucursal;
import com.bancoagricultura.banco_agricultura.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccionPersonalRepository extends JpaRepository<AccionPersonal, Long> {

    // Buscar acciones por empleado
    List<AccionPersonal> findByEmpleadoUsuario(Usuario empleadoUsuario);

    // Buscar acciones por tipo (ALTA_EMPLEADO, BAJA_EMPLEADO, CAMBIO_ROL)
    List<AccionPersonal> findByTipoAccion(AccionPersonal.TipoAccion tipoAccion);

    // Buscar acciones por estado (APROBADO, RECHAZADO, EN_ESPERA)
    List<AccionPersonal> findByEstado(AccionPersonal.Estado estado);

    // Buscar acciones por sucursal
    List<AccionPersonal> findBySucursal(Sucursal sucursal);

    // Buscar acciones generadas por un usuario específico
    List<AccionPersonal> findByGeneradoPorUsuario(Usuario generadoPorUsuario);

    // Buscar acciones gestionadas por un usuario específico
    List<AccionPersonal> findByGestionadoPorUsuario(Usuario gestionadoPorUsuario);

    // Buscar acciones por tipo y estado
    List<AccionPersonal> findByTipoAccionAndEstado(AccionPersonal.TipoAccion tipoAccion, AccionPersonal.Estado estado);
}
