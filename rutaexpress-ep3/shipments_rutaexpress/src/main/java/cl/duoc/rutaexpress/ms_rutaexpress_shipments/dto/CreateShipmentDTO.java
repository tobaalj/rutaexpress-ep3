package cl.duoc.rutaexpress.ms_rutaexpress_shipments.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateShipmentDTO {
    private String senderEmail;
    private String recipientAddress;
    private BigDecimal weightKg;
}