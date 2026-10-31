package cl.duoc.rutaexpress.ms_rutaexpress_rabbitadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Datos para crear un exchange. */
public class ExchangeRequestDTO {

    @NotBlank(message = "El nombre del exchange es obligatorio")
    @Size(max = 255, message = "El nombre del exchange no puede superar 255 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9._:-]*$",
            message = "El nombre solo puede contener letras, números, '.', '_', ':' y '-'")
    private String name;

    @NotNull(message = "El tipo de exchange es obligatorio (DIRECT, TOPIC, FANOUT o HEADERS)")
    private ExchangeType type;

    private boolean durable = true;
    private boolean autoDelete = false;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public ExchangeType getType() { return type; }
    public void setType(ExchangeType type) { this.type = type; }

    public boolean isDurable() { return durable; }
    public void setDurable(boolean durable) { this.durable = durable; }

    public boolean isAutoDelete() { return autoDelete; }
    public void setAutoDelete(boolean autoDelete) { this.autoDelete = autoDelete; }
}
