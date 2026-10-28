package cl.duoc.rutaexpress.ms_rutaexpress_shipments.controller;

import cl.duoc.rutaexpress.ms_rutaexpress_shipments.dto.CreateShipmentDTO;
import cl.duoc.rutaexpress.ms_rutaexpress_shipments.dto.ShipmentDTO;
import cl.duoc.rutaexpress.ms_rutaexpress_shipments.service.ShipmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shipments")
public class ShipmentController {

    private final ShipmentService service;

    public ShipmentController(ShipmentService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ShipmentDTO>> getAll() {
        return ResponseEntity.ok(service.getAllShipments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getShipmentById(id));
    }

    @PostMapping
    public ResponseEntity<ShipmentDTO> create(@RequestBody CreateShipmentDTO dto) {
        return new ResponseEntity<>(service.createShipment(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ShipmentDTO> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(service.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteShipment(id);
        return ResponseEntity.noContent().build();
    }
}