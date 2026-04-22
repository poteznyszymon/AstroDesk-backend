package io.astrodesk.history;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface HistoryRepository extends JpaRepository<HistoryEntry, Long>, JpaSpecificationExecutor<HistoryEntry> {

    List<HistoryEntry> findByTargetTypeAndTargetIdOrderByChangedAtDesc(
            HistoryTargetType targetType,
            Long targetId
    );
}