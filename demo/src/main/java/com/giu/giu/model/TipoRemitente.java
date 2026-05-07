package com.giu.giu.model;

public enum TipoRemitente {
    USUARIO("Usuario"),
    ADMINISTRADOR("Administrador");

    private final String label;

    TipoRemitente(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
