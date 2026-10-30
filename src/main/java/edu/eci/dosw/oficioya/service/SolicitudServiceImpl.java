package edu.eci.dosw.oficioya.service;

import edu.eci.dosw.oficioya.dto.SolicitudRequest;
import edu.eci.dosw.oficioya.dto.SolicitudResponse;
import edu.eci.dosw.oficioya.entity.EstadoSolicitud;
import edu.eci.dosw.oficioya.entity.Rol;
import edu.eci.dosw.oficioya.entity.Solicitud;
import edu.eci.dosw.oficioya.entity.Usuario;
import edu.eci.dosw.oficioya.exception.EstadoInvalidoException;
import edu.eci.dosw.oficioya.exception.RecursoNoEncontradoException;
import edu.eci.dosw.oficioya.repository.SolicitudRepository;
import edu.eci.dosw.oficioya.repository.UsuarioRepository;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SolicitudServiceImpl implements SolicitudService {

    private static final String NO_ENCONTRADA = "Solicitud no encontrada";
    private static final Set<EstadoSolicitud> CANCELABLES = EnumSet.of(EstadoSolicitud.PENDIENTE, EstadoSolicitud.ACEPTADA);

    private final SolicitudRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;

    public SolicitudServiceImpl(SolicitudRepository solicitudRepository, UsuarioRepository usuarioRepository) {
        this.solicitudRepository = solicitudRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional
    public SolicitudResponse crear(SolicitudRequest request, String correoAutenticado) {

        Usuario contratante = usuarioActual(correoAutenticado);
        Usuario trabajador = usuarioRepository.findById(request.trabajadorId()).filter(u -> u.getRol() == Rol.TRABAJADOR).orElseThrow(() -> new RecursoNoEncontradoException("Trabajador no encontrado"));

        Solicitud solicitud = new Solicitud();
        solicitud.setDescripcion(request.descripcion().trim());
        solicitud.setContratante(contratante);
        solicitud.setTrabajador(trabajador);

        return convertirAResponse(solicitudRepository.save(solicitud));
    }

    @Override
    public List<SolicitudResponse> listarMias(String correoAutenticado) {

        Usuario actual = usuarioActual(correoAutenticado);

        List<Solicitud> solicitudes = switch (actual.getRol()) {
            case CONTRATANTE -> solicitudRepository.findAllByContratanteId(actual.getId());
            case TRABAJADOR -> solicitudRepository.findAllByTrabajadorId(actual.getId());
            default -> List.of();
        };

        return solicitudes.stream().map(this::convertirAResponse).toList();
    }

    @Override
    public SolicitudResponse obtener(Long id, String correoAutenticado) {

        Usuario actual = usuarioActual(correoAutenticado);

        return solicitudRepository.findByIdAndParticipante(id, actual.getId()).map(this::convertirAResponse).orElseThrow(() -> new RecursoNoEncontradoException(NO_ENCONTRADA));
    }

    @Override
    @Transactional
    public SolicitudResponse aceptar(Long id, String correoAutenticado) {

        Solicitud solicitud = asignadaAlTrabajador(id, correoAutenticado);
        exigirPendiente(solicitud, "Solo se puede aceptar una solicitud pendiente");
        solicitud.setEstado(EstadoSolicitud.ACEPTADA);

        return convertirAResponse(solicitudRepository.save(solicitud));
    }

    @Override
    @Transactional
    public SolicitudResponse rechazar(Long id, String correoAutenticado) {

        Solicitud solicitud = asignadaAlTrabajador(id, correoAutenticado);
        exigirPendiente(solicitud, "Solo se puede rechazar una solicitud pendiente");
        solicitud.setEstado(EstadoSolicitud.RECHAZADA);

        return convertirAResponse(solicitudRepository.save(solicitud));
    }

    @Override
    @Transactional
    public SolicitudResponse cancelar(Long id, String correoAutenticado) {

        Usuario actual = usuarioActual(correoAutenticado);
        Solicitud solicitud = solicitudRepository.findByIdAndContratanteId(id, actual.getId()).orElseThrow(() -> new RecursoNoEncontradoException(NO_ENCONTRADA));

        if (!CANCELABLES.contains(solicitud.getEstado())) {
            throw new EstadoInvalidoException("Solo se puede cancelar una solicitud pendiente o aceptada");
        }
        solicitud.setEstado(EstadoSolicitud.CANCELADA);

        return convertirAResponse(solicitudRepository.save(solicitud));
    }

    private Usuario usuarioActual(String correoAutenticado) {

        return usuarioRepository.findByCorreo(correoAutenticado).orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    private Solicitud asignadaAlTrabajador(Long id, String correoAutenticado) {

        Usuario actual = usuarioActual(correoAutenticado);

        return solicitudRepository.findByIdAndTrabajadorId(id, actual.getId()).orElseThrow(() -> new RecursoNoEncontradoException(NO_ENCONTRADA));
    }

    private void exigirPendiente(Solicitud solicitud, String mensaje) {

        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new EstadoInvalidoException(mensaje);
        }
    }

    private SolicitudResponse convertirAResponse(Solicitud solicitud) {

        return new SolicitudResponse(
                solicitud.getId(),
                solicitud.getDescripcion(),
                solicitud.getEstado().name(),
                solicitud.getFechaCreacion(),
                solicitud.getContratante().getId(),
                solicitud.getContratante().getNombre(),
                solicitud.getTrabajador().getId(),
                solicitud.getTrabajador().getNombre()
        );
    }
}