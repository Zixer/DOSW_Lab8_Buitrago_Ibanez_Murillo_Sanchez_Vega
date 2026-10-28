package edu.eci.dosw.oficioya.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtServiceTest{
    private static final String SECRET = "clave-solo-para-pruebas-de-al-menos-32-bytes";

    private final JwtService jwtService = new JwtService(SECRET, "oficioya", 30);

    @Test
    @DisplayName("El token trae sub, role, iss, iat y exp (30 min) y se firma con HS256")
    void claimsYAlgoritmo(){
        String token = jwtService.generar("pedro@example.com", "TRABAJADOR");

        Claims claims = jwtService.validar(token);
        assertEquals("pedro@example.com", claims.getSubject());
        assertEquals("TRABAJADOR", claims.get("role", String.class));
        assertEquals("oficioya", claims.getIssuer());
        assertEquals(30 * 60 * 1000L, claims.getExpiration().getTime() - claims.getIssuedAt().getTime());

        String header = new String(Base64.getUrlDecoder().decode(token.split("\\.")[0]), StandardCharsets.UTF_8);
        assertTrue(header.contains("\"alg\":\"HS256\""), "El header debe indicar HS256: " + header);
    }

    @Test
    @DisplayName("El payload no lleva contraseñas ni teléfono")
    void payloadSinDatosSensibles(){
        String token = jwtService.generar("pedro@example.com", "TRABAJADOR");
        String payload = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]), StandardCharsets.UTF_8);
        assertFalse(payload.contains("password"));
        assertFalse(payload.contains("telefono"));
    }

    @Test
    @DisplayName("Una clave de menos de 32 bytes impide crear el servicio")
    void claveCorta(){
        assertThrows(IllegalStateException.class, () -> new JwtService("corta", "oficioya", 30));
    }

    @Test
    @DisplayName("Un token con la firma alterada se rechaza")
    void firmaAlterada(){
        String token = jwtService.generar("pedro@example.com", "TRABAJADOR");
        int i = token.length() - 10;
        char c = token.charAt(i) == 'A' ? 'B' : 'A';
        String alterado = token.substring(0, i) + c + token.substring(i + 1);
        assertThrows(JwtException.class, () -> jwtService.validar(alterado));
    }

    @Test
    @DisplayName("Un token vencido se rechaza")
    void tokenVencido(){
        String vencido = new JwtService(SECRET, "oficioya", -1).generar("pedro@example.com", "TRABAJADOR");
        assertThrows(JwtException.class, () -> jwtService.validar(vencido));
    }

    @Test
    @DisplayName("Un token con otro emisor se rechaza")
    void emisorDistinto(){
        String token = new JwtService(SECRET, "otro-emisor", 30).generar("pedro@example.com", "TRABAJADOR");
        assertThrows(JwtException.class, () -> jwtService.validar(token));
    }

    @Test
    @DisplayName("Un token sin firma (alg none) se rechaza")
    void tokenSinFirma(){
        String sinFirma = Jwts.builder().subject("pedro@example.com").claim("role", "ADMIN").compact();
        assertThrows(JwtException.class, () -> jwtService.validar(sinFirma));
    }

    @Test
    @DisplayName("Un token firmado con otra clave se rechaza")
    void otraClave(){
        JwtService ajeno = new JwtService("otra-clave-distinta-de-al-menos-32-bytes!!", "oficioya", 30);
        String token = ajeno.generar("pedro@example.com", "ADMIN");
        assertThrows(JwtException.class, () -> jwtService.validar(token));
    }
}
