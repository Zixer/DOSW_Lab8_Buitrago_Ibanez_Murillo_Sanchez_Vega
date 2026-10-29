package edu.eci.dosw.oficioya.security;

import edu.eci.dosw.oficioya.entity.Rol;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Lee "Authorization: Bearer <token>", valida el JWT y deja al usuario en el SecurityContext
 * Si el token no es válido NO autentica: el AuthenticationEntryPoint responde 401
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String PREFIJO = "Bearer ";
    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService){
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException{
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if(header != null && header.regionMatches(true, 0, PREFIJO, 0, PREFIJO.length())){
            String token = header.substring(PREFIJO.length()).trim();
            try{
                Claims claims = jwtService.validar(token);

                String correo = claims.getSubject();
                String role = claims.get(JwtService.CLAIM_ROLE, String.class);
                if(correo == null || role == null){
                    throw new JwtException("Token sin sub o role");
                }
                Rol rol = Rol.valueOf(role);   //IllegalArgumentException si el rol no existe

                var autenticacion = new UsernamePasswordAuthenticationToken(correo, null, List.of(new SimpleGrantedAuthority("ROLE_" + rol.name())));
                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            }catch(JwtException | IllegalArgumentException e){
                //Sin autenticación - el punto de entrada responderá 401. No se registra el token
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}
