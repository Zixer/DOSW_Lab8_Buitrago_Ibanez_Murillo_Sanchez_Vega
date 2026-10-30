
package edu.eci.dosw.oficioya.config;

import edu.eci.dosw.oficioya.entity.Rol;
import edu.eci.dosw.oficioya.entity.Usuario;
import edu.eci.dosw.oficioya.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("dev")
public class AdminSeeder {

    @Bean
    public CommandLineRunner crearAdmin(UsuarioRepository usuarioRepository,PasswordEncoder passwordEncoder,@Value("${ADMIN_CORREO}") String correo,
                                        @Value("${ADMIN_PASSWORD}") String password) {

        return args -> {

            String correoNormalizado = correo.trim().toLowerCase();

            if (usuarioRepository.existsByCorreo(correoNormalizado)) {
                return;
            }

            Usuario admin = new Usuario();

            admin.setNombre("Administrador");
            admin.setCorreo(correoNormalizado);
            admin.setTelefono("0000000000");
            admin.setRol(Rol.ADMIN);

            admin.setPasswordHash(passwordEncoder.encode(password));

            usuarioRepository.save(admin);
        };
    }
}
