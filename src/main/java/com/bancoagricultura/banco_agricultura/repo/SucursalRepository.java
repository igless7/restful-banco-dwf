package com.bancoagricultura.banco_agricultura.repo;

import com.bancoagricultura.banco_agricultura.model.Sucursal;
import com.bancoagricultura.banco_agricultura.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SucursalRepository extends JpaRepository<Sucursal, Long> {

    // Buscar sucursal por código único
    Optional<Sucursal> findByCodigoSucursal(String codigoSucursal);

    // Buscar sucursal por nombre
    List<Sucursal> findByNombreContainingIgnoreCase(String nombre);

    // Buscar sucursales por estado (ACTIVO o INACTIVO)
    List<Sucursal> findByEstado(Sucursal.Estado estado);

    // Buscar sucursales por gerente asignado
    List<Sucursal> findByGerenteUsuario(Usuario gerenteUsuario);

    // Buscar sucursales por dirección
    List<Sucursal> findByDireccionContainingIgnoreCase(String direccion);
}
