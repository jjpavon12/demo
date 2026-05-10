package com.giu.giu.model;

import jakarta.persistence.*;

@Entity
@Table(name = "configuracion_categorias")
public class ConfiguracionCategoria {

    @Id
    @Enumerated(EnumType.STRING)
    private CategoriaIncidencia categoria;

    @Column(nullable = false)
    private boolean activa = true;

    public ConfiguracionCategoria() {}

    public ConfiguracionCategoria(CategoriaIncidencia categoria, boolean activa) {
        this.categoria = categoria;
        this.activa = activa;
    }

    public CategoriaIncidencia getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaIncidencia categoria) {
        this.categoria = categoria;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }
}