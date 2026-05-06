package io.astrodesk.network.repository;

import io.astrodesk.network.entity.NetworkDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NetworkDeviceRepository
        extends JpaRepository<NetworkDevice, Long>,
                JpaSpecificationExecutor<NetworkDevice> {

    Optional<NetworkDevice> findByMacAddress(String macAddress);

    boolean existsByMacAddress(String macAddress);
}
