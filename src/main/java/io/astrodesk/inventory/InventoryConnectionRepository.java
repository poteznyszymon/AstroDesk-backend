package io.astrodesk.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryConnectionRepository extends JpaRepository<InventoryConnection, Long> {

    @Query("SELECT c FROM InventoryConnection c WHERE c.deviceA = :device OR c.deviceB = :device")
    List<InventoryConnection> findAllByDevice(@Param("device") Inventory device);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM InventoryConnection c " +
           "WHERE (c.deviceA = :a AND c.deviceB = :b) OR (c.deviceA = :b AND c.deviceB = :a)")
    boolean existsConnection(@Param("a") Inventory a, @Param("b") Inventory b);

    @Query("SELECT c FROM InventoryConnection c WHERE c.id = :id AND (c.deviceA = :device OR c.deviceB = :device)")
    Optional<InventoryConnection> findByIdAndDevice(@Param("id") Long id, @Param("device") Inventory device);
}
