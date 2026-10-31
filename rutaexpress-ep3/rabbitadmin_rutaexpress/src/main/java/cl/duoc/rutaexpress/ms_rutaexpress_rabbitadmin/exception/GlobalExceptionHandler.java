package cl.duoc.rutaexpress.ms_rutaexpress_rabbitadmin.exception;

import cl.duoc.rutaexpress.ms_rutaexpress_rabbitadmin.dto.ErrorResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.AmqpIOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Parámetros inválidos", details);
    }

    @ExceptionHandler(InvalidRabbitRequestException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalid(InvalidRabbitRequestException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), List.of());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), List.of());
    }

    @ExceptionHandler(AmqpConnectException.class)
    public ResponseEntity<ErrorResponseDTO> handleConnection(AmqpConnectException ex) {
        log.error("RabbitMQ no disponible: {}", ex.getMessage());
        return build(HttpStatus.SERVICE_UNAVAILABLE, "RabbitMQ no está disponible", List.of());
    }

    // Ej.: redeclarar una cola existente con argumentos distintos (PRECONDITION_FAILED)
    @ExceptionHandler(AmqpIOException.class)
    public ResponseEntity<ErrorResponseDTO> handleConflict(AmqpIOException ex) {
        log.warn("Conflicto en RabbitMQ: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT,
                "RabbitMQ rechazó la operación (¿el recurso ya existe con otra configuración?)", List.of());
    }

    @ExceptionHandler(AmqpException.class)
    public ResponseEntity<ErrorResponseDTO> handleAmqp(AmqpException ex) {
        log.error("Error de RabbitMQ: {}", ex.getMessage());
        return build(HttpStatus.BAD_GATEWAY, "Error al comunicarse con RabbitMQ", List.of());
    }

    private ResponseEntity<ErrorResponseDTO> build(HttpStatus status, String message, List<String> details) {
        List<String> all = details.isEmpty() ? List.of(message) : details;
        return ResponseEntity.status(status)
                .body(new ErrorResponseDTO(Instant.now(), status.value(), message, all));
    }
}
