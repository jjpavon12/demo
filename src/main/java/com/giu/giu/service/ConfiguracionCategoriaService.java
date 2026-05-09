package com.giu.giu.service;

import com.giu.giu.model.CategoriaIncidencia;
import com.giu.giu.model.ConfiguracionCategoria;
import com.giu.giu.repository.ConfiguracionCategoriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConfiguracionCategoriaService {

    private final ConfiguracionCategoriaRepository repository;

    public ConfiguracionCategoriaService(ConfiguracionCategoriaRepository repository) {
        this.repository = repository;
    }

    public void inicializarSiNoExiste() {
        for (CategoriaIncidencia categoria : CategoriaIncidencia.values()) {
            repository.findById(categoria)
                    .orElseGet(() -> repository.save(new ConfiguracionCategoria(categoria, true)));
        }
    }

    public List<ConfiguracionCategoria> obtenerTodas() {
        inicializarSiNoExiste();
        return repository.findAll();
    }

    public List<ConfiguracionCategoria> obtenerActivas() {
        inicializarSiNoExiste();
        return repository.findByActivaIsTrue();
    }

    public void cambiarEstado(CategoriaIncidencia categoria, boolean activa) {
        if (categoria == null) return;

        ConfiguracionCategoria config = repository.findById(categoria)
                .orElse(new ConfiguracionCategoria(categoria, true));

        config.setActiva(activa);
        repository.save(config);
    }
}