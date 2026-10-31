package cl.duoc.rutaexpress.ms_rutaexpress_catalog.messaging.producer;

import cl.duoc.rutaexpress.ms_rutaexpress_catalog.config.MessagingProperties;
import cl.duoc.rutaexpress.ms_rutaexpress_catalog.dto.CatalogItemDTO;
import cl.duoc.rutaexpress.ms_rutaexpress_catalog.messaging.event.CatalogItemEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

/** Productor de eventos del catálogo. Un fallo del broker solo se registra; no afecta al negocio. */
@Component
public class CatalogEventProducer {

    private static final Logger log = LoggerFactory.getLogger(CatalogEventProducer.class);

    private final RabbitTemplate rabbitTemplate;
    private final MessagingProperties props;

    public CatalogEventProducer(RabbitTemplate rabbitTemplate, MessagingProperties props) {
        this.rabbitTemplate = rabbitTemplate;
        this.props = props;
    }

    public void publishCreated(CatalogItemDTO item) {
        CatalogItemEvent event = new CatalogItemEvent(CatalogItemEvent.CREATED, item.getId(),
                item.getServiceName(), item.getBasePrice(), item.getAvailableCapacity(), Instant.now());
        try {
            rabbitTemplate.convertAndSend(props.exchange(), props.routingKeys().itemCreated(), event);
            log.info("Evento {} publicado (itemId={})", event.eventType(), event.itemId());
        } catch (AmqpException ex) {
            log.error("No se pudo publicar el evento {} del servicio {}: {}",
                    event.eventType(), event.itemId(), ex.getMessage());
        }
    }
}
