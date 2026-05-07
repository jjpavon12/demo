package com.giu.giu.repository;

import com.giu.giu.model.EquipoTecnicoConfig;
import com.giu.giu.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EquipoTecnicoConfigRepository extends JpaRepository<EquipoTecnicoConfig, Long> {
    Optional<EquipoTecnicoConfig> findByUsuarioId(Long usuarioId);

    List<EquipoTecnicoConfig> findAllByUsuarioRolOrderByNombreEquipoAsc(Rol rol);
}