package io.astrodesk.ticket;

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
    public List<TicketEntity> getAllTickets() {
        return ticketService.showTickets();
    }

    @GetMapping("/{id}")
    public TicketEntity getTicket(@PathVariable long id) {
        return ticketService.getTicket(id);
    }

    @PostMapping
    public TicketEntity createTicket(@RequestBody TicketEntity t) {
        return ticketService.saveTicket(t.getTitle(), t.getDescription(), t.getPriority(), t.getAuthor());
    }

}
