package cl.duoc.rutaexpress.ms_rutaexpress_notify.messaging.consumer.label;

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

/** Dominio: documentos. Genera el PDF de la guía de despacho / etiqueta. */
@Component
public class LabelConsumer {

    private static final Logger log = LoggerFactory.getLogger(LabelConsumer.class);

    private final MessageProcessor processor;

    public LabelConsumer(MessageProcessor processor) {
        this.processor = processor;
    }

    @RabbitListener(queues = "${app.messaging.queues.label}")
    public void consume(Message message, Channel channel) throws IOException {
        processor.process(message, channel, ShipmentEvent.class, this::generateLabel);
    }

    private void generateLabel(ShipmentEvent event) {
        if (event.trackingNumber() == null || event.trackingNumber().isBlank()) {
            throw new NonRecoverableMessageException("El envío no tiene número de seguimiento");
        }
        if (event.recipientAddress() == null || event.recipientAddress().isBlank()) {
            throw new NonRecoverableMessageException("El envío no tiene dirección de destino");
        }
        // Aquí iría la generación real del PDF; se simula con un log.
        log.info("PDF de etiqueta guia-{}.pdf generado (destino: {})", event.trackingNumber(), event.recipientAddress());
    }
}
