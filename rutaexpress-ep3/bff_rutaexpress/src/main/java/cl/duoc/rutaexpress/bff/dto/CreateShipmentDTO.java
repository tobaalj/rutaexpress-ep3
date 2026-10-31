package cl.duoc.rutaexpress.bff.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateShipmentDTO {
    private String senderEmail;
    private String recipientAddress;
    private BigDecimal weightKg;
}
