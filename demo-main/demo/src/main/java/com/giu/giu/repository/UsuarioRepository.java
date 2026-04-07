package com.giu.giu.repository;

import com.giu.giu.model.Usuario;
import com.giu.giu.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    List<Usuario> findByActivoFalse();
    List<Usuario> findByRolIn(List<Rol> roles);
    boolean existsByRol(Rol rol);
}