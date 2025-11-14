package com.bancoagricultura.banco_agricultura.repo;

import com.bancoagricultura.banco_agricultura.model.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, Long> {

    // Buscar por DUI (único)
    Optional<Persona> findByDui(String dui);

    // Buscar por email (único)
    Optional<Persona> findByEmail(String email);

    // Buscar por estado
    List<Persona> findByEstado(String estado);
}
