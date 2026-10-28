package cl.duoc.rutaexpress.ms_rutaexpress_catalog.controller;

import cl.duoc.rutaexpress.ms_rutaexpress_catalog.dto.CatalogItemDTO;
import cl.duoc.rutaexpress.ms_rutaexpress_catalog.dto.CreateCatalogItemDTO;
import cl.duoc.rutaexpress.ms_rutaexpress_catalog.service.CatalogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final CatalogService service;

    public CatalogController(CatalogService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<CatalogItemDTO>> getAllActive() {
        return ResponseEntity.ok(service.getAllActiveItems());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CatalogItemDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getItemById(id));
    }

    @PostMapping
    public ResponseEntity<CatalogItemDTO> create(@RequestBody CreateCatalogItemDTO dto) {
        return new ResponseEntity<>(service.createItem(dto), HttpStatus.CREATED);
    }
}