package cl.duoc.rutaexpress.ms_rutaexpress_rabbitadmin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Datos para crear una cola. */
public class QueueRequestDTO {

    @NotBlank(message = "El nombre de la cola es obligatorio")
    @Size(max = 255, message = "El nombre de la cola no puede superar 255 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9._:-]*$",
            message = "El nombre solo puede contener letras, números, '.', '_', ':' y '-'")
    private String name;

    private boolean durable = true;
    private boolean exclusive = false;
    private boolean autoDelete = false;

    @Size(max = 255, message = "El dead-letter exchange no puede superar 255 caracteres")
    private String deadLetterExchange;

    @Size(max = 255, message = "La dead-letter routing key no puede superar 255 caracteres")
    private String deadLetterRoutingKey;

    @Min(value = 1, message = "El TTL de mensajes debe ser mayor que 0 (ms)")
    private Long messageTtlMs;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isDurable() { return durable; }
    public void setDurable(boolean durable) { this.durable = durable; }

    public boolean isExclusive() { return exclusive; }
    public void setExclusive(boolean exclusive) { this.exclusive = exclusive; }

    public boolean isAutoDelete() { return autoDelete; }
    public void setAutoDelete(boolean autoDelete) { this.autoDelete = autoDelete; }

    public String getDeadLetterExchange() { return deadLetterExchange; }
    public void setDeadLetterExchange(String deadLetterExchange) { this.deadLetterExchange = deadLetterExchange; }

    public String getDeadLetterRoutingKey() { return deadLetterRoutingKey; }
    public void setDeadLetterRoutingKey(String deadLetterRoutingKey) { this.deadLetterRoutingKey = deadLetterRoutingKey; }

    public Long getMessageTtlMs() { return messageTtlMs; }
    public void setMessageTtlMs(Long messageTtlMs) { this.messageTtlMs = messageTtlMs; }
}
