package edu.eci.dosw.oficioya.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * 403 - el usuario esta autenticado pero no tiene permisos
 * JsonAccessDeniedHandler
 */
@Component 
public class JsonAccessDeniedHandler implements AccessDeniedHandler {
    private final SecurityErrorWriter errorWriter;

    public JsonAccessDeniedHandler(SecurityErrorWriter erorrWriter){
        this.errorWriter = erorrWriter;
    }

    @Override 
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex) throws IOException{
        errorWriter.escribir(request, response, HttpServletResponse.SC_FORBIDDEN, "Forbidden", "El usuario no tiene permiso para acceder a este recuroso");
    }
}

