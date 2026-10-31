package cl.duoc.rutaexpress.ms_rutaexpress_notify.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Nombres de exchanges, colas, DLQs y routing keys (prefijo app.messaging en application.yaml).
 * No hay ningún nombre escrito en el código.
 */
@ConfigurationProperties(prefix = "app.messaging")
public record MessagingProperties(
        int maxRetries,
        Exchanges exchanges,
        Names queues,
        Names deadLetterQueues,
        Names directKeys,
        Names topicKeys) {

    public record Exchanges(String direct, String topic, String deadLetter) {}

    /** Un valor por cada flujo funcional. */
    public record Names(String email, String warehouse, String label) {}
}
