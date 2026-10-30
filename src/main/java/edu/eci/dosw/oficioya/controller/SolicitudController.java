package edu.eci.dosw.oficioya.controller;

import edu.eci.dosw.oficioya.dto.SolicitudRequest;
import edu.eci.dosw.oficioya.dto.SolicitudResponse;
import edu.eci.dosw.oficioya.service.SolicitudService;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/solicitudes")
public class SolicitudController {

    private final SolicitudService solicitudService;

    public SolicitudController(SolicitudService solicitudService) {
        this.solicitudService = solicitudService;
    }

    @PreAuthorize("hasRole('CONTRATANTE')")
    @PostMapping
    public ResponseEntity<SolicitudResponse> crear(@Valid @RequestBody SolicitudRequest request, Authentication authentication) {

        SolicitudResponse creada = solicitudService.crear(request, authentication.getName());

        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PreAuthorize("hasAnyRole('CONTRATANTE', 'TRABAJADOR')")
    @GetMapping
    public ResponseEntity<List<SolicitudResponse>> listarMias(Authentication authentication) {

        return ResponseEntity.ok(solicitudService.listarMias(authentication.getName()));
    }

    @PreAuthorize("hasAnyRole('CONTRATANTE', 'TRABAJADOR')")
    @GetMapping("/{id}")
    public ResponseEntity<SolicitudResponse> obtener(@PathVariable Long id, Authentication authentication) {

        return ResponseEntity.ok(solicitudService.obtener(id, authentication.getName()));
    }

    @PreAuthorize("hasRole('TRABAJADOR')")
    @PatchMapping("/{id}/aceptar")
    public ResponseEntity<SolicitudResponse> aceptar(@PathVariable Long id, Authentication authentication) {

        return ResponseEntity.ok(solicitudService.aceptar(id, authentication.getName()));
    }

    @PreAuthorize("hasRole('TRABAJADOR')")
    @PatchMapping("/{id}/rechazar")
    public ResponseEntity<SolicitudResponse> rechazar(@PathVariable Long id, Authentication authentication) {

        return ResponseEntity.ok(solicitudService.rechazar(id, authentication.getName()));
    }

    @PreAuthorize("hasRole('CONTRATANTE')")
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<SolicitudResponse> cancelar(@PathVariable Long id, Authentication authentication) {

        return ResponseEntity.ok(solicitudService.cancelar(id, authentication.getName()));
    }
}