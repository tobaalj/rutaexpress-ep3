package cl.duoc.rutaexpress.ms_rutaexpress_shipments.messaging;

import cl.duoc.rutaexpress.ms_rutaexpress_shipments.config.MessagingProperties;
<<<<<<< HEAD
import cl.duoc.rutaexpress.ms_rutaexpress_shipments.dto.ShipmentDTO;

import java.util.List;
=======
import cl.duoc.rutaexpress.ms_rutaexpress_shipments.messaging.event.ShipmentEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.time.Instant;
>>>>>>> d39a0dbc18aebda18ac1db90e44f7b6a6d865fab

final class MessagingTestSupport {

    private MessagingTestSupport() {}

<<<<<<< HEAD
    static MessagingProperties props() {
        return new MessagingProperties(
                new MessagingProperties.Exchanges("cmd.direct", "cmd.topic"),
                new MessagingProperties.RoutingKeys("email.send", "warehouse.ticket", "label.gen"),
                List.of("ACEPTADO", "EN_RUTA", "ENTREGADO"));
    }

    static ShipmentDTO shipment(String status) {
        ShipmentDTO dto = new ShipmentDTO();
        dto.setId(1L);
        dto.setTrackingNumber("TRK-1");
        dto.setSenderEmail("a@b.cl");
        dto.setRecipientAddress("Calle 1");
        dto.setStatus(status);
        return dto;
=======
    static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    static MessagingProperties props() {
        return new MessagingProperties("ex", "dlx", 2,
                new MessagingProperties.Queues("notif.q", "tickets.q"),
                new MessagingProperties.DeadLetterQueues("notif.dlq", "tickets.dlq"),
                new MessagingProperties.RoutingKeys("shipment.created", "shipment.status.changed",
                        "shipment.#", "shipment.created", "notif.dead", "tickets.dead"));
    }

    static ShipmentEvent event(String email) {
        return new ShipmentEvent(ShipmentEvent.CREATED, 1L, "TRK-1", email, "Calle 1", "PENDING",
                Instant.parse("2026-01-01T00:00:00Z"));
    }

    static Message message(byte[] body, Integer retries) {
        MessageProperties mp = new MessageProperties();
        mp.setDeliveryTag(1L);
        mp.setConsumerQueue("notif.q");
        if (retries != null) {
            mp.setHeader("x-retry-count", retries);
        }
        return new Message(body, mp);
    }

    static Message message(ShipmentEvent event, Integer retries) throws Exception {
        return message(MAPPER.writeValueAsBytes(event), retries);
>>>>>>> d39a0dbc18aebda18ac1db90e44f7b6a6d865fab
    }
}
