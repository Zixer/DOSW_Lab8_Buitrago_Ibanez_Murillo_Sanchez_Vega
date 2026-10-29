package edu.eci.dosw.oficioya.controller;

import edu.eci.dosw.oficioya.dto.UsuarioMeResponse;
import edu.eci.dosw.oficioya.service.UsuarioService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioMeResponse> obtenerMiPerfil(Authentication authentication) {

        String correoAutenticado = authentication.getName();

        UsuarioMeResponse perfil = usuarioService.obtenerMiPerfil(correoAutenticado);

        return ResponseEntity.ok(perfil);
    }
}
