package cl.duoc.rutaexpress.ms_rutaexpress_catalog.messaging.consumer.dlq;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** Dominio: mensajes no entregados. Registra en logs todo lo que llega a la DLQ. */
@Component
public class DeadLetterConsumer {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterConsumer.class);

    @RabbitListener(queues = "${app.messaging.dead-letter-queues.audit}")
    public void consume(Message message, Channel channel) throws IOException {
        MessageProperties mp = message.getMessageProperties();
        log.error("[DLQ {}] Mensaje no entregado. x-death={} payload={}",
                mp.getConsumerQueue(),
                mp.getHeaders().get("x-death"),
                new String(message.getBody(), StandardCharsets.UTF_8));
        channel.basicAck(mp.getDeliveryTag(), false);
    }
}
