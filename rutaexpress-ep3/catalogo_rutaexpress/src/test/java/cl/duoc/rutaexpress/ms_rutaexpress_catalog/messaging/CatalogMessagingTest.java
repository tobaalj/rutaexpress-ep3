package cl.duoc.rutaexpress.ms_rutaexpress_catalog.messaging;

import cl.duoc.rutaexpress.ms_rutaexpress_catalog.config.MessagingProperties;
import cl.duoc.rutaexpress.ms_rutaexpress_catalog.config.RabbitMQConfig;
import cl.duoc.rutaexpress.ms_rutaexpress_catalog.dto.CatalogItemDTO;
import cl.duoc.rutaexpress.ms_rutaexpress_catalog.messaging.event.CatalogItemEvent;
import cl.duoc.rutaexpress.ms_rutaexpress_catalog.messaging.producer.CatalogEventProducer;
import cl.duoc.rutaexpress.ms_rutaexpress_catalog.messaging.support.MessageProcessor;
import cl.duoc.rutaexpress.ms_rutaexpress_catalog.messaging.support.NonRecoverableMessageException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CatalogMessagingTest {

    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    private final MessagingProperties props = new MessagingProperties("ex", "dlx", 2,
            new MessagingProperties.Queues("audit.q"),
            new MessagingProperties.DeadLetterQueues("audit.dlq"),
            new MessagingProperties.RoutingKeys("catalog.item.created", "catalog.#", "audit.dead"));

    private Message message(CatalogItemEvent event) throws Exception {
        MessageProperties mp = new MessageProperties();
        mp.setDeliveryTag(1L);
        mp.setConsumerQueue("audit.q");
        return new Message(MAPPER.writeValueAsBytes(event), mp);
    }

    @Test
    void auditQueueHasDeadLetterArguments() {
        Queue q = new RabbitMQConfig().auditQueue(props);

        assertEquals("dlx", q.getArguments().get("x-dead-letter-exchange"));
        assertEquals("audit.dead", q.getArguments().get("x-dead-letter-routing-key"));
    }

    @Test
    void producerPublishesAndSwallowsBrokerErrors() {
        RabbitTemplate template = mock(RabbitTemplate.class);
        CatalogEventProducer producer = new CatalogEventProducer(template, props);
        CatalogItemDTO dto = new CatalogItemDTO();
        dto.setId(1L);
        dto.setServiceName("Express");

        producer.publishCreated(dto);
        verify(template).convertAndSend(eq("ex"), eq("catalog.item.created"), any(CatalogItemEvent.class));

        doThrow(new AmqpException("broker caído"))
                .when(template).convertAndSend(eq("ex"), eq("catalog.item.created"), any(CatalogItemEvent.class));
        assertDoesNotThrow(() -> producer.publishCreated(dto));
    }

    @Test
    void invalidEventGoesToDlq() throws Exception {
        Channel channel = mock(Channel.class);
        MessageProcessor processor = new MessageProcessor(MAPPER, mock(RabbitTemplate.class), props);
        CatalogItemEvent bad = new CatalogItemEvent(CatalogItemEvent.CREATED, 1L, "", BigDecimal.TEN, 5, Instant.now());

        processor.process(message(bad), channel, CatalogItemEvent.class, e -> {
            throw new NonRecoverableMessageException("evento inválido");
        });

        verify(channel).basicNack(1L, false, false);
    }
}
