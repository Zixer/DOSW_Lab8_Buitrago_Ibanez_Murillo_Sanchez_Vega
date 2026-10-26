package edu.eci.dosw.oficioya.service;

import edu.eci.dosw.oficioya.dto.RegistroRequest;
import edu.eci.dosw.oficioya.dto.UsuarioResponse;
import edu.eci.dosw.oficioya.entity.Rol;
import edu.eci.dosw.oficioya.entity.Usuario;
import edu.eci.dosw.oficioya.exception.CorreoDuplicadoException;
import edu.eci.dosw.oficioya.exception.RolNoPermitidoException;
import edu.eci.dosw.oficioya.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UsuarioResponse registrar(RegistroRequest req) {
        // Normalizar email
        String email = req.correo().trim().toLowerCase();

        // Validar rol permitido (solo TRABAJADOR o CONTRATANTE)
        if (req.rol() != Rol.TRABAJADOR && req.rol() != Rol.CONTRATANTE) {
            throw new RolNoPermitidoException(req.rol().name());
        }

        // Verificar correo duplicado
        if (usuarioRepository.existsByCorreo(email)) {
            throw new CorreoDuplicadoException(email);
        }

        // Crear entidad Usuario
        Usuario usuario = new Usuario();
        usuario.setNombre(req.nombre());
        usuario.setCorreo(email);
        usuario.setTelefono(req.telefono());
        usuario.setRol(req.rol());
        usuario.setOficioPrincipal(req.oficioPrincipal());
        usuario.setZona(req.zona());
        usuario.setPasswordHash(passwordEncoder.encode(req.password()));

        Usuario guardado = usuarioRepository.save(usuario);

        return new UsuarioResponse(
                guardado.getId(),
                guardado.getNombre(),
                guardado.getCorreo(),
                guardado.getTelefono(),
                guardado.getRol().name(),
                guardado.getOficioPrincipal(),
                guardado.getZona()
        );
    }
}
