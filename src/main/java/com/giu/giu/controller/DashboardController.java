package com.giu.giu.controller;

import com.giu.giu.model.CategoriaIncidencia;
import com.giu.giu.model.ComentarioIncidencia;
import com.giu.giu.model.EquipoTecnicoConfig;
import com.giu.giu.model.EstadoIncidencia;
import com.giu.giu.model.Incidencia;
import com.giu.giu.model.PrioridadIncidencia;
import com.giu.giu.model.SolicitudExtension;
import com.giu.giu.model.Usuario;
import com.giu.giu.security.CustomUserDetails;
import com.giu.giu.service.IncidenciaService;
import com.giu.giu.service.NotificationService;
import com.giu.giu.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.giu.giu.model.CategoriaIncidencia;
import com.giu.giu.model.PrioridadIncidencia;
import com.giu.giu.service.ConfiguracionCategoriaService;
import com.giu.giu.service.ConfiguracionPrioridadService;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private static final EstadoIncidencia[] ESTADOS_OPERADOR = {
        EstadoIncidencia.VALIDADA,
        EstadoIncidencia.RECHAZADA
    };

    private final IncidenciaService incidenciaService;
    private final UsuarioService usuarioService;
    private final NotificationService notificationService;

    public DashboardController(IncidenciaService incidenciaService, UsuarioService usuarioService, NotificationService notificationService) {
        this.incidenciaService = incidenciaService;
        this.usuarioService = usuarioService;
        this.notificationService = notificationService;
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
            Long usuarioId = userDetails.getUsuario().getId();
            Optional<Usuario> usuarioOpt = usuarioService.obtenerPorId(usuarioId);
            if (usuarioOpt.isEmpty()) return "redirect:/logout";
            Usuario usuario = usuarioOpt.get();
            model.addAttribute("usuario", usuario);
            model.addAttribute("rol", "CIUDADANO");
            model.addAttribute("notificaciones", notificationService.buildFor(usuario));
            return "dashboard-ciudadano";
        }

        return "redirect:/login";
    }

    private static final Set<EstadoIncidencia> ESTADOS_VERIFICADOS = Set.of(
        EstadoIncidencia.VALIDADA, EstadoIncidencia.ASIGNADA,
        EstadoIncidencia.EN_CURSO, EstadoIncidencia.RESUELTA, EstadoIncidencia.CERRADA
    );

    @GetMapping("/operador")
    public String dashboardOperador(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            Long usuarioId = userDetails.getUsuario().getId();
            Optional<Usuario> usuarioOpt = usuarioService.obtenerPorId(usuarioId);
            if (usuarioOpt.isEmpty()) return "redirect:/logout";
            Usuario usuario = usuarioOpt.get();

            List<Incidencia> todas = incidenciaService.obtenerTodas();

            List<Incidencia> pendientes = todas.stream()
                .filter(i -> i.getEstado() == EstadoIncidencia.PENDIENTE_VALIDACION)
                .sorted(Comparator.comparing(Incidencia::getFechaCreacion).reversed())
                .collect(Collectors.toList());

            List<Incidencia> verificadas = todas.stream()
                .filter(i -> ESTADOS_VERIFICADOS.contains(i.getEstado()))
                .sorted(Comparator.comparing(Incidencia::getFechaModificacion,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

            List<Incidencia> rechazadas = todas.stream()
                .filter(i -> i.getEstado() == EstadoIncidencia.RECHAZADA)
                .sorted(Comparator.comparing(Incidencia::getFechaModificacion,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

            Map<Long, List<EquipoTecnicoConfig>> equiposSugeridosPorIncidencia = new HashMap<>();
            for (Incidencia inc : pendientes) {
                equiposSugeridosPorIncidencia.put(
                    inc.getId(),
                    incidenciaService.obtenerEquiposTecnicosOrdenadosParaIncidencia(inc)
                );
            }
            for (Incidencia inc : verificadas) {
                if (inc.getEstado() == EstadoIncidencia.VALIDADA || inc.getEstado() == EstadoIncidencia.ASIGNADA) {
                    equiposSugeridosPorIncidencia.put(
                        inc.getId(),
                        incidenciaService.obtenerEquiposTecnicosOrdenadosParaIncidencia(inc)
                    );
                }
            }

            Map<Long, List<ComentarioIncidencia>> comentariosPorIncidencia = new HashMap<>();
            for (Incidencia inc : todas) {
                comentariosPorIncidencia.put(inc.getId(), incidenciaService.obtenerComentariosPorIncidencia(inc));
            }

            List<SolicitudExtension> solicitudesPendientes = incidenciaService.obtenerSolicitudesPendientes();
            Map<Long, SolicitudExtension> solicitudPendientePorIncidencia = new HashMap<>();
            for (SolicitudExtension sol : solicitudesPendientes) {
                solicitudPendientePorIncidencia.put(sol.getIncidencia().getId(), sol);
            }

            model.addAttribute("usuario", usuario);
            model.addAttribute("rol", "OPERADOR");
            model.addAttribute("incidencias", pendientes);
            model.addAttribute("incidenciasVerificadas", verificadas);
            model.addAttribute("incidenciasRechazadas", rechazadas);
            model.addAttribute("estadosOperador", ESTADOS_OPERADOR);
            model.addAttribute("categorias", CategoriaIncidencia.values());
            model.addAttribute("prioridades", PrioridadIncidencia.values());
            model.addAttribute("equiposSugeridosPorIncidencia", equiposSugeridosPorIncidencia);
            model.addAttribute("comentariosPorIncidencia", comentariosPorIncidencia);
            model.addAttribute("solicitudPendientePorIncidencia", solicitudPendientePorIncidencia);
            model.addAttribute("totalSolicitudesPendientes", solicitudesPendientes.size());
            model.addAttribute("notificaciones", notificationService.buildFor(usuario));

            return "dashboard-operador";
        }

        return "redirect:/login";
    }

    @PostMapping("/operador/validar-y-asignar")
    public String validarYAsignar(@RequestParam Long id,
                                  @RequestParam(required = false) String tecnicoId) {
        if (incidenciaService.estaBloqueadaParaOperador(id)) {
            return "redirect:/dashboard/operador?view=pendientes";
        }
        incidenciaService.cambiarEstado(id, EstadoIncidencia.VALIDADA);
        if (tecnicoId != null && !tecnicoId.isBlank()) {
            incidenciaService.asignarATecnico(id, Long.parseLong(tecnicoId));
        }
        return "redirect:/dashboard/operador?view=pendientes";
    }

    @PostMapping("/operador/cambiar-estado")
    public String cambiarEstadoOperador(@RequestParam Long id, @RequestParam EstadoIncidencia estado) {
        if (incidenciaService.estaBloqueadaParaOperador(id)) {
            return "redirect:/dashboard/operador?view=pendientes";
        }
        if (estado != EstadoIncidencia.VALIDADA && estado != EstadoIncidencia.RECHAZADA) {
            return "redirect:/dashboard/operador?view=pendientes";
        }
        incidenciaService.cambiarEstado(id, estado);
        return "redirect:/dashboard/operador?view=pendientes";
    }

    @PostMapping("/operador/cerrar")
    public String cerrarIncidencia(@RequestParam Long id) {
        incidenciaService.cerrar(id);
        return "redirect:/dashboard/operador?view=verificadas";
    }

    @PostMapping("/operador/devolver-tecnico")
    public String devolverATecnico(@RequestParam Long id) {
        incidenciaService.devolverATecnico(id);
        return "redirect:/dashboard/operador?view=verificadas";
    }

    @PostMapping("/operador/rechazar")
    public String rechazarIncidencia(@RequestParam Long id,
                                     @RequestParam(required = false) String motivoRechazo) {
        if (incidenciaService.estaBloqueadaParaOperador(id)) {
            return "redirect:/dashboard/operador?view=pendientes";
        }
        incidenciaService.rechazar(id, motivoRechazo);
        return "redirect:/dashboard/operador?view=pendientes";
    }

    @PostMapping("/operador/cambiar-categoria")
    public String cambiarCategoriaOperador(@RequestParam Long id, @RequestParam List<CategoriaIncidencia> categorias) {
        if (incidenciaService.estaBloqueadaParaOperador(id)) {
            return "redirect:/dashboard/operador?view=pendientes";
        }
        incidenciaService.cambiarCategorias(id, new HashSet<>(categorias));
        return "redirect:/dashboard/operador?view=pendientes";
    }

    @PostMapping("/operador/cambiar-prioridad")
    public String cambiarPrioridadOperador(@RequestParam Long id, @RequestParam PrioridadIncidencia prioridad) {
        incidenciaService.cambiarPrioridad(id, prioridad);
        return "redirect:/dashboard/operador?view=pendientes";
    }

    @PostMapping("/operador/asignar-incidencia")
    public String asignarIncidencia(@RequestParam Long incidenciaId, @RequestParam Long tecnicoId) {
        incidenciaService.asignarATecnico(incidenciaId, tecnicoId);
        return "redirect:/dashboard/operador?view=verificadas";
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

            List<Incidencia> incidencias = incidenciaService.obtenerAsignadasATecnico(usuario);

            Map<Long, List<ComentarioIncidencia>> comentariosPorIncidencia = new HashMap<>();
            Map<Long, SolicitudExtension> solicitudPendientePorIncidencia = new HashMap<>();
            for (Incidencia inc : incidencias) {
                comentariosPorIncidencia.put(inc.getId(), incidenciaService.obtenerComentariosPorIncidencia(inc));
                if (inc.isTieneSolicitudExtensionPendiente()) {
                    solicitudPendientePorIncidencia.put(inc.getId(), null);
                }
            }

            model.addAttribute("usuario", usuario);
            model.addAttribute("rol", "TECNICO");
            model.addAttribute("configEquipo", configOpt.orElse(null));
            model.addAttribute("incidencias", incidencias);
            model.addAttribute("estados", EstadoIncidencia.values());
            model.addAttribute("comentariosPorIncidencia", comentariosPorIncidencia);
            model.addAttribute("incidenciasConSolicitudPendiente", solicitudPendientePorIncidencia.keySet());
            model.addAttribute("notificaciones", notificationService.buildFor(usuario));

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

    @PostMapping("/tecnico/comentar")
    public String comentarTecnico(@RequestParam Long incidenciaId, @RequestParam String contenido) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            incidenciaService.agregarComentario(incidenciaId, userDetails.getUsuario().getId(), contenido);
        }
        return "redirect:/dashboard/tecnico";
    }

    @PostMapping("/tecnico/solicitar-extension")
    public String solicitarExtension(@RequestParam Long incidenciaId,
                                     @RequestParam String motivo,
                                     @RequestParam(required = false) String comentarioAdicional,
                                     @RequestParam String fechaSolicitada) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) {
            return "redirect:/login";
        }
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        LocalDate fecha;
        try {
            fecha = LocalDate.parse(fechaSolicitada);
        } catch (Exception e) {
            return "redirect:/dashboard/tecnico?errorExtension=1";
        }
        String error = incidenciaService.solicitarExtension(
            incidenciaId, userDetails.getUsuario().getId(), motivo, comentarioAdicional, fecha
        );
        if (error != null) {
            return "redirect:/dashboard/tecnico?errorExtension=1";
        }
        return "redirect:/dashboard/tecnico?extensionEnviada=1";
    }

    @GetMapping("/ciudadano/notificaciones/estados")
    public String abrirNotificacionesCiudadanoEstados() {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null) return "redirect:/login";
        var resumen = notificationService.buildFor(usuario);
        String ids = notificationService.idsParam(resumen.getCitizenStateChangeIds());
        notificationService.markCitizenStateSeen(usuario);
        return "redirect:/ciudadano/incidencias/mis-incidencias?highlight=" + ids;
    }

    @GetMapping("/operador/notificaciones/nuevas")
    public String abrirNotificacionesOperadorNuevas() {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null) return "redirect:/login";
        var resumen = notificationService.buildFor(usuario);
        String ids = notificationService.idsParam(resumen.getOperatorNewIncidentIds());
        notificationService.markOperatorNewSeen(usuario);
        return "redirect:/dashboard/operador?view=pendientes&highlight=" + ids;
    }

    @GetMapping("/operador/notificaciones/cambios")
    public String abrirNotificacionesOperadorCambios() {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null) return "redirect:/login";
        var resumen = notificationService.buildFor(usuario);
        String ids = notificationService.idsParam(resumen.getOperatorStateChangeIds());
        notificationService.markOperatorChangesSeen(usuario);
        return "redirect:/dashboard/operador?view=verificadas&highlight=" + ids;
    }

    @GetMapping("/tecnico/notificaciones/asignadas")
    public String abrirNotificacionesTecnicoAsignadas() {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null) return "redirect:/login";
        var resumen = notificationService.buildFor(usuario);
        String ids = notificationService.idsParam(resumen.getTechnicianNewAssignmentIds());
        notificationService.markTechnicianAssignmentsSeen(usuario);
        return "redirect:/dashboard/tecnico?status=ASIGNADA&highlight=" + ids;
    }

    @GetMapping("/tecnico/notificaciones/extensiones")
    public String abrirNotificacionesTecnicoExtensiones() {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null) return "redirect:/login";
        var resumen = notificationService.buildFor(usuario);
        String ids = notificationService.idsParam(resumen.getTechnicianApprovedExtensionIds());
        notificationService.markTechnicianExtensionsSeen(usuario);
        return "redirect:/dashboard/tecnico?status=" + estadoPrimeraIncidencia(resumen.getTechnicianApprovedExtensionIds(), "EN_CURSO") + "&highlight=" + ids;
    }

    @GetMapping("/tecnico/notificaciones/limite")
    public String abrirNotificacionesTecnicoLimite() {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null) return "redirect:/login";
        var resumen = notificationService.buildFor(usuario);
        String ids = notificationService.idsParam(resumen.getTechnicianDueSoonIds());
        notificationService.markTechnicianDueSoonSeen(usuario);
        return "redirect:/dashboard/tecnico?status=" + estadoPrimeraIncidencia(resumen.getTechnicianDueSoonIds(), "EN_CURSO") + "&highlight=" + ids;
    }

    private Usuario getUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            Long id = userDetails.getUsuario().getId();
            return usuarioService.obtenerPorId(id).orElse(userDetails.getUsuario());
        }
        return null;
    }

    private String estadoPrimeraIncidencia(List<Long> ids, String fallback) {
        if (ids == null || ids.isEmpty()) return fallback;
        return incidenciaService.obtenerPorId(ids.get(0))
            .map(i -> i.getEstado().name())
            .orElse(fallback);
    }

    @PostMapping("/operador/comentar")
    public String comentarOperador(@RequestParam Long incidenciaId, @RequestParam String contenido) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            incidenciaService.agregarComentario(incidenciaId, userDetails.getUsuario().getId(), contenido);
        }
        return "redirect:/dashboard/operador?view=verificadas";
    }

    @PostMapping("/operador/decidir-extension")
    public String decidirExtension(@RequestParam Long solicitudId,
                                   @RequestParam boolean aprobada,
                                   @RequestParam(required = false) String motivoDecision) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            incidenciaService.decidirExtension(solicitudId, userDetails.getUsuario().getId(), aprobada, motivoDecision);
        }
        return "redirect:/dashboard/operador?view=verificadas";
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
            model.addAttribute("prioridadesConfig", configuracionPrioridadService.obtenerTodas());
            model.addAttribute("categoriasConfig", configuracionCategoriaService.obtenerTodas());
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
