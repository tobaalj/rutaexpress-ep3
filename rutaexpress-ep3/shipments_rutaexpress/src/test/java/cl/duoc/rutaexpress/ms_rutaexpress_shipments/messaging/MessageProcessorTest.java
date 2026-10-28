package cl.duoc.rutaexpress.ms_rutaexpress_shipments.messaging;

import cl.duoc.rutaexpress.ms_rutaexpress_shipments.messaging.event.ShipmentEvent;
import cl.duoc.rutaexpress.ms_rutaexpress_shipments.messaging.support.MessageProcessor;
import cl.duoc.rutaexpress.ms_rutaexpress_shipments.messaging.support.NonRecoverableMessageException;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class MessageProcessorTest {

    private Channel channel;
    private RabbitTemplate template;
    private MessageProcessor processor;

    @BeforeEach
    void setUp() {
        channel = mock(Channel.class);
        template = mock(RabbitTemplate.class);
        processor = new MessageProcessor(MessagingTestSupport.MAPPER, template, MessagingTestSupport.props());
    }

    @Test
    void ackWhenHandlerSucceeds() throws Exception {
        Message msg = MessagingTestSupport.message(MessagingTestSupport.event("a@b.cl"), null);

        processor.process(msg, channel, ShipmentEvent.class, e -> { });

        verify(channel).basicAck(1L, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void nackWithoutRequeueWhenMalformed() throws Exception {
        Message msg = MessagingTestSupport.message("no-es-json".getBytes(StandardCharsets.UTF_8), null);

        processor.process(msg, channel, ShipmentEvent.class, e -> { });

        verify(channel).basicNack(1L, false, false);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }

    @Test
    void nackWithoutRequeueWhenNonRecoverable() throws Exception {
        Message msg = MessagingTestSupport.message(MessagingTestSupport.event("x"), null);

        processor.process(msg, channel, ShipmentEvent.class, e -> {
            throw new NonRecoverableMessageException("datos inválidos");
        });

        verify(channel).basicNack(1L, false, false);
        verify(template, never()).send(any(String.class), any(String.class), any(Message.class));
    }

    @Test
    void retriesRecoverableErrorOnSameQueue() throws Exception {
        Message msg = MessagingTestSupport.message(MessagingTestSupport.event("a@b.cl"), 0);

        processor.process(msg, channel, ShipmentEvent.class, e -> {
            throw new IllegalStateException("servicio caído");
        });

        verify(template).send(eq(""), eq("notif.q"), any(Message.class));
        verify(channel).basicAck(1L, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void sendsToDlqWhenRetriesExhausted() throws Exception {
        Message msg = MessagingTestSupport.message(MessagingTestSupport.event("a@b.cl"), 2);

        processor.process(msg, channel, ShipmentEvent.class, e -> {
            throw new IllegalStateException("servicio caído");
        });

        verify(channel).basicNack(1L, false, false);
        verify(template, never()).send(any(String.class), any(String.class), any(Message.class));
    }
}
