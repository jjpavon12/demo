package com.giu.giu.controller;

import com.giu.giu.model.CategoriaIncidencia;
import com.giu.giu.model.Incidencia;
import com.giu.giu.model.Usuario;
import com.giu.giu.security.CustomUserDetails;
import com.giu.giu.service.ConfiguracionCategoriaService;
import com.giu.giu.service.IncidenciaService;
import com.giu.giu.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/ciudadano/incidencias")
public class IncidenciaController {

    private static final double CM_MIN_LAT = 39.88, CM_MAX_LAT = 41.17;
    private static final double CM_MIN_LON = -4.58, CM_MAX_LON = -3.05;

    @Autowired
    private IncidenciaService incidenciaService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ConfiguracionCategoriaService configuracionCategoriaService;

    @GetMapping("/registrar")
    public String mostrarFormulario(Model model) {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null) return "redirect:/login";
        model.addAttribute("categorias", CategoriaIncidencia.values());
        model.addAttribute("usuario", usuario);
        model.addAttribute("categoriasConfig", configuracionCategoriaService.obtenerActivas());
        return "registrar-incidencia";
    }

    @PostMapping("/registrar")
    public String registrarIncidencia(@RequestParam String descripcion,
                                      @RequestParam String ubicacion,
                                      @RequestParam(required = false) Double latitud,
                                      @RequestParam(required = false) Double longitud,
                                      @RequestParam List<CategoriaIncidencia> categorias,
                                      @RequestParam(required = false) MultipartFile imagen,
                                      Model model) {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null) return "redirect:/login";

        String imagenNombre = null;
        if (imagen != null && !imagen.isEmpty()) {
            String contentType = imagen.getContentType();
            if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
                model.addAttribute("errorImagen", "Solo se permiten imágenes .jpg, .jpeg o .png");
                model.addAttribute("categorias", CategoriaIncidencia.values());
                return "registrar-incidencia";
            }
            if (imagen.getSize() > 5L * 1024 * 1024) {
                model.addAttribute("errorImagen", "La imagen no puede superar los 5 MB");
                model.addAttribute("categorias", CategoriaIncidencia.values());
                return "registrar-incidencia";
            }
            try {
                String ext = contentType.equals("image/png") ? ".png" : ".jpg";
                imagenNombre = UUID.randomUUID().toString() + ext;
                Path uploadDir = Paths.get("uploads/incidencias");
                Files.createDirectories(uploadDir);
                Files.copy(imagen.getInputStream(), uploadDir.resolve(imagenNombre), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                model.addAttribute("errorImagen", "Error al guardar la imagen. Inténtalo de nuevo.");
                model.addAttribute("categorias", CategoriaIncidencia.values());
                return "registrar-incidencia";
            }
        }

        if (fueraDeCAM(latitud, longitud)) {
            model.addAttribute("errorUbicacion", "La ubicación seleccionada está fuera de la Comunidad de Madrid. Solo se pueden registrar incidencias dentro de la CAM.");
            model.addAttribute("categorias", CategoriaIncidencia.values());
            return "registrar-incidencia";
        }

        incidenciaService.registrar(descripcion, ubicacion, latitud, longitud, new HashSet<>(categorias), imagenNombre, usuario);
        return "redirect:/ciudadano/incidencias/mis-incidencias?exito";
    }

    @GetMapping("/mis-incidencias")
    public String misIncidencias(Model model) {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null) return "redirect:/login";
        List<Incidencia> incidencias = incidenciaService.obtenerPorUsuario(usuario);
        model.addAttribute("incidencias", incidencias);
        model.addAttribute("usuario", usuario);
        return "mis-incidencias";
    }

    @GetMapping("/notificaciones/estado")
    public String abrirNotificacionesEstado() {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null) return "redirect:/login";
        var resumen = notificationService.buildFor(usuario);
        String ids = notificationService.idsParam(resumen.getCitizenStateChangeIds());
        notificationService.markCitizenStateSeen(usuario);
        return "redirect:/ciudadano/incidencias/mis-incidencias?highlight=" + ids;
    }

    private boolean fueraDeCAM(Double lat, Double lon) {
        return lat != null && lon != null &&
               (lat < CM_MIN_LAT || lat > CM_MAX_LAT || lon < CM_MIN_LON || lon > CM_MAX_LON);
    }

    private Usuario getUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) auth.getPrincipal()).getUsuario();
        }
        return null;
    }
}
