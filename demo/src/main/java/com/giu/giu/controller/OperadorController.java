package com.giu.giu.controller;

import com.giu.giu.model.CategoriaIncidencia;
import com.giu.giu.model.Incidencia;
import com.giu.giu.model.Usuario;
import com.giu.giu.security.CustomUserDetails;
import com.giu.giu.service.IncidenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/operador")
public class OperadorController {

    @Autowired
    private IncidenciaService incidenciaService;

    /**
     * Página de gestión de solicitudes para operador
     */
    @GetMapping("/gestion-solicitudes")
    public String gestionSolicitudes(Model model) {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null || !usuario.getRol().name().equals("OPERADOR")) {
            return "redirect:/login";
        }

        List<Incidencia> solicitudesPendientes = incidenciaService.obtenerPendientes();
        Map<Long, List<Incidencia>> duplicadosPorIncidencia = incidenciaService.obtenerDuplicadosPendientes();
        model.addAttribute("solicitudes", solicitudesPendientes);
        model.addAttribute("duplicadosPorIncidencia", duplicadosPorIncidencia);
        model.addAttribute("categorias", CategoriaIncidencia.values());
        model.addAttribute("usuario", usuario);
        return "gestion-solicitudes";
    }

    /**
     * Aprobar una solicitud
     */
    @GetMapping("/aprobar-solicitud")
    public String aprobarSolicitud(@RequestParam Long id) {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null || !usuario.getRol().name().equals("OPERADOR")) {
            return "redirect:/login";
        }

        incidenciaService.aprobarIncidencia(id);
        return "redirect:/operador/gestion-solicitudes";
    }

    /**
     * Rechazar una solicitud
     */
    @PostMapping("/rechazar-solicitud")
    public String rechazarSolicitud(@RequestParam Long id, @RequestParam String razon) {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null || !usuario.getRol().name().equals("OPERADOR")) {
            return "redirect:/login";
        }

        incidenciaService.rechazarIncidenciaConRazon(id, razon);
        return "redirect:/operador/gestion-solicitudes";
    }

    /**
     * Editar categoría de una solicitud
     */
    @PostMapping("/editar-categoria")
    public String editarCategoria(@RequestParam Long id, @RequestParam CategoriaIncidencia categoria) {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null || !usuario.getRol().name().equals("OPERADOR")) {
            return "redirect:/login";
        }

        incidenciaService.editarCategoria(id, categoria);
        return "redirect:/operador/gestion-solicitudes";
    }

    private Usuario getUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            return userDetails.getUsuario();
        }
        return null;
    }
}
