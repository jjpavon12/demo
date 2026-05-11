package com.giu.giu.repository;

import com.giu.giu.model.EstadoIncidencia;
import com.giu.giu.model.Incidencia;
import com.giu.giu.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidenciaRepository extends JpaRepository<Incidencia, Long> {

    List<Incidencia> findByUsuarioOrderByFechaCreacionDesc(Usuario usuario);

    List<Incidencia> findByTecnicoAsignadoOrderByFechaAsignacionDesc(Usuario tecnicoAsignado);

    List<Incidencia> findByEstadoOrderByFechaCreacionDesc(EstadoIncidencia estado);

    List<Incidencia> findByUsuarioAndFechaModificacionAfterOrderByFechaModificacionDesc(Usuario usuario, java.time.LocalDateTime fecha);

    List<Incidencia> findByFechaCreacionAfterOrderByFechaCreacionDesc(java.time.LocalDateTime fecha);

    List<Incidencia> findByFechaModificacionAfterOrderByFechaModificacionDesc(java.time.LocalDateTime fecha);

    List<Incidencia> findByTecnicoAsignadoAndFechaAsignacionAfterOrderByFechaAsignacionDesc(Usuario tecnicoAsignado, java.time.LocalDateTime fecha);
}
