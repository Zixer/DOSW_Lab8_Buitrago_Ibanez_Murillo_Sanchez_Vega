package edu.eci.dosw.oficioya.security;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** 
 * PT-07, PT-08 y PT-09, más el 403 por rol
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SeguridadTokenTest{

    private static final String PROTEGIDO = "/api/v1/usuarios/me";
    private static final String SOLO_ADMIN = "/api/v1/admin/usuarios";

    @Autowired MockMvc mockMvc;
    @Autowired JwtService jwtService;
    @Value("${app.jwt.secret}") String secret;
    @Value("${app.jwt.issuer}") String issuer;

    //PT-07
    @Test
    @DisplayName("PT-07: recurso protegido sin token responde 401 con formato ApiError")
    void sinToken() throws Exception {
        mockMvc.perform(get(PROTEGIDO))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.ruta").value(PROTEGIDO))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    @DisplayName("PT-07: un header Authorization con basura responde 401")
    void headerBasura() throws Exception {
        mockMvc.perform(get(PROTEGIDO).header("Authorization", "Bearer esto-no-es-un-jwt"))
                .andExpect(status().isUnauthorized());
    }

    //PT-08
    @Test
    @DisplayName("PT-08: token con la firma alterada responde 401")
    void tokenAlterado() throws Exception {
        String token = jwtService.generar("luis@example.com", "CONTRATANTE");
        mockMvc.perform(get(PROTEGIDO).header("Authorization", "Bearer " + alterarFirma(token)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("PT-08 (ataque 2): cambiar role a ADMIN sin la clave invalida la firma: 401")
    void roleEscalado() throws Exception {
        String token = jwtService.generar("luis@example.com", "CONTRATANTE");
        mockMvc.perform(get(SOLO_ADMIN).header("Authorization", "Bearer " + escalarARoleAdmin(token)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PT-08: token con emisor distinto responde 401")
    void emisorDistinto() throws Exception {
        String token = new JwtService(secret, "otro-emisor", 30).generar("luis@example.com", "CONTRATANTE");
        mockMvc.perform(get(PROTEGIDO).header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    //PT-09
    @Test
    @DisplayName("PT-09: token vencido responde 401")
    void tokenVencido() throws Exception {
        String vencido = new JwtService(secret, issuer, -1).generar("luis@example.com", "CONTRATANTE");
        mockMvc.perform(get(PROTEGIDO).header("Authorization", "Bearer " + vencido))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    //Token válido
    @Test
    @DisplayName("Un token válido pasa el filtro (no responde 401; 404 si el endpoint aún no existe)")
    void tokenValido() throws Exception {
        String token = jwtService.generar("luis@example.com", "CONTRATANTE");
        mockMvc.perform(get(PROTEGIDO).header("Authorization", "Bearer " + token))
                .andExpect(result -> assertNotEquals(401, result.getResponse().getStatus()));
    }

    //403 por rol
    @Test
    @DisplayName("Un CONTRATANTE autenticado en /admin/** responde 403 con formato ApiError")
    void contratanteEnAdmin() throws Exception {
        String token = jwtService.generar("luis@example.com", "CONTRATANTE");
        mockMvc.perform(get(SOLO_ADMIN).header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.ruta").value(SOLO_ADMIN));
    }

    @Test
    @DisplayName("Un ADMIN con token válido pasa la regla de /admin/** (ni 401 ni 403)")
    void adminEnAdmin() throws Exception {
        String token = jwtService.generar("admin@example.com", "ADMIN");
        mockMvc.perform(get(SOLO_ADMIN).header("Authorization", "Bearer " + token))
                .andExpect(result -> {
                    int s = result.getResponse().getStatus();
                    assertNotEquals(401, s);
                    assertNotEquals(403, s);
                });
    }

    //Utilidades
    private String alterarFirma(String token) {
        int i = token.length() - 10;
        char c = token.charAt(i) == 'A' ? 'B' : 'A';
        return token.substring(0, i) + c + token.substring(i + 1);
    }

    /** 
     * Cambia el rol en el payload y deja la firma original
     */
    private String escalarARoleAdmin(String token) {
        String[] partes = token.split("\\.");
        String payload = new String(Base64.getUrlDecoder().decode(partes[1]), StandardCharsets.UTF_8);
        String modificado = payload.replace("\"role\":\"CONTRATANTE\"", "\"role\":\"ADMIN\"");
        assertNotEquals(payload, modificado, "No se pudo modificar el rol del payload");
        String nuevoPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(modificado.getBytes(StandardCharsets.UTF_8));
        return partes[0] + "." + nuevoPayload + "." + partes[2];
    }
}

