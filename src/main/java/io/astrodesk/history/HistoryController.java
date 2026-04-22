package io.astrodesk.history;

import io.astrodesk.inventory.InventoryService;
import io.astrodesk.ticket.TicketService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public class HistoryController {

    private final HistoryService historyService;
    private final InventoryService inventoryService;
    private final TicketService ticketService;

    public HistoryController(
            HistoryService historyService,
            InventoryService inventoryService,
            TicketService ticketService
    ) {
        this.historyService = historyService;
        this.inventoryService = inventoryService;
        this.ticketService = ticketService;
    }


    @GetMapping("/inventory/{id}/history")
    public List<HistoryEntry> getInventoryHistory(@PathVariable Long id) {

        inventoryService.getInventory(id);

        return historyService.getHistory(
                HistoryTargetType.INVENTORY,
                id
        );
    }

    @GetMapping("/tickets/{id}/history")
    public List<HistoryEntry> getTicketHistory(@PathVariable Long id) {
        // sprawdzenie czy istnieje
        ticketService.getTicketEntity(id);

        return historyService.getHistory(
                HistoryTargetType.TICKET,
                id
        );
    }

    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('HEADADMIN', 'TICKET_ADMIN', 'ASSET_ADMIN')")
    public List<HistoryEntry> getAllHistory() {
        return historyService.getAllHistory();
    }
}
