package io.astrodesk.inventory;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class InventoryService {

    private final InventoryRepository repository;

    public InventoryService(InventoryRepository repository) {
        this.repository = repository;
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
            String author,
            String notes
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
                author,
                notes
        );

        repository.save(inventory);
        return inventory;
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
        inventory.assign(assignedTo, assignedBy);
        return repository.save(inventory);
    }

    public Inventory returnInventory(long id) {
        Inventory inventory = getInventory(id);
        inventory.returnToStock();
        return repository.save(inventory);
    }

    public Inventory sendToService(long id) {
        Inventory inventory = getInventory(id);
        inventory.sendToService();
        return repository.save(inventory);
    }

    public Inventory disposeInventory(long id) {
        Inventory inventory = getInventory(id);
        inventory.markAsDisposed();
        return repository.save(inventory);
    }
    public Inventory updateInventoryPartial(long id, Inventory updatedInventory) {
        Inventory inventory = getInventory(id);

        if (updatedInventory.getName() != null) {
            inventory.setName(updatedInventory.getName());
        }
        if (updatedInventory.getItemType() != null) {
            inventory.setItemType(updatedInventory.getItemType());
        }
        if (updatedInventory.getSerialNumber() != null) {
            inventory.setSerialNumber(updatedInventory.getSerialNumber());
        }
        if (updatedInventory.getModel() != null) {
            inventory.setModel(updatedInventory.getModel());
        }
        if (updatedInventory.getBoughtDate() != null) {
            inventory.setBoughtDate(updatedInventory.getBoughtDate());
        }
        if (updatedInventory.getPrice() != null) {
            inventory.setPrice(updatedInventory.getPrice());
        }
        if (updatedInventory.getInvoiceNumber() != null) {
            inventory.setInvoiceNumber(updatedInventory.getInvoiceNumber());
        }
        if (updatedInventory.getLocation() != null) {
            inventory.setLocation(updatedInventory.getLocation());
        }
        if (updatedInventory.getPriority() != null) {
            inventory.setPriority(updatedInventory.getPriority());
        }
        if (updatedInventory.getAuthor() != null) {
            inventory.setAuthor(updatedInventory.getAuthor());
        }
        if (updatedInventory.getNotes() != null) {
            inventory.setNotes(updatedInventory.getNotes());
        }

        return repository.save(inventory);
    }
}