package edu.eci.dosw.oficioya.exception;

import edu.eci.dosw.oficioya.dto.ApiError;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(CorreoDuplicadoException.class)
    public ResponseEntity<ApiError> handleCorreoDuplicado(CorreoDuplicadoException ex, WebRequest request) {
        ApiError err = ApiError.of(HttpStatus.CONFLICT.value(),
                                   HttpStatus.CONFLICT.getReasonPhrase(),
                                   ex.getMessage(),
                                   request.getDescription(false));
        return new ResponseEntity<>(err, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(RolNoPermitidoException.class)
    public ResponseEntity<ApiError> handleRolNoPermitido(RolNoPermitidoException ex, WebRequest request) {
        ApiError err = ApiError.of(HttpStatus.BAD_REQUEST.value(),
                                   HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                   ex.getMessage(),
                                   request.getDescription(false));
        return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        String mensaje = ex.getBindingResult()
                           .getFieldErrors()
                           .stream()
                           .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                           .reduce((a, b) -> a + "; " + b)
                           .orElse("Invalid request");
        ApiError err = ApiError.of(HttpStatus.BAD_REQUEST.value(),
                                   HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                   mensaje,
                                   request.getDescription(false));
        return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        String mensaje = ex.getConstraintViolations()
                           .stream()
                           .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                           .reduce((a, b) -> a + "; " + b)
                           .orElse("Invalid parameters");
        ApiError err = ApiError.of(HttpStatus.BAD_REQUEST.value(),
                                   HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                   mensaje,
                                   request.getDescription(false));
        return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
    }
}
