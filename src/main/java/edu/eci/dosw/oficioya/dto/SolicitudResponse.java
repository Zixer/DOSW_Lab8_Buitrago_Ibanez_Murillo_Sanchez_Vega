package edu.eci.dosw.oficioya.dto;

import java.time.LocalDateTime;

public record SolicitudResponse(Long id,
                                String descripcion,
                                String estado,
                                LocalDateTime fechaCreacion,
                                Long contratanteId,
                                String contratanteNombre,
                                Long trabajadorId,
                                String trabajadorNombre) {}