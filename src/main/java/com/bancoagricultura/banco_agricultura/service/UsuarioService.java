package com.bancoagricultura.banco_agricultura.service;

import com.bancoagricultura.banco_agricultura.dto.UsuarioCreateDTO;
import com.bancoagricultura.banco_agricultura.dto.UsuarioDTO;
import com.bancoagricultura.banco_agricultura.mapper.UsuarioMapper;
import com.bancoagricultura.banco_agricultura.model.AccionPersonal;
import com.bancoagricultura.banco_agricultura.model.Persona;
import com.bancoagricultura.banco_agricultura.model.Sucursal;
import com.bancoagricultura.banco_agricultura.model.Usuario;
import com.bancoagricultura.banco_agricultura.repo.AccionPersonalRepository;
import com.bancoagricultura.banco_agricultura.repo.PersonaRepository;
import com.bancoagricultura.banco_agricultura.repo.SucursalRepository;
import com.bancoagricultura.banco_agricultura.repo.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PersonaRepository personaRepository;
    private final SucursalRepository sucursalRepository;
    private final AccionPersonalRepository accionPersonalRepository;
    private final UsuarioMapper usuarioMapper;


    // Creación de usuarios

    // 1) Un CLIENTE puede crearse a sí mismo (actor: null o cliente)
    public UsuarioDTO crearClientePorCliente(UsuarioCreateDTO dto) {
        validarRolObjetivo(dto, Usuario.Rol.CLIENTE);
        Usuario entidad = prepararNuevoUsuario(dto, null);
        entidad.setRol(Usuario.Rol.CLIENTE);
        entidad.setEstado(Usuario.Estado.ACTIVO); // cliente se activa al registrarse
        return usuarioMapper.toDTO(usuarioRepository.save(entidad));
    }

    // 2) Un CAJERO puede crear un CLIENTE
    public UsuarioDTO crearClientePorCajero(Long actorId, UsuarioCreateDTO dto) {
        Usuario actor = requireUsuario(actorId);
        requireRol(actor, Usuario.Rol.CAJERO);
        validarRolObjetivo(dto, Usuario.Rol.CLIENTE);

        Usuario entidad = prepararNuevoUsuario(dto, actor);
        entidad.setRol(Usuario.Rol.CLIENTE);
        entidad.setEstado(Usuario.Estado.ACTIVO);
        return usuarioMapper.toDTO(usuarioRepository.save(entidad));
    }

    // 3) Un CAJERO puede crear un COLABORADOR
    public UsuarioDTO crearColaboradorPorCajero(Long actorId, UsuarioCreateDTO dto) {
        Usuario actor = requireUsuario(actorId);
        requireRol(actor, Usuario.Rol.CAJERO);
        validarRolObjetivo(dto, Usuario.Rol.COLABORADOR);

        Usuario entidad = prepararNuevoUsuario(dto, actor);
        entidad.setRol(Usuario.Rol.COLABORADOR);
        entidad.setEstado(Usuario.Estado.ACTIVO);
        return usuarioMapper.toDTO(usuarioRepository.save(entidad));
    }

    // 4) Un GERENTE_SUCURSAL crea un CAJERO:
    //    - Se crea el usuario CAJERO en EN_ESPERA
    //    - Se registra AccionPersonal (ALTA_EMPLEADO) en EN_ESPERA para aprobación posterior
    public UsuarioDTO crearCajeroPorGerenteSucursal(Long actorId, UsuarioCreateDTO dto) {
        Usuario actor = requireUsuario(actorId);
        requireRol(actor, Usuario.Rol.GERENTE_SUCURSAL);
        validarRolObjetivo(dto, Usuario.Rol.CAJERO);

        Usuario cajero = prepararNuevoUsuario(dto, actor);
        cajero.setRol(Usuario.Rol.CAJERO);
        cajero.setEstado(Usuario.Estado.EN_ESPERA); // queda en espera de aprobación
        cajero = usuarioRepository.save(cajero);

        // Crear acción de personal en EN_ESPERA
        AccionPersonal ap = new AccionPersonal();
        ap.setEmpleadoUsuario(cajero);
        ap.setTipoAccion(AccionPersonal.TipoAccion.ALTA_EMPLEADO);
        ap.setEstado(AccionPersonal.Estado.EN_ESPERA);
        ap.setSucursal(actor.getSucursal());
        ap.setGeneradoPorUsuario(actor);
        ap.setObservaciones("Alta de cajero pendiente de aprobación");
        accionPersonalRepository.save(ap);

        return usuarioMapper.toDTO(cajero);
    }

    /**
     * Aprobar una acción de personal (ej. alta de cajero).
     * El rol que aprueba debe ser GERENTE_GENERAL.
     */
    public void aprobarAccionPersonal(Long actorId, Long accionId) {
        Usuario actor = requireUsuario(actorId);
        requireRol(actor, Usuario.Rol.GERENTE_GENERAL);

        AccionPersonal accion = requireAccionPersonal(accionId);

        if (accion.getEstado() != AccionPersonal.Estado.EN_ESPERA) {
            throw new IllegalStateException("La acción ya fue gestionada");
        }

        // Cambiar estado de la acción
        accion.setEstado(AccionPersonal.Estado.APROBADO);
        accion.setGestionadoPorUsuario(actor);
        accionPersonalRepository.save(accion);

        // Activar el empleado asociado
        Usuario empleado = accion.getEmpleadoUsuario();
        empleado.setEstado(Usuario.Estado.ACTIVO);
        usuarioRepository.save(empleado);
    }

    /**
     * Rechazar una acción de personal.
     * El rol que rechaza debe ser GERENTE_GENERAL.
     */
    public void rechazarAccionPersonal(Long actorId, Long accionId, String observaciones) {
        Usuario actor = requireUsuario(actorId);
        requireRol(actor, Usuario.Rol.GERENTE_GENERAL);

        AccionPersonal accion = requireAccionPersonal(accionId);

        if (accion.getEstado() != AccionPersonal.Estado.EN_ESPERA) {
            throw new IllegalStateException("La acción ya fue gestionada");
        }

        // Cambiar estado de la acción
        accion.setEstado(AccionPersonal.Estado.RECHAZADO);
        accion.setGestionadoPorUsuario(actor);
        accion.setObservaciones(observaciones);
        accionPersonalRepository.save(accion);

        // Mantener al empleado en estado EN_ESPERA o pasarlo a INACTIVO
        Usuario empleado = accion.getEmpleadoUsuario();
        empleado.setEstado(Usuario.Estado.INACTIVO);
        usuarioRepository.save(empleado);
    }

    // Baja de empleados

    // 5) GERENTE_SUCURSAL puede dar de baja (ACTIVO -> INACTIVO) a un empleado
    public void darDeBajaEmpleado(Long actorId, Long empleadoId) {
        Usuario actor = requireUsuario(actorId);
        requireRol(actor, Usuario.Rol.GERENTE_SUCURSAL);

        Usuario empleado = requireUsuario(empleadoId);

        if (empleado.getEstado() == Usuario.Estado.INACTIVO) {
            throw new IllegalStateException("El empleado ya está INACTIVO");
        }

        empleado.setEstado(Usuario.Estado.INACTIVO);
        usuarioRepository.save(empleado);

        // Opcional: registrar acción de personal de BAJA_EMPLEADO en APROBADO automáticamente
        AccionPersonal ap = new AccionPersonal();
        ap.setEmpleadoUsuario(empleado);
        ap.setTipoAccion(AccionPersonal.TipoAccion.BAJA_EMPLEADO);
        ap.setEstado(AccionPersonal.Estado.APROBADO);
        ap.setSucursal(actor.getSucursal());
        ap.setGeneradoPorUsuario(actor);
        ap.setObservaciones("Baja de empleado realizada por gerente de sucursal");
        accionPersonalRepository.save(ap);
    }

    // Helpers

    private Usuario prepararNuevoUsuario(UsuarioCreateDTO dto, Usuario creadoPor) {
        // Validar y cargar referencias
        Persona persona = requirePersona(dto.getPersonaId());
        Sucursal sucursal = dto.getSucursalId() != null ? requireSucursal(dto.getSucursalId()) : null;

        Usuario entidad = new Usuario();
        entidad.setPersona(persona);
        entidad.setUsername(dto.getUsername());
        entidad.setPassword(dto.getPassword());
        entidad.setRol(dto.getRol());
        entidad.setEstado(Usuario.Estado.EN_ESPERA); // estado por defecto (se ajusta según regla)
        entidad.setSucursal(sucursal);
        entidad.setCreadoPorUsuario(creadoPor);
        return entidad;
    }

    private Usuario requireUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
    }

    private Persona requirePersona(Long id) {
        return personaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada: " + id));
    }

    private Sucursal requireSucursal(Long id) {
        return sucursalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sucursal no encontrada: " + id));
    }

    private void requireRol(Usuario actor, Usuario.Rol rolRequerido) {
        if (actor.getRol() != rolRequerido) {
            throw new SecurityException("Acción permitida solo para rol: " + rolRequerido);
        }
    }

    private void validarRolObjetivo(UsuarioCreateDTO dto, Usuario.Rol rolEsperado) {
        if (!Objects.equals(dto.getRol(), rolEsperado)) {
            throw new IllegalArgumentException("El DTO debe tener rol " + rolEsperado);
        }
    }
    private AccionPersonal requireAccionPersonal(Long id) {
        return accionPersonalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Acción de personal no encontrada: " + id));
    }
}
