package com.giu.giu.controller;

import com.giu.giu.config.DatabaseInitializer;
import com.giu.giu.model.EstadoIncidencia;
import com.giu.giu.model.Incidencia;
import com.giu.giu.model.Usuario;
import com.giu.giu.security.CustomUserDetails;
import com.giu.giu.service.IncidenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/incidencias")
public class IncidenciaApiController {

    @Autowired
    private IncidenciaService incidenciaService;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Devuelve incidencias con coordenadas para el mapa público */
    @GetMapping("/publicas")
    public List<Map<String, Object>> publicas() {
        return incidenciaService.obtenerTodas().stream()
                .filter(inc -> inc.getLatitud() != null && inc.getLongitud() != null)
                .map(inc -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("latitud", inc.getLatitud());
                    m.put("longitud", inc.getLongitud());
                    m.put("estado", inc.getEstado().name());
                    m.put("estadoDesc", inc.getEstado().getDescripcion());
                    m.put("descripcion", inc.getDescripcion());
                    m.put("categorias", inc.getCategorias().stream()
                            .map(c -> c.getDescripcion()).collect(Collectors.toList()));
                    m.put("imagenNombre", inc.getImagenNombre());
                    return m;
                })
                .collect(Collectors.toList());
    }

    /** Devuelve TODAS las incidencias (para operador/técnico) */
    @GetMapping
    public List<Map<String, Object>> todas() {
        return incidenciaService.obtenerTodas().stream()
                .map(this::toMap)
                .collect(Collectors.toList());
    }

    /** Devuelve las incidencias asignadas al técnico autenticado (ASIGNADA, EN_CURSO, RESUELTA) */
    @GetMapping("/mis-asignadas")
    public List<Map<String, Object>> misAsignadas() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            Usuario usuario = ((CustomUserDetails) auth.getPrincipal()).getUsuario();
            EnumSet<EstadoIncidencia> estados = EnumSet.of(
                    EstadoIncidencia.ASIGNADA,
                    EstadoIncidencia.EN_CURSO,
                    EstadoIncidencia.RESUELTA,
                    EstadoIncidencia.CERRADA);
            return incidenciaService.obtenerAsignadasATecnico(usuario).stream()
                    .filter(inc -> estados.contains(inc.getEstado()))
                    .map(this::toMap)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * Devuelve las incidencias del ciudadano autenticado
     */
    @GetMapping("/mis")
    public List<Map<String, Object>> misIncidencias() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            Usuario usuario = ((CustomUserDetails) auth.getPrincipal()).getUsuario();
            return incidenciaService.obtenerPorUsuario(usuario).stream()
                    .map(this::toMap)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @GetMapping("/imagen/{nombre}")
    public ResponseEntity<Resource> servirImagen(@PathVariable String nombre) throws IOException {
        Path imagePath = Paths.get("uploads/incidencias").resolve(nombre).normalize();
        Resource resource = new UrlResource(imagePath.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }
        String contentType = nombre.toLowerCase().endsWith(".png") ? "image/png" : "image/jpeg";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    private Map<String, Object> toMap(Incidencia inc) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", inc.getId());
        map.put("descripcion", inc.getDescripcion());
        map.put("ubicacion", inc.getUbicacion());
        map.put("latitud", inc.getLatitud());
        map.put("longitud", inc.getLongitud());
        map.put("estado", inc.getEstado().name());
        map.put("estadoDesc", inc.getEstado().getDescripcion());
        map.put("categorias", inc.getCategorias().stream()
                .map(c -> c.getDescripcion())
                .collect(Collectors.toList()));
        map.put("fecha", inc.getFechaCreacion().format(FMT));
        Usuario autor = inc.getUsuario();
        boolean esAnonimo = autor == null || DatabaseInitializer.EMAIL_ANONIMO.equals(autor.getEmail());
        map.put("email", esAnonimo ? "Anónimo" : autor.getEmail());
        map.put("prioridad", inc.getPrioridad() != null ? inc.getPrioridad().name() : null);
        map.put("prioridadDesc", inc.getPrioridad() != null ? inc.getPrioridad().getDescripcion() : null);
        map.put("fechaMod", inc.getFechaModificacion() != null ? inc.getFechaModificacion().format(FMT) : null);
        map.put("fechaAsignacion", inc.getFechaAsignacion() != null ? inc.getFechaAsignacion().format(FMT) : null);
        return map;
    }
}
