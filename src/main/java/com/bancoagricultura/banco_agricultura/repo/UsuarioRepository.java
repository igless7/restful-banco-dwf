package com.bancoagricultura.banco_agricultura.repo;
import com.bancoagricultura.banco_agricultura.model.Persona;
import com.bancoagricultura.banco_agricultura.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Buscar por username (único)
    Optional<Usuario> findByUsername(String username);

    // Buscar por persona asociada
    List<Usuario> findByPersonaId(Long personaId);

    // Buscar por rol
    List<Usuario> findByRol(Usuario.Rol rol);

    // Buscar por estado
    List<Usuario> findByEstado(Usuario.Estado estado);

    // Buscar por sucursal
    List<Usuario> findBySucursalId(Long sucursalId);

    // Buscar por rol y estado
    List<Usuario> findByRolAndEstado(Usuario.Rol rol, Usuario.Estado estado);

    Optional<Usuario> findByPersona(Persona persona);
}