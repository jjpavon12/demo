package com.giu.giu.controller;

import com.giu.giu.dto.LoginRequest;
import com.giu.giu.model.Usuario;
import com.giu.giu.security.CustomUserDetails;
import com.giu.giu.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final UsuarioService usuarioService;

    public DashboardController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Página de Home - Redirige según el rol del usuario
     */
    @GetMapping("/home")
    public String home() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            String rol = userDetails.getUsuario().getRol().name().toLowerCase();
            return "redirect:/dashboard/" + rol;
        }

        return "redirect:/login";
    }

    /**
     * Dashboard para CIUDADANO
     */
    @GetMapping("/ciudadano")
    public String dashboardCiudadano(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            Usuario usuario = userDetails.getUsuario();
            model.addAttribute("usuario", usuario);
            model.addAttribute("rol", "CIUDADANO");
            return "dashboard-ciudadano";
        }

        return "redirect:/login";
    }

    /**
     * Dashboard para OPERADOR
     */
    @GetMapping("/operador")
    public String dashboardOperador(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            Usuario usuario = userDetails.getUsuario();
            model.addAttribute("usuario", usuario);
            model.addAttribute("rol", "OPERADOR");
            return "dashboard-operador";
        }

        return "redirect:/login";
    }

    /**
     * Dashboard para TECNICO
     */
    @GetMapping("/tecnico")
    public String dashboardTecnico(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            Usuario usuario = userDetails.getUsuario();
            model.addAttribute("usuario", usuario);
            model.addAttribute("rol", "TECNICO");
            return "dashboard-tecnico";
        }

        return "redirect:/login";
    }

    /**
     * Dashboard para ADMINISTRADOR
     */
    @GetMapping("/admin")
    public String dashboardAdmin(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            Usuario usuario = userDetails.getUsuario();
            model.addAttribute("usuario", usuario);
            model.addAttribute("rol", "ADMINISTRADOR");
            return "dashboard-admin";
        }

        return "redirect:/login";
    }
}
