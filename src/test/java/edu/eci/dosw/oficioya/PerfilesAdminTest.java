package edu.eci.dosw.oficioya;

import edu.eci.dosw.oficioya.entity.Rol;
import edu.eci.dosw.oficioya.entity.Usuario;
import edu.eci.dosw.oficioya.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PerfilesAdminTest extends BaseIntegrationTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Long trabajadorId;

    @BeforeEach
    void prepararDatos() {

        usuarioRepository.deleteAll();

        Usuario trabajador = new Usuario();
        trabajador.setNombre("Pedro Gomez");
        trabajador.setCorreo("pedro@example.com");
        trabajador.setTelefono("3001234567");
        trabajador.setPasswordHash(passwordEncoder.encode("ContrasenaDemo123!"));
        trabajador.setRol(Rol.TRABAJADOR);
        trabajador.setOficioPrincipal("Plomero");
        trabajador.setZona("Chapinero");

        trabajadorId = usuarioRepository.save(trabajador).getId();
    }

    // PT-10: El perfil publico no expone datos de contacto
    @Test
    @WithMockUser(username = "cliente@example.com",roles = "CONTRATANTE")
    @DisplayName("PT-10: Perfil publico sin correo ni telefono")
    void perfilPublicoSinContacto() throws Exception {

        mockMvc.perform(get("/api/v1/trabajadores/{id}", trabajadorId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.nombre").value("Pedro Gomez"))
                .andExpect(jsonPath("$.oficioPrincipal").value("Plomero"))
                .andExpect(jsonPath("$.correo").doesNotExist())
                .andExpect(jsonPath("$.telefono").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }
        // El 404 de un trabajador inexistente usa el formato ApiError
    @Test
    @WithMockUser(username = "cliente@example.com", roles = "CONTRATANTE")
    @DisplayName("Trabajador inexistente responde 404 con formato ApiError")
    void trabajadorInexistente() throws Exception {

        mockMvc.perform(get("/api/v1/trabajadores/{id}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensaje").value("Trabajador no encontrado"))
                .andExpect(jsonPath("$.ruta").value("/api/v1/trabajadores/999999"));
    }

    // PT-11: Un contratante no puede consultar administracion
    @Test
    @WithMockUser(username = "cliente@example.com",roles = "CONTRATANTE")
    @DisplayName("PT-11: Contratante recibe 403 en administracion")
    void contratanteNoAccedeAdmin() throws Exception {

        mockMvc.perform(get("/api/v1/admin/usuarios")).andExpect(status().isForbidden());
    }

    // PT-12: Un administrador puede consultar los contactos
    @Test
    @WithMockUser(username = "admin@example.com",roles = "ADMIN")
    @DisplayName("PT-12: Administrador consulta datos de contacto")
    void administradorConsultaUsuarios() throws Exception {

        mockMvc.perform(get("/api/v1/admin/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Pedro Gomez"))
                .andExpect(jsonPath("$[0].correo").value("pedro@example.com"))
                .andExpect(jsonPath("$[0].telefono").value("3001234567"))
                .andExpect(jsonPath("$[0].passwordHash").doesNotExist());
    }
}
