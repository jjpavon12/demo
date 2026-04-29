package com.giu.giu.service;

import com.giu.giu.dto.LoginRequest;
import com.giu.giu.dto.LoginResponse;
import com.giu.giu.model.CategoriaIncidencia;
import com.giu.giu.model.EquipoTecnicoConfig;
import com.giu.giu.model.Incidencia;
import com.giu.giu.model.Rol;
import com.giu.giu.model.Usuario;
import com.giu.giu.repository.EquipoTecnicoConfigRepository;
import com.giu.giu.repository.IncidenciaRepository;
import com.giu.giu.repository.UsuarioRepository;
import com.giu.giu.config.DatabaseInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EquipoTecnicoConfigRepository equipoTecnicoConfigRepository;

    @Autowired
    private IncidenciaRepository incidenciaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Autentica un usuario con email y contraseña
     */
    public LoginResponse login(LoginRequest request) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(request.getEmail());

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if (!usuario.isActivo()) {
                return new LoginResponse(null, null, null, "Usuario no habilitado. Espera validación del administrador.");
            }
            if (passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
                return new LoginResponse(
                    usuario.getId(),
                    usuario.getEmail(),
                    usuario.getRol(),
                    "Login exitoso"
                );
            }
        }

        return new LoginResponse(null, null, null, "Email o contraseña incorrectos");
    }

    /**
     * Registra un nuevo usuario con un rol específico
     */
    public LoginResponse registrar(String email, String password, Rol rol) {
        if (usuarioRepository.findByEmail(email).isPresent()) {
            return new LoginResponse(null, null, null, "El email ya está registrado");
        }

        if (password.length() < 6) {
            return new LoginResponse(null, null, null, "La contraseña debe tener al menos 6 caracteres");
        }

        if (rol == Rol.ADMINISTRADOR && usuarioRepository.existsByRol(Rol.ADMINISTRADOR)) {
            return new LoginResponse(null, null, null, "No está permitido registrar más administradores desde esta pantalla");
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setEmail(email);
        nuevoUsuario.setPassword(passwordEncoder.encode(password));
        nuevoUsuario.setRol(rol);

        // Operadores y técnicos requieren aprobación del administrador
        boolean requiereAprobacion = (rol == Rol.OPERADOR || rol == Rol.TECNICO);
        nuevoUsuario.setActivo(!requiereAprobacion);

        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);

        String mensaje = requiereAprobacion
            ? "Cuenta creada. Un administrador debe aprobarla antes de poder iniciar sesión."
            : "Usuario registrado exitosamente";

        return new LoginResponse(
            usuarioGuardado.getId(),
            usuarioGuardado.getEmail(),
            usuarioGuardado.getRol(),
            mensaje
        );
    }

    public boolean existeAdministrador() {
        return usuarioRepository.existsByRol(Rol.ADMINISTRADOR);
    }

    /**
     * Obtiene un usuario por email
     */
    public Optional<Usuario> obtenerPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    /**
     * Obtiene un usuario por ID
     */
    public Optional<Usuario> obtenerPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    /**
     * Lista usuarios pendientes de aprobación (excluye el usuario del sistema ANÓNIMO)
     */
    public List<Usuario> obtenerPendientes() {
        return usuarioRepository.findByActivoFalse().stream()
            .filter(u -> !DatabaseInitializer.EMAIL_ANONIMO.equals(u.getEmail()))
            .collect(Collectors.toList());
    }

    /**
     * Aprueba un usuario (lo activa)
     */
    public void aprobarUsuario(Long id) {
        usuarioRepository.findById(id).ifPresent(u -> {
            u.setActivo(true);
            usuarioRepository.save(u);
        });
    }

    /**
     * Deniega un usuario (lo elimina)
     */
    public void denegarUsuario(Long id) {
        equipoTecnicoConfigRepository.findByUsuarioId(id)
            .ifPresent(equipoTecnicoConfigRepository::delete);
        usuarioRepository.deleteById(id);
    }

    /**
     * Elimina un usuario activo (solo para administradores)
     */
    public String eliminarUsuario(Long id) {
        Optional<Usuario> opt = usuarioRepository.findById(id);
        if (opt.isEmpty()) return "Usuario no encontrado";

        Usuario usuario = opt.get();
        if (DatabaseInitializer.EMAIL_ANONIMO.equals(usuario.getEmail())) {
            return "No se puede eliminar el usuario del sistema";
        }
        if (usuario.getRol() == Rol.ADMINISTRADOR) {
            long numAdmins = usuarioRepository.countByRol(Rol.ADMINISTRADOR);
            if (numAdmins <= 1) {
                return "No se puede eliminar el último administrador";
            }
        }

        equipoTecnicoConfigRepository.findByUsuarioId(id)
            .ifPresent(equipoTecnicoConfigRepository::delete);

        usuarioRepository.deleteById(id);
        return null;
    }

    /**
     * Lista todos los usuarios (excluye el usuario del sistema ANÓNIMO)
     */
    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll().stream()
            .filter(u -> !DatabaseInitializer.EMAIL_ANONIMO.equals(u.getEmail()))
            .collect(Collectors.toList());
    }

    /**
     * Solicita un cambio de rol (queda pendiente de aprobación del admin)
     * Devuelve null si OK, o un mensaje de error
     */
    public String solicitarCambioRol(Long id, Rol nuevoRol) {
        Optional<Usuario> opt = usuarioRepository.findById(id);
        if (opt.isEmpty()) return "Usuario no encontrado";

        Usuario usuario = opt.get();
        if (usuario.getRol() == nuevoRol) return "Ya tienes ese rol";
        if (nuevoRol == Rol.ADMINISTRADOR) return "No puedes solicitar el rol de Administrador";

        usuario.setRolSolicitado(nuevoRol);
        usuarioRepository.save(usuario);
        return null;
    }

    /**
     * Lista usuarios con solicitudes de cambio de rol pendientes
     */
    public java.util.List<Usuario> obtenerSolicitudesRol() {
        return usuarioRepository.findByRolSolicitadoIsNotNull();
    }

    /**
     * Aprueba la solicitud de cambio de rol
     */
    public void aprobarCambioRol(Long id) {
        usuarioRepository.findById(id).ifPresent(u -> {
            if (u.getRolSolicitado() != null) {
                u.setRol(u.getRolSolicitado());
                u.setRolSolicitado(null);
                usuarioRepository.save(u);
            }
        });
    }

    /**
     * Deniega la solicitud de cambio de rol
     */
    public void denegarCambioRol(Long id) {
        usuarioRepository.findById(id).ifPresent(u -> {
            u.setRolSolicitado(null);
            usuarioRepository.save(u);
        });
    }

    /**
     * Actualiza el perfil del ciudadano (email y/o contraseña, nunca el rol)
     * Devuelve null si OK, o un mensaje de error
     */
    public String actualizarPerfil(Long id, String nuevoEmail, String nuevaPassword) {
        Optional<Usuario> opt = usuarioRepository.findById(id);
        if (opt.isEmpty()) return "Usuario no encontrado";

        Usuario usuario = opt.get();

        if (!usuario.getEmail().equals(nuevoEmail)) {
            Optional<Usuario> existente = usuarioRepository.findByEmail(nuevoEmail);
            if (existente.isPresent()) {
                return "El email ya está en uso por otro usuario";
            }
            usuario.setEmail(nuevoEmail);
        }

        if (nuevaPassword != null && !nuevaPassword.isBlank()) {
            usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        }

        usuarioRepository.save(usuario);
        return null;
    }

    /**
     * Elimina la cuenta propia del usuario (no permitido para ADMINISTRADOR).
     * Las incidencias creadas quedan con autor anónimo (usuario = null).
     * Las incidencias asignadas a este técnico quedan sin asignar.
     */
    @Transactional
    public String eliminarCuentaPropia(Long id, String password) {
        Optional<Usuario> opt = usuarioRepository.findById(id);
        if (opt.isEmpty()) return "Usuario no encontrado";

        Usuario usuario = opt.get();

        if (usuario.getRol() == Rol.ADMINISTRADOR) {
            return "Los administradores no pueden eliminar su cuenta";
        }
        if (DatabaseInitializer.EMAIL_ANONIMO.equals(usuario.getEmail())) {
            return "Esta cuenta no puede eliminarse";
        }

        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            return "Contraseña incorrecta";
        }

        Usuario anonimo = usuarioRepository.findByEmail(DatabaseInitializer.EMAIL_ANONIMO)
            .orElseThrow(() -> new IllegalStateException("Usuario ANÓNIMO del sistema no encontrado"));

        // Reasignar las incidencias creadas por este usuario al ANÓNIMO
        for (Incidencia inc : incidenciaRepository.findByUsuarioOrderByFechaCreacionDesc(usuario)) {
            inc.setUsuario(anonimo);
            incidenciaRepository.save(inc);
        }

        // Desasignar incidencias donde este usuario era técnico responsable
        for (Incidencia inc : incidenciaRepository.findByTecnicoAsignadoOrderByFechaAsignacionDesc(usuario)) {
            inc.setTecnicoAsignado(null);
            inc.setFechaAsignacion(null);
            incidenciaRepository.save(inc);
        }

        // Eliminar configuración de equipo técnico si existe
        equipoTecnicoConfigRepository.findByUsuarioId(id)
            .ifPresent(equipoTecnicoConfigRepository::delete);

        usuarioRepository.deleteById(id);
        return null;
    }

    /**
     * Obtiene la configuración del equipo técnico del usuario, si existe
     */
    public Optional<EquipoTecnicoConfig> obtenerConfigEquipoTecnico(Long usuarioId) {
        return equipoTecnicoConfigRepository.findByUsuarioId(usuarioId);
    }

    /**
     * Crea o actualiza la configuración del equipo técnico
     */
    public String configurarEquipoTecnico(Long usuarioId, String nombreEquipo, Set<CategoriaIncidencia> especialidades) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
        if (usuarioOpt.isEmpty()) {
            return "Usuario no encontrado";
        }

        Usuario usuario = usuarioOpt.get();

        if (usuario.getRol() != Rol.TECNICO) {
            return "Solo los usuarios técnicos pueden configurar un equipo técnico";
        }

        if (nombreEquipo == null || nombreEquipo.isBlank()) {
            return "El nombre del equipo es obligatorio";
        }

        if (especialidades == null || especialidades.isEmpty()) {
            return "Debes seleccionar al menos una categoría";
        }

        Set<CategoriaIncidencia> especialidadesLimpias = new HashSet<>(especialidades);

        EquipoTecnicoConfig config = equipoTecnicoConfigRepository.findByUsuarioId(usuarioId)
            .orElseGet(EquipoTecnicoConfig::new);

        config.setUsuario(usuario);
        config.setNombreEquipo(nombreEquipo.trim());
        config.setEspecialidades(especialidadesLimpias);

        equipoTecnicoConfigRepository.save(config);
        return null;
    }
}