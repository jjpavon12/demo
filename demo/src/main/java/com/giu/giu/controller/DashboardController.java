package com.giu.giu.controller;

import com.giu.giu.model.CategoriaIncidencia;
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

import java.util.HashSet;
import java.util.List;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private static final EstadoIncidencia[] ESTADOS_OPERADOR = {
        EstadoIncidencia.VALIDADA,
        EstadoIncidencia.RECHAZADA
    };

    private final IncidenciaService incidenciaService;
    private final UsuarioService usuarioService;

    public DashboardController(IncidenciaService incidenciaService, UsuarioService usuarioService) {
        this.incidenciaService = incidenciaService;
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
            model.addAttribute("estadosOperador", ESTADOS_OPERADOR);
            model.addAttribute("categorias", CategoriaIncidencia.values());
            return "dashboard-operador";
        }

        return "redirect:/login";
    }

    @PostMapping("/operador/cambiar-estado")
    public String cambiarEstadoOperador(@RequestParam Long id, @RequestParam EstadoIncidencia estado) {
        if (incidenciaService.estaBloqueadaParaOperador(id)) {
            return "redirect:/dashboard/operador";
        }
        if (estado != EstadoIncidencia.VALIDADA && estado != EstadoIncidencia.RECHAZADA) {
            return "redirect:/dashboard/operador";
        }
        incidenciaService.cambiarEstado(id, estado);
        return "redirect:/dashboard/operador";
    }

    @PostMapping("/operador/cambiar-categoria")
    public String cambiarCategoriaOperador(@RequestParam Long id, @RequestParam List<CategoriaIncidencia> categorias) {
        if (incidenciaService.estaBloqueadaParaOperador(id)) {
            return "redirect:/dashboard/operador";
        }
        incidenciaService.cambiarCategorias(id, new HashSet<>(categorias));
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
     * Dashboard para ADMINISTRADOR — gestión de usuarios
     */
    @GetMapping("/admin")
    public String dashboardAdmin(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            Usuario usuario = userDetails.getUsuario();
            model.addAttribute("usuario", usuario);
            model.addAttribute("rol", "ADMINISTRADOR");
            model.addAttribute("pendientes", usuarioService.obtenerPendientes());
            model.addAttribute("todosUsuarios", usuarioService.obtenerTodos());
            return "dashboard-admin";
        }

        return "redirect:/login";
    }

    @PostMapping("/admin/aprobar-usuario")
    public String aprobarUsuario(@RequestParam Long id) {
        usuarioService.aprobarUsuario(id);
        return "redirect:/dashboard/admin";
    }

    @PostMapping("/admin/denegar-usuario")
    public String denegarUsuario(@RequestParam Long id) {
        usuarioService.denegarUsuario(id);
        return "redirect:/dashboard/admin";
    }
}
