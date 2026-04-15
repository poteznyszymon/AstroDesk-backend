package io.astrodesk.inventory;

import io.astrodesk.history.HistoryService;
import io.astrodesk.history.HistoryTargetType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
public class InventoryService {

    private final InventoryRepository repository;
    private final HistoryService historyService;

    public InventoryService(
            InventoryRepository repository,
            HistoryService historyService
    ) {
        this.repository = repository;
        this.historyService = historyService;
    }

    public Inventory saveInventory(
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
        Inventory inventory = new Inventory(
                name,
                itemType,
                serialNumber,
                model,
                boughtDate,
                price,
                invoiceNumber,
                location,
                priority,
                author
        );

        Inventory savedInventory = repository.save(inventory);

        historyService.saveMessage(
                HistoryTargetType.INVENTORY,
                savedInventory.getId(),
                "Utworzono urządzenie",
                author
        );

        return savedInventory;
    }

    public List<Inventory> showInventory() {
        return repository.findAll();
    }

    public Inventory getInventory(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        ErrorMessages.INVENTORY_NOT_FOUND
                ));
    }

    public Inventory assignInventory(long id, String assignedTo, String assignedBy) {
        Inventory inventory = getInventory(id);

        String oldAssignedTo = inventory.getAssignedTo();
        String oldStatus = inventory.getStatus() != null ? inventory.getStatus().name() : null;

        inventory.assign(assignedTo, assignedBy);
        Inventory savedInventory = repository.save(inventory);

        historyService.saveFieldChange(
                HistoryTargetType.INVENTORY,
                savedInventory.getId(),
                "assignedTo",
                oldAssignedTo,
                savedInventory.getAssignedTo(),
                assignedBy
        );

        historyService.saveFieldChange(
                HistoryTargetType.INVENTORY,
                savedInventory.getId(),
                "status",
                oldStatus,
                savedInventory.getStatus() != null ? savedInventory.getStatus().name() : null,
                assignedBy
        );

        historyService.saveMessage(
                HistoryTargetType.INVENTORY,
                savedInventory.getId(),
                "Przypisano urządzenie do: " + assignedTo,
                assignedBy
        );

        return savedInventory;
    }

    public Inventory returnInventory(long id) {
        Inventory inventory = getInventory(id);

        String changedBy = inventory.getAssignedBy() != null ? inventory.getAssignedBy() : inventory.getAuthor();
        String oldAssignedTo = inventory.getAssignedTo();
        String oldStatus = inventory.getStatus() != null ? inventory.getStatus().name() : null;

        inventory.returnToStock();
        Inventory savedInventory = repository.save(inventory);

        historyService.saveFieldChange(
                HistoryTargetType.INVENTORY,
                savedInventory.getId(),
                "assignedTo",
                oldAssignedTo,
                savedInventory.getAssignedTo(),
                changedBy
        );

        historyService.saveFieldChange(
                HistoryTargetType.INVENTORY,
                savedInventory.getId(),
                "status",
                oldStatus,
                savedInventory.getStatus() != null ? savedInventory.getStatus().name() : null,
                changedBy
        );

        historyService.saveMessage(
                HistoryTargetType.INVENTORY,
                savedInventory.getId(),
                "Zwrócono urządzenie do magazynu",
                changedBy
        );

        return savedInventory;
    }

    public Inventory sendToService(long id) {
        Inventory inventory = getInventory(id);

        String changedBy = inventory.getAuthor();
        String oldStatus = inventory.getStatus() != null ? inventory.getStatus().name() : null;

        inventory.sendToService();
        Inventory savedInventory = repository.save(inventory);

        historyService.saveFieldChange(
                HistoryTargetType.INVENTORY,
                savedInventory.getId(),
                "status",
                oldStatus,
                savedInventory.getStatus() != null ? savedInventory.getStatus().name() : null,
                changedBy
        );

        historyService.saveMessage(
                HistoryTargetType.INVENTORY,
                savedInventory.getId(),
                "Przekazano urządzenie do serwisu",
                changedBy
        );

        return savedInventory;
    }

    public Inventory disposeInventory(long id) {
        Inventory inventory = getInventory(id);

        String changedBy = inventory.getAuthor();
        String oldStatus = inventory.getStatus() != null ? inventory.getStatus().name() : null;

        inventory.markAsDisposed();
        Inventory savedInventory = repository.save(inventory);

        historyService.saveFieldChange(
                HistoryTargetType.INVENTORY,
                savedInventory.getId(),
                "status",
                oldStatus,
                savedInventory.getStatus() != null ? savedInventory.getStatus().name() : null,
                changedBy
        );

        historyService.saveMessage(
                HistoryTargetType.INVENTORY,
                savedInventory.getId(),
                "Oznaczono urządzenie jako zutylizowane",
                changedBy
        );

        return savedInventory;
    }

    public Inventory updateInventoryPartial(long id, Inventory updatedInventory) {
        Inventory inventory = getInventory(id);

        String changedBy = updatedInventory.getAuthor() != null
                ? updatedInventory.getAuthor()
                : inventory.getAuthor();

        if (updatedInventory.getName() != null) {
            historyService.saveFieldChange(
                    HistoryTargetType.INVENTORY,
                    inventory.getId(),
                    "name",
                    inventory.getName(),
                    updatedInventory.getName(),
                    changedBy
            );
            inventory.setName(updatedInventory.getName());
        }

        if (updatedInventory.getItemType() != null) {
            historyService.saveFieldChange(
                    HistoryTargetType.INVENTORY,
                    inventory.getId(),
                    "itemType",
                    inventory.getItemType(),
                    updatedInventory.getItemType(),
                    changedBy
            );
            inventory.setItemType(updatedInventory.getItemType());
        }

        if (updatedInventory.getSerialNumber() != null) {
            historyService.saveFieldChange(
                    HistoryTargetType.INVENTORY,
                    inventory.getId(),
                    "serialNumber",
                    inventory.getSerialNumber(),
                    updatedInventory.getSerialNumber(),
                    changedBy
            );
            inventory.setSerialNumber(updatedInventory.getSerialNumber());
        }

        if (updatedInventory.getModel() != null) {
            historyService.saveFieldChange(
                    HistoryTargetType.INVENTORY,
                    inventory.getId(),
                    "model",
                    inventory.getModel(),
                    updatedInventory.getModel(),
                    changedBy
            );
            inventory.setModel(updatedInventory.getModel());
        }

        if (updatedInventory.getBoughtDate() != null) {
            historyService.saveFieldChange(
                    HistoryTargetType.INVENTORY,
                    inventory.getId(),
                    "boughtDate",
                    inventory.getBoughtDate(),
                    updatedInventory.getBoughtDate(),
                    changedBy
            );
            inventory.setBoughtDate(updatedInventory.getBoughtDate());
        }

        if (updatedInventory.getPrice() != null) {
            historyService.saveFieldChange(
                    HistoryTargetType.INVENTORY,
                    inventory.getId(),
                    "price",
                    inventory.getPrice(),
                    updatedInventory.getPrice(),
                    changedBy
            );
            inventory.setPrice(updatedInventory.getPrice());
        }

        if (updatedInventory.getInvoiceNumber() != null) {
            historyService.saveFieldChange(
                    HistoryTargetType.INVENTORY,
                    inventory.getId(),
                    "invoiceNumber",
                    inventory.getInvoiceNumber(),
                    updatedInventory.getInvoiceNumber(),
                    changedBy
            );
            inventory.setInvoiceNumber(updatedInventory.getInvoiceNumber());
        }

        if (updatedInventory.getLocation() != null) {
            historyService.saveFieldChange(
                    HistoryTargetType.INVENTORY,
                    inventory.getId(),
                    "location",
                    inventory.getLocation(),
                    updatedInventory.getLocation(),
                    changedBy
            );
            inventory.setLocation(updatedInventory.getLocation());
        }

        if (updatedInventory.getPriority() != null) {
            historyService.saveFieldChange(
                    HistoryTargetType.INVENTORY,
                    inventory.getId(),
                    "priority",
                    inventory.getPriority(),
                    updatedInventory.getPriority(),
                    changedBy
            );
            inventory.setPriority(updatedInventory.getPriority());
        }

        if (updatedInventory.getStatus() != null) {
            historyService.saveFieldChange(
                    HistoryTargetType.INVENTORY,
                    inventory.getId(),
                    "status",
                    inventory.getStatus() != null ? inventory.getStatus().name() : null,
                    updatedInventory.getStatus().name(),
                    changedBy
            );
            inventory.setStatus(updatedInventory.getStatus());
        }

        if (updatedInventory.getAuthor() != null && !Objects.equals(inventory.getAuthor(), updatedInventory.getAuthor())) {
            historyService.saveFieldChange(
                    HistoryTargetType.INVENTORY,
                    inventory.getId(),
                    "author",
                    inventory.getAuthor(),
                    updatedInventory.getAuthor(),
                    changedBy
            );
            inventory.setAuthor(updatedInventory.getAuthor());
        }

        return repository.save(inventory);
    }

    public void deleteInventory(long id, String deletedBy) {
        Inventory inventory = getInventory(id);

        historyService.saveMessage(
                HistoryTargetType.INVENTORY,
                inventory.getId(),
                "Usunięto urządzenie",
                deletedBy
        );

        repository.delete(inventory);
    }
}