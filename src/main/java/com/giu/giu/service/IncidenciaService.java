package com.giu.giu.service;

import com.giu.giu.model.CategoriaIncidencia;
import com.giu.giu.model.ComentarioIncidencia;
import com.giu.giu.model.EquipoTecnicoConfig;
import com.giu.giu.model.EstadoIncidencia;
import com.giu.giu.model.EstadoSolicitud;
import com.giu.giu.model.Incidencia;
import com.giu.giu.model.PrioridadIncidencia;
import com.giu.giu.model.Rol;
import com.giu.giu.model.SolicitudExtension;
import com.giu.giu.model.TipoComentario;
import com.giu.giu.model.Usuario;
import com.giu.giu.repository.ComentarioIncidenciaRepository;
import com.giu.giu.repository.EquipoTecnicoConfigRepository;
import com.giu.giu.repository.IncidenciaRepository;
import com.giu.giu.repository.SolicitudExtensionRepository;
import com.giu.giu.repository.UsuarioRepository;

import com.giu.giu.service.ConfiguracionPrioridadService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class IncidenciaService {

    @Autowired
    private IncidenciaRepository incidenciaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EquipoTecnicoConfigRepository equipoTecnicoConfigRepository;

    @Autowired
    private ComentarioIncidenciaRepository comentarioIncidenciaRepository;

    @Autowired
    private SolicitudExtensionRepository solicitudExtensionRepository;

    @Autowired
    private ConfiguracionPrioridadService configuracionPrioridadService;

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

    public void rechazar(Long id, String motivo) {
        incidenciaRepository.findById(id).ifPresent(inc -> {
            inc.setEstado(EstadoIncidencia.RECHAZADA);
            inc.setMotivoRechazo(motivo != null ? motivo.trim() : null);
            incidenciaRepository.save(inc);
        });
    }

    public void cerrar(Long id) {
        incidenciaRepository.findById(id).ifPresent(inc -> {
            if (inc.getEstado() == EstadoIncidencia.RESUELTA) {
                inc.setEstado(EstadoIncidencia.CERRADA);
                incidenciaRepository.save(inc);
            }
        });
    }

    public void devolverATecnico(Long id) {
        incidenciaRepository.findById(id).ifPresent(inc -> {
            if (inc.getEstado() == EstadoIncidencia.RESUELTA) {
                inc.setEstado(EstadoIncidencia.EN_CURSO);
                incidenciaRepository.save(inc);
            }
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
        LocalDateTime ahora = LocalDateTime.now();
        incidencia.setFechaAsignacion(ahora);
        incidencia.setEstado(EstadoIncidencia.ASIGNADA);
        incidencia.setFechaLimiteResolucion(calcularFechaLimite(incidencia.getPrioridad(), ahora.toLocalDate()));

        incidenciaRepository.save(incidencia);
        return null;
    }

    public List<EquipoTecnicoConfig> obtenerEquiposTecnicosOrdenadosParaIncidencia(Incidencia incidencia) {
        List<EquipoTecnicoConfig> equipos = equipoTecnicoConfigRepository.findAllByUsuarioRolOrderByNombreEquipoAsc(Rol.TECNICO);

        return equipos.stream()
            .filter(config -> coincidenciasCategorias(incidencia.getCategorias(), config.getEspecialidades()) > 0)
            .sorted(
                Comparator
                    .comparingInt((EquipoTecnicoConfig config) -> coincidenciasCategorias(incidencia.getCategorias(), config.getEspecialidades()))
                    .reversed()
                    .thenComparing(EquipoTecnicoConfig::getNombreEquipo, String.CASE_INSENSITIVE_ORDER)
            )
            .collect(Collectors.toList());
    }

    public List<ComentarioIncidencia> obtenerComentariosPorIncidencia(Incidencia incidencia) {
        return comentarioIncidenciaRepository.findByIncidenciaOrderByFechaCreacionAsc(incidencia);
    }

    public void agregarComentario(Long incidenciaId, Long autorId, String contenido) {
        Optional<Incidencia> incOpt = incidenciaRepository.findById(incidenciaId);
        Optional<Usuario> autorOpt = usuarioRepository.findById(autorId);
        if (incOpt.isEmpty() || autorOpt.isEmpty() || contenido == null || contenido.isBlank()) return;

        ComentarioIncidencia comentario = new ComentarioIncidencia();
        comentario.setIncidencia(incOpt.get());
        comentario.setAutor(autorOpt.get());
        comentario.setContenido(contenido.trim());
        comentario.setTipo(TipoComentario.COMENTARIO);
        comentarioIncidenciaRepository.save(comentario);
    }

    public String solicitarExtension(Long incidenciaId, Long tecnicoId, String motivo, String comentarioAdicional, LocalDate fechaSolicitada) {
        Optional<Incidencia> incOpt = incidenciaRepository.findById(incidenciaId);
        if (incOpt.isEmpty()) return "Incidencia no encontrada";

        Incidencia incidencia = incOpt.get();

        if (incidencia.getEstado() == EstadoIncidencia.CERRADA || incidencia.getEstado() == EstadoIncidencia.RESUELTA) {
            return "No se puede solicitar extensión para incidencias cerradas o resueltas";
        }

        if (incidencia.isTieneSolicitudExtensionPendiente()) {
            return "Ya existe una solicitud de extensión pendiente para esta incidencia";
        }

        Optional<Usuario> tecnicoOpt = usuarioRepository.findById(tecnicoId);
        if (tecnicoOpt.isEmpty()) return "Técnico no encontrado";

        if (motivo == null || motivo.isBlank()) return "El motivo es obligatorio";
        if (fechaSolicitada == null || fechaSolicitada.isBefore(LocalDate.now())) {
            return "La fecha solicitada no puede ser anterior a la fecha actual";
        }

        SolicitudExtension solicitud = new SolicitudExtension();
        solicitud.setIncidencia(incidencia);
        solicitud.setSolicitante(tecnicoOpt.get());
        solicitud.setMotivo(motivo.trim());
        solicitud.setComentarioAdicional(comentarioAdicional != null && !comentarioAdicional.isBlank() ? comentarioAdicional.trim() : null);
        solicitud.setFechaSolicitada(fechaSolicitada);
        solicitudExtensionRepository.save(solicitud);

        StringBuilder textoComentario = new StringBuilder();
        textoComentario.append("Solicitud de extension de tiempo\n");
        textoComentario.append("Fecha solicitada: ").append(fechaSolicitada).append("\n");
        textoComentario.append("Motivo: ").append(motivo.trim());
        if (comentarioAdicional != null && !comentarioAdicional.isBlank()) {
            textoComentario.append("\nComentario adicional: ").append(comentarioAdicional.trim());
        }

        ComentarioIncidencia comentario = new ComentarioIncidencia();
        comentario.setIncidencia(incidencia);
        comentario.setAutor(tecnicoOpt.get());
        comentario.setContenido(textoComentario.toString());
        comentario.setTipo(TipoComentario.SOLICITUD_EXTENSION);
        comentarioIncidenciaRepository.save(comentario);

        incidencia.setTieneSolicitudExtensionPendiente(true);
        incidenciaRepository.save(incidencia);

        return null;
    }

    public String decidirExtension(Long solicitudId, Long operadorId, boolean aprobada, String motivoDecision) {
        Optional<SolicitudExtension> solOpt = solicitudExtensionRepository.findById(solicitudId);
        if (solOpt.isEmpty()) return "Solicitud no encontrada";

        Optional<Usuario> opOpt = usuarioRepository.findById(operadorId);
        if (opOpt.isEmpty()) return "Operador no encontrado";

        SolicitudExtension solicitud = solOpt.get();
        solicitud.setEstado(aprobada ? EstadoSolicitud.APROBADA : EstadoSolicitud.RECHAZADA);
        solicitud.setMotivoDecision(motivoDecision != null && !motivoDecision.isBlank() ? motivoDecision.trim() : null);
        solicitudExtensionRepository.save(solicitud);

        String decision = aprobada ? "APROBADA" : "RECHAZADA";
        StringBuilder textoDecision = new StringBuilder();
        textoDecision.append("Decision sobre solicitud de extension: ").append(decision).append("\n");
        textoDecision.append("Operador: ").append(opOpt.get().getEmail());
        if (motivoDecision != null && !motivoDecision.isBlank()) {
            textoDecision.append("\nMotivo: ").append(motivoDecision.trim());
        }

        ComentarioIncidencia comentario = new ComentarioIncidencia();
        comentario.setIncidencia(solicitud.getIncidencia());
        comentario.setAutor(opOpt.get());
        comentario.setContenido(textoDecision.toString());
        comentario.setTipo(TipoComentario.DECISION_EXTENSION);
        comentarioIncidenciaRepository.save(comentario);

        solicitud.getIncidencia().setTieneSolicitudExtensionPendiente(false);
        incidenciaRepository.save(solicitud.getIncidencia());

        return null;
    }

    public List<SolicitudExtension> obtenerSolicitudesPendientes() {
        return solicitudExtensionRepository.findByEstadoOrderByFechaSolicitudDesc(EstadoSolicitud.PENDIENTE);
    }

    private LocalDate calcularFechaLimite(PrioridadIncidencia prioridad, LocalDate desde) {
        if (prioridad == null || desde == null) return null;

        int dias = configuracionPrioridadService.obtenerDiasResolucion(prioridad);
        return desde.plusDays(dias);
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