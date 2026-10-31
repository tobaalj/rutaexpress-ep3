package cl.duoc.rutaexpress.ms_rutaexpress_catalog.repository;

import cl.duoc.rutaexpress.ms_rutaexpress_catalog.entity.CatalogItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CatalogRepository extends JpaRepository<CatalogItem, Long> {
    List<CatalogItem> findByActiveTrue();
}