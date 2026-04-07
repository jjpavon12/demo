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
            if (!usuario.isValidado()) {
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

        boolean validado = rol == Rol.CIUDADANO;
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setEmail(email);
        nuevoUsuario.setPassword(passwordEncoder.encode(password));
        nuevoUsuario.setRol(rol);
        nuevoUsuario.setValidado(validado);

        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);

        return new LoginResponse(
            usuarioGuardado.getId(),
            usuarioGuardado.getEmail(),
            usuarioGuardado.getRol(),
            validado
                ? "Usuario registrado exitosamente"
                : "Usuario registrado correctamente. Espera validación del administrador."
        );
    }

    public java.util.List<Usuario> obtenerUsuariosPendientes() {
        return usuarioRepository.findByValidadoFalse();
    }

    public void validarUsuario(Long id) {
        usuarioRepository.findById(id).ifPresent(usuario -> {
            usuario.setValidado(true);
            usuarioRepository.save(usuario);
        });
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
}
