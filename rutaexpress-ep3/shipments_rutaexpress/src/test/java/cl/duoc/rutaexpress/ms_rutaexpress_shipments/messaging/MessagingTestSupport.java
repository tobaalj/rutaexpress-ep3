package cl.duoc.rutaexpress.ms_rutaexpress_shipments.messaging;

import cl.duoc.rutaexpress.ms_rutaexpress_shipments.config.MessagingProperties;
import cl.duoc.rutaexpress.ms_rutaexpress_shipments.messaging.event.ShipmentEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.time.Instant;

final class MessagingTestSupport {

    private MessagingTestSupport() {}

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
    }
}
