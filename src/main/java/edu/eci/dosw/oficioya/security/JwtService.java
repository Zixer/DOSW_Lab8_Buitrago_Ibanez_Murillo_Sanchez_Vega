package edu.eci.dosw.oficioya.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Genera y valida los JWT de OficioYa
 * La clave llega por la variable de entorno JWT_SECRET
 */
@Service
public class JwtService{
    private static final int MIN_SECRET_BYTES = 32;
    private static final String ALGORITMO = "HS256";
    public static final String CLAIM_ROLE = "role";

    private final SecretKey key;
    private final String issuer;
    private final long ttlMinutes;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.issuer}") String issuer,
                      @Value("${app.jwt.ttl-minutes}") long ttlMinutes){
        byte[] bytes = secret == null ? new byte[0] : secret.getBytes(StandardCharsets.UTF_8);
        if(bytes.length < MIN_SECRET_BYTES){
            throw new IllegalStateException("JWT_SECRET debe tener al menos " + MIN_SECRET_BYTES + " bytes"); //No se imprime la clave en el mensaje
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.issuer = issuer;
        this.ttlMinutes = ttlMinutes;
    }

    /** 
     * Genera un token para el correo y el rol 
     */
    public String generar(String correo, String rol){
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(correo)
                .claim(CLAIM_ROLE, rol)
                .issuer(issuer)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(ttlMinutes, ChronoUnit.MINUTES)))
                .signWith(key, Jwts.SIG.HS256)   //fija HS256 sin importar el largo de la clave
                .compact();
    }

    /**
     * Valida firma, expiración y emisor. Lanza JwtException si el token no es aceptable
     * o IllegalArgumentException si es nulo o vacio
     */
    public Claims validar(String token){
        Jws<Claims> jws = Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token);

        if(!ALGORITMO.equals(jws.getHeader().getAlgorithm())){
            throw new JwtException("Algoritmo no permitido");
        }
        return jws.getPayload();
    }

    /** 
     * Vigencia en segundos, para el campo "expiraEn" de la respuesta de login. 
     */
    public long getTtlSeconds(){
        return ttlMinutes * 60;
    }
}