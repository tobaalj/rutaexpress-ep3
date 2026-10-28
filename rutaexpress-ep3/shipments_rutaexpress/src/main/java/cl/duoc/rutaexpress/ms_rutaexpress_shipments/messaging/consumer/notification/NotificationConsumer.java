package cl.duoc.rutaexpress.ms_rutaexpress_shipments.messaging.consumer.notification;

import cl.duoc.rutaexpress.ms_rutaexpress_shipments.messaging.event.ShipmentEvent;
import cl.duoc.rutaexpress.ms_rutaexpress_shipments.messaging.support.MessageProcessor;
import cl.duoc.rutaexpress.ms_rutaexpress_shipments.messaging.support.NonRecoverableMessageException;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Dominio: notificaciones. Consume todos los eventos de envíos (shipment.#). */
@Component
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    private final MessageProcessor processor;

    public NotificationConsumer(MessageProcessor processor) {
        this.processor = processor;
    }

    @RabbitListener(queues = "${app.messaging.queues.notifications}")
    public void consume(Message message, Channel channel) throws IOException {
        processor.process(message, channel, ShipmentEvent.class, this::notifySender);
    }

    private void notifySender(ShipmentEvent event) {
        String email = event.senderEmail();
        if (email == null || !email.contains("@")) {
            throw new NonRecoverableMessageException("Email del remitente inválido: " + email);
        }
        // Aquí iría la integración real (SMTP, SES, etc.); se simula con un log.
        log.info("Notificación enviada a {}: envío {} -> estado {}", email, event.trackingNumber(), event.status());
    }
}
