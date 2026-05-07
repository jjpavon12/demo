package com.giu.giu.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "equipo_tecnico_config")
public class EquipoTecnicoConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "nombre_equipo", nullable = false)
    private String nombreEquipo;

    @ElementCollection(targetClass = CategoriaIncidencia.class, fetch = FetchType.EAGER)
    @CollectionTable(
        name = "equipo_tecnico_especialidades",
        joinColumns = @JoinColumn(name = "config_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false)
    private Set<CategoriaIncidencia> especialidades = new HashSet<>();

    public EquipoTecnicoConfig() {
    }

    public EquipoTecnicoConfig(Usuario usuario, String nombreEquipo, Set<CategoriaIncidencia> especialidades) {
        this.usuario = usuario;
        this.nombreEquipo = nombreEquipo;
        this.especialidades = especialidades;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getNombreEquipo() {
        return nombreEquipo;
    }

    public void setNombreEquipo(String nombreEquipo) {
        this.nombreEquipo = nombreEquipo;
    }

    public Set<CategoriaIncidencia> getEspecialidades() {
        return especialidades;
    }

    public void setEspecialidades(Set<CategoriaIncidencia> especialidades) {
        this.especialidades = especialidades;
    }

    public boolean isConfigurado() {
        return nombreEquipo != null && !nombreEquipo.isBlank()
                && especialidades != null && !especialidades.isEmpty();
    }
}