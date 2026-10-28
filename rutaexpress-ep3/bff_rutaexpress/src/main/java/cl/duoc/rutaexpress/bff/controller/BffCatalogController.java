package cl.duoc.rutaexpress.bff.controller;

import cl.duoc.rutaexpress.bff.dto.CatalogItemDTO;
import cl.duoc.rutaexpress.bff.service.CatalogClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bff/catalog")
public class BffCatalogController {

    private final CatalogClientService catalogClientService;

    // Constructor explícito para inyección de dependencia
    public BffCatalogController(CatalogClientService catalogClientService) {
        this.catalogClientService = catalogClientService;
    }

    @GetMapping
    public ResponseEntity<List<CatalogItemDTO>> getCatalogItems() {
        return ResponseEntity.ok(catalogClientService.getCatalogItems());
    }
}