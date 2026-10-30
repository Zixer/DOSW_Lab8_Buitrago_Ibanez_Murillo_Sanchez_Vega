package edu.eci.dosw.oficioya;

import edu.eci.dosw.oficioya.dto.SolicitudRequest;
import edu.eci.dosw.oficioya.entity.EstadoSolicitud;
import edu.eci.dosw.oficioya.entity.Rol;
import edu.eci.dosw.oficioya.entity.Solicitud;
import edu.eci.dosw.oficioya.entity.Usuario;
import edu.eci.dosw.oficioya.repository.SolicitudRepository;
import edu.eci.dosw.oficioya.repository.UsuarioRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SolicitudesTest extends BaseIntegrationTest {

    private static final String URL = "/api/v1/solicitudes";

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SolicitudRepository solicitudRepository;

    private Usuario pedro;
    private Long solicitudDeAna;

    @BeforeEach
    void prepararDatos() {

        limpiar();

        Usuario ana = crearUsuario("Ana Contratante", "ana@solicitudes.test", Rol.CONTRATANTE);
        Usuario luis = crearUsuario("Luis Contratante", "luis@solicitudes.test", Rol.CONTRATANTE);
        pedro = crearUsuario("Pedro Trabajador", "pedro@solicitudes.test", Rol.TRABAJADOR);
        crearUsuario("Marta Trabajadora", "marta@solicitudes.test", Rol.TRABAJADOR);
        crearUsuario("Administrador", "admin@solicitudes.test", Rol.ADMIN);

        solicitudDeAna = crearSolicitud(ana, pedro, "Tengo una gotera en el baño");
        crearSolicitud(luis, pedro, "Cambiar la llave del lavamanos");
    }

    @AfterEach
    void limpiar() {

        solicitudRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "ana@solicitudes.test", roles = "CONTRATANTE")
    @DisplayName("Un CONTRATANTE crea una solicitud: 201, PENDIENTE y sin datos de contacto")
    void contratanteCreaSolicitud() throws Exception {

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoSolicitud(pedro.getId(), "Arreglar el calentador")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.contratanteNombre").value("Ana Contratante"))
                .andExpect(jsonPath("$.trabajadorId").value(pedro.getId().intValue()))
                .andExpect(jsonPath("$.correo").doesNotExist())
                .andExpect(jsonPath("$.telefono").doesNotExist());
    }

    // PT-13: Un trabajador no puede crear solicitudes
    @Test
    @WithMockUser(username = "pedro@solicitudes.test", roles = "TRABAJADOR")
    @DisplayName("PT-13: un TRABAJADOR intenta crear una solicitud: 403")
    void trabajadorNoCreaSolicitudes() throws Exception {

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoSolicitud(pedro.getId(), "Quiero contratarme a mí mismo")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        assertEquals(2, solicitudRepository.count());
    }

    // PT-14: Un contratante solo ve sus solicitudes
    @Test
    @WithMockUser(username = "ana@solicitudes.test", roles = "CONTRATANTE")
    @DisplayName("PT-14: el contratante lista solo sus propias solicitudes")
    void contratanteListaSoloLasSuyas() throws Exception {

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(solicitudDeAna.intValue()))
                .andExpect(jsonPath("$[0].contratanteNombre").value("Ana Contratante"));
    }

    // PT-15: Solicitud ajena responde igual que una inexistente
    @Test
    @WithMockUser(username = "luis@solicitudes.test", roles = "CONTRATANTE")
    @DisplayName("PT-15: consultar una solicitud ajena responde 404, igual que una inexistente")
    void consultarSolicitudAjena() throws Exception {

        mockMvc.perform(get(URL + "/{id}", solicitudDeAna))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensaje").value("Solicitud no encontrada"));

        mockMvc.perform(get(URL + "/{id}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Solicitud no encontrada"));
    }

    // PT-15: Cancelar una solicitud ajena no cambia la base de datos
    @Test
    @WithMockUser(username = "luis@solicitudes.test", roles = "CONTRATANTE")
    @DisplayName("PT-15: cancelar una solicitud ajena responde 404 y no cambia su estado")
    void cancelarSolicitudAjena() throws Exception {

        mockMvc.perform(patch(URL + "/{id}/cancelar", solicitudDeAna))
                .andExpect(status().isNotFound());

        assertEquals(EstadoSolicitud.PENDIENTE, estadoDe(solicitudDeAna));
    }

    // PT-16: Aceptar con un trabajador no asignado
    @Test
    @WithMockUser(username = "marta@solicitudes.test", roles = "TRABAJADOR")
    @DisplayName("PT-16: un trabajador no asignado intenta aceptar: 404 y sin cambios")
    void aceptarTrabajadorNoAsignado() throws Exception {

        mockMvc.perform(patch(URL + "/{id}/aceptar", solicitudDeAna))
                .andExpect(status().isNotFound());

        assertEquals(EstadoSolicitud.PENDIENTE, estadoDe(solicitudDeAna));
    }

    // PT-16: Aceptar con un contratante
    @Test
    @WithMockUser(username = "ana@solicitudes.test", roles = "CONTRATANTE")
    @DisplayName("PT-16: un contratante intenta aceptar: 403")
    void aceptarComoContratante() throws Exception {

        mockMvc.perform(patch(URL + "/{id}/aceptar", solicitudDeAna))
                .andExpect(status().isForbidden());

        assertEquals(EstadoSolicitud.PENDIENTE, estadoDe(solicitudDeAna));
    }

    // PT-17: El administrador no accede a solicitudes
    @Test
    @WithMockUser(username = "admin@solicitudes.test", roles = "ADMIN")
    @DisplayName("PT-17: ADMIN consulta /solicitudes: 403")
    void adminNoAccedeSolicitudes() throws Exception {

        mockMvc.perform(get(URL))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(username = "pedro@solicitudes.test", roles = "TRABAJADOR")
    @DisplayName("El trabajador asignado acepta una solicitud pendiente: 200 y ACEPTADA")
    void trabajadorAsignadoAcepta() throws Exception {

        mockMvc.perform(patch(URL + "/{id}/aceptar", solicitudDeAna))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACEPTADA"));
    }

    @Test
    @WithMockUser(username = "pedro@solicitudes.test", roles = "TRABAJADOR")
    @DisplayName("Aceptar una solicitud rechazada responde 409")
    void aceptarRechazadaEsConflicto() throws Exception {

        cambiarEstado(solicitudDeAna, EstadoSolicitud.RECHAZADA);

        mockMvc.perform(patch(URL + "/{id}/aceptar", solicitudDeAna))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @WithMockUser(username = "ana@solicitudes.test", roles = "CONTRATANTE")
    @DisplayName("El contratante autor cancela una solicitud aceptada: 200 y CANCELADA")
    void contratanteCancelaAceptada() throws Exception {

        cambiarEstado(solicitudDeAna, EstadoSolicitud.ACEPTADA);

        mockMvc.perform(patch(URL + "/{id}/cancelar", solicitudDeAna))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));
    }

    @Test
    @WithMockUser(username = "ana@solicitudes.test", roles = "CONTRATANTE")
    @DisplayName("Cancelar una solicitud ya cancelada responde 409")
    void cancelarCanceladaEsConflicto() throws Exception {

        cambiarEstado(solicitudDeAna, EstadoSolicitud.CANCELADA);

        mockMvc.perform(patch(URL + "/{id}/cancelar", solicitudDeAna))
                .andExpect(status().isConflict());
    }

    private Usuario crearUsuario(String nombre, String correo, Rol rol) {

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setCorreo(correo);
        usuario.setTelefono("3000000000");
        usuario.setPasswordHash(passwordEncoder.encode("ContrasenaDemo123!"));
        usuario.setRol(rol);

        return usuarioRepository.save(usuario);
    }

    private Long crearSolicitud(Usuario contratante, Usuario trabajador, String descripcion) {

        Solicitud solicitud = new Solicitud();
        solicitud.setDescripcion(descripcion);
        solicitud.setContratante(contratante);
        solicitud.setTrabajador(trabajador);

        return solicitudRepository.save(solicitud).getId();
    }

    private void cambiarEstado(Long id, EstadoSolicitud estado) {

        Solicitud solicitud = solicitudRepository.findById(id).orElseThrow();
        solicitud.setEstado(estado);
        solicitudRepository.save(solicitud);
    }

    private EstadoSolicitud estadoDe(Long id) {

        return solicitudRepository.findById(id).orElseThrow().getEstado();
    }

    private String cuerpoSolicitud(Long trabajadorId, String descripcion) throws Exception {

        return mapper.writeValueAsString(new SolicitudRequest(trabajadorId, descripcion));
    }
}