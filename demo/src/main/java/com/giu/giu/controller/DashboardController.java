package com.giu.giu.controller;

import com.giu.giu.model.EstadoIncidencia;
import com.giu.giu.model.Usuario;
import com.giu.giu.security.CustomUserDetails;
import com.giu.giu.service.IncidenciaService;
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
    private final IncidenciaService incidenciaService;

    public DashboardController(UsuarioService usuarioService, IncidenciaService incidenciaService) {
        this.usuarioService = usuarioService;
        this.incidenciaService = incidenciaService;
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
            
            // Mapear ADMINISTRADOR a "admin"
            if (rol.equals("administrador")) {
                rol = "admin";
            }
            
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
            model.addAttribute("incidencias", incidenciaService.obtenerTodas());
            model.addAttribute("estados", EstadoIncidencia.values());
            return "dashboard-operador";
        }

        return "redirect:/login";
    }

    @PostMapping("/operador/cambiar-estado")
    public String cambiarEstadoOperador(@RequestParam Long id, @RequestParam EstadoIncidencia estado) {
        incidenciaService.cambiarEstado(id, estado);
        return "redirect:/dashboard/operador";
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
            model.addAttribute("incidencias", incidenciaService.obtenerTodas());
            model.addAttribute("estados", EstadoIncidencia.values());
            return "dashboard-tecnico";
        }

        return "redirect:/login";
    }

    @PostMapping("/tecnico/cambiar-estado")
    public String cambiarEstadoTecnico(@RequestParam Long id, @RequestParam EstadoIncidencia estado) {
        incidenciaService.cambiarEstado(id, estado);
        return "redirect:/dashboard/tecnico";
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
            model.addAttribute("incidencias", incidenciaService.obtenerTodas());
            model.addAttribute("estados", EstadoIncidencia.values());
            return "dashboard-admin";
        }

        return "redirect:/login";
    }

    @PostMapping("/admin/cambiar-estado")
    public String cambiarEstadoAdmin(@RequestParam Long id, @RequestParam EstadoIncidencia estado) {
        incidenciaService.cambiarEstado(id, estado);
        return "redirect:/dashboard/admin";
    }
}
