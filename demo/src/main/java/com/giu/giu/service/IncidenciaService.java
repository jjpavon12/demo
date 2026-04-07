package com.giu.giu.service;

import com.giu.giu.model.CategoriaIncidencia;
import com.giu.giu.model.EstadoIncidencia;
import com.giu.giu.model.Incidencia;
import com.giu.giu.model.Usuario;
import com.giu.giu.repository.IncidenciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class IncidenciaService {

    @Autowired
    private IncidenciaRepository incidenciaRepository;

    public Incidencia registrar(String descripcion, String ubicacion, java.util.Set<CategoriaIncidencia> categorias, Usuario usuario) {
        Incidencia incidencia = new Incidencia();
        incidencia.setDescripcion(descripcion);
        incidencia.setUbicacion(ubicacion);
        incidencia.setCategorias(categorias);
        incidencia.setEstado(EstadoIncidencia.PENDIENTE_VALIDACION);
        incidencia.setUsuario(usuario);
        return incidenciaRepository.save(incidencia);
    }

    public List<Incidencia> obtenerPorUsuario(Usuario usuario) {
        return incidenciaRepository.findByUsuarioOrderByFechaCreacionDesc(usuario);
    }

    public List<Incidencia> obtenerTodas() {
        return incidenciaRepository.findAll();
    }

    public Optional<Incidencia> obtenerPorId(Long id) {
        return incidenciaRepository.findById(id);
    }

    public boolean estaBloqueadaParaOperador(Long id) {
        return incidenciaRepository.findById(id)
            .map(inc -> inc.getEstado() == EstadoIncidencia.VALIDADA || inc.getEstado() == EstadoIncidencia.RECHAZADA)
            .orElse(true);
    }

    public void cambiarEstado(Long id, EstadoIncidencia nuevoEstado) {
        incidenciaRepository.findById(id).ifPresent(inc -> {
            inc.setEstado(nuevoEstado);
            incidenciaRepository.save(inc);
        });
    }

    public void cambiarCategoria(Long id, CategoriaIncidencia nuevaCategoria) {
        incidenciaRepository.findById(id).ifPresent(inc -> {
            inc.setCategorias(java.util.Set.of(nuevaCategoria));
            incidenciaRepository.save(inc);
        });
    }
}
