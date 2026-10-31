package cl.duoc.rutaexpress.ms_rutaexpress_rabbitadmin.dto;

import java.time.Instant;
import java.util.List;

/** Respuesta de error uniforme de la API. */
public record ErrorResponseDTO(Instant timestamp, int status, String error, List<String> details) {}
