package com.giu.giu;

import com.giu.giu.model.Rol;
import com.giu.giu.model.Usuario;
import com.giu.giu.security.CustomUserDetails;
import com.giu.giu.service.ConfiguracionCategoriaService;
import com.giu.giu.service.ConfiguracionPrioridadService;
import com.giu.giu.service.IncidenciaService;
import com.giu.giu.service.NotificationService;
import com.giu.giu.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * TC-04: Cierre de sesión
 * TC-05: Acceso denegado por rol insuficiente (Spring Security)
 * TC-06: Acceso denegado a usuario no autenticado
 * TC-18: Generar informe (Administrador)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
class SeguridadWebTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockitoBean private IncidenciaService incidenciaService;
    @MockitoBean private UsuarioService usuarioService;
    @MockitoBean private NotificationService notificationService;
    @MockitoBean private ConfiguracionPrioridadService configuracionPrioridadService;
    @MockitoBean private ConfiguracionCategoriaService configuracionCategoriaService;

    @BeforeEach
    void configurarMockMvc() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    // -----------------------------------------------------------------------
    // TC-04 — Cierre de sesión
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("TC-04: POST /logout invalida la sesión y redirige a la página de inicio")
    void tc04_cierreSesion_redirigePaginaInicio() throws Exception {
        mockMvc.perform(post("/logout")
                        .with(autenticarComo(crearCiudadano())))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @DisplayName("TC-04: Tras cerrar sesión, acceder a recurso protegido redirige al login (sesión invalidada)")
    void tc04_trasCierreSesion_accesoRecursoProtegido_redirigeLogin() throws Exception {
        // Sin autenticación activa (equivale al estado tras un logout), se redirige al login
        mockMvc.perform(get("/ciudadano/incidencias/mis-incidencias"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    // -----------------------------------------------------------------------
    // TC-05 — Acceso denegado por rol insuficiente (Spring Security)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("TC-05: Ciudadano intenta acceder a dashboard de Operador — Spring Security deniega (HTTP 403)")
    void tc05_ciudadanoAccedeRecursoOperador_deniega() throws Exception {
        // Spring Security intercepta y devuelve 403 Forbidden (AccessDeniedHandler con forward)
        mockMvc.perform(get("/dashboard/operador")
                        .with(autenticarComo(crearCiudadano())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("TC-05: Ciudadano intenta acceder a dashboard de Técnico — Spring Security deniega (HTTP 403)")
    void tc05_ciudadanoAccedeRecursoTecnico_deniega() throws Exception {
        mockMvc.perform(get("/dashboard/tecnico")
                        .with(autenticarComo(crearCiudadano())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("TC-05: Ciudadano intenta acceder a dashboard de Administrador — Spring Security deniega (HTTP 403)")
    void tc05_ciudadanoAccedeRecursoAdministrador_deniega() throws Exception {
        mockMvc.perform(get("/dashboard/admin")
                        .with(autenticarComo(crearCiudadano())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("TC-05: Operador intenta acceder a dashboard de Administrador — Spring Security deniega (HTTP 403)")
    void tc05_operadorAccedeRecursoAdministrador_deniega() throws Exception {
        Usuario operador = new Usuario();
        operador.setId(5L);
        operador.setEmail("operador@test.com");
        operador.setPassword("$2a$encoded");
        operador.setRol(Rol.OPERADOR);
        operador.setActivo(true);

        mockMvc.perform(get("/dashboard/admin")
                        .with(autenticarComo(operador)))
                .andExpect(status().isForbidden());
    }

    // -----------------------------------------------------------------------
    // TC-06 — Acceso denegado a usuario no autenticado
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("TC-06: Sin sesión activa, acceder a /ciudadano/incidencias redirige al login")
    void tc06_usuarioNoAutenticado_accedeRecursoProtegidoCiudadano_redirigeLogin() throws Exception {
        mockMvc.perform(get("/ciudadano/incidencias/mis-incidencias"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("TC-06: Sin sesión activa, acceder a /dashboard/operador redirige al login")
    void tc06_usuarioNoAutenticado_accedeRecursoOperador_redirigeLogin() throws Exception {
        mockMvc.perform(get("/dashboard/operador"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("TC-06: Sin sesión activa, acceder a /dashboard/admin redirige al login")
    void tc06_usuarioNoAutenticado_accedeRecursoAdmin_redirigeLogin() throws Exception {
        mockMvc.perform(get("/dashboard/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("TC-06: La página de login es accesible sin autenticación (permitAll)")
    void tc06_paginaLogin_accesibleSinAutenticar() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    // -----------------------------------------------------------------------
    // TC-18 — Generar informe (Administrador)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("TC-18: Administrador accede al dashboard — el sistema genera datos estadísticos")
    void tc18_administradorGeneraInforme_visualizaDatosEstadisticos() throws Exception {
        Usuario admin = new Usuario();
        admin.setId(99L);
        admin.setEmail("admin@test.com");
        admin.setPassword("$2a$encoded");
        admin.setRol(Rol.ADMINISTRADOR);
        admin.setActivo(true);

        when(usuarioService.obtenerPendientes()).thenReturn(List.of());
        when(usuarioService.obtenerSolicitudesRol()).thenReturn(List.of());
        when(usuarioService.obtenerTodos()).thenReturn(List.of());
        when(configuracionPrioridadService.obtenerTodas()).thenReturn(List.of());
        when(configuracionCategoriaService.obtenerTodas()).thenReturn(List.of());

        mockMvc.perform(get("/dashboard/admin")
                        .with(autenticarComo(admin)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("TC-18: No administrador intenta acceder al informe — Spring Security deniega")
    void tc18_noAdministrador_accedeInforme_deniega() throws Exception {
        mockMvc.perform(get("/dashboard/admin")
                        .with(autenticarComo(crearCiudadano())))
                .andExpect(status().isForbidden());
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private RequestPostProcessor autenticarComo(Usuario usuario) {
        CustomUserDetails details = new CustomUserDetails(usuario);
        Authentication auth = UsernamePasswordAuthenticationToken.authenticated(
                details, null, details.getAuthorities());
        return authentication(auth);
    }

    private Usuario crearCiudadano() {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setEmail("ciudadano@test.com");
        u.setPassword("$2a$encoded");
        u.setRol(Rol.CIUDADANO);
        u.setActivo(true);
        return u;
    }
}
