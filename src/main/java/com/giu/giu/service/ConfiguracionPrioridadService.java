package com.giu.giu.service;

import com.giu.giu.model.ConfiguracionPrioridad;
import com.giu.giu.model.PrioridadIncidencia;
import com.giu.giu.repository.ConfiguracionPrioridadRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConfiguracionPrioridadService {

    private final ConfiguracionPrioridadRepository repository;

    public ConfiguracionPrioridadService(ConfiguracionPrioridadRepository repository) {
        this.repository = repository;
    }

    public void inicializarSiNoExiste() {
        crearSiNoExiste(PrioridadIncidencia.CRITICA, 4);
        crearSiNoExiste(PrioridadIncidencia.ALTA, 15);
        crearSiNoExiste(PrioridadIncidencia.MEDIA, 30);
        crearSiNoExiste(PrioridadIncidencia.BAJA, 60);
    }

    private void crearSiNoExiste(PrioridadIncidencia prioridad, int dias) {
        repository.findById(prioridad)
                .orElseGet(() -> repository.save(new ConfiguracionPrioridad(prioridad, dias)));
    }

    public List<ConfiguracionPrioridad> obtenerTodas() {
        inicializarSiNoExiste();
        return repository.findAll();
    }

    public int obtenerDiasResolucion(PrioridadIncidencia prioridad) {
        inicializarSiNoExiste();

        return repository.findById(prioridad)
                .map(ConfiguracionPrioridad::getDiasResolucion)
                .orElse(60);
    }

    public void actualizar(PrioridadIncidencia prioridad, Integer diasResolucion) {
        if (prioridad == null || diasResolucion == null || diasResolucion < 1) {
            return;
        }

        ConfiguracionPrioridad config = repository.findById(prioridad)
                .orElse(new ConfiguracionPrioridad(prioridad, diasResolucion));

        config.setDiasResolucion(diasResolucion);
        repository.save(config);
    }
}