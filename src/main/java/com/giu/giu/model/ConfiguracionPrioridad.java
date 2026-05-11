package com.giu.giu.model;

import jakarta.persistence.*;

@Entity
@Table(name = "configuracion_prioridades")
public class ConfiguracionPrioridad {

    @Id
    @Enumerated(EnumType.STRING)
    private PrioridadIncidencia prioridad;

    @Column(nullable = false)
    private Integer diasResolucion;

    public ConfiguracionPrioridad() {}

    public ConfiguracionPrioridad(PrioridadIncidencia prioridad, Integer diasResolucion) {
        this.prioridad = prioridad;
        this.diasResolucion = diasResolucion;
    }

    public PrioridadIncidencia getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(PrioridadIncidencia prioridad) {
        this.prioridad = prioridad;
    }

    public Integer getDiasResolucion() {
        return diasResolucion;
    }

    public void setDiasResolucion(Integer diasResolucion) {
        this.diasResolucion = diasResolucion;
    }
}