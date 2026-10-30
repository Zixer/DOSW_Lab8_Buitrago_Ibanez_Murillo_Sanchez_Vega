package edu.eci.dosw.oficioya.controller;

import edu.eci.dosw.oficioya.dto.PerfilPublico;
import edu.eci.dosw.oficioya.service.UsuarioService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/trabajadores")
public class TrabajadorController {

    private final UsuarioService usuarioService;

    public TrabajadorController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<PerfilPublico>> listarTrabajadores() {

        List<PerfilPublico> trabajadores =usuarioService.listarTrabajadores();

        return ResponseEntity.ok(trabajadores);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PerfilPublico> obtenerTrabajador(
            @PathVariable Long id) {

        PerfilPublico trabajador = usuarioService.obtenerTrabajador(id);

        return ResponseEntity.ok(trabajador);
    }
}
