package com.giu.giu.model;

public enum TipoComentario {
    COMENTARIO("Comentario"),
    SOLICITUD_EXTENSION("Solicitud de extensión"),
    DECISION_EXTENSION("Decisión de extensión");

    private final String descripcion;

    TipoComentario(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
