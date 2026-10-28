package cl.duoc.rutaexpress.ms_rutaexpress_shipments.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

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
}
