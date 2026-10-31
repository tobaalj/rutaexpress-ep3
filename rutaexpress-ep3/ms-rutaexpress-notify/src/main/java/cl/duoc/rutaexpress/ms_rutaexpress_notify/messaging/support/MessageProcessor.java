package cl.duoc.rutaexpress.ms_rutaexpress_notify.messaging.support;

import cl.duoc.rutaexpress.ms_rutaexpress_notify.config.MessagingProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Manejo de confirmaciones (ACK manual) común a todos los consumidores.
 *
 *  - Éxito                  -> basicAck.
 *  - Mensaje malformado     -> basicNack(requeue=false)  => DLQ.
 *  - Error no recuperable   -> basicNack(requeue=false)  => DLQ.
 *  - Error recuperable      -> se reintenta (máx. app.messaging.max-retries) republicando en la misma cola
 *                              con la cabecera x-retry-count; al agotar reintentos => DLQ.
 */
@Component
public class MessageProcessor {

    public static final String RETRY_HEADER = "x-retry-count";

    private static final Logger log = LoggerFactory.getLogger(MessageProcessor.class);

    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;
    private final MessagingProperties props;

    public MessageProcessor(ObjectMapper objectMapper, RabbitTemplate rabbitTemplate, MessagingProperties props) {
        this.objectMapper = objectMapper;
        this.rabbitTemplate = rabbitTemplate;
        this.props = props;
    }

    public <T> void process(Message message, Channel channel, Class<T> type, Consumer<T> handler) throws IOException {
        MessageProperties mp = message.getMessageProperties();
        long tag = mp.getDeliveryTag();
        String queue = mp.getConsumerQueue();

        T payload;
        try {
            payload = objectMapper.readValue(message.getBody(), type);
        } catch (IOException ex) {
            log.error("[{}] Mensaje malformado, se envía a DLQ: {}", queue, ex.getMessage());
            channel.basicNack(tag, false, false);
            return;
        }

        try {
            handler.accept(payload);
            channel.basicAck(tag, false);
        } catch (NonRecoverableMessageException ex) {
            log.error("[{}] Error no recuperable, se envía a DLQ: {}", queue, ex.getMessage());
            channel.basicNack(tag, false, false);
        } catch (RuntimeException ex) {
            retryOrDeadLetter(message, channel, ex);
        }
    }

    private void retryOrDeadLetter(Message message, Channel channel, RuntimeException cause) throws IOException {
        MessageProperties mp = message.getMessageProperties();
        long tag = mp.getDeliveryTag();
        String queue = mp.getConsumerQueue();
        int attempts = currentRetries(mp);

        if (attempts >= props.maxRetries()) {
            log.error("[{}] Reintentos agotados ({}), se envía a DLQ: {}", queue, attempts, cause.getMessage());
            channel.basicNack(tag, false, false);
            return;
        }

        try {
            Message retry = MessageBuilder.fromMessage(message).setHeader(RETRY_HEADER, attempts + 1).build();
            // Exchange por defecto: enruta solo a la cola que falló (evita duplicar en otros consumidores).
            rabbitTemplate.send("", queue, retry);
            channel.basicAck(tag, false);
            log.warn("[{}] Error recuperable, reintento {}/{}: {}", queue, attempts + 1, props.maxRetries(), cause.getMessage());
        } catch (AmqpException ex) {
            log.error("[{}] No se pudo reintentar, se envía a DLQ: {}", queue, ex.getMessage());
            channel.basicNack(tag, false, false);
        }
    }

    private int currentRetries(MessageProperties mp) {
        Object header = mp.getHeader(RETRY_HEADER);
        return header instanceof Number n ? n.intValue() : 0;
    }
}
