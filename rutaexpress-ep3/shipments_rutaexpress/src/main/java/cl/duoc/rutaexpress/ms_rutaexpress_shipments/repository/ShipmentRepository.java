package cl.duoc.rutaexpress.ms_rutaexpress_shipments.repository;

import cl.duoc.rutaexpress.ms_rutaexpress_shipments.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    List<Shipment> findBySenderEmail(String senderEmail);
}