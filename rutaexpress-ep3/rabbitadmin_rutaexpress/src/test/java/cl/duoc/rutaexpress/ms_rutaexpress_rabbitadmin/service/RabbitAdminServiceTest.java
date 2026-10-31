package cl.duoc.rutaexpress.ms_rutaexpress_rabbitadmin.service;

import cl.duoc.rutaexpress.ms_rutaexpress_rabbitadmin.config.RabbitAdminProperties;
import cl.duoc.rutaexpress.ms_rutaexpress_rabbitadmin.dto.BindingRequestDTO;
import cl.duoc.rutaexpress.ms_rutaexpress_rabbitadmin.dto.ExchangeRequestDTO;
import cl.duoc.rutaexpress.ms_rutaexpress_rabbitadmin.dto.ExchangeType;
import cl.duoc.rutaexpress.ms_rutaexpress_rabbitadmin.dto.QueueRequestDTO;
import cl.duoc.rutaexpress.ms_rutaexpress_rabbitadmin.exception.InvalidRabbitRequestException;
import cl.duoc.rutaexpress.ms_rutaexpress_rabbitadmin.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueInformation;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RabbitAdminServiceTest {

    private AmqpAdmin amqpAdmin;
    private RabbitAdminService service;

    @BeforeEach
    void setUp() {
        amqpAdmin = mock(AmqpAdmin.class);
        service = new RabbitAdminService(amqpAdmin, new RabbitAdminProperties(List.of("amq.")));
    }

    private QueueRequestDTO queue(String name) {
        QueueRequestDTO dto = new QueueRequestDTO();
        dto.setName(name);
        return dto;
    }

    @Test
    void createQueueDeclaresQueueWithDeadLetterArguments() {
        QueueRequestDTO dto = queue("orders.queue");
        dto.setDeadLetterExchange("orders.dlx");
        dto.setDeadLetterRoutingKey("orders.dead");
        dto.setMessageTtlMs(60000L);

        service.createQueue(dto);

        ArgumentCaptor<Queue> captor = ArgumentCaptor.forClass(Queue.class);
        verify(amqpAdmin).declareQueue(captor.capture());
        Queue declared = captor.getValue();
        assertEquals("orders.queue", declared.getName());
        assertTrue(declared.isDurable());
        assertEquals("orders.dlx", declared.getArguments().get("x-dead-letter-exchange"));
        assertEquals("orders.dead", declared.getArguments().get("x-dead-letter-routing-key"));
        assertEquals(60000L, declared.getArguments().get("x-message-ttl"));
    }

    @Test
    void rejectsBlankAndReservedQueueNames() {
        assertThrows(InvalidRabbitRequestException.class, () -> service.createQueue(queue("  ")));
        assertThrows(InvalidRabbitRequestException.class, () -> service.createQueue(queue("amq.gen-123")));
        verify(amqpAdmin, never()).declareQueue(any(Queue.class));
    }

    @Test
    void rejectsDeadLetterRoutingKeyWithoutExchange() {
        QueueRequestDTO dto = queue("q1");
        dto.setDeadLetterRoutingKey("dead");

        assertThrows(InvalidRabbitRequestException.class, () -> service.createQueue(dto));
    }

    @Test
    void deleteQueueFailsWhenQueueDoesNotExist() {
        when(amqpAdmin.getQueueInfo("missing")).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> service.deleteQueue("missing"));
        verify(amqpAdmin, never()).deleteQueue("missing");
    }

    @Test
    void deleteQueueRemovesExistingQueue() {
        when(amqpAdmin.getQueueInfo("q1")).thenReturn(new QueueInformation("q1", 0, 0));

        service.deleteQueue("q1");

        verify(amqpAdmin).deleteQueue("q1");
    }

    @Test
    void createExchangeDeclaresRequestedType() {
        ExchangeRequestDTO dto = new ExchangeRequestDTO();
        dto.setName("events");
        dto.setType(ExchangeType.TOPIC);

        service.createExchange(dto);

        ArgumentCaptor<Exchange> captor = ArgumentCaptor.forClass(Exchange.class);
        verify(amqpAdmin).declareExchange(captor.capture());
        assertEquals("topic", captor.getValue().getType());
        assertEquals("events", captor.getValue().getName());
    }

    @Test
    void createAndDeleteBinding() {
        BindingRequestDTO dto = new BindingRequestDTO();
        dto.setQueue("q1");
        dto.setExchange("events");
        dto.setRoutingKey("a.b");

        service.createBinding(dto);
        service.deleteBinding("q1", "events", "a.b");

        verify(amqpAdmin).declareBinding(any(Binding.class));
        verify(amqpAdmin).removeBinding(any(Binding.class));
    }

    @Test
    void deleteBindingRequiresQueueAndExchange() {
        assertThrows(InvalidRabbitRequestException.class, () -> service.deleteBinding("", "events", "k"));
    }
}
