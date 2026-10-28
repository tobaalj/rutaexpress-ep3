package cl.duoc.rutaexpress.bff.service;

import cl.duoc.rutaexpress.bff.dto.CreateShipmentDTO;
import cl.duoc.rutaexpress.bff.dto.ShipmentDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class ShipmentClientService {

    private final RestClient restClient;

    public ShipmentClientService(@Qualifier("shipmentsRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public List<ShipmentDTO> getAllShipments() {
        return restClient.get()
                .uri("/api/shipments")
                .retrieve()
                .body(new ParameterizedTypeReference<List<ShipmentDTO>>() {});
    }

    public ShipmentDTO getShipmentById(Long id) {
        return restClient.get()
                .uri("/api/shipments/{id}", id)
                .retrieve()
                .body(ShipmentDTO.class);
    }

    public ShipmentDTO createShipment(CreateShipmentDTO dto) {
        return restClient.post()
                .uri("/api/shipments")
                .contentType(MediaType.APPLICATION_JSON)
                .body(dto)
                .retrieve()
                .body(ShipmentDTO.class);
    }

    public ShipmentDTO updateStatus(Long id, String status) {
        return restClient.put()
                .uri("/api/shipments/{id}/status?status={status}", id, status)
                .retrieve()
                .body(ShipmentDTO.class);
    }
}