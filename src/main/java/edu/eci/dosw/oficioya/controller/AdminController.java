
package edu.eci.dosw.oficioya.controller;

import edu.eci.dosw.oficioya.dto.PerfilAdmin;
import edu.eci.dosw.oficioya.service.UsuarioService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final UsuarioService usuarioService;

    public AdminController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/usuarios")
    public ResponseEntity<List<PerfilAdmin>> listarUsuarios() {

        List<PerfilAdmin> usuarios = usuarioService.listarParaAdmin();

        return ResponseEntity.ok(usuarios);
    }
}
