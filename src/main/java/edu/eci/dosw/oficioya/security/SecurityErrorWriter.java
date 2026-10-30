package edu.eci.dosw.oficioya.security;

import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import edu.eci.dosw.oficioya.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.MediaType;

/**
 * Escribe un ApiError como json en la respuesta HTTP cuando hay un error de seguridad (401 o 403)
 * SecurityErrorWriter
 */
@Component 
public class SecurityErrorWriter {
    private final ObjectMapper objectMapper;

    public SecurityErrorWriter(ObjectMapper objectMapper){
        this.objectMapper = objectMapper;
    }

    public void escribir(HttpServletRequest request, HttpServletResponse response, 
                        int status, String error, String mensaje) throws IOException{
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), ApiError.of(status, error, mensaje, request.getRequestURI()));
    }
}

