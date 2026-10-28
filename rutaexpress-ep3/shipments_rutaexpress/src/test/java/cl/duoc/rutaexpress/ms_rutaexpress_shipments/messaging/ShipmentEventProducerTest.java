package cl.duoc.rutaexpress.ms_rutaexpress_shipments.messaging;

import cl.duoc.rutaexpress.ms_rutaexpress_shipments.dto.ShipmentDTO;
import cl.duoc.rutaexpress.ms_rutaexpress_shipments.messaging.event.ShipmentEvent;
import cl.duoc.rutaexpress.ms_rutaexpress_shipments.messaging.producer.ShipmentEventProducer;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ShipmentEventProducerTest {

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
    }

    @Test
    void brokerFailureDoesNotBreakBusinessFlow() {
        RabbitTemplate template = mock(RabbitTemplate.class);
        doThrow(new AmqpException("broker caído"))
                .when(template).convertAndSend(eq("ex"), eq("shipment.status.changed"), any(ShipmentEvent.class));
        ShipmentEventProducer producer = new ShipmentEventProducer(template, MessagingTestSupport.props());

        assertDoesNotThrow(() -> producer.publishStatusChanged(shipment()));
    }
}
