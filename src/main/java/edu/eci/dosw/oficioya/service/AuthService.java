package edu.eci.dosw.oficioya.service;

import edu.eci.dosw.oficioya.dto.LoginRequest;
import edu.eci.dosw.oficioya.dto.LoginResponse;
import edu.eci.dosw.oficioya.service.AuthService;

public interface AuthService {
    LoginResponse login(LoginRequest req);
}
