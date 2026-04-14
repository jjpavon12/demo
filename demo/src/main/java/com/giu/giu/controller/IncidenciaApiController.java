package com.giu.giu.controller;

import com.giu.giu.model.Incidencia;
import com.giu.giu.model.Usuario;
import com.giu.giu.security.CustomUserDetails;
import com.giu.giu.service.IncidenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/incidencias")
public class IncidenciaApiController {

    @Autowired
    private IncidenciaService incidenciaService;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Devuelve TODAS las incidencias (para operador/técnico)
     */
    @GetMapping
    public List<Map<String, Object>> todas() {
        return incidenciaService.obtenerTodas().stream()
                .map(this::toMap)
                .collect(Collectors.toList());
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
        map.put("email", inc.getUsuario().getEmail());
        return map;
    }
}
