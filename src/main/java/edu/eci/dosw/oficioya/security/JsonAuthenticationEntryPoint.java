package edu.eci.dosw.oficioya.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * 401 - no hay token o esta invalido/invalido/vencido
 */
@Component 
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint{
    private final SecurityErrorWriter errorWriter;

    public JsonAuthenticationEntryPoint(SecurityErrorWriter errorWriter){
        this.errorWriter = errorWriter;
    }

    @Override 
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex) throws IOException{
        errorWriter.escribir(request, response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", "Token ausente, inválido o vencido");
    }
    
}
