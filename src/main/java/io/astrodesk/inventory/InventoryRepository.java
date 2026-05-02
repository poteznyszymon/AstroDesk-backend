package io.astrodesk.inventory;

import io.astrodesk.user.DbUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    List<Inventory> findByStatusNotIn(Collection<InventoryStatus> statuses);
    List<Inventory> findByAssignedToAndStatusNotIn(DbUserEntity assignedTo, Collection<InventoryStatus> statuses);
    Optional<Inventory> findBySerialNumber(String serialNumber);
}
