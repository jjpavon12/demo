package com.giu.giu.controller;

import com.giu.giu.model.CategoriaIncidencia;
import com.giu.giu.model.EquipoTecnicoConfig;
import com.giu.giu.model.EstadoIncidencia;
import com.giu.giu.model.Incidencia;
import com.giu.giu.model.PrioridadIncidencia;
import com.giu.giu.model.Usuario;
import com.giu.giu.security.CustomUserDetails;
import com.giu.giu.service.IncidenciaService;
import com.giu.giu.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

    @GetMapping("/home")
    public String home() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            String rol = userDetails.getUsuario().getRol().name().toLowerCase();

            if (rol.equals("administrador")) {
                rol = "admin";
            }

            return "redirect:/dashboard/" + rol;
        }

        return "redirect:/login";
    }

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

    @GetMapping("/operador")
    public String dashboardOperador(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            Usuario usuario = userDetails.getUsuario();

            List<Incidencia> incidencias = incidenciaService.obtenerTodas();
            Map<Long, List<EquipoTecnicoConfig>> equiposSugeridosPorIncidencia = new HashMap<>();

            for (Incidencia inc : incidencias) {
                if (inc.getEstado() == EstadoIncidencia.VALIDADA || inc.getEstado() == EstadoIncidencia.ASIGNADA) {
                    equiposSugeridosPorIncidencia.put(
                        inc.getId(),
                        incidenciaService.obtenerEquiposTecnicosOrdenadosParaIncidencia(inc)
                    );
                }
            }

            model.addAttribute("usuario", usuario);
            model.addAttribute("rol", "OPERADOR");
            model.addAttribute("incidencias", incidencias);
            model.addAttribute("estadosOperador", ESTADOS_OPERADOR);
            model.addAttribute("categorias", CategoriaIncidencia.values());
            model.addAttribute("prioridades", PrioridadIncidencia.values());
            model.addAttribute("equiposSugeridosPorIncidencia", equiposSugeridosPorIncidencia);

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

    @PostMapping("/operador/cambiar-prioridad")
    public String cambiarPrioridadOperador(@RequestParam Long id, @RequestParam PrioridadIncidencia prioridad) {
        incidenciaService.cambiarPrioridad(id, prioridad);
        return "redirect:/dashboard/operador";
    }

    @PostMapping("/operador/asignar-incidencia")
    public String asignarIncidencia(@RequestParam Long incidenciaId, @RequestParam Long tecnicoId) {
        incidenciaService.asignarATecnico(incidenciaId, tecnicoId);
        return "redirect:/dashboard/operador";
    }

    @GetMapping("/tecnico")
    public String dashboardTecnico(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            Long usuarioId = userDetails.getUsuario().getId();

            Optional<Usuario> usuarioOpt = usuarioService.obtenerPorId(usuarioId);
            if (usuarioOpt.isEmpty()) {
                return "redirect:/logout";
            }

            Usuario usuario = usuarioOpt.get();
            Optional<EquipoTecnicoConfig> configOpt = usuarioService.obtenerConfigEquipoTecnico(usuario.getId());

            model.addAttribute("usuario", usuario);
            model.addAttribute("rol", "TECNICO");
            model.addAttribute("configEquipo", configOpt.orElse(null));
            model.addAttribute("incidencias", incidenciaService.obtenerAsignadasATecnico(usuario));
            model.addAttribute("estados", EstadoIncidencia.values());

            return "dashboard-tecnico";
        }

        return "redirect:/login";
    }

    @PostMapping("/tecnico/configurar-equipo")
    public String configurarEquipoTecnico(@RequestParam String nombreEquipo,
                                          @RequestParam(required = false) List<CategoriaIncidencia> especialidades) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) {
            return "redirect:/login";
        }

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        Long usuarioId = userDetails.getUsuario().getId();

        Set<CategoriaIncidencia> categoriasSeleccionadas =
            especialidades != null ? new HashSet<>(especialidades) : new HashSet<>();

        String error = usuarioService.configurarEquipoTecnico(
            usuarioId,
            nombreEquipo,
            categoriasSeleccionadas
        );

        if (error != null) {
            return "redirect:/dashboard/tecnico?error=1";
        }

        return "redirect:/dashboard/tecnico?ok=1";
    }

    @PostMapping("/tecnico/cambiar-estado")
    public String cambiarEstadoTecnico(@RequestParam Long id, @RequestParam EstadoIncidencia estado) {
        incidenciaService.cambiarEstado(id, estado);
        return "redirect:/dashboard/tecnico";
    }

    @GetMapping("/admin")
    public String dashboardAdmin(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            Usuario usuario = userDetails.getUsuario();
            model.addAttribute("usuario", usuario);
            model.addAttribute("rol", "ADMINISTRADOR");
            model.addAttribute("pendientes", usuarioService.obtenerPendientes());
            model.addAttribute("solicitudesRol", usuarioService.obtenerSolicitudesRol());
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

    @PostMapping("/admin/aprobar-rol")
    public String aprobarCambioRol(@RequestParam Long id) {
        usuarioService.aprobarCambioRol(id);
        return "redirect:/dashboard/admin";
    }

    @PostMapping("/admin/denegar-rol")
    public String denegarCambioRol(@RequestParam Long id) {
        usuarioService.denegarCambioRol(id);
        return "redirect:/dashboard/admin";
    }

    @PostMapping("/admin/eliminar-usuario")
    public String eliminarUsuario(@RequestParam Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            if (userDetails.getUsuario().getId().equals(id)) {
                return "redirect:/dashboard/admin";
            }
        }

        String error = usuarioService.eliminarUsuario(id);
        if (error != null) {
            return "redirect:/dashboard/admin";
        }
        return "redirect:/dashboard/admin";
    }
}