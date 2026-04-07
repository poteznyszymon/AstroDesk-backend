package io.astrodesk.ticket;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public ResponseEntity<List<TicketEntity>> getAllTickets() {
        List<TicketEntity> tickets = ticketService.showTickets();
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketEntity> getTicket(@PathVariable long id) {
        TicketEntity ticket = ticketService.getTicket(id);
        return ResponseEntity.ok(ticket);
    }

    @PostMapping("/add")
    public ResponseEntity<TicketEntity> addTicket(@RequestBody TicketEntity t, Authentication authentication) {
        TicketEntity savedTicket = ticketService.createTicket(t, authentication);
        return ResponseEntity.ok(savedTicket);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TICKET_ADMIN', 'HEADADMIN')")
    public ResponseEntity<Void> deleteTicket(@PathVariable long id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TicketEntity> updateTicket(@PathVariable long id, @RequestBody TicketEntity update) {
        TicketEntity updated = ticketService.updateTicketAttributes(id, update.getTitle(), update.getDescription(), update.getPriority());
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/accept")
    @PreAuthorize("hasAnyRole('TICKET_ADMIN', 'HEADADMIN')")
    public ResponseEntity<TicketEntity> acceptTicket(@PathVariable long id) {
        return ResponseEntity.ok(ticketService.acceptTicket(id));
    }

    @PatchMapping("/{id}/open")
    @PreAuthorize("hasAnyRole('TICKET_ADMIN', 'HEADADMIN')")
    public ResponseEntity<TicketEntity> openTicketStatus(@PathVariable long id) {
        return ResponseEntity.ok(ticketService.startTicket(id));
    }

    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('TICKET_ADMIN', 'HEADADMIN')")
    public ResponseEntity<TicketEntity> resolveTicketStatus(@PathVariable long id) {
        return ResponseEntity.ok(ticketService.resolveTicket(id));
    }

    @PatchMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('TICKET_ADMIN', 'HEADADMIN')")
    public ResponseEntity<TicketEntity> closeTicketStatus(@PathVariable long id) {
        return ResponseEntity.ok(ticketService.closeTicket(id));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('TICKET_ADMIN', 'HEADADMIN')")
    public ResponseEntity<TicketEntity> cancelTicketStatus(@PathVariable long id) {
        return ResponseEntity.ok(ticketService.cancelTicket(id));
    }
}
