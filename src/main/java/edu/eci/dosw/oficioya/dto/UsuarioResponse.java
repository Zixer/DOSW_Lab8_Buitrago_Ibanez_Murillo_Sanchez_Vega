package edu.eci.dosw.oficioya.dto;

public record UsuarioResponse(Long id,
                              String nombre,
                              String correo,
                              String telefono,
                              String rol,
                              String oficioPrincipal,
                              String zona) {}
