package edu.eci.dosw.oficioya.service;

import edu.eci.dosw.oficioya.dto.PerfilPublico;
import edu.eci.dosw.oficioya.dto.UsuarioMeResponse;
import edu.eci.dosw.oficioya.entity.Rol;
import edu.eci.dosw.oficioya.entity.Usuario;
import edu.eci.dosw.oficioya.repository.UsuarioRepository;
import edu.eci.dosw.oficioya.dto.PerfilAdmin;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import edu.eci.dosw.oficioya.exception.RecursoNoEncontradoException;

@Service
@Transactional(readOnly = true)
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UsuarioMeResponse obtenerMiPerfil(String correoAutenticado) {

                Usuario usuario = usuarioRepository.findByCorreo(correoAutenticado).orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        return new UsuarioMeResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getTelefono(),
                usuario.getRol().name(),
                usuario.getOficioPrincipal(),
                usuario.getZona()
        );
    }

    @Override
    public List<PerfilPublico> listarTrabajadores() {

        return usuarioRepository.findAllByRol(Rol.TRABAJADOR).stream().map(this::convertirAPerfilPublico).toList();
    }

    @Override
    public PerfilPublico obtenerTrabajador(Long id) {

        Usuario usuario = usuarioRepository.findById(id).filter(u -> u.getRol() == Rol.TRABAJADOR).orElseThrow(() -> new RecursoNoEncontradoException("Trabajador no encontrado"));
        return convertirAPerfilPublico(usuario);
    }

    private PerfilPublico convertirAPerfilPublico(Usuario usuario) {

        return new PerfilPublico(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getOficioPrincipal(),
                usuario.getZona()
        );
    }

    @Override
    public List<PerfilAdmin> listarParaAdmin() {

        return usuarioRepository.findAll().stream().map(this::convertirAPerfilAdmin).toList();
    }

    private PerfilAdmin convertirAPerfilAdmin(Usuario usuario) {

        return new PerfilAdmin(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getTelefono(),
                usuario.getRol().name(),
                usuario.getOficioPrincipal(),
                usuario.getZona()
        );
    }
}
