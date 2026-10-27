package edu.eci.dosw.oficioya.exception;

public class CorreoYaRegistradoException extends RuntimeException {
    public CorreoYaRegistradoException(String correo) {
        super("El correo ya está registrado: " + correo);
    }
}
