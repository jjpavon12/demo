package com.giu.giu.service;

import com.giu.giu.model.CategoriaIncidencia;
import com.giu.giu.model.EquipoTecnicoConfig;
import com.giu.giu.model.EstadoIncidencia;
import com.giu.giu.model.Incidencia;
import com.giu.giu.model.PrioridadIncidencia;
import com.giu.giu.model.Rol;
import com.giu.giu.model.Usuario;
import com.giu.giu.repository.EquipoTecnicoConfigRepository;
import com.giu.giu.repository.IncidenciaRepository;
import com.giu.giu.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class IncidenciaService {

    @Autowired
    private IncidenciaRepository incidenciaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EquipoTecnicoConfigRepository equipoTecnicoConfigRepository;

    public Incidencia registrar(String descripcion, String ubicacion, Double latitud, Double longitud, Set<CategoriaIncidencia> categorias, String imagenNombre, Usuario usuario) {
        Incidencia incidencia = new Incidencia();
        incidencia.setDescripcion(descripcion);
        incidencia.setUbicacion(ubicacion);
        incidencia.setLatitud(latitud);
        incidencia.setLongitud(longitud);
        incidencia.setCategorias(categorias);
        incidencia.setEstado(EstadoIncidencia.PENDIENTE_VALIDACION);
        incidencia.setUsuario(usuario);
        incidencia.setImagenNombre(imagenNombre);
        return incidenciaRepository.save(incidencia);
    }

    public List<Incidencia> obtenerPorUsuario(Usuario usuario) {
        return incidenciaRepository.findByUsuarioOrderByFechaCreacionDesc(usuario);
    }

    public List<Incidencia> obtenerTodas() {
        return incidenciaRepository.findAll();
    }

    public List<Incidencia> obtenerAsignadasATecnico(Usuario tecnico) {
        return incidenciaRepository.findByTecnicoAsignadoOrderByFechaAsignacionDesc(tecnico);
    }

    public Optional<Incidencia> obtenerPorId(Long id) {
        return incidenciaRepository.findById(id);
    }

    public boolean estaBloqueadaParaOperador(Long id) {
    return incidenciaRepository.findById(id)
        .map(inc ->
            inc.getEstado() == EstadoIncidencia.VALIDADA ||
            inc.getEstado() == EstadoIncidencia.RECHAZADA ||
            inc.getEstado() == EstadoIncidencia.ASIGNADA
        )
        .orElse(true);
}

    public void cambiarEstado(Long id, EstadoIncidencia nuevoEstado) {
        incidenciaRepository.findById(id).ifPresent(inc -> {
            inc.setEstado(nuevoEstado);
            incidenciaRepository.save(inc);
        });
    }

    public void cambiarCategorias(Long id, Set<CategoriaIncidencia> nuevasCategorias) {
        incidenciaRepository.findById(id).ifPresent(inc -> {
            inc.setCategorias(nuevasCategorias);
            incidenciaRepository.save(inc);
        });
    }

    public void cambiarPrioridad(Long id, PrioridadIncidencia nuevaPrioridad) {
        incidenciaRepository.findById(id).ifPresent(inc -> {
            inc.setPrioridad(nuevaPrioridad);
            inc.setPrioridadAsignada(true);
            incidenciaRepository.save(inc);
        });
    }

    public String asignarATecnico(Long incidenciaId, Long tecnicoId) {
        Optional<Incidencia> incidenciaOpt = incidenciaRepository.findById(incidenciaId);
        if (incidenciaOpt.isEmpty()) {
            return "Incidencia no encontrada";
        }

        Optional<Usuario> tecnicoOpt = usuarioRepository.findById(tecnicoId);
        if (tecnicoOpt.isEmpty()) {
            return "Técnico no encontrado";
        }

        Incidencia incidencia = incidenciaOpt.get();
        Usuario tecnico = tecnicoOpt.get();

        if (tecnico.getRol() != Rol.TECNICO) {
            return "El usuario seleccionado no es técnico";
        }

        if (incidencia.getEstado() != EstadoIncidencia.VALIDADA && incidencia.getEstado() != EstadoIncidencia.ASIGNADA) {
            return "Solo se pueden asignar incidencias validadas";
        }

        boolean tecnicoTieneConfig = equipoTecnicoConfigRepository.findByUsuarioId(tecnicoId).isPresent();
        if (!tecnicoTieneConfig) {
            return "El técnico seleccionado no tiene equipo configurado";
        }

        incidencia.setTecnicoAsignado(tecnico);
        incidencia.setFechaAsignacion(LocalDateTime.now());
        incidencia.setEstado(EstadoIncidencia.ASIGNADA);

        incidenciaRepository.save(incidencia);
        return null;
    }

    public List<EquipoTecnicoConfig> obtenerEquiposTecnicosOrdenadosParaIncidencia(Incidencia incidencia) {
        List<EquipoTecnicoConfig> equipos = equipoTecnicoConfigRepository.findAllByUsuarioRolOrderByNombreEquipoAsc(Rol.TECNICO);

        equipos.sort(
            Comparator
                .comparingInt((EquipoTecnicoConfig config) -> coincidenciasCategorias(incidencia.getCategorias(), config.getEspecialidades()))
                .reversed()
                .thenComparing(EquipoTecnicoConfig::getNombreEquipo, String.CASE_INSENSITIVE_ORDER)
        );

        return equipos;
    }

    private int coincidenciasCategorias(Set<CategoriaIncidencia> categoriasIncidencia, Set<CategoriaIncidencia> categoriasEquipo) {
        if (categoriasIncidencia == null || categoriasEquipo == null) {
            return 0;
        }

        int coincidencias = 0;
        for (CategoriaIncidencia categoria : categoriasIncidencia) {
            if (categoriasEquipo.contains(categoria)) {
                coincidencias++;
            }
        }
        return coincidencias;
    }
}