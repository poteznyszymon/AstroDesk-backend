package io.astrodesk.ticket;

import io.astrodesk.user.DbUserEntity;
import io.astrodesk.user.UserDTO;
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
    public ResponseEntity<TicketEntity> addTicket(@RequestBody TicketRequest request, Authentication authentication) {
        TicketEntity ticket = new TicketEntity();
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setPriority(request.getPriority());

        TicketEntity savedTicket = ticketService.createTicket(ticket, authentication, request.getLinkedInventoryId());
        return ResponseEntity.ok(savedTicket);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TICKET_ADMIN', 'HEADADMIN')")
    public ResponseEntity<Void> deleteTicket(@PathVariable long id, Authentication authentication) {
        ticketService.deleteTicket(id, authentication);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TicketEntity> updateTicket(@PathVariable long id, @RequestBody TicketEntity update, Authentication authentication) {
        TicketEntity updated = ticketService.updateTicketAttributes(id, update.getTitle(), update.getDescription(), update.getPriority(), update.getLinkedInventoryId(), authentication);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/accept")
    @PreAuthorize("hasAnyRole('TICKET_ADMIN', 'HEADADMIN')")
    public ResponseEntity<TicketEntity> acceptTicket(@PathVariable long id, Authentication authentication) {
        return ResponseEntity.ok(ticketService.acceptTicket(id, authentication));
    }

    @PatchMapping("/{id}/open")
    @PreAuthorize("hasAnyRole('TICKET_ADMIN', 'HEADADMIN')")
    public ResponseEntity<TicketEntity> openTicketStatus(@PathVariable long id, Authentication authentication) {
        return ResponseEntity.ok(ticketService.startTicket(id, authentication));
    }

    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('TICKET_ADMIN', 'HEADADMIN')")
    public ResponseEntity<TicketEntity> resolveTicketStatus(@PathVariable long id, Authentication authentication) {
        return ResponseEntity.ok(ticketService.resolveTicket(id, authentication));
    }

    @PatchMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('TICKET_ADMIN', 'HEADADMIN')")
    public ResponseEntity<TicketEntity> closeTicketStatus(@PathVariable long id, Authentication authentication) {
        return ResponseEntity.ok(ticketService.closeTicket(id, authentication));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('TICKET_ADMIN', 'HEADADMIN')")
    public ResponseEntity<TicketEntity> cancelTicketStatus(@PathVariable long id, Authentication authentication) {
        return ResponseEntity.ok(ticketService.cancelTicket(id, authentication));
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('TICKET_ADMIN', 'HEADADMIN')")
    public ResponseEntity<TicketEntity> assignToTicket(@PathVariable long id, @RequestBody DbUserEntity assignee, Authentication authentication) {
        return ResponseEntity.ok(ticketService.addAssignee(id, assignee.getUserId(), authentication));
    }
}
