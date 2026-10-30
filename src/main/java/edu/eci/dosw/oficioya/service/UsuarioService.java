
package edu.eci.dosw.oficioya.service;

import edu.eci.dosw.oficioya.dto.UsuarioMeResponse;
import edu.eci.dosw.oficioya.dto.PerfilPublico;
import edu.eci.dosw.oficioya.dto.PerfilAdmin;

import java.util.List;

public interface UsuarioService {

    UsuarioMeResponse obtenerMiPerfil(String correoAutenticado);

    List<PerfilPublico> listarTrabajadores();

    PerfilPublico obtenerTrabajador(Long id);

    List<PerfilAdmin> listarParaAdmin();
}
