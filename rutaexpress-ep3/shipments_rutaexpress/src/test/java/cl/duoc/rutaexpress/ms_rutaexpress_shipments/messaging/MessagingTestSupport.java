package cl.duoc.rutaexpress.ms_rutaexpress_shipments.messaging;

import cl.duoc.rutaexpress.ms_rutaexpress_shipments.config.MessagingProperties;
import cl.duoc.rutaexpress.ms_rutaexpress_shipments.dto.ShipmentDTO;

import java.util.List;

final class MessagingTestSupport {

    private MessagingTestSupport() {}

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
    }
}
