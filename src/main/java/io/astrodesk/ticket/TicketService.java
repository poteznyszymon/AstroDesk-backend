package io.astrodesk.ticket;

import io.astrodesk.user.DbUserEntity;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class TicketService {

    private final TicketRepository repository;

    public TicketService(TicketRepository repository) {
        this.repository = repository;
    }

    public TicketEntity saveTicket(String title, String description, TicketPriority priority, DbUserEntity author) {
        TicketEntity ticketEntity = new TicketEntity(title, description, priority, author);
        repository.save(ticketEntity);
        return ticketEntity;
    }

    public void acceptTicket(int ticketId) {
        TicketEntity ticket = getTicket(ticketId);
        ticket.accept();
        repository.save(ticket);
    }

    public void startTicket(int ticketId) {
        TicketEntity ticket = getTicket(ticketId);
        ticket.startProgress();
        repository.save(ticket);
    }

    public void resolveTicket(int ticketId) {
        TicketEntity ticket = getTicket(ticketId);
        ticket.resolve();
        repository.save(ticket);
    }

    public void closeTicket(int ticketId) {
        TicketEntity ticket = getTicket(ticketId);
        ticket.close();
        repository.save(ticket);
    }

    public void cancelTicket(int ticketId) {
        TicketEntity ticket = getTicket(ticketId);
        ticket.cancel();
        repository.save(ticket);
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
        return repository.findAll();
    }

    public TicketEntity getTicket(long id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Ticket not found!"));
    }
}
