package cl.duoc.rutaexpress.ms_rutaexpress_shipments.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ShipmentDTO {
    private Long id;
    private String trackingNumber;
    private String senderEmail;
    private String recipientAddress;
    private String status;
    private BigDecimal weightKg;
    private LocalDateTime createdAt;
}