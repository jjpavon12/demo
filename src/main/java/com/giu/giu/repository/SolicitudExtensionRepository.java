package com.giu.giu.repository;

import com.giu.giu.model.EstadoSolicitud;
import com.giu.giu.model.Incidencia;
import com.giu.giu.model.SolicitudExtension;
import com.giu.giu.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SolicitudExtensionRepository extends JpaRepository<SolicitudExtension, Long> {
    Optional<SolicitudExtension> findFirstByIncidenciaAndEstadoOrderByFechaSolicitudDesc(Incidencia incidencia, EstadoSolicitud estado);
    List<SolicitudExtension> findByEstadoOrderByFechaSolicitudDesc(EstadoSolicitud estado);
    List<SolicitudExtension> findBySolicitanteAndEstadoAndFechaDecisionAfterOrderByFechaDecisionDesc(Usuario solicitante, EstadoSolicitud estado, java.time.LocalDateTime fechaDecision);
}
