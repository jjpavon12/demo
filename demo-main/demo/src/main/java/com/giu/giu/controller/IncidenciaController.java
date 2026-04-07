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
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/ciudadano/incidencias")
public class IncidenciaController {

    @Autowired
    private IncidenciaService incidenciaService;

    /**
     * Formulario para registrar una nueva incidencia
     */
    @GetMapping("/registrar")
    public String mostrarFormulario(Model model) {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null) return "redirect:/login";

        model.addAttribute("categorias", CategoriaIncidencia.values());
        model.addAttribute("usuario", usuario);
        return "registrar-incidencia";
    }

    /**
     * Procesa el envío del formulario de nueva incidencia
     */
    @PostMapping("/registrar")
    public String registrarIncidencia(@RequestParam String descripcion,
                                      @RequestParam String ubicacion,
                                      @RequestParam List<CategoriaIncidencia> categorias,
                                      Model model) {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null) return "redirect:/login";

        incidenciaService.registrar(descripcion, ubicacion, new HashSet<>(categorias), usuario);
        return "redirect:/ciudadano/incidencias/mis-incidencias?exito";
    }

    /**
     * Lista de incidencias del ciudadano autenticado
     */
    @GetMapping("/mis-incidencias")
    public String misIncidencias(Model model) {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null) return "redirect:/login";

        List<Incidencia> incidencias = incidenciaService.obtenerPorUsuario(usuario);
        model.addAttribute("incidencias", incidencias);
        model.addAttribute("usuario", usuario);
        return "mis-incidencias";
    }

    private Usuario getUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) auth.getPrincipal()).getUsuario();
        }
        return null;
    }
}
