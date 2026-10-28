package cl.duoc.rutaexpress.ms_rutaexpress_catalog.dto;

import java.math.BigDecimal;

public class CreateCatalogItemDTO {
    private String serviceName;
    private String description;
    private BigDecimal basePrice;
    private Integer availableCapacity;

    public CreateCatalogItemDTO() {}

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

    public Integer getAvailableCapacity() { return availableCapacity; }
    public void setAvailableCapacity(Integer availableCapacity) { this.availableCapacity = availableCapacity; }
}