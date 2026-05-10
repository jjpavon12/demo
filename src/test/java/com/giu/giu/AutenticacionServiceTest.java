package com.giu.giu;

import com.giu.giu.dto.LoginRequest;
import com.giu.giu.dto.LoginResponse;
import com.giu.giu.model.Rol;
import com.giu.giu.model.Usuario;
import com.giu.giu.repository.EquipoTecnicoConfigRepository;
import com.giu.giu.repository.IncidenciaRepository;
import com.giu.giu.repository.UsuarioRepository;
import com.giu.giu.service.UsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * TC-01: Registro de nuevo usuario
 * TC-02: Inicio de sesión con credenciales válidas
 * TC-03: Inicio de sesión con credenciales incorrectas
 */
@ExtendWith(MockitoExtension.class)
class AutenticacionServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EquipoTecnicoConfigRepository equipoTecnicoConfigRepository;

    @Mock
    private IncidenciaRepository incidenciaRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    // -----------------------------------------------------------------------
    // TC-01 — Registro de nuevo usuario
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("TC-01: Registro con datos válidos registra al usuario correctamente")
    void tc01_registroNuevoUsuario_conDatosValidos_creaLaCuenta() {
        when(usuarioRepository.findByEmail("nuevo@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Password1")).thenReturn("$2a$encoded");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        LoginResponse respuesta = usuarioService.registrar(
                "nuevo@test.com", "Password1", "Password1", Rol.CIUDADANO);

        assertNotNull(respuesta.getId(), "El usuario debe recibir un ID tras el registro");
        assertEquals("nuevo@test.com", respuesta.getEmail());
        assertEquals(Rol.CIUDADANO, respuesta.getRol());
        assertEquals("Usuario registrado exitosamente", respuesta.getMensaje());
    }

    @Test
    @DisplayName("TC-01: Registro con email duplicado devuelve error")
    void tc01_registroNuevoUsuario_emailDuplicado_devuelveError() {
        Usuario existente = new Usuario();
        existente.setEmail("existente@test.com");
        when(usuarioRepository.findByEmail("existente@test.com")).thenReturn(Optional.of(existente));

        LoginResponse respuesta = usuarioService.registrar(
                "existente@test.com", "Password1", "Password1", Rol.CIUDADANO);

        assertNull(respuesta.getId(), "No debe generarse ID cuando el email ya está registrado");
        assertEquals("El email ya está registrado", respuesta.getMensaje());
    }

    @Test
    @DisplayName("TC-01: Registro con contraseñas no coincidentes devuelve error")
    void tc01_registroNuevoUsuario_contrasenhasNoCoinciden_devuelveError() {
        when(usuarioRepository.findByEmail("nuevo@test.com")).thenReturn(Optional.empty());

        LoginResponse respuesta = usuarioService.registrar(
                "nuevo@test.com", "Password1", "OtraPassword", Rol.CIUDADANO);

        assertNull(respuesta.getId());
        assertEquals("Las contraseñas no coinciden", respuesta.getMensaje());
    }

    @Test
    @DisplayName("TC-01: Registro con contraseña demasiado corta devuelve error")
    void tc01_registroNuevoUsuario_contrasenhaCorta_devuelveError() {
        when(usuarioRepository.findByEmail("nuevo@test.com")).thenReturn(Optional.empty());

        LoginResponse respuesta = usuarioService.registrar(
                "nuevo@test.com", "abc", "abc", Rol.CIUDADANO);

        assertNull(respuesta.getId());
        assertEquals("La contraseña debe tener al menos 6 caracteres", respuesta.getMensaje());
    }

    @Test
    @DisplayName("TC-01: Registro de Operador queda pendiente de aprobación")
    void tc01_registroOperador_quedaPendienteAprobacion() {
        when(usuarioRepository.findByEmail("operador@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Password1")).thenReturn("$2a$encoded");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(2L);
            return u;
        });

        LoginResponse respuesta = usuarioService.registrar(
                "operador@test.com", "Password1", "Password1", Rol.OPERADOR);

        assertNotNull(respuesta.getId());
        assertTrue(respuesta.getMensaje().contains("administrador debe aprobarla"),
                "El mensaje debe indicar que la cuenta requiere aprobación");
    }

    // -----------------------------------------------------------------------
    // TC-02 — Inicio de sesión con credenciales válidas
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("TC-02: Login con credenciales válidas devuelve sesión autenticada con rol correcto")
    void tc02_inicioSesion_credencialesValidas_devuelveSesionConRol() {
        Usuario usuario = crearUsuario(1L, "ciudadano@test.com", "$2a$encoded", Rol.CIUDADANO, true);
        when(usuarioRepository.findByEmail("ciudadano@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("Password1", "$2a$encoded")).thenReturn(true);

        LoginRequest request = new LoginRequest("ciudadano@test.com", "Password1");
        LoginResponse respuesta = usuarioService.login(request);

        assertEquals(1L, respuesta.getId());
        assertEquals("ciudadano@test.com", respuesta.getEmail());
        assertEquals(Rol.CIUDADANO, respuesta.getRol());
        assertEquals("Login exitoso", respuesta.getMensaje());
    }

    @Test
    @DisplayName("TC-02: Login con cuenta inactiva (pendiente de aprobación) devuelve error")
    void tc02_inicioSesion_cuentaInactiva_devuelveMensajeEspera() {
        Usuario usuario = crearUsuario(2L, "operador@test.com", "$2a$encoded", Rol.OPERADOR, false);
        when(usuarioRepository.findByEmail("operador@test.com")).thenReturn(Optional.of(usuario));

        LoginRequest request = new LoginRequest("operador@test.com", "Password1");
        LoginResponse respuesta = usuarioService.login(request);

        assertNull(respuesta.getId(), "Un usuario inactivo no debe poder iniciar sesión");
        assertTrue(respuesta.getMensaje().contains("no habilitado"),
                "El mensaje debe indicar que la cuenta no está habilitada");
    }

    // -----------------------------------------------------------------------
    // TC-03 — Inicio de sesión con credenciales incorrectas
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("TC-03: Login con contraseña incorrecta rechaza la autenticación")
    void tc03_inicioSesion_contrasenhaIncorrecta_rechazaAcceso() {
        Usuario usuario = crearUsuario(1L, "ciudadano@test.com", "$2a$encoded", Rol.CIUDADANO, true);
        when(usuarioRepository.findByEmail("ciudadano@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("ContrasenhaErronea", "$2a$encoded")).thenReturn(false);

        LoginRequest request = new LoginRequest("ciudadano@test.com", "ContrasenhaErronea");
        LoginResponse respuesta = usuarioService.login(request);

        assertNull(respuesta.getId(), "No debe haber ID de sesión con credenciales incorrectas");
        assertNull(respuesta.getRol(), "No debe asignarse rol cuando la autenticación falla");
        assertEquals("Email o contraseña incorrectos", respuesta.getMensaje());
    }

    @Test
    @DisplayName("TC-03: Login con email no registrado rechaza la autenticación")
    void tc03_inicioSesion_emailNoRegistrado_rechazaAcceso() {
        when(usuarioRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest("noexiste@test.com", "Password1");
        LoginResponse respuesta = usuarioService.login(request);

        assertNull(respuesta.getId());
        assertEquals("Email o contraseña incorrectos", respuesta.getMensaje());
    }

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------

    private Usuario crearUsuario(Long id, String email, String password, Rol rol, boolean activo) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setEmail(email);
        u.setPassword(password);
        u.setRol(rol);
        u.setActivo(activo);
        return u;
    }
}
