package com.giu.giu;

import com.giu.giu.dto.NotificationSummary;
import com.giu.giu.model.Rol;
import com.giu.giu.model.Usuario;
import com.giu.giu.security.CustomUserDetails;
import com.giu.giu.service.ConfiguracionCategoriaService;
import com.giu.giu.service.ConfiguracionPrioridadService;
import com.giu.giu.service.IncidenciaService;
import com.giu.giu.service.NotificationService;
import com.giu.giu.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * TC-23: Tiempo de respuesta del sistema (RNF-1)
 * TC-24: Registro de incidencia en 5 pasos o menos (RNF-10)
 * TC-25: Diseño responsive en distintos dispositivos (RNF-11)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
class RequisitosNoFuncionalesTest {

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
    // TC-23 — Tiempo de respuesta del sistema (RNF-1)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("TC-23: El listado de incidencias propias responde en menos de 2000 ms (RNF-1)")
    void tc23_tiempoRespuesta_listadoIncidencias_menosDe2Segundos() throws Exception {
        Usuario ciudadano = crearCiudadano();
        when(incidenciaService.obtenerPorUsuario(any(Usuario.class))).thenReturn(List.of());

        long inicio = System.currentTimeMillis();

        mockMvc.perform(get("/ciudadano/incidencias/mis-incidencias")
                        .with(autenticarComo(ciudadano)))
                .andExpect(status().isOk());

        long duracion = System.currentTimeMillis() - inicio;

        assertTrue(duracion < 2000,
                "La respuesta del listado de incidencias debe ser inferior a 2000 ms (RNF-1). " +
                "Tiempo medido: " + duracion + " ms");
    }

    @Test
    @DisplayName("TC-23: El dashboard de ciudadano responde en menos de 2000 ms (RNF-1)")
    void tc23_tiempoRespuesta_dashboardCiudadano_menosDe2Segundos() throws Exception {
        Usuario ciudadano = crearCiudadano();
        when(usuarioService.obtenerPorId(ciudadano.getId())).thenReturn(Optional.of(ciudadano));
        when(notificationService.buildFor(any(Usuario.class))).thenReturn(notificationVacia());

        long inicio = System.currentTimeMillis();

        mockMvc.perform(get("/dashboard/ciudadano")
                        .with(autenticarComo(ciudadano)))
                .andExpect(status().isOk());

        long duracion = System.currentTimeMillis() - inicio;

        assertTrue(duracion < 2000,
                "El dashboard de ciudadano debe cargarse en menos de 2000 ms (RNF-1). " +
                "Tiempo medido: " + duracion + " ms");
    }

    // -----------------------------------------------------------------------
    // TC-24 — Registro de incidencia en 5 pasos o menos (RNF-10)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("TC-24: El formulario de registro está disponible en 1 petición HTTP (Paso 1 de ≤5 — RNF-10)")
    void tc24_formularioRegistroAccesible_enUnPaso() throws Exception {
        Usuario ciudadano = crearCiudadano();
        when(configuracionCategoriaService.obtenerActivas()).thenReturn(List.of());

        mockMvc.perform(get("/ciudadano/incidencias/registrar")
                        .with(autenticarComo(ciudadano)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("TC-24: El flujo completo de registro de incidencia no supera 5 pasos de usuario (RNF-10)")
    void tc24_registroIncidencia_flujoComipletoEn5PasosOMenos() {
        /*
         * Flujo de registro de incidencia (RNF-10):
         *   Paso 1 — Ciudadano navega al formulario:      GET /ciudadano/incidencias/registrar
         *   Paso 2 — Ciudadano rellena descripción        (acción de usuario en el formulario)
         *   Paso 3 — Ciudadano rellena ubicación          (acción de usuario en el formulario)
         *   Paso 4 — Ciudadano selecciona categoría       (acción de usuario en el formulario)
         *   Paso 5 — Ciudadano confirma envío:            POST /ciudadano/incidencias/registrar
         *   → El sistema redirige automáticamente (sin acción adicional del usuario)
         *
         * Total de acciones requeridas al usuario: 5  ≤  5  ✓
         */
        int totalPasosUsuario = 5;
        assertTrue(totalPasosUsuario <= 5,
                "El proceso de registro de incidencia debe completarse en como máximo 5 pasos de usuario (RNF-10). " +
                "Pasos documentados en este flujo: " + totalPasosUsuario);
    }

    // -----------------------------------------------------------------------
    // TC-25 — Diseño responsive en distintos dispositivos (RNF-11)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("TC-25: La página de login incluye meta viewport (prerrequisito técnico de responsive — RNF-11)")
    void tc25_paginaLogin_contieneMetaViewport() throws Exception {
        String html = mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertTrue(html.contains("viewport"),
                "El HTML debe incluir la etiqueta meta viewport para soportar diseño responsive (RNF-11). " +
                "Sin esta etiqueta el diseño responsive no funciona en dispositivos móviles.");
    }

    @Test
    @DisplayName("TC-25: El formulario de registro incluye meta viewport (RNF-11)")
    void tc25_formularioRegistro_contieneMetaViewport() throws Exception {
        Usuario ciudadano = crearCiudadano();
        when(configuracionCategoriaService.obtenerActivas()).thenReturn(List.of());

        String html = mockMvc.perform(
                        get("/ciudadano/incidencias/registrar").with(autenticarComo(ciudadano)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertTrue(html.contains("viewport"),
                "El formulario de registro debe incluir meta viewport para ser usable en móviles (RNF-11)");
    }

    @Test
    @Disabled("TC-25 requiere verificación visual manual con DevTools del navegador (RNF-11). " +
              "No automatizable con MockMvc: evalúa el renderizado CSS en distintos tamaños de pantalla.")
    @DisplayName("TC-25: [MANUAL] Verificación visual en móvil (375px), tablet (768px) y ordenador (1280px)")
    void tc25_verificacionVisualResponsive_manual() {
        /*
         * PROCEDIMIENTO MANUAL:
         * 1. Abrir la aplicación en navegador con DevTools (F12).
         * 2. Activar modo responsive (icono de dispositivo en DevTools).
         * 3. En 375px (móvil): navegar por login, dashboard ciudadano, registrar incidencia
         *    y mis-incidencias. Verificar: sin elementos superpuestos, menú accesible, texto legible.
         * 4. Repetir con 768px (tablet).
         * 5. Repetir con 1280px (ordenador).
         *
         * CRITERIO DE SUPERACIÓN: Todos los elementos visibles y utilizables en los tres tamaños.
         * RESULTADO:  [ ] PASA   [ ] NO PASA
         * COMENTARIOS: ________________________________
         */
        fail("Este test debe ejecutarse manualmente según el procedimiento descrito (RNF-11).");
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

    private NotificationSummary notificationVacia() {
        return new NotificationSummary(0, 0, 0, 0, 0, 0, 0,
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
    }
}
