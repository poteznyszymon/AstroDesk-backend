package io.astrodesk.history;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class HistoryService {

    private final HistoryRepository historyRepository;

    public HistoryService(HistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    public List<HistoryEntry> getAllHistory() {
        return historyRepository.findAll(Sort.by(Sort.Direction.DESC, "changedAt"));
    }

    public void saveFieldChange(
            HistoryTargetType targetType,
            Long targetId,
            String fieldName,
            Object oldValue,
            Object newValue,
            String changedBy
    ) {
        if (Objects.equals(oldValue, newValue)) {
            return;
        }

        HistoryEntry entry = new HistoryEntry(
                targetType,
                targetId,
                fieldName,
                oldValue == null ? null : String.valueOf(oldValue),
                newValue == null ? null : String.valueOf(newValue),
                null,
                changedBy
        );

        historyRepository.save(entry);
    }

    public void saveMessage(
            HistoryTargetType targetType,
            Long targetId,
            String message,
            String changedBy
    ) {
        HistoryEntry entry = new HistoryEntry(
                targetType,
                targetId,
                null,
                null,
                null,
                message,
                changedBy
        );

        historyRepository.save(entry);
    }

    public List<HistoryEntry> getHistory(HistoryTargetType targetType, Long targetId) {
        return historyRepository.findByTargetTypeAndTargetIdOrderByChangedAtDesc(targetType, targetId);
    }
}