package com.giu.giu.model;

public enum EstadoIncidencia {
    PENDIENTE_VALIDACION("PendienteValidacion"),
    VALIDADA("Validada"),
    ASIGNADA("Asignada"),
    EN_CURSO("EnCurso"),
    RESUELTA("Resuelta"),
    CERRADA("Cerrada"),
    RECHAZADA("Rechazada");

    private final String descripcion;

    EstadoIncidencia(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
