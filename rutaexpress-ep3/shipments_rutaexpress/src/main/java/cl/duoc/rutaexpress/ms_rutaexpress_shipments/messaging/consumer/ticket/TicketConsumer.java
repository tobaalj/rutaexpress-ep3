package cl.duoc.rutaexpress.ms_rutaexpress_shipments.messaging.consumer.ticket;

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

/** Dominio: tickets/documentos. Genera el ticket de cada envío creado (shipment.created). */
@Component
public class TicketConsumer {

    private static final Logger log = LoggerFactory.getLogger(TicketConsumer.class);

    private final MessageProcessor processor;

    public TicketConsumer(MessageProcessor processor) {
        this.processor = processor;
    }

    @RabbitListener(queues = "${app.messaging.queues.tickets}")
    public void consume(Message message, Channel channel) throws IOException {
        processor.process(message, channel, ShipmentEvent.class, this::generateTicket);
    }

    private void generateTicket(ShipmentEvent event) {
        if (event.trackingNumber() == null || event.trackingNumber().isBlank()) {
            throw new NonRecoverableMessageException("El envío no tiene número de seguimiento");
        }
        // Aquí iría la generación real del documento (PDF); se simula con un log.
        log.info("Ticket TCK-{} generado para el envío {} (destino: {})",
                event.trackingNumber(), event.shipmentId(), event.recipientAddress());
    }
}
