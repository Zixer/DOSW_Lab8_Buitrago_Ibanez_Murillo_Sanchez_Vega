package edu.eci.dosw.oficioya.service;

import edu.eci.dosw.oficioya.dto.SolicitudRequest;
import edu.eci.dosw.oficioya.dto.SolicitudResponse;

import java.util.List;

public interface SolicitudService {

    SolicitudResponse crear(SolicitudRequest request, String correoAutenticado);

    List<SolicitudResponse> listarMias(String correoAutenticado);

    SolicitudResponse obtener(Long id, String correoAutenticado);

    SolicitudResponse aceptar(Long id, String correoAutenticado);

    SolicitudResponse rechazar(Long id, String correoAutenticado);

    SolicitudResponse cancelar(Long id, String correoAutenticado);
}