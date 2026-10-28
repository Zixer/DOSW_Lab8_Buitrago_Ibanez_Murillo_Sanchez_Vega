package edu.eci.dosw.oficioya.service;

import edu.eci.dosw.oficioya.dto.RegistroRequest;
import edu.eci.dosw.oficioya.dto.UsuarioResponse;

public interface AuthService {
    UsuarioResponse registrar(RegistroRequest request);
}
