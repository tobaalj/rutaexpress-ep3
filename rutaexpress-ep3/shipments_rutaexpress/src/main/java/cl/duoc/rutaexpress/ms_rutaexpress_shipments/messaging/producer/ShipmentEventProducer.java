package cl.duoc.rutaexpress.ms_rutaexpress_shipments.messaging.producer;

import cl.duoc.rutaexpress.ms_rutaexpress_shipments.config.MessagingProperties;
import cl.duoc.rutaexpress.ms_rutaexpress_shipments.dto.ShipmentDTO;
import cl.duoc.rutaexpress.ms_rutaexpress_shipments.messaging.event.ShipmentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Productor de eventos de envíos. Si RabbitMQ no está disponible el error solo se registra:
 * la lógica de negocio existente (guardar el envío) no se ve afectada.
 */
@Component
public class ShipmentEventProducer {

    private static final Logger log = LoggerFactory.getLogger(ShipmentEventProducer.class);

    private final RabbitTemplate rabbitTemplate;
    private final MessagingProperties props;

    public ShipmentEventProducer(RabbitTemplate rabbitTemplate, MessagingProperties props) {
        this.rabbitTemplate = rabbitTemplate;
        this.props = props;
    }

    public void publishCreated(ShipmentDTO shipment) {
        publish(props.routingKeys().shipmentCreated(), toEvent(ShipmentEvent.CREATED, shipment));
    }

    public void publishStatusChanged(ShipmentDTO shipment) {
        publish(props.routingKeys().shipmentStatusChanged(), toEvent(ShipmentEvent.STATUS_CHANGED, shipment));
    }

    private void publish(String routingKey, ShipmentEvent event) {
        try {
            rabbitTemplate.convertAndSend(props.exchange(), routingKey, event);
            log.info("Evento {} publicado (routingKey={}, tracking={})",
                    event.eventType(), routingKey, event.trackingNumber());
        } catch (AmqpException ex) {
            log.error("No se pudo publicar el evento {} del envío {}: {}",
                    event.eventType(), event.trackingNumber(), ex.getMessage());
        }
    }

    private ShipmentEvent toEvent(String type, ShipmentDTO s) {
        return new ShipmentEvent(type, s.getId(), s.getTrackingNumber(), s.getSenderEmail(),
                s.getRecipientAddress(), s.getStatus(), Instant.now());
    }
}
