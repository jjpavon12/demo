package com.giu.giu.model;

public enum CategoriaIncidencia {
    BACHES_PAVIMENTO("Baches y Pavimento"),
    ACERAS_ACCESIBILIDAD("Aceras y Accesibilidad"),
    SENALIZACION_SEMAFOROS("Señalización y Semáforos"),
    ALUMBRADO_PUBLICO("Alumbrado Público"),
    LIMPIEZA_VIARIA("Limpieza Viaria"),
    RESIDUOS_CONTENEDORES("Gestión de Residuos y Contenedores"),
    MOBILIARIO_URBANO("Mobiliario Urbano"),
    PARQUES_JARDINES("Parques y Jardines"),
    AREAS_INFANTILES("Áreas Infantiles"),
    ARBOLADO("Arbolado"),
    ALCANTARILLADO_SANEAMIENTO("Alcantarillado y Saneamiento"),
    CONTROL_PLAGAS("Control de Plagas"),
    VEHICULOS_ABANDONADOS("Vehículos Abandonados"),
    CONTAMINACION_ACUSTICA("Contaminación Acústica"),
    PINTADAS_GRAFITIS("Pintadas y Grafitis"),
    OTROS("Otros / Varios");

    private final String descripcion;

    CategoriaIncidencia(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
