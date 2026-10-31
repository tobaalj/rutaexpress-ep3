package cl.duoc.rutaexpress.ms_rutaexpress_notify.messaging.event;

import java.time.Instant;

/** Mensaje publicado por ms-rutaexpress-shipments (mismo contrato JSON). */
public record ShipmentEvent(
        String eventType,
        Long shipmentId,
        String trackingNumber,
        String senderEmail,
        String recipientAddress,
        String status,
        Instant occurredAt) {
}
