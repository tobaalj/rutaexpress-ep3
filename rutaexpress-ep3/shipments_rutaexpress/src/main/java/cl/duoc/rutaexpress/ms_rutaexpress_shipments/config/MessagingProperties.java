package cl.duoc.rutaexpress.ms_rutaexpress_shipments.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

<<<<<<< HEAD
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
=======
/**
 * Nombres de exchanges, colas, DLQs y routing keys.
 * Todo se define en application.yaml (prefijo app.messaging); no hay nombres escritos en el código.
 */
@ConfigurationProperties(prefix = "app.messaging")
public record MessagingProperties(
        String exchange,
        String deadLetterExchange,
        int maxRetries,
        Queues queues,
        DeadLetterQueues deadLetterQueues,
        RoutingKeys routingKeys) {

    public record Queues(String notifications, String tickets) {}

    public record DeadLetterQueues(String notifications, String tickets) {}

    public record RoutingKeys(
            String shipmentCreated,
            String shipmentStatusChanged,
            String notificationsBinding,
            String ticketsBinding,
            String notificationsDead,
            String ticketsDead) {}
>>>>>>> d39a0dbc18aebda18ac1db90e44f7b6a6d865fab
}
