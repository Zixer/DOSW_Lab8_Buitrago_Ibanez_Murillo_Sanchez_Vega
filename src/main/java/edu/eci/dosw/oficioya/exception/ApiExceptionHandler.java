package edu.eci.dosw.oficioya.exception;

import edu.eci.dosw.oficioya.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(CorreoDuplicadoException.class)
    public ResponseEntity<ApiError> handleCorreoDuplicado(CorreoDuplicadoException ex, HttpServletRequest request) {
        ApiError err = ApiError.of(HttpStatus.CONFLICT.value(),
                                   HttpStatus.CONFLICT.getReasonPhrase(),
                                   ex.getMessage(),
                                   request.getRequestURI());
        return new ResponseEntity<>(err, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(RolNoPermitidoException.class)
    public ResponseEntity<ApiError> handleRolNoPermitido(RolNoPermitidoException ex, HttpServletRequest request) {
        ApiError err = ApiError.of(HttpStatus.BAD_REQUEST.value(),
                                   HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                   ex.getMessage(),
                                   request.getRequestURI());
        return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String mensaje = ex.getBindingResult()
                           .getFieldErrors()
                           .stream()
                           .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                           .reduce((a, b) -> a + "; " + b)
                           .orElse("Invalid request");
        ApiError err = ApiError.of(HttpStatus.BAD_REQUEST.value(),
                                   HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                   mensaje,
                                   request.getRequestURI());
        return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        ApiError err = ApiError.of(HttpStatus.BAD_REQUEST.value(),
                                   HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                   "Cuerpo de la solicitud inválido o formato no legible: " + ex.getMostSpecificCause().getMessage(),
                                   request.getRequestURI());
        return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        String mensaje = ex.getConstraintViolations()
                           .stream()
                           .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                           .reduce((a, b) -> a + "; " + b)
                           .orElse("Invalid parameters");
        ApiError err = ApiError.of(HttpStatus.BAD_REQUEST.value(),
                                   HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                   mensaje,
                                   request.getRequestURI());
        return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ApiError> handleCredencialesInvalidas(CredencialesInvalidasException ex, HttpServletRequest request) {
        ApiError err = ApiError.of(HttpStatus.UNAUTHORIZED.value(),
                                   HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                                   ex.getMessage(),
                                   request.getRequestURI());
        return new ResponseEntity<>(err, HttpStatus.UNAUTHORIZED);
    }

        @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiError> handleRecursoNoEncontrado(RecursoNoEncontradoException ex, HttpServletRequest request) {
        ApiError err = ApiError.of(HttpStatus.NOT_FOUND.value(),
                                   HttpStatus.NOT_FOUND.getReasonPhrase(),
                                   ex.getMessage(),
                                   request.getRequestURI());
        return new ResponseEntity<>(err, HttpStatus.NOT_FOUND);
    }

        @ExceptionHandler(EstadoInvalidoException.class)
    public ResponseEntity<ApiError> handleEstadoInvalido(EstadoInvalidoException ex, HttpServletRequest request) {
        ApiError err = ApiError.of(HttpStatus.CONFLICT.value(),
                                   HttpStatus.CONFLICT.getReasonPhrase(),
                                   ex.getMessage(),
                                   request.getRequestURI());
        return new ResponseEntity<>(err, HttpStatus.CONFLICT);
    }
}
