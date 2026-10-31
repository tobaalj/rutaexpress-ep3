package cl.duoc.rutaexpress.ms_rutaexpress_notify.messaging;

import cl.duoc.rutaexpress.ms_rutaexpress_notify.config.MessagingProperties;
import cl.duoc.rutaexpress.ms_rutaexpress_notify.messaging.event.ShipmentEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.time.Instant;

final class NotifyTestSupport {

    private NotifyTestSupport() {}

    static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    static MessagingProperties props() {
        return new MessagingProperties(2,
                new MessagingProperties.Exchanges("cmd.direct", "cmd.topic", "cmd.dead.dlx"),
                new MessagingProperties.Names("q.cmd.email", "q.cmd.warehouse", "q.cmd.label"),
                new MessagingProperties.Names("q.cmd.email.dlq", "q.cmd.warehouse.dlq", "q.cmd.label.dlq"),
                new MessagingProperties.Names("email.send", "warehouse.ticket", "label.gen"),
                new MessagingProperties.Names("email.*", "warehouse.#", "label.*"));
    }

    static ShipmentEvent event(String email, String tracking, String address, String status) {
        return new ShipmentEvent("SHIPMENT_STATUS_CHANGED", 1L, tracking, email, address, status,
                Instant.parse("2026-01-01T00:00:00Z"));
    }

    static Message message(byte[] body, String queue, Integer retries) {
        MessageProperties mp = new MessageProperties();
        mp.setDeliveryTag(1L);
        mp.setConsumerQueue(queue);
        if (retries != null) {
            mp.setHeader("x-retry-count", retries);
        }
        return new Message(body, mp);
    }

    static Message message(ShipmentEvent event, String queue, Integer retries) throws Exception {
        return message(MAPPER.writeValueAsBytes(event), queue, retries);
    }
}
