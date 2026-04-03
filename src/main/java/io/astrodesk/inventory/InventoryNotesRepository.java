package io.astrodesk.inventory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryNotesRepository extends JpaRepository<InventoryNotes, Long> {
    List<InventoryNotes> findByInventoryIdOrderByCreatedAtDesc(Long inventoryId);
    Optional<InventoryNotes> findByIdAndInventoryId(Long id, Long inventoryId);
}