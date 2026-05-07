package io.astrodesk.inventory;

import io.astrodesk.user.DbUserEntity;
import io.astrodesk.user.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;
    private final UserRepository userRepository;

    public InventoryController(
            InventoryService inventoryService,
            UserRepository userRepository
    ) {
        this.inventoryService = inventoryService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<Inventory> getAllInventory(Authentication authentication) {
        return inventoryService.showInventory(authentication);
    }

    @GetMapping("/assignable")
    public List<AssignableInventoryDTO> getAssignableInventory(Authentication authentication) {
        return inventoryService.getAssignableInventory(authentication);
    }

    @GetMapping("/{id}")
    public Inventory getInventory(
            @PathVariable long id,
            Authentication authentication
    ) {
        return inventoryService.getInventory(id, authentication);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ASSET_ADMIN', 'HEADADMIN')")
    public Inventory updateInventoryPartial(
            @PathVariable long id,
            @RequestBody Inventory updated,
            Authentication authentication
    ) {
        return inventoryService.updateInventoryPartial(id, updated, authentication);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ASSET_ADMIN', 'HEADADMIN')")
    public void deleteInventory(
            @PathVariable Long id,
            Authentication authentication
    ) {
        inventoryService.deleteInventory(id, authentication.getName());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ASSET_ADMIN', 'HEADADMIN')")
    public Inventory createInventory(
            @RequestBody Inventory inventory,
            Authentication authentication
    ) {
        DbUserEntity author = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

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
                author
        );
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ASSET_ADMIN', 'HEADADMIN')")
    public Inventory assignInventory(
            @PathVariable long id,
            @RequestParam String assignedTo,
            Authentication authentication
    ) {
        return inventoryService.assignInventory(id, assignedTo, authentication.getName());
    }

    @PatchMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('ASSET_ADMIN', 'HEADADMIN')")
    public Inventory returnInventory(
            @PathVariable long id,
            Authentication authentication
    ) {
        return inventoryService.returnInventory(id, authentication.getName());
    }

    @PatchMapping("/{id}/service")
    @PreAuthorize("hasAnyRole('ASSET_ADMIN', 'HEADADMIN')")
    public Inventory sendToService(
            @PathVariable long id,
            Authentication authentication
    ) {
        return inventoryService.sendToService(id, authentication.getName());
    }

    @PatchMapping("/{id}/dispose")
    @PreAuthorize("hasAnyRole('ASSET_ADMIN', 'HEADADMIN')")
    public Inventory disposeInventory(
            @PathVariable long id,
            Authentication authentication
    ) {
        return inventoryService.disposeInventory(id, authentication.getName());
    }
}
