package cl.duoc.rutaexpress.ms_rutaexpress_catalog.messaging.consumer.audit;

import cl.duoc.rutaexpress.ms_rutaexpress_catalog.messaging.event.CatalogItemEvent;
import cl.duoc.rutaexpress.ms_rutaexpress_catalog.messaging.support.MessageProcessor;
import cl.duoc.rutaexpress.ms_rutaexpress_catalog.messaging.support.NonRecoverableMessageException;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Dominio: auditoría del catálogo. Registra cada cambio publicado por el productor. */
@Component
public class CatalogAuditConsumer {

    private static final Logger log = LoggerFactory.getLogger(CatalogAuditConsumer.class);

    private final MessageProcessor processor;

    public CatalogAuditConsumer(MessageProcessor processor) {
        this.processor = processor;
    }

    @RabbitListener(queues = "${app.messaging.queues.audit}")
    public void consume(Message message, Channel channel) throws IOException {
        processor.process(message, channel, CatalogItemEvent.class, this::audit);
    }

    private void audit(CatalogItemEvent event) {
        if (event.itemId() == null || event.serviceName() == null || event.serviceName().isBlank()) {
            throw new NonRecoverableMessageException("Evento de catálogo incompleto: " + event);
        }
        log.info("AUDITORÍA catálogo: {} -> servicio '{}' (id={}, precio={}, capacidad={})",
                event.eventType(), event.serviceName(), event.itemId(), event.basePrice(), event.availableCapacity());
    }
}
