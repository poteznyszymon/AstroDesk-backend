package io.astrodesk.ticket;

import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Tymczasowa klasa repozytorium do testow.

@Repository
public class TicketRepository {
    private final List<Ticket> tickets = new ArrayList<>();

    public void saveTicket(Ticket ticket) {
        tickets.add(ticket);
    }

    public List<Ticket> findALLTickets() {
        return tickets;
    }

    public Optional<Ticket> findById(int id) {
        for(Ticket ticket : tickets) {
            if(id == ticket.getId())
            {
                return Optional.of(ticket);
            }
        }
        return Optional.empty();
    }
}
