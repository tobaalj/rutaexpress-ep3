package cl.duoc.rutaexpress.ms_rutaexpress_rabbitadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Datos para crear un binding exchange -> cola. */
public class BindingRequestDTO {

    @NotBlank(message = "La cola es obligatoria")
    @Size(max = 255, message = "El nombre de la cola no puede superar 255 caracteres")
    private String queue;

    @NotBlank(message = "El exchange es obligatorio")
    @Size(max = 255, message = "El nombre del exchange no puede superar 255 caracteres")
    private String exchange;

    @Size(max = 255, message = "La routing key no puede superar 255 caracteres")
    private String routingKey = "";

    public String getQueue() { return queue; }
    public void setQueue(String queue) { this.queue = queue; }

    public String getExchange() { return exchange; }
    public void setExchange(String exchange) { this.exchange = exchange; }

    public String getRoutingKey() { return routingKey; }
    public void setRoutingKey(String routingKey) { this.routingKey = routingKey; }
}
