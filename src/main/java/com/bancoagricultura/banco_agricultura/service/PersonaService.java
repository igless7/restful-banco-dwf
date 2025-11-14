package com.bancoagricultura.banco_agricultura.service;

import com.bancoagricultura.banco_agricultura.model.Persona;
import com.bancoagricultura.banco_agricultura.repo.PersonaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PersonaService {

    private final PersonaRepository personaRepository;

    // Crear nueva persona
    public Persona crearPersona(Persona persona) {
        persona.setEstado(Persona.Estado.ACTIVO); // por defecto activa
        return personaRepository.save(persona);
    }

    // Actualizar persona existente
    public Persona actualizarPersona(Long id, Persona datos) {
        Persona persona = personaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada: " + id));

        persona.setNombreCompleto(datos.getNombreCompleto());
        persona.setFechaNacimiento(datos.getFechaNacimiento());
        persona.setSalario(datos.getSalario());
        persona.setDireccion(datos.getDireccion());
        persona.setTelefono(datos.getTelefono());
        persona.setEmail(datos.getEmail());
        persona.setEstado(datos.getEstado());

        return personaRepository.save(persona);
    }

    // Obtener persona por ID
    public Persona obtenerPersona(Long id) {
        return personaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada: " + id));
    }

    // Buscar persona por DUI
    public Persona obtenerPersonaPorDui(String dui) {
        return personaRepository.findByDui(dui)
                .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada con DUI: " + dui));
    }

    // Listar todas las personas
    public List<Persona> listarPersonas() {
        return personaRepository.findAll();
    }

    // Dar de baja persona (cambiar estado a INACTIVO)
    public void darDeBajaPersona(Long id) {
        Persona persona = personaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada: " + id));
        persona.setEstado(Persona.Estado.INACTIVO);
        personaRepository.save(persona);
    }
}
