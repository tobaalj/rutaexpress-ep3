package cl.duoc.rutaexpress.ms_rutaexpress_catalog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Nombres de exchanges, colas, DLQs y routing keys (prefijo app.messaging en application.yaml). */
@ConfigurationProperties(prefix = "app.messaging")
public record MessagingProperties(
        String exchange,
        String deadLetterExchange,
        int maxRetries,
        Queues queues,
        DeadLetterQueues deadLetterQueues,
        RoutingKeys routingKeys) {

    public record Queues(String audit) {}

    public record DeadLetterQueues(String audit) {}

    public record RoutingKeys(String itemCreated, String auditBinding, String auditDead) {}
}
