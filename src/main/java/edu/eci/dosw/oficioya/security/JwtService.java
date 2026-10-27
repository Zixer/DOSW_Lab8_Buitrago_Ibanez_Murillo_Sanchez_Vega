package edu.eci.dosw.oficioya.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Servicio JWT (Persona B) - genera y valida tokens HS256.
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final String issuer;
    private final long ttlMillis;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.ttl-minutes}") long ttlMinutes) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.ttlMillis = ttlMinutes * 60_000L;
    }

    /** Genera un JWT firmado con correo como subject y rol como claim. */
    public String generar(String correo, String rol) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(correo)
                .claim("rol", rol)
                .issuer(issuer)
                .issuedAt(new Date(now))
                .expiration(new Date(now + ttlMillis))
                .signWith(key)
                .compact();
    }

    /** Devuelve el TTL configurado en segundos. */
    public long getTtlSeconds() {
        return ttlMillis / 1000L;
    }

    /** Extrae el subject (correo) del token. */
    public String extraerCorreo(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /** Valida que el token sea correcto y no haya expirado. */
    public boolean esValido(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
