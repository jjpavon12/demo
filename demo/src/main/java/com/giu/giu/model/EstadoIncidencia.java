package com.giu.giu.model;

public enum EstadoIncidencia {
    PENDIENTE_REVISION("Pendiente de revisión"),
    EN_PROCESO("En proceso"),
    SOLUCIONADA("Solucionada");

    private final String descripcion;

    EstadoIncidencia(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
