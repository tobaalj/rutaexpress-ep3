package cl.duoc.rutaexpress.ms_rutaexpress_notify.messaging.consumer.warehouse;

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

/** Dominio: bodega. Genera el ticket de picking / etiqueta de bodega de cada envío. */
@Component
public class WarehouseConsumer {

    private static final Logger log = LoggerFactory.getLogger(WarehouseConsumer.class);

    private final MessageProcessor processor;

    public WarehouseConsumer(MessageProcessor processor) {
        this.processor = processor;
    }

    @RabbitListener(queues = "${app.messaging.queues.warehouse}")
    public void consume(Message message, Channel channel) throws IOException {
        processor.process(message, channel, ShipmentEvent.class, this::createPickingTicket);
    }

    private void createPickingTicket(ShipmentEvent event) {
        if (event.trackingNumber() == null || event.trackingNumber().isBlank()) {
            throw new NonRecoverableMessageException("El envío no tiene número de seguimiento");
        }
        log.info("TICKET de picking PK-{} generado para el envío {} (destino: {})",
                event.trackingNumber(), event.shipmentId(), event.recipientAddress());
    }
}
