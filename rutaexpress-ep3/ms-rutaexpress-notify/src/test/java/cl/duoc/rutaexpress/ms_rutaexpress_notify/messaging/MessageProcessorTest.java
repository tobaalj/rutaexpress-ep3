package cl.duoc.rutaexpress.ms_rutaexpress_notify.messaging;

import cl.duoc.rutaexpress.ms_rutaexpress_notify.messaging.event.ShipmentEvent;
import cl.duoc.rutaexpress.ms_rutaexpress_notify.messaging.support.MessageProcessor;
import cl.duoc.rutaexpress.ms_rutaexpress_notify.messaging.support.NonRecoverableMessageException;
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
        processor = new MessageProcessor(NotifyTestSupport.MAPPER, template, NotifyTestSupport.props());
    }

    @Test
    void ackWhenHandlerSucceeds() throws Exception {
        Message msg = NotifyTestSupport.message(NotifyTestSupport.event("a@b.cl", "TRK-1", "Calle 1", "PENDING"), "q.cmd.email", null);

        processor.process(msg, channel, ShipmentEvent.class, e -> { });

        verify(channel).basicAck(1L, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void nackWithoutRequeueWhenMalformed() throws Exception {
        Message msg = NotifyTestSupport.message("no-es-json".getBytes(StandardCharsets.UTF_8), "q.cmd.email", null);

        processor.process(msg, channel, ShipmentEvent.class, e -> { });

        verify(channel).basicNack(1L, false, false);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }

    @Test
    void nackWithoutRequeueWhenNonRecoverable() throws Exception {
        Message msg = NotifyTestSupport.message(NotifyTestSupport.event("x", "TRK-1", "Calle 1", "PENDING"), "q.cmd.email", null);

        processor.process(msg, channel, ShipmentEvent.class, e -> {
            throw new NonRecoverableMessageException("datos inválidos");
        });

        verify(channel).basicNack(1L, false, false);
        verify(template, never()).send(any(String.class), any(String.class), any(Message.class));
    }

    @Test
    void retriesRecoverableErrorOnSameQueue() throws Exception {
        Message msg = NotifyTestSupport.message(NotifyTestSupport.event("a@b.cl", "TRK-1", "Calle 1", "PENDING"), "q.cmd.email", 0);

        processor.process(msg, channel, ShipmentEvent.class, e -> {
            throw new IllegalStateException("servicio caído");
        });

        verify(template).send(eq(""), eq("q.cmd.email"), any(Message.class));
        verify(channel).basicAck(1L, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void sendsToDlqWhenRetriesExhausted() throws Exception {
        Message msg = NotifyTestSupport.message(NotifyTestSupport.event("a@b.cl", "TRK-1", "Calle 1", "PENDING"), "q.cmd.email", 2);

        processor.process(msg, channel, ShipmentEvent.class, e -> {
            throw new IllegalStateException("servicio caído");
        });

        verify(channel).basicNack(1L, false, false);
        verify(template, never()).send(any(String.class), any(String.class), any(Message.class));
    }
}
