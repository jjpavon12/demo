package com.giu.giu.repository;

import com.giu.giu.model.Incidencia;
import com.giu.giu.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidenciaRepository extends JpaRepository<Incidencia, Long> {

    List<Incidencia> findByUsuarioOrderByFechaCreacionDesc(Usuario usuario);
}
