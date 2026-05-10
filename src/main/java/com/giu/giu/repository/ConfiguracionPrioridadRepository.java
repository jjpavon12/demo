package com.giu.giu.repository;

import com.giu.giu.model.ConfiguracionPrioridad;
import com.giu.giu.model.PrioridadIncidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfiguracionPrioridadRepository extends JpaRepository<ConfiguracionPrioridad, PrioridadIncidencia> {
}