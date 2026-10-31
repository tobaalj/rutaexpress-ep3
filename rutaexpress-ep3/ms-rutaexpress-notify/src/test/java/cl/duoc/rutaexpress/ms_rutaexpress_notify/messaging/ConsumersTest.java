package cl.duoc.rutaexpress.ms_rutaexpress_notify.messaging;

import cl.duoc.rutaexpress.ms_rutaexpress_notify.messaging.consumer.email.EmailConsumer;
import cl.duoc.rutaexpress.ms_rutaexpress_notify.messaging.consumer.label.LabelConsumer;
import cl.duoc.rutaexpress.ms_rutaexpress_notify.messaging.consumer.warehouse.WarehouseConsumer;
import cl.duoc.rutaexpress.ms_rutaexpress_notify.messaging.support.MessageProcessor;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ConsumersTest {

    private Channel channel;
    private MessageProcessor processor;

    @BeforeEach
    void setUp() {
        channel = mock(Channel.class);
        processor = new MessageProcessor(NotifyTestSupport.MAPPER, mock(RabbitTemplate.class), NotifyTestSupport.props());
    }

    @Test
    void emailConsumerAcksValidMessage() throws Exception {
        Message msg = NotifyTestSupport.message(
                NotifyTestSupport.event("a@b.cl", "TRK-1", "Calle 1", "ENTREGADO"), "q.cmd.email", null);

        new EmailConsumer(processor).consume(msg, channel);

        verify(channel).basicAck(1L, false);
    }

    @Test
    void emailConsumerSendsInvalidEmailToDlq() throws Exception {
        Message msg = NotifyTestSupport.message(
                NotifyTestSupport.event("sin-arroba", "TRK-1", "Calle 1", "ENTREGADO"), "q.cmd.email", null);

        new EmailConsumer(processor).consume(msg, channel);

        verify(channel).basicNack(1L, false, false);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }

    @Test
    void warehouseConsumerSendsMissingTrackingToDlq() throws Exception {
        Message msg = NotifyTestSupport.message(
                NotifyTestSupport.event("a@b.cl", " ", "Calle 1", "PENDING"), "q.cmd.warehouse", null);

        new WarehouseConsumer(processor).consume(msg, channel);

        verify(channel).basicNack(1L, false, false);
    }

    @Test
    void labelConsumerAcksValidMessage() throws Exception {
        Message msg = NotifyTestSupport.message(
                NotifyTestSupport.event("a@b.cl", "TRK-1", "Calle 1", "PENDING"), "q.cmd.label", null);

        new LabelConsumer(processor).consume(msg, channel);

        verify(channel).basicAck(1L, false);
    }

    @Test
    void labelConsumerSendsMalformedJsonToDlq() throws Exception {
        Message msg = NotifyTestSupport.message("esto no es json".getBytes(StandardCharsets.UTF_8), "q.cmd.label", null);

        new LabelConsumer(processor).consume(msg, channel);

        verify(channel).basicNack(1L, false, false);
    }
}
