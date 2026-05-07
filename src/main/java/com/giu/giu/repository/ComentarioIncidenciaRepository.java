package com.giu.giu.repository;

import com.giu.giu.model.ComentarioIncidencia;
import com.giu.giu.model.Incidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ComentarioIncidenciaRepository extends JpaRepository<ComentarioIncidencia, Long> {
    List<ComentarioIncidencia> findByIncidenciaOrderByFechaCreacionAsc(Incidencia incidencia);
}
