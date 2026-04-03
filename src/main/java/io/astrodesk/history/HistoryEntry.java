package io.astrodesk.history;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "history_entries")
public class HistoryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private HistoryTargetType targetType;

    @Column(nullable = false)
    private Long targetId;

    @Column(length = 100)
    private String fieldName;

    @Column(length = 1000)
    private String oldValue;

    @Column(length = 1000)
    private String newValue;

    @Column(length = 1000)
    private String message;

    @Column(nullable = false, length = 255)
    private String changedBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime changedAt;

    protected HistoryEntry() {
    }

    public HistoryEntry(
            HistoryTargetType targetType,
            Long targetId,
            String fieldName,
            String oldValue,
            String newValue,
            String message,
            String changedBy
    ) {
        this.targetType = targetType;
        this.targetId = targetId;
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.message = message;
        this.changedBy = changedBy;
    }

    @PrePersist
    protected void onCreate() {
        this.changedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public HistoryTargetType getTargetType() {
        return targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public String getMessage() {
        return message;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }
}