package edu.eci.dosw.oficioya;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.eci.dosw.oficioya.dto.LoginRequest;
import edu.eci.dosw.oficioya.dto.RegistroRequest;
import edu.eci.dosw.oficioya.entity.Rol;
import edu.eci.dosw.oficioya.entity.Usuario;
import edu.eci.dosw.oficioya.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest extends BaseIntegrationTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void whenValidRequest_thenReturns201AndNoPassword() throws Exception {
        RegistroRequest req = new RegistroRequest(
                "Ana Pérez",
                "ana@test.com",
                "StrongPass123",
                "5551234",
                Rol.TRABAJADOR,
                "Carpintería",
                "Norte"
        );
        mockMvc.perform(post("/api/v1/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.nombre", is("Ana Pérez")))
                .andExpect(jsonPath("$.correo", is("ana@test.com")))
                .andExpect(jsonPath("$.rol", is("TRABAJADOR")))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void whenDuplicateEmail_thenReturns409() throws Exception {
        RegistroRequest first = new RegistroRequest(
                "Bob", "bob@test.com", "Password123", "5550001", Rol.CONTRATANTE, "Electricista", "Sur");
        mockMvc.perform(post("/api/v1/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(first)))
                .andExpect(status().isCreated());

        RegistroRequest dup = new RegistroRequest(
                "Bob 2", "bob@test.com", "AnotherPass", "5550002", Rol.CONTRATANTE, "Electricista", "Sur");
        mockMvc.perform(post("/api/v1/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dup)))
                .andExpect(status().isConflict());
    }

    @Test
    void whenInvalidRolOrPassword_thenReturns400() throws Exception {
        RegistroRequest badRol = new RegistroRequest(
                "Carlos", "carlos@test.com", "Password123", "5551111", Rol.ADMIN, "Mecánico", "Este");
        mockMvc.perform(post("/api/v1/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(badRol)))
                .andExpect(status().isBadRequest());

        RegistroRequest shortPwd = new RegistroRequest(
                "Diana", "diana@test.com", "short", "5552222", Rol.TRABAJADOR, "Pintor", "Oeste");
        mockMvc.perform(post("/api/v1/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(shortPwd)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenRegisteredPasswordIsHashed() throws Exception {
        RegistroRequest req = new RegistroRequest(
                "Eva", "eva@test.com", "SecretPass777", "5553333", Rol.TRABAJADOR, "Plomera", "Norte");
        mockMvc.perform(post("/api/v1/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        Usuario usuario = usuarioRepository.findByCorreo("eva@test.com").orElseThrow();
        assertNotEquals("SecretPass777", usuario.getPasswordHash());
        assertTrue(passwordEncoder.matches("SecretPass777", usuario.getPasswordHash()));
    }


    @Test
    void pt05_loginExitoso() throws Exception {
        // Registramos primero al usuario para poder loguearnos
        RegistroRequest registro = new RegistroRequest(
                "Pedro Gomez", "pedro@test.com", "Password123!", "5559999", Rol.TRABAJADOR, "Plomero", "Centro");
        mockMvc.perform(post("/api/v1/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(registro)))
                .andExpect(status().isCreated());

        LoginRequest loginReq = new LoginRequest("pedro@test.com", "Password123!");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo", is("Bearer")))
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.expiraEn", is(1800)));
    }

    @Test
    void pt06_loginCredencialesIncorrectas() throws Exception {
        // Caso 1: Correo inexistente
        LoginRequest loginInexistente = new LoginRequest("noexiste@test.com", "Password123!");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(loginInexistente)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje", is("Credenciales inválidas")));

        // Caso 2: Contraseña errónea de un usuario registrado
        RegistroRequest registro = new RegistroRequest(
                "Maria Test", "maria@test.com", "Password123!", "5558888", Rol.CONTRATANTE, "Asistente", "Norte");
        mockMvc.perform(post("/api/v1/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(registro)))
                .andExpect(status().isCreated());

        LoginRequest passwordErronea = new LoginRequest("maria@test.com", "PasswordMalo999!");
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(passwordErronea)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje", is("Credenciales inválidas")));
    }
}