package cl.duoc.rutaexpress.ms_rutaexpress_catalog.messaging.event;

import java.math.BigDecimal;
import java.time.Instant;

/** Evento publicado cuando se crea un servicio en el catálogo. */
public record CatalogItemEvent(
        String eventType,
        Long itemId,
        String serviceName,
        BigDecimal basePrice,
        Integer availableCapacity,
        Instant occurredAt) {

    public static final String CREATED = "CATALOG_ITEM_CREATED";
}
