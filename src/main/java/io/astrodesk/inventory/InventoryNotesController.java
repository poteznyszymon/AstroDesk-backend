package io.astrodesk.inventory;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory/{inventoryId}/notes")
public class InventoryNotesController {

    private final InventoryNotesService inventoryNotesService;

    public InventoryNotesController(InventoryNotesService inventoryNotesService) {
        this.inventoryNotesService = inventoryNotesService;
    }

    @GetMapping
    public List<InventoryNotes> getNotes(
            @PathVariable Long inventoryId,
            Authentication authentication
    ) {
        return inventoryNotesService.getNotes(inventoryId, authentication);
    }

    @PostMapping
    public InventoryNotes addNote(
            @PathVariable Long inventoryId,
            @RequestBody InventoryNotes request,
            Authentication authentication
    ) {
        return inventoryNotesService.addNote(
                inventoryId,
                request.getContent(),
                authentication
        );
    }

    @PatchMapping("/{noteId}")
    public InventoryNotes updateNote(
            @PathVariable Long inventoryId,
            @PathVariable Long noteId,
            @RequestBody InventoryNotes request,
            Authentication authentication
    ) {
        return inventoryNotesService.updateNote(
                inventoryId,
                noteId,
                request.getContent(),
                authentication
        );
    }

    @DeleteMapping("/{noteId}")
    public void deleteNote(
            @PathVariable Long inventoryId,
            @PathVariable Long noteId,
            Authentication authentication
    ) {
        inventoryNotesService.deleteNote(inventoryId, noteId, authentication);
    }
}