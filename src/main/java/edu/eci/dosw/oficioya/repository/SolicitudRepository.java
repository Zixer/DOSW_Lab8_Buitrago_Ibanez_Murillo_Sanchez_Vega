package edu.eci.dosw.oficioya.repository;

import edu.eci.dosw.oficioya.entity.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    // Consultas para contratante
    List<Solicitud> findAllByContratanteId(Long contratanteId);
    Optional<Solicitud> findByIdAndContratanteId(Long id, Long contratanteId);

    // Consultas para trabajador
    List<Solicitud> findAllByTrabajadorId(Long trabajadorId);
    Optional<Solicitud> findByIdAndTrabajadorId(Long id, Long trabajadorId);
}
