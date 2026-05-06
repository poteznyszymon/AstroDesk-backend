package io.astrodesk.network.repository;

import io.astrodesk.network.entity.NetworkHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NetworkHistoryRepository extends JpaRepository<NetworkHistory, Long> {

    List<NetworkHistory> findByMacAddressOrderBySeenAtDesc(String macAddress);
}
