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
 * Productor de comandos asíncronos hacia ms-rutaexpress-notify.
 *
 *  - Envío creado        -> warehouse.ticket (ticket de picking) y label.gen (PDF de etiqueta).
 *  - Cambio de estado    -> email.send, solo si el nuevo estado está en app.messaging.email-statuses
 *                           (aceptado, en ruta, entregado).
 *
 * Si RabbitMQ no está disponible el error solo se registra: la lógica de negocio existente
 * (guardar el envío) no se ve afectada.
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
        ShipmentEvent event = toEvent(ShipmentEvent.CREATED, shipment);
        publish(props.routingKeys().warehouseTicket(), event);
        publish(props.routingKeys().labelGen(), event);
    }

    public void publishStatusChanged(ShipmentDTO shipment) {
        String status = shipment.getStatus() == null ? "" : shipment.getStatus().toUpperCase();
        if (props.emailStatuses().contains(status)) {
            publish(props.routingKeys().emailSend(), toEvent(ShipmentEvent.STATUS_CHANGED, shipment));
        } else {
            log.debug("Estado {} no requiere email (envío {})", status, shipment.getTrackingNumber());
        }
    }

    private void publish(String routingKey, ShipmentEvent event) {
        try {
            rabbitTemplate.convertAndSend(props.exchanges().direct(), routingKey, event);
            log.info("Comando {} publicado (routingKey={}, tracking={})",
                    event.eventType(), routingKey, event.trackingNumber());
        } catch (AmqpException ex) {
            log.error("No se pudo publicar {} (routingKey={}) del envío {}: {}",
                    event.eventType(), routingKey, event.trackingNumber(), ex.getMessage());
        }
    }

    private ShipmentEvent toEvent(String type, ShipmentDTO s) {
        return new ShipmentEvent(type, s.getId(), s.getTrackingNumber(), s.getSenderEmail(),
                s.getRecipientAddress(), s.getStatus(), Instant.now());
    }
}
