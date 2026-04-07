package io.astrodesk.ticket;

import io.astrodesk.user.DbUserEntity;
import io.astrodesk.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public TicketService(TicketRepository ticketRepository, UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    public TicketEntity saveTicket(String title, String description, TicketPriority priority, DbUserEntity author) {
        TicketEntity ticketEntity = new TicketEntity(title, description, priority, author);
        ticketRepository.save(ticketEntity);
        return ticketEntity;
    }

    public TicketEntity acceptTicket(long ticketId) {
        TicketEntity ticket = getTicket(ticketId);
        ticket.accept();
        ticketRepository.save(ticket);
        return ticket;
    }

    public TicketEntity startTicket(long ticketId) {
        TicketEntity ticket = getTicket(ticketId);
        ticket.startProgress();
        ticketRepository.save(ticket);
        return ticket;
    }

    public TicketEntity resolveTicket(long ticketId) {
        TicketEntity ticket = getTicket(ticketId);
        ticket.resolve();
        ticketRepository.save(ticket);
        return ticket;
    }

    public TicketEntity closeTicket(long ticketId) {
        TicketEntity ticket = getTicket(ticketId);
        ticket.close();
        ticketRepository.save(ticket);
        return ticket;
    }

    public TicketEntity cancelTicket(long ticketId) {
        TicketEntity ticket = getTicket(ticketId);
        ticket.cancel();
        ticketRepository.save(ticket);
        return ticket;
    }
/*
    public TicketEntity createSampleTicket_1() {
        return saveTicket("Broken Computer", "dawdwwada", TicketPriority.MEDIUM, "Jake Kowalski");
    }

    public TicketEntity createSampleTicket_2() {
        return saveTicket("Network problem", "jdwkjwada", TicketPriority.HIGH, "Amanda Nowak");
    }

    public TicketEntity createSampleTicket_3() {
        return saveTicket("Bad Environment", "fjwafjoaa", TicketPriority.LOW, "Micheal Krasinski");
    }
*/

    public List<TicketEntity> showTickets() {
        return ticketRepository.findAll();
    }

    public TicketEntity getTicket(long id) {
        return ticketRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Ticket not found"));
    }

    public void deleteTicket(long id) {
        if(!ticketRepository.existsById(id)) {
            throw new IllegalArgumentException("Ticket does not exist");
        }
        ticketRepository.deleteById(id);
    }

    public TicketEntity createTicket(TicketEntity ticket, Authentication authentication) {
        String username = authentication.getName();
        DbUserEntity author = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return saveTicket(ticket.getTitle(), ticket.getDescription(), ticket.getPriority(), author);

    }

    public TicketEntity updateTicketAttributes(long id, String title, String description, TicketPriority priority) {
        TicketEntity ticket = getTicket(id);
        if(title != null) {
            ticket.setTitle(title);
        }
        if(description != null) {
            ticket.setDescription(description);
        }
        if(priority != null) {
            ticket.setPriority(priority);
        }
        return ticketRepository.save(ticket);
    }
}
