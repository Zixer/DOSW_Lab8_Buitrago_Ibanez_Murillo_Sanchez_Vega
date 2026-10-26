package edu.eci.dosw.oficioya.repository;

import edu.eci.dosw.oficioya.entity.Usuario;
import edu.eci.dosw.oficioya.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    List<Usuario> findAllByRol(Rol rol);
}
