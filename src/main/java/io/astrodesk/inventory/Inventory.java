package io.astrodesk.inventory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.astrodesk.user.DbUserEntity;
import io.astrodesk.user.UserDTO;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inventory")
@JsonPropertyOrder({
        "id",
        "name",
        "itemType",
        "serialNumber",
        "model",
        "boughtDate",
        "price",
        "invoiceNumber",
        "location",
        "assignedTo",
        "assignedBy",
        "assignedDate",
        "notes",
        "status",
        "priority",
        "author",
        "createdAt",
        "updatedAt"
})
public class Inventory {

    @OneToMany(mappedBy = "inventory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InventoryNotes> notes = new ArrayList<>();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    private InventoryItemType itemType;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String serialNumber;

    private String model;

    private LocalDate boughtDate;
    private Double price;
    private String invoiceNumber;

    private String location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id", referencedColumnName = "userId")
    private DbUserEntity assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_id", referencedColumnName = "userId")
    private DbUserEntity assignedBy;

    private LocalDate assignedDate;

    @Enumerated(EnumType.STRING)
    private InventoryStatus status;

    @Enumerated(EnumType.STRING)
    private InventoryPriority priority;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "userId")
    private DbUserEntity author;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Inventory(
            String name,
            InventoryItemType itemType,
            String serialNumber,
            String model,
            LocalDate boughtDate,
            Double price,
            String invoiceNumber,
            String location,
            InventoryPriority priority,
            DbUserEntity author
    ) {
        this.name = name;
        this.itemType = itemType;
        this.serialNumber = serialNumber;
        this.model = model;
        this.boughtDate = boughtDate;
        this.price = price;
        this.invoiceNumber = invoiceNumber;
        this.location = location;
        this.priority = priority;
        this.author = author;
        this.status = InventoryStatus.DOSTEPNE;
    }

    protected Inventory() {
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void assign(DbUserEntity assignedTo, DbUserEntity assignedBy) {
        if (status != InventoryStatus.DOSTEPNE && status != InventoryStatus.DO_WYDANIA) {
            throw new IllegalStateException("Inventory item must be DOSTEPNE or DO_WYDANIA to assign");
        }
        this.assignedTo = assignedTo;
        this.assignedBy = assignedBy;
        this.assignedDate = LocalDate.now();
        this.status = InventoryStatus.WYDANE;
    }

    public void returnToStock() {
        if (status != InventoryStatus.WYDANE) {
            throw new IllegalStateException("Inventory item must be WYDANE to return");
        }
        this.assignedTo = null;
        this.assignedBy = null;
        this.assignedDate = null;
        this.status = InventoryStatus.DOSTEPNE;
    }

    public void sendToService() {
        if (status == InventoryStatus.UTYLIZACJA) {
            throw new IllegalStateException("Inventory item in UTYLIZACJA cannot be sent to service");
        }
        this.status = InventoryStatus.SERWIS;
    }

    public void markAsDisposed() {
        this.status = InventoryStatus.UTYLIZACJA;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public InventoryItemType getItemType() {
        return itemType;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getModel() {
        return model;
    }

    public LocalDate getBoughtDate() {
        return boughtDate;
    }

    public Double getPrice() {
        return price;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public String getLocation() {
        return location;
    }

    public LocalDate getAssignedDate() {
        return assignedDate;
    }

    public InventoryStatus getStatus() {
        return status;
    }

    public InventoryPriority getPriority() {
        return priority;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<InventoryNotes> getNotes() {
        return notes;
    }

    public UserDTO getAuthor() {
        if (author == null) {
            return null;
        }

        UserDTO dto = new UserDTO();
        dto.setUserId(author.getUserId());
        dto.setUsername(author.getUsername());
        dto.setFirstName(author.getFirstName());
        dto.setLastName(author.getLastName());
        dto.setEmail(author.getEmail());
        dto.setRole(author.getRole());
        return dto;
    }

    public UserDTO getAssignedTo() {
        if (assignedTo == null) {
            return null;
        }

        UserDTO dto = new UserDTO();
        dto.setUserId(assignedTo.getUserId());
        dto.setUsername(assignedTo.getUsername());
        dto.setFirstName(assignedTo.getFirstName());
        dto.setLastName(assignedTo.getLastName());
        dto.setEmail(assignedTo.getEmail());
        dto.setRole(assignedTo.getRole());
        return dto;
    }

    public UserDTO getAssignedBy() {
        if (assignedBy == null) {
            return null;
        }

        UserDTO dto = new UserDTO();
        dto.setUserId(assignedBy.getUserId());
        dto.setUsername(assignedBy.getUsername());
        dto.setFirstName(assignedBy.getFirstName());
        dto.setLastName(assignedBy.getLastName());
        dto.setEmail(assignedBy.getEmail());
        dto.setRole(assignedBy.getRole());
        return dto;
    }

    @JsonIgnore
    public DbUserEntity getAuthorEntity() {
        return author;
    }

    @JsonIgnore
    public DbUserEntity getAssignedToEntity() {
        return assignedTo;
    }

    @JsonIgnore
    public DbUserEntity getAssignedByEntity() {
        return assignedBy;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setItemType(InventoryItemType itemType) {
        this.itemType = itemType;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setBoughtDate(LocalDate boughtDate) {
        this.boughtDate = boughtDate;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setPriority(InventoryPriority priority) {
        this.priority = priority;
    }

    public void setAuthor(DbUserEntity author) {
        this.author = author;
    }

    public void setAssignedTo(DbUserEntity assignedTo) {
        this.assignedTo = assignedTo;
    }

    public void setAssignedBy(DbUserEntity assignedBy) {
        this.assignedBy = assignedBy;
    }

    public void setStatus(InventoryStatus status) {
        this.status = status;
    }
}