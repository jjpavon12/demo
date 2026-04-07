package com.giu.giu.controller;

import com.giu.giu.dto.LoginRequest;
import com.giu.giu.dto.LoginResponse;
import com.giu.giu.model.Rol;
import com.giu.giu.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Endpoint de login
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = usuarioService.login(request);

        if (response.getId() != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body(response);
        }
    }

    /**
     * Endpoint de registro
     * POST /api/auth/registro
     */
    @PostMapping("/registro")
    public ResponseEntity<LoginResponse> registro(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(defaultValue = "CIUDADANO") Rol rol) {

        LoginResponse response = usuarioService.registrar(email, password, rol);

        if (response.getId() != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Endpoint de prueba para usuarios autenticados
     */
    @GetMapping("/perfil")
    public ResponseEntity<String> perfil() {
        return ResponseEntity.ok("Acceso autenticado permitido");
    }
}
