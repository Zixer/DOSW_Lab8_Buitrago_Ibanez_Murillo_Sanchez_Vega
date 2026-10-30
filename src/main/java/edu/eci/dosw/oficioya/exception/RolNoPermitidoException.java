package edu.eci.dosw.oficioya.exception;

public class RolNoPermitidoException extends RuntimeException {
    public RolNoPermitidoException(String rol) {
        super("Rol no permitido para registro: " + rol);
    }
}
