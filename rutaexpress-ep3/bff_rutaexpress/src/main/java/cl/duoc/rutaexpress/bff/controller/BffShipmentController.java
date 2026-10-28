package cl.duoc.rutaexpress.bff.controller;

import cl.duoc.rutaexpress.bff.dto.CreateShipmentDTO;
import cl.duoc.rutaexpress.bff.dto.ShipmentDTO;
import cl.duoc.rutaexpress.bff.service.ShipmentClientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bff/shipments")
public class BffShipmentController {

    private final ShipmentClientService shipmentClientService;

    // Constructor manual para inyectar la dependencia
    public BffShipmentController(ShipmentClientService shipmentClientService) {
        this.shipmentClientService = shipmentClientService;
    }

    @GetMapping
    public ResponseEntity<List<ShipmentDTO>> getAllShipments() {
        return ResponseEntity.ok(shipmentClientService.getAllShipments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentDTO> getShipmentById(@PathVariable Long id) {
        return ResponseEntity.ok(shipmentClientService.getShipmentById(id));
    }

    @PostMapping
    public ResponseEntity<ShipmentDTO> createShipment(@RequestBody CreateShipmentDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shipmentClientService.createShipment(dto));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ShipmentDTO> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(shipmentClientService.updateStatus(id, status));
    }
}
