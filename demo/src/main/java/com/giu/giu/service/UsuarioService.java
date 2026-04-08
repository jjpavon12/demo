package com.giu.giu.service;

import com.giu.giu.dto.LoginRequest;
import com.giu.giu.dto.LoginResponse;
import com.giu.giu.model.Rol;
import com.giu.giu.model.Usuario;
import com.giu.giu.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

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
            // En producción, deberías comparar la contraseña hasheada
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
     * Lista usuarios pendientes de aprobación
     */
    public java.util.List<Usuario> obtenerPendientes() {
        return usuarioRepository.findByActivoFalse();
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
        usuarioRepository.deleteById(id);
    }

    /**
     * Lista todos los usuarios
     */
    public java.util.List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }
}
