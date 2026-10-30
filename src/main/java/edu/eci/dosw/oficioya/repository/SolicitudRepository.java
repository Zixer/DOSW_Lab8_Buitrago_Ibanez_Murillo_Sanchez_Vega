package edu.eci.dosw.oficioya.repository;

import edu.eci.dosw.oficioya.entity.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // Consulta para cualquiera de los dos participantes (GET /solicitudes/{id})
    @Query("SELECT s FROM Solicitud s WHERE s.id = :id AND (s.contratante.id = :usuarioId OR s.trabajador.id = :usuarioId)")
    Optional<Solicitud> findByIdAndParticipante(@Param("id") Long id, @Param("usuarioId") Long usuarioId);
}