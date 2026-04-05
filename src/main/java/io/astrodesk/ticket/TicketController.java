package io.astrodesk.ticket;

import io.astrodesk.user.DbUserEntity;
import io.astrodesk.user.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final UserRepository userRepository;

    public TicketController(TicketService ticketService, UserRepository userRepository) {
        this.ticketService = ticketService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<TicketEntity> getAllTickets() {
        return ticketService.showTickets();
    }

    @GetMapping("/{id}")
    public TicketEntity getTicket(@PathVariable long id) {
        return ticketService.getTicket(id);
    }

    @PostMapping("/add")
    public TicketEntity createTicket(@RequestBody TicketEntity t) {
        DbUserEntity author = userRepository.findById(t.getAuthor()).orElseThrow(() -> new IllegalArgumentException("User not found!"));
        return ticketService.saveTicket(t.getTitle(), t.getDescription(), t.getPriority(), author);
    }

}
