package edu.eci.dosw.oficioya.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import edu.eci.dosw.oficioya.entity.Rol;

public record RegistroRequest(
        @NotBlank String nombre,
        @NotBlank @Email String correo,
        @NotBlank @Size(min = 8) String password,
        @NotNull Rol rol,
        String oficioPrincipal,
        String zona) {}
