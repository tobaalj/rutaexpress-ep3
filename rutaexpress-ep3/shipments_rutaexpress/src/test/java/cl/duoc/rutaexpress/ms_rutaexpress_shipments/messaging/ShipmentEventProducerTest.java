package cl.duoc.rutaexpress.ms_rutaexpress_shipments.messaging;

import cl.duoc.rutaexpress.ms_rutaexpress_shipments.messaging.event.ShipmentEvent;
import cl.duoc.rutaexpress.ms_rutaexpress_shipments.messaging.producer.ShipmentEventProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ShipmentEventProducerTest {

    private RabbitTemplate template;
    private ShipmentEventProducer producer;

    @BeforeEach
    void setUp() {
        template = mock(RabbitTemplate.class);
        producer = new ShipmentEventProducer(template, MessagingTestSupport.props());
    }

    @Test
    void createdShipmentSendsTicketAndLabelCommands() {
        producer.publishCreated(MessagingTestSupport.shipment("PENDING"));

        verify(template).convertAndSend(eq("cmd.direct"), eq("warehouse.ticket"), any(ShipmentEvent.class));
        verify(template).convertAndSend(eq("cmd.direct"), eq("label.gen"), any(ShipmentEvent.class));
        verify(template, never()).convertAndSend(eq("cmd.direct"), eq("email.send"), any(ShipmentEvent.class));
    }

    @Test
    void emailIsSentOnlyForConfiguredStatuses() {
        producer.publishStatusChanged(MessagingTestSupport.shipment("entregado"));
        verify(template).convertAndSend(eq("cmd.direct"), eq("email.send"), any(ShipmentEvent.class));
    }

    @Test
    void otherStatusesDoNotSendEmail() {
        producer.publishStatusChanged(MessagingTestSupport.shipment("EN_BODEGA"));
        verify(template, never()).convertAndSend(anyString(), anyString(), any(ShipmentEvent.class));
    }

    @Test
    void brokerFailureDoesNotBreakBusinessFlow() {
        doThrow(new AmqpException("broker caído"))
                .when(template).convertAndSend(eq("cmd.direct"), eq("email.send"), any(ShipmentEvent.class));

        assertDoesNotThrow(() -> producer.publishStatusChanged(MessagingTestSupport.shipment("ACEPTADO")));
    }
}
