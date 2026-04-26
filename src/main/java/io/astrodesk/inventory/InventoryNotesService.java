package io.astrodesk.inventory;

import io.astrodesk.history.HistoryService;
import io.astrodesk.history.HistoryTargetType;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class InventoryNotesService {

    private final InventoryRepository inventoryRepository;
    private final InventoryNotesRepository inventoryNotesRepository;
    private final HistoryService historyService;

    public InventoryNotesService(
            InventoryRepository inventoryRepository,
            InventoryNotesRepository inventoryNotesRepository,
            HistoryService historyService
    ) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryNotesRepository = inventoryNotesRepository;
        this.historyService = historyService;
    }


    public List<InventoryNotes> getNotes(Long inventoryId) {
        if (!inventoryRepository.existsById(inventoryId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    ErrorMessages.INVENTORY_NOT_FOUND
            );
        }

        return inventoryNotesRepository.findByInventoryIdOrderByCreatedAtDesc(inventoryId);
    }

    public InventoryNotes addNote(Long inventoryId, String content, String author) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        ErrorMessages.INVENTORY_NOT_FOUND
                ));

        InventoryNotes note = new InventoryNotes(content, author, inventory);
        InventoryNotes savedNote = inventoryNotesRepository.save(note);

        historyService.saveMessage(
                HistoryTargetType.INVENTORY,
                inventory.getId(),
                "Dodano notatkę",
                getCurrentUsername()
        );

        return savedNote;
    }

    public InventoryNotes updateNote(Long inventoryId, Long noteId, String content) {
        InventoryNotes note = inventoryNotesRepository.findByIdAndInventoryId(noteId, inventoryId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        ErrorMessages.NOTES_NOT_FOUND
                ));

        note.setContent(content);

        return inventoryNotesRepository.save(note);

    }

    public void deleteNote(Long inventoryId, Long noteId ) {
        InventoryNotes note = inventoryNotesRepository.findByIdAndInventoryId(noteId, inventoryId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        ErrorMessages.NOTES_NOT_FOUND
                ));

        historyService.saveMessage(
                HistoryTargetType.INVENTORY,
                inventoryId,
                "Usunięto notatkę: " + note.getContent(),
                getCurrentUsername()
        );

        inventoryNotesRepository.delete(note);
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        return authentication.getName();
    }
}
