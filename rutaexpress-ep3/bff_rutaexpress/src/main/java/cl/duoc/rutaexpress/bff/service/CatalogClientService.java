package cl.duoc.rutaexpress.bff.service;

import cl.duoc.rutaexpress.bff.dto.CatalogItemDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class CatalogClientService {

    private final RestClient restClient;

    public CatalogClientService(@Qualifier("catalogRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public List<CatalogItemDTO> getCatalogItems() {
        return restClient.get()
                .uri("/api/catalog")
                .retrieve()
                .body(new ParameterizedTypeReference<List<CatalogItemDTO>>() {});
    }
}