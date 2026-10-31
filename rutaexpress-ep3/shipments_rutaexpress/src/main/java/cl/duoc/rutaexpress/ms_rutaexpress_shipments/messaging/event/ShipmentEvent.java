package cl.duoc.rutaexpress.ms_rutaexpress_shipments.messaging.event;

import java.time.Instant;

/** Evento de dominio publicado cuando un envío se crea o cambia de estado. */
public record ShipmentEvent(
        String eventType,
        Long shipmentId,
        String trackingNumber,
        String senderEmail,
        String recipientAddress,
        String status,
        Instant occurredAt) {

    public static final String CREATED = "SHIPMENT_CREATED";
    public static final String STATUS_CHANGED = "SHIPMENT_STATUS_CHANGED";
}
