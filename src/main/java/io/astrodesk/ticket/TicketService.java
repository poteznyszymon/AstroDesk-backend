package io.astrodesk.ticket;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketService {

    private final TicketRepository repository;

    public TicketService(TicketRepository repository) {
        this.repository = repository;
    }

    public Ticket createTicket(int id, String title, String description, TicketPriority priority, String author) {
        Ticket ticket = new Ticket(id, title, description, priority, author);
        repository.saveTicket(ticket);
        return ticket;
    }

    public void acceptTicket(int ticketId) {
        Ticket ticket = getTicket(ticketId);
        ticket.accept();
    }

    public void startTicket(int ticketId) {
        Ticket ticket = getTicket(ticketId);
        ticket.startProgress();
    }

    public void resolveTicket(int ticketId) {
        Ticket ticket = getTicket(ticketId);
        ticket.resolve();
    }

    public void closeTicket(int ticketId) {
        Ticket ticket = getTicket(ticketId);
        ticket.close();
    }

    public void cancelTicket(int ticketId) {
        Ticket ticket = getTicket(ticketId);
        ticket.cancel();
    }

    public Ticket createSampleTicket_1() {
        return createTicket(1, "Broken Computer", "dawdwwada", TicketPriority.MEDIUM, "Jake Kowalski");
    }

    public Ticket createSampleTicket_2() {
        return createTicket(2, "Network problem", "jdwkjwada", TicketPriority.HIGH, "Amanda Nowak");
    }

    public Ticket createSampleTicket_3() {
        return createTicket(3, "Bad Environment", "fjwafjoaa", TicketPriority.LOW, "Micheal Krasinski");
    }

    public Ticket getTicket(int id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Ticket not found"));
    }

    public List<Ticket> showTickets() {
        return repository.findALLTickets();
    }
}
