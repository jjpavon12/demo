package com.giu.giu.controller;

import com.giu.giu.model.Rol;
import com.giu.giu.security.CustomUserDetails;
import com.giu.giu.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/ciudadano/perfil")
public class PerfilController {

    private final UsuarioService usuarioService;

    public PerfilController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String mostrarPerfil(Model model,
                                @RequestParam(required = false) String exito,
                                @RequestParam(required = false) String error) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            model.addAttribute("usuario", userDetails.getUsuario());
            model.addAttribute("exito", exito);
            model.addAttribute("error", error);
            // Roles disponibles para solicitar (excluye ADMINISTRADOR y el rol actual)
            model.addAttribute("rolesDisponibles", Rol.values());
            return "perfil-ciudadano";
        }
        return "redirect:/login";
    }

    @PostMapping
    public String actualizarPerfil(@RequestParam String email,
                                   @RequestParam(required = false) String password,
                                   @RequestParam(required = false) String confirmarPassword) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails userDetails)) {
            return "redirect:/login";
        }

        Long id = userDetails.getUsuario().getId();
        String emailActual = userDetails.getUsuario().getEmail();

        if (email == null || email.isBlank()) {
            return "redirect:/ciudadano/perfil?error=El+email+no+puede+estar+vacío";
        }

        if (password != null && !password.isBlank()) {
            if (!password.equals(confirmarPassword)) {
                return "redirect:/ciudadano/perfil?error=Las+contraseñas+no+coinciden";
            }
        }

        String errorMsg = usuarioService.actualizarPerfil(id, email, password);
        if (errorMsg != null) {
            return "redirect:/ciudadano/perfil?error=" + errorMsg.replace(" ", "+");
        }

        // Si cambió el email, forzar re-login para que Spring Security recargue las credenciales
        if (!emailActual.equals(email)) {
            return "redirect:/logout";
        }

        return "redirect:/ciudadano/perfil?exito=Perfil+actualizado+correctamente";
    }

    @PostMapping("/solicitar-rol")
    public String solicitarRol(@RequestParam Rol rolSolicitado) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails userDetails)) {
            return "redirect:/login";
        }
        Long id = userDetails.getUsuario().getId();
        String error = usuarioService.solicitarCambioRol(id, rolSolicitado);
        if (error != null) {
            return "redirect:/ciudadano/perfil?error=" + error.replace(" ", "+");
        }
        return "redirect:/ciudadano/perfil?exito=Solicitud+enviada.+Un+administrador+la+revisará+en+breve";
    }
}
