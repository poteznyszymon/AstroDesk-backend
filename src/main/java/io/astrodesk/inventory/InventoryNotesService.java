package io.astrodesk.inventory;

import io.astrodesk.history.HistoryService;
import io.astrodesk.history.HistoryTargetType;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class InventoryNotesService {

    private final InventoryRepository inventoryRepository;
    private final InventoryNotesRepository inventoryNotesRepository;
    private final HistoryService historyService;
    private final InventoryService inventoryService;

    public InventoryNotesService(
            InventoryRepository inventoryRepository,
            InventoryNotesRepository inventoryNotesRepository,
            HistoryService historyService,
            InventoryService inventoryService
    ) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryNotesRepository = inventoryNotesRepository;
        this.historyService = historyService;
        this.inventoryService = inventoryService;
    }


    public List<InventoryNotes> getNotes(Long inventoryId, Authentication authentication) {
        inventoryService.getInventory(inventoryId, authentication);

        return inventoryNotesRepository.findByInventoryIdOrderByCreatedAtDesc(inventoryId);
    }

    public InventoryNotes addNote(Long inventoryId, String content, Authentication authentication) {
        Inventory inventory = inventoryService.getInventory(inventoryId, authentication);
        String author = getCurrentUsername(authentication);

        InventoryNotes note = new InventoryNotes(content, author, inventory);
        InventoryNotes savedNote = inventoryNotesRepository.save(note);

        historyService.saveMessage(
                HistoryTargetType.INVENTORY,
                inventory.getId(),
                "Dodano notatkę",
                getCurrentUsername(authentication)
        );

        return savedNote;
    }

    public InventoryNotes updateNote(Long inventoryId, Long noteId, String content, Authentication authentication) {
        inventoryService.getInventory(inventoryId, authentication);

        InventoryNotes note = inventoryNotesRepository.findByIdAndInventoryId(noteId, inventoryId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        ErrorMessages.NOTES_NOT_FOUND
                ));

        String oldContent = note.getContent();
        note.setContent(content);
        InventoryNotes savedNote = inventoryNotesRepository.save(note);

        historyService.saveMessage(
                HistoryTargetType.INVENTORY,
                inventoryId,
                "Zmieniono notatkę: " + oldContent + " -> " + content,
                getCurrentUsername(authentication)
        );

        return savedNote;

    }

    public void deleteNote(Long inventoryId, Long noteId, Authentication authentication) {
        inventoryService.getInventory(inventoryId, authentication);

        InventoryNotes note = inventoryNotesRepository.findByIdAndInventoryId(noteId, inventoryId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        ErrorMessages.NOTES_NOT_FOUND
                ));

        historyService.saveMessage(
                HistoryTargetType.INVENTORY,
                inventoryId,
                "Usunięto notatkę: " + note.getContent(),
                getCurrentUsername(authentication)
        );

        inventoryNotesRepository.delete(note);
    }

    private String getCurrentUsername(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        return authentication.getName();
    }
}
