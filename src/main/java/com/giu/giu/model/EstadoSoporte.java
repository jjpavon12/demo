package com.giu.giu.model;

public enum EstadoSoporte {
    PENDIENTE("Pendiente"),
    RESPONDIDO("Respondido"),
    CERRADO("Cerrado");

    private final String label;

    EstadoSoporte(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
