package cl.duoc.rutaexpress.ms_rutaexpress_notify.messaging.consumer.email;

import cl.duoc.rutaexpress.ms_rutaexpress_notify.messaging.event.ShipmentEvent;
import cl.duoc.rutaexpress.ms_rutaexpress_notify.messaging.support.MessageProcessor;
import cl.duoc.rutaexpress.ms_rutaexpress_notify.messaging.support.NonRecoverableMessageException;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Dominio: email/push al destinatario cuando el envío es aceptado, sale en ruta o se entrega. */
@Component
public class EmailConsumer {

    private static final Logger log = LoggerFactory.getLogger(EmailConsumer.class);

    private final MessageProcessor processor;

    public EmailConsumer(MessageProcessor processor) {
        this.processor = processor;
    }

    @RabbitListener(queues = "${app.messaging.queues.email}")
    public void consume(Message message, Channel channel) throws IOException {
        processor.process(message, channel, ShipmentEvent.class, this::sendEmail);
    }

    private void sendEmail(ShipmentEvent event) {
        String email = event.senderEmail();
        if (email == null || !email.contains("@")) {
            throw new NonRecoverableMessageException("Email inválido: " + email);
        }
        if (event.status() == null || event.status().isBlank()) {
            throw new NonRecoverableMessageException("El envío no tiene estado");
        }
        // Aquí iría la integración real (SMTP, SES, push...); se simula con un log.
        log.info("EMAIL enviado a {}: tu envío {} está en estado {}", email, event.trackingNumber(), event.status());
    }
}
