package edu.eci.dosw.oficioya;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.eci.dosw.oficioya.dto.RegistroRequest;
import edu.eci.dosw.oficioya.entity.Rol;
import edu.eci.dosw.oficioya.entity.Usuario;
import edu.eci.dosw.oficioya.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest extends BaseIntegrationTest {

    @Autowired private UsuarioRepository usuarioRepository;

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
        // first registration
        RegistroRequest first = new RegistroRequest(
                "Bob", "bob@test.com", "Password123", "5550001", Rol.CONTRATANTE, "Electricista", "Sur");
        mockMvc.perform(post("/api/v1/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(first)))
                .andExpect(status().isCreated());
        // second with same email
        RegistroRequest dup = new RegistroRequest(
                "Bob 2", "bob@test.com", "AnotherPass", "5550002", Rol.CONTRATANTE, "Electricista", "Sur");
        mockMvc.perform(post("/api/v1/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dup)))
                .andExpect(status().isConflict());
    }

    @Test
    void whenInvalidRolOrPassword_thenReturns400() throws Exception {
        // invalid rol (ADMIN not allowed)
        RegistroRequest badRol = new RegistroRequest(
                "Carlos", "carlos@test.com", "Password123", "5551111", Rol.ADMIN, "Mecánico", "Este");
        mockMvc.perform(post("/api/v1/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(badRol)))
                .andExpect(status().isBadRequest());
        // short password
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
}
