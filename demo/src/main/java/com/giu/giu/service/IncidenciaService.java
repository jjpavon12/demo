package com.giu.giu.service;

import com.giu.giu.model.CategoriaIncidencia;
import com.giu.giu.model.EstadoIncidencia;
import com.giu.giu.model.Incidencia;
import com.giu.giu.model.Usuario;
import com.giu.giu.repository.IncidenciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class IncidenciaService {

    @Autowired
    private IncidenciaRepository incidenciaRepository;

    public Incidencia registrar(String descripcion, String ubicacion, CategoriaIncidencia categoria, Usuario usuario) {
        Incidencia incidencia = new Incidencia();
        incidencia.setDescripcion(descripcion);
        incidencia.setUbicacion(ubicacion);
        incidencia.setCategoria(categoria);
        incidencia.setEstado(EstadoIncidencia.PENDIENTE_REVISION);
        incidencia.setUsuario(usuario);
        return incidenciaRepository.save(incidencia);
    }

    public List<Incidencia> obtenerPorUsuario(Usuario usuario) {
        return incidenciaRepository.findByUsuarioOrderByFechaCreacionDesc(usuario);
    }

    public List<Incidencia> obtenerPendientes() {
        return incidenciaRepository.findWithUsuarioByEstado(EstadoIncidencia.PENDIENTE_REVISION);
    }

    public void aprobarIncidencia(Long id) {
        Incidencia incidencia = incidenciaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incidencia no encontrada"));
        incidencia.setEstado(EstadoIncidencia.EN_PROCESO);
        incidenciaRepository.save(incidencia);
    }

    public void rechazarIncidencia(Long id) {
        Incidencia incidencia = incidenciaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incidencia no encontrada"));
        incidencia.setEstado(EstadoIncidencia.RECHAZADA);
        incidenciaRepository.save(incidencia);
    }

    public void rechazarIncidenciaConRazon(Long id, String razon) {
        Incidencia incidencia = incidenciaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incidencia no encontrada"));
        incidencia.setEstado(EstadoIncidencia.RECHAZADA);
        incidencia.setRazonRechazo(razon);
        incidenciaRepository.save(incidencia);
    }

    public void editarCategoria(Long id, CategoriaIncidencia nuevaCategoria) {
        Incidencia incidencia = incidenciaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incidencia no encontrada"));
        incidencia.setCategoria(nuevaCategoria);
        incidencia.setCategoriaEditada(nuevaCategoria);
        incidenciaRepository.save(incidencia);
    }

    public List<Incidencia> obtenerDuplicadosPorUbicacion(String ubicacion) {
        return incidenciaRepository.findByUbicacionAndEstado(ubicacion, EstadoIncidencia.PENDIENTE_REVISION);
    }

    public void actualizar(Incidencia incidencia) {
        incidenciaRepository.save(incidencia);
    }

    public void marcarDuplicados() {
        actualizarDuplicados(incidenciaRepository.findByEstado(EstadoIncidencia.PENDIENTE_REVISION));
    }

    public Map<Long, List<Incidencia>> obtenerDuplicadosPendientes() {
        List<Incidencia> pendientes = obtenerPendientes();
        actualizarDuplicados(pendientes);
        return construirMapaDuplicados(pendientes);
    }

    private void actualizarDuplicados(List<Incidencia> incidencias) {
        Map<String, List<Incidencia>> porUbicacion = agruparPorUbicacionNormalizada(incidencias);

        for (List<Incidencia> grupo : porUbicacion.values()) {
            for (Incidencia incidencia : grupo) {
                Long nuevoDuplicadoId = null;
                if (grupo.size() > 1) {
                    nuevoDuplicadoId = grupo.stream()
                            .filter(otra -> !otra.getId().equals(incidencia.getId()))
                            .map(Incidencia::getId)
                            .findFirst()
                            .orElse(null);
                }

                if (!Objects.equals(incidencia.getIncidenciaDuplicadaId(), nuevoDuplicadoId)) {
                    incidencia.setIncidenciaDuplicadaId(nuevoDuplicadoId);
                    incidenciaRepository.save(incidencia);
                }
            }
        }
    }

    private Map<Long, List<Incidencia>> construirMapaDuplicados(List<Incidencia> incidencias) {
        Map<String, List<Incidencia>> porUbicacion = agruparPorUbicacionNormalizada(incidencias);

        return incidencias.stream().collect(Collectors.toMap(
                Incidencia::getId,
                incidencia -> porUbicacion.getOrDefault(normalizarUbicacion(incidencia.getUbicacion()), Collections.emptyList())
                        .stream()
                        .filter(otra -> !otra.getId().equals(incidencia.getId()))
                        .sorted(Comparator.comparing(Incidencia::getFechaCreacion))
                        .collect(Collectors.toList())
        ));
    }

    private Map<String, List<Incidencia>> agruparPorUbicacionNormalizada(List<Incidencia> incidencias) {
        return incidencias.stream()
                .collect(Collectors.groupingBy(incidencia -> normalizarUbicacion(incidencia.getUbicacion())));
    }

    private String normalizarUbicacion(String ubicacion) {
        if (ubicacion == null) {
            return "";
        }
        return ubicacion.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
