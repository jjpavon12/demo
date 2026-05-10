package com.giu.giu.repository;

import com.giu.giu.model.CategoriaIncidencia;
import com.giu.giu.model.ConfiguracionCategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfiguracionCategoriaRepository extends JpaRepository<ConfiguracionCategoria, CategoriaIncidencia> {

    List<ConfiguracionCategoria> findByActivaIsTrue();
}