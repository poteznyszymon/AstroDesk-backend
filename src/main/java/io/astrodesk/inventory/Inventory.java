package io.astrodesk.inventory;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

    public List<InventoryNotes> getNotes() {
        return notes;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    // podstawowe informacje
    @NotBlank
    @Column(nullable = false, length = 100)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    private InventoryItemType itemType;

    // dane sprzętu
    @NotBlank
    @Column(nullable = false, unique = true)
    private String serialNumber;

    private String model;

    // zakup
    private LocalDate boughtDate;
    private Double price;
    private String invoiceNumber;

    // lokalizacja i przypisanie
    private String location;
    private String assignedTo;
    private String assignedBy;
    private LocalDate assignedDate;

    // status
    @Enumerated(EnumType.STRING)
    private InventoryStatus status;

    @Enumerated(EnumType.STRING)
    private InventoryPriority priority;

    // administracyjne
    @NotBlank
    private String author;



    // daty systemowe
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
            String author
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

    public void assign(String assignedTo, String assignedBy) {
        if (status != InventoryStatus.DOSTEPNE) {
            throw new IllegalStateException("Inventory item must be DOSTEPNE to assign");
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

    public String getAssignedTo() {
        return assignedTo;
    }

    public String getAssignedBy() {
        return assignedBy;
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

    public String getAuthor() {
        return author;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
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

    public void setAuthor(String author) {
        this.author = author;
    }

}