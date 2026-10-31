package cl.duoc.rutaexpress.ms_rutaexpress_shipments.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Nombres de exchanges y routing keys de los comandos que envíos le manda a ms-rutaexpress-notify
 * (prefijo app.messaging en application.yaml). Las colas y DLQs las declara el consumidor (notify).
 */
@ConfigurationProperties(prefix = "app.messaging")
public record MessagingProperties(
        Exchanges exchanges,
        RoutingKeys routingKeys,
        List<String> emailStatuses) {

    public MessagingProperties {
        emailStatuses = emailStatuses == null ? List.of() : List.copyOf(emailStatuses);
    }

    public record Exchanges(String direct, String topic) {}

    public record RoutingKeys(String emailSend, String warehouseTicket, String labelGen) {}
}
