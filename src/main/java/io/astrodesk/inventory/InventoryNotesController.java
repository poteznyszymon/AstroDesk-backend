package io.astrodesk.inventory;

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
    public List<InventoryNotes> getNotes(@PathVariable Long inventoryId) {
        return inventoryNotesService.getNotes(inventoryId);
    }

    @PostMapping
    public InventoryNotes addNote(
            @PathVariable Long inventoryId,
            @RequestBody InventoryNotes request
    ) {
        return inventoryNotesService.addNote(
                inventoryId,
                request.getContent(),
                request.getAuthor()
        );
    }

    @PatchMapping("/{noteId}")
    public InventoryNotes updateNote(
            @PathVariable Long inventoryId,
            @PathVariable Long noteId,
            @RequestBody InventoryNotes request
    ) {
        return inventoryNotesService.updateNote(
                inventoryId,
                noteId,
                request.getContent()
        );
    }

    @DeleteMapping("/{noteId}")
    public void deleteNote(
            @PathVariable Long inventoryId,
            @PathVariable Long noteId
    ) {
        inventoryNotesService.deleteNote(inventoryId, noteId);
    }
}