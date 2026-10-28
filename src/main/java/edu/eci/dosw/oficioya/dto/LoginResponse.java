package edu.eci.dosw.oficioya.dto;

public record LoginResponse(
        String tokenType,
        String accessToken,
        long expiresIn
) {}