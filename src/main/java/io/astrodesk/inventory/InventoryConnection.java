package io.astrodesk.inventory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.astrodesk.user.DbUserEntity;
import io.astrodesk.user.UserDTO;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "inventory_connections",
        uniqueConstraints = @UniqueConstraint(columnNames = {"device_a_id", "device_b_id"})
)
public class InventoryConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_a_id", nullable = false)
    @JsonIgnore
    private Inventory deviceA;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_b_id", nullable = false)
    @JsonIgnore
    private Inventory deviceB;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", referencedColumnName = "userId", nullable = false)
    private DbUserEntity createdBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public InventoryConnection(Inventory deviceA, Inventory deviceB, DbUserEntity createdBy) {
        this.deviceA = deviceA;
        this.deviceB = deviceB;
        this.createdBy = createdBy;
    }

    protected InventoryConnection() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Inventory getDeviceA() {
        return deviceA;
    }

    public Inventory getDeviceB() {
        return deviceB;
    }

    public UserDTO getCreatedBy() {
        if (createdBy == null) return null;
        UserDTO dto = new UserDTO();
        dto.setUserId(createdBy.getUserId());
        dto.setUsername(createdBy.getUsername());
        dto.setFirstName(createdBy.getFirstName());
        dto.setLastName(createdBy.getLastName());
        dto.setEmail(createdBy.getEmail());
        dto.setRole(createdBy.getRole());
        return dto;
    }

    @JsonIgnore
    public DbUserEntity getCreatedByEntity() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
