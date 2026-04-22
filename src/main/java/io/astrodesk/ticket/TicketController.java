package io.astrodesk.ticket;

import io.astrodesk.user.DbUserEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final TicketMessageService ticketMessageService;

    public TicketController(TicketService ticketService, TicketMessageService ticketMessageService) {
        this.ticketService = ticketService;
        this.ticketMessageService = ticketMessageService;
    }

    @GetMapping
    public ResponseEntity<List<TicketDTO>> getAllTickets(Authentication authentication) {
        List<TicketDTO> tickets = ticketService.showTickets(authentication);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketDTO> getTicket(@PathVariable long id, Authentication authentication) {
        TicketDTO ticket = ticketService.getTicket(id, authentication);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<TicketMessageDTO>> getMessagesForTicket(@PathVariable long id, Authentication authentication) {
        List<TicketMessageDTO> messages = ticketMessageService.showMessagesForTicket(id, authentication);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/add")
    public ResponseEntity<TicketDTO> addTicket(@RequestBody TicketRequest request, Authentication authentication) {
        TicketEntity ticket = new TicketEntity();
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setPriority(request.getPriority());

        TicketDTO savedTicket = ticketService.createTicket(ticket, authentication, request.getLinkedInventoryId());
        return ResponseEntity.ok(savedTicket);
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<TicketMessageDTO> sendMessage(
            @PathVariable long id,
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        TicketMessageDTO message = ticketMessageService.addMessage(id, request.get("content"), authentication);
        return ResponseEntity.ok(message);
    }

    @DeleteMapping("/{id}/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable long id,
            @PathVariable long messageId,
            Authentication authentication) {
        ticketMessageService.deleteMessage(id, messageId, authentication);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/messages/{messageId}")
    public ResponseEntity<TicketMessageDTO> updateMessage(
            @PathVariable long id,
            @PathVariable long messageId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        TicketMessageDTO updated = ticketMessageService.updateMessage(id, messageId, request.get("content"), authentication);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TICKET_ADMIN', 'HEADADMIN')")
    public ResponseEntity<Void> deleteTicket(@PathVariable long id, Authentication authentication) {
        ticketService.deleteTicket(id, authentication);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TicketDTO> updateTicket(@PathVariable long id, @RequestBody TicketEntity update, Authentication authentication) {
        TicketDTO updated = ticketService.updateTicketAttributes(id, update.getTitle(), update.getDescription(), update.getPriority(), update.getLinkedInventoryId(), authentication);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/accept")
    @PreAuthorize("hasAnyRole('TICKET_ADMIN', 'HEADADMIN')")
    public ResponseEntity<TicketDTO> acceptTicket(@PathVariable long id, Authentication authentication) {
        return ResponseEntity.ok(ticketService.acceptTicket(id, authentication));
    }

    @PatchMapping("/{id}/open")
    @PreAuthorize("hasAnyRole('TICKET_ADMIN', 'HEADADMIN')")
    public ResponseEntity<TicketDTO> openTicketStatus(@PathVariable long id, Authentication authentication) {
        return ResponseEntity.ok(ticketService.startTicket(id, authentication));
    }

    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('TICKET_ADMIN', 'HEADADMIN')")
    public ResponseEntity<TicketDTO> resolveTicketStatus(@PathVariable long id, Authentication authentication) {
        return ResponseEntity.ok(ticketService.resolveTicket(id, authentication));
    }

    @PatchMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('TICKET_ADMIN', 'HEADADMIN')")
    public ResponseEntity<TicketDTO> closeTicketStatus(@PathVariable long id, Authentication authentication) {
        return ResponseEntity.ok(ticketService.closeTicket(id, authentication));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('TICKET_ADMIN', 'HEADADMIN')")
    public ResponseEntity<TicketDTO> cancelTicketStatus(@PathVariable long id, Authentication authentication) {
        return ResponseEntity.ok(ticketService.cancelTicket(id, authentication));
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('TICKET_ADMIN', 'HEADADMIN')")
    public ResponseEntity<TicketDTO> assignToTicket(@PathVariable long id, @RequestBody DbUserEntity assignee, Authentication authentication) {
        return ResponseEntity.ok(ticketService.addAssignee(id, assignee.getUserId(), authentication));
    }
}
