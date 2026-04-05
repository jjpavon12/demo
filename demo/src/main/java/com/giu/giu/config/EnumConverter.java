package com.giu.giu.config;

import com.giu.giu.model.CategoriaIncidencia;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class EnumConverter implements Converter<String, CategoriaIncidencia> {

    @Override
    public CategoriaIncidencia convert(String source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        try {
            return CategoriaIncidencia.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Error converting categoria: " + source);
            throw new IllegalArgumentException("Categoría no válida: " + source);
        }
    }
}
