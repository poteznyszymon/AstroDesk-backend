package io.astrodesk.inventory;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public List<Inventory> getAllInventory() {
        return inventoryService.showInventory();
    }

    @GetMapping("/{id}")
    public Inventory getInventory(@PathVariable long id)
    {
        return inventoryService.getInventory(id);
    }
    @PatchMapping("/{id}")
    public Inventory updateInventoryPartial(
            @PathVariable long id,
            @RequestBody Inventory updated
    ) {
        return inventoryService.updateInventoryPartial(id, updated);
    }

    @PostMapping
    public Inventory createInventory(@RequestBody Inventory inventory) {
        return inventoryService.saveInventory(
                inventory.getName(),
                inventory.getItemType(),
                inventory.getSerialNumber(),
                inventory.getModel(),
                inventory.getBoughtDate(),
                inventory.getPrice(),
                inventory.getInvoiceNumber(),
                inventory.getLocation(),
                inventory.getPriority(),
                inventory.getAuthor(),
                inventory.getNotes()
        );
    }

    @PatchMapping("/{id}/assign")
    public Inventory assignInventory(
            @PathVariable long id,
            @RequestParam String assignedTo,
            @RequestParam String assignedBy
    ) {
        return inventoryService.assignInventory(id, assignedTo, assignedBy);
    }

    @PatchMapping("/{id}/return")
    public Inventory returnInventory(@PathVariable long id) {
        return inventoryService.returnInventory(id);
    }

    @PatchMapping("/{id}/service")
    public Inventory sendToService(@PathVariable long id) {
        return inventoryService.sendToService(id);
    }

    @PatchMapping("/{id}/dispose")
    public Inventory disposeInventory(@PathVariable long id) {
        return inventoryService.disposeInventory(id);
    }
}