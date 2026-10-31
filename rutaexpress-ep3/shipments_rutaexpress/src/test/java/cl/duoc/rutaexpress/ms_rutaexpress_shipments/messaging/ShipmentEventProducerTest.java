package cl.duoc.rutaexpress.ms_rutaexpress_shipments.messaging;

<<<<<<< HEAD
import cl.duoc.rutaexpress.ms_rutaexpress_shipments.messaging.event.ShipmentEvent;
import cl.duoc.rutaexpress.ms_rutaexpress_shipments.messaging.producer.ShipmentEventProducer;
import org.junit.jupiter.api.BeforeEach;
=======
import cl.duoc.rutaexpress.ms_rutaexpress_shipments.dto.ShipmentDTO;
import cl.duoc.rutaexpress.ms_rutaexpress_shipments.messaging.event.ShipmentEvent;
import cl.duoc.rutaexpress.ms_rutaexpress_shipments.messaging.producer.ShipmentEventProducer;
>>>>>>> d39a0dbc18aebda18ac1db90e44f7b6a6d865fab
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
<<<<<<< HEAD
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
=======
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
>>>>>>> d39a0dbc18aebda18ac1db90e44f7b6a6d865fab
import static org.mockito.Mockito.verify;

class ShipmentEventProducerTest {

<<<<<<< HEAD
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
=======
    private ShipmentDTO shipment() {
        ShipmentDTO dto = new ShipmentDTO();
        dto.setId(1L);
        dto.setTrackingNumber("TRK-1");
        dto.setSenderEmail("a@b.cl");
        dto.setRecipientAddress("Calle 1");
        dto.setStatus("PENDING");
        return dto;
    }

    @Test
    void publishesCreatedEventWithConfiguredNames() {
        RabbitTemplate template = mock(RabbitTemplate.class);
        ShipmentEventProducer producer = new ShipmentEventProducer(template, MessagingTestSupport.props());

        producer.publishCreated(shipment());

        verify(template).convertAndSend(eq("ex"), eq("shipment.created"), any(ShipmentEvent.class));
>>>>>>> d39a0dbc18aebda18ac1db90e44f7b6a6d865fab
    }

    @Test
    void brokerFailureDoesNotBreakBusinessFlow() {
<<<<<<< HEAD
        doThrow(new AmqpException("broker caído"))
                .when(template).convertAndSend(eq("cmd.direct"), eq("email.send"), any(ShipmentEvent.class));

        assertDoesNotThrow(() -> producer.publishStatusChanged(MessagingTestSupport.shipment("ACEPTADO")));
=======
        RabbitTemplate template = mock(RabbitTemplate.class);
        doThrow(new AmqpException("broker caído"))
                .when(template).convertAndSend(eq("ex"), eq("shipment.status.changed"), any(ShipmentEvent.class));
        ShipmentEventProducer producer = new ShipmentEventProducer(template, MessagingTestSupport.props());

        assertDoesNotThrow(() -> producer.publishStatusChanged(shipment()));
>>>>>>> d39a0dbc18aebda18ac1db90e44f7b6a6d865fab
    }
}
