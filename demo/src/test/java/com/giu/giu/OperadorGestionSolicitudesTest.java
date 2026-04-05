package com.giu.giu;

import com.giu.giu.model.CategoriaIncidencia;
import com.giu.giu.model.EstadoIncidencia;
import com.giu.giu.model.Incidencia;
import com.giu.giu.model.Rol;
import com.giu.giu.model.Usuario;
import com.giu.giu.repository.IncidenciaRepository;
import com.giu.giu.repository.UsuarioRepository;
import com.giu.giu.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class OperadorGestionSolicitudesTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private IncidenciaRepository incidenciaRepository;

    private Usuario operador;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();

        incidenciaRepository.deleteAll();
        usuarioRepository.deleteAll();

        operador = new Usuario();
        operador.setEmail("operador@test.com");
        operador.setPassword("secret");
        operador.setRol(Rol.OPERADOR);
        operador = usuarioRepository.save(operador);

        Incidencia incidencia = new Incidencia();
        incidencia.setDescripcion("Farola apagada");
        incidencia.setUbicacion("Calle Mayor 1");
        incidencia.setCategoria(CategoriaIncidencia.ALUMBRADO_PUBLICO);
        incidencia.setEstado(EstadoIncidencia.PENDIENTE_REVISION);
        incidencia.setUsuario(operador);
        incidenciaRepository.save(incidencia);
    }

    @Test
    void gestionSolicitudesDebeRenderizar() throws Exception {
        mockMvc.perform(get("/operador/gestion-solicitudes")
                .with(user(new CustomUserDetails(operador))))
            .andExpect(status().isOk());
    }

    @Test
    void editarCategoriaDebeActualizarCategoriaVisible() throws Exception {
        Incidencia incidencia = incidenciaRepository.findAll().get(0);

        mockMvc.perform(post("/operador/editar-categoria")
                .param("id", incidencia.getId().toString())
                .param("categoria", CategoriaIncidencia.LIMPIEZA_VIARIA.name())
                .with(user(new CustomUserDetails(operador))))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/operador/gestion-solicitudes"));

        Incidencia actualizada = incidenciaRepository.findById(incidencia.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(CategoriaIncidencia.LIMPIEZA_VIARIA, actualizada.getCategoria());
        org.junit.jupiter.api.Assertions.assertEquals(CategoriaIncidencia.LIMPIEZA_VIARIA, actualizada.getCategoriaEditada());
    }

    @Test
    void gestionSolicitudesDebeMostrarAvisoDeDuplicadaPorUbicacion() throws Exception {
        Usuario ciudadano = new Usuario();
        ciudadano.setEmail("ciudadano@test.com");
        ciudadano.setPassword("secret");
        ciudadano.setRol(Rol.CIUDADANO);
        ciudadano = usuarioRepository.save(ciudadano);

        Incidencia duplicada = new Incidencia();
        duplicada.setDescripcion("Otra farola apagada");
        duplicada.setUbicacion("  calle mayor 1 ");
        duplicada.setCategoria(CategoriaIncidencia.ALUMBRADO_PUBLICO);
        duplicada.setEstado(EstadoIncidencia.PENDIENTE_REVISION);
        duplicada.setUsuario(ciudadano);
        incidenciaRepository.save(duplicada);

        mockMvc.perform(get("/operador/gestion-solicitudes")
                .with(user(new CustomUserDetails(operador))))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Posible duplicada")))
            .andExpect(content().string(containsString("Otra farola apagada")))
            .andExpect(content().string(containsString("ciudadano@test.com")));
    }
}
