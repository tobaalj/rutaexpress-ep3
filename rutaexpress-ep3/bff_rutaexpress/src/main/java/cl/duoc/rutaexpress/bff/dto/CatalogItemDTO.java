package cl.duoc.rutaexpress.bff.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CatalogItemDTO {
    private Long id;
    private String serviceName;
    private String description;
    private BigDecimal basePrice;
    private Integer availableCapacity;
    private Boolean active;
}
