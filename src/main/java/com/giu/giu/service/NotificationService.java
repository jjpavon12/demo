package com.giu.giu.service;

import com.giu.giu.dto.NotificationSummary;
import com.giu.giu.model.EstadoIncidencia;
import com.giu.giu.model.EstadoSolicitud;
import com.giu.giu.model.Incidencia;
import com.giu.giu.model.Rol;
import com.giu.giu.model.SolicitudExtension;
import com.giu.giu.model.Usuario;
import com.giu.giu.repository.IncidenciaRepository;
import com.giu.giu.repository.SolicitudExtensionRepository;
import com.giu.giu.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class NotificationService {
    private static final LocalDateTime DEFAULT_SINCE = LocalDateTime.of(1970, 1, 1, 0, 0);
    private static final Set<EstadoIncidencia> OPERATOR_TRACKED_STATES = Set.of(
        EstadoIncidencia.ASIGNADA,
        EstadoIncidencia.EN_CURSO,
        EstadoIncidencia.RESUELTA,
        EstadoIncidencia.CERRADA
    );

    private final IncidenciaRepository incidenciaRepository;
    private final SolicitudExtensionRepository solicitudExtensionRepository;
    private final UsuarioRepository usuarioRepository;

    public NotificationService(IncidenciaRepository incidenciaRepository,
                               SolicitudExtensionRepository solicitudExtensionRepository,
                               UsuarioRepository usuarioRepository) {
        this.incidenciaRepository = incidenciaRepository;
        this.solicitudExtensionRepository = solicitudExtensionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public NotificationSummary buildFor(Usuario usuario) {
        List<Long> citizenChanged = List.of();
        List<Long> operatorNew = List.of();
        List<Long> operatorChanged = List.of();
        List<Long> technicianNew = List.of();
        List<Long> technicianApproved = List.of();
        List<Long> technicianDueSoon = List.of();
        List<Long> technicianOverdue = List.of();

        if (usuario.getRol() == Rol.CIUDADANO) {
            citizenChanged = incidenciaRepository
                .findByUsuarioAndFechaModificacionAfterOrderByFechaModificacionDesc(usuario, since(usuario.getNotifCiudadanoEstadosVistaHasta()))
                .stream()
                .map(Incidencia::getId)
                .toList();
        } else if (usuario.getRol() == Rol.OPERADOR) {
            operatorNew = incidenciaRepository.findByFechaCreacionAfterOrderByFechaCreacionDesc(since(usuario.getNotifOperadorNuevasVistaHasta()))
                .stream()
                .filter(i -> i.getEstado() == EstadoIncidencia.PENDIENTE_VALIDACION)
                .map(Incidencia::getId)
                .toList();

            operatorChanged = incidenciaRepository.findByFechaModificacionAfterOrderByFechaModificacionDesc(since(usuario.getNotifOperadorCambiosVistaHasta()))
                .stream()
                .filter(i -> OPERATOR_TRACKED_STATES.contains(i.getEstado()))
                .map(Incidencia::getId)
                .toList();
        } else if (usuario.getRol() == Rol.TECNICO) {
            technicianNew = incidenciaRepository
                .findByTecnicoAsignadoAndFechaAsignacionAfterOrderByFechaAsignacionDesc(usuario, since(usuario.getNotifTecnicoAsignadasVistaHasta()))
                .stream()
                .filter(i -> i.getEstado() == EstadoIncidencia.ASIGNADA)
                .map(Incidencia::getId)
                .toList();

            technicianApproved = solicitudExtensionRepository
                .findBySolicitanteAndEstadoAndFechaDecisionAfterOrderByFechaDecisionDesc(usuario, EstadoSolicitud.APROBADA, since(usuario.getNotifTecnicoExtensionesVistaHasta()))
                .stream()
                .map(SolicitudExtension::getIncidencia)
                .map(Incidencia::getId)
                .distinct()
                .toList();

            LocalDate hoy = LocalDate.now();
            LocalDate limite = hoy.plusDays(7);
            LocalDateTime ahora = LocalDateTime.now();
            LocalDateTime limiteSince = since(usuario.getNotifTecnicoLimiteVistaHasta());
            technicianDueSoon = incidenciaRepository.findByTecnicoAsignadoOrderByFechaAsignacionDesc(usuario)
                .stream()
                .filter(i -> i.getEstado() == EstadoIncidencia.ASIGNADA || i.getEstado() == EstadoIncidencia.EN_CURSO)
                .filter(i -> i.getFechaLimiteResolucion() != null)
                .filter(i -> !i.getFechaLimiteResolucion().isBefore(hoy) && !i.getFechaLimiteResolucion().isAfter(limite))
                .filter(i -> {
                    LocalDateTime entraEnSemanaFinal = i.getFechaLimiteResolucion().minusDays(7).atStartOfDay();
                    return entraEnSemanaFinal.isAfter(limiteSince) && !entraEnSemanaFinal.isAfter(ahora);
                })
                .map(Incidencia::getId)
                .toList();

            technicianOverdue = incidenciaRepository.findByTecnicoAsignadoOrderByFechaAsignacionDesc(usuario)
                .stream()
                .filter(i -> i.getEstado() == EstadoIncidencia.ASIGNADA || i.getEstado() == EstadoIncidencia.EN_CURSO)
                .filter(i -> i.getFechaLimiteResolucion() != null)
                .filter(i -> i.getFechaLimiteResolucion().isBefore(hoy))
                .map(Incidencia::getId)
                .toList();
        }

        return new NotificationSummary(
            citizenChanged.size(),
            operatorNew.size(),
            operatorChanged.size(),
            technicianNew.size(),
            technicianApproved.size(),
            technicianDueSoon.size(),
            technicianOverdue.size(),
            citizenChanged,
            operatorNew,
            operatorChanged,
            technicianNew,
            technicianApproved,
            technicianDueSoon,
            technicianOverdue
        );
    }

    public void markCitizenStateSeen(Usuario usuario) {
        usuario.setNotifCiudadanoEstadosVistaHasta(LocalDateTime.now());
        usuarioRepository.save(usuario);
    }

    public void markOperatorNewSeen(Usuario usuario) {
        usuario.setNotifOperadorNuevasVistaHasta(LocalDateTime.now());
        usuarioRepository.save(usuario);
    }

    public void markOperatorChangesSeen(Usuario usuario) {
        usuario.setNotifOperadorCambiosVistaHasta(LocalDateTime.now());
        usuarioRepository.save(usuario);
    }

    public void markTechnicianAssignmentsSeen(Usuario usuario) {
        usuario.setNotifTecnicoAsignadasVistaHasta(LocalDateTime.now());
        usuarioRepository.save(usuario);
    }

    public void markTechnicianExtensionsSeen(Usuario usuario) {
        usuario.setNotifTecnicoExtensionesVistaHasta(LocalDateTime.now());
        usuarioRepository.save(usuario);
    }

    public void markTechnicianDueSoonSeen(Usuario usuario) {
        usuario.setNotifTecnicoLimiteVistaHasta(LocalDateTime.now());
        usuarioRepository.save(usuario);
    }

    public String idsParam(List<Long> ids) {
        return ids.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    public String idsParam(List<Long> first, List<Long> second) {
        return Stream.concat(first.stream(), second.stream())
            .distinct()
            .map(String::valueOf)
            .collect(Collectors.joining(","));
    }

    private LocalDateTime since(LocalDateTime value) {
        return value != null ? value : DEFAULT_SINCE;
    }
}
