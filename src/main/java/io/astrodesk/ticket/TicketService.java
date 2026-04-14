package io.astrodesk.ticket;

import io.astrodesk.inventory.Inventory;
import io.astrodesk.inventory.InventoryRepository;
import io.astrodesk.user.DbUserEntity;
import io.astrodesk.user.UserRepository;
import io.astrodesk.user.UserService;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final InventoryRepository inventoryRepository;

    public TicketService(TicketRepository ticketRepository, UserRepository userRepository, UserService userService, InventoryRepository inventoryRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.inventoryRepository = inventoryRepository;
    }

    public TicketEntity saveTicket(String title, String description, TicketPriority priority, DbUserEntity author, DbUserEntity assignedTo, Inventory asset) {
        TicketEntity ticketEntity = new TicketEntity(title, description, priority, author, assignedTo, asset);
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

    public TicketEntity createTicket(TicketEntity ticket, Authentication authentication, Long linkedInventoryId) {
        String username = authentication.getName();
        DbUserEntity author = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Inventory asset = null;
        if(linkedInventoryId != null) {
            asset = inventoryRepository.findById(linkedInventoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Asset not found"));

        }

        return saveTicket(ticket.getTitle(), ticket.getDescription(), ticket.getPriority(), author, null, asset);

    }

    public TicketEntity updateTicketAttributes(long id, String title, String description, TicketPriority priority, Long assetId) {
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
        if(assetId != null) {
            Inventory asset = inventoryRepository.findById(assetId)
                            .orElseThrow(() -> new IllegalArgumentException("Asset not found"));
            ticket.setLinkedInventoryId(asset);
        }
        return ticketRepository.save(ticket);
    }


    public TicketEntity addAssignee(long ticketId, long userId) {
        TicketEntity ticket = getTicket(ticketId);
        List<DbUserEntity> admins = userService.showTicketAdmins();
        DbUserEntity assignee = null;
        for(DbUserEntity user : admins) {
            if(userId == user.getUserId()) {
                assignee = user;
                ticket.setAssignedTo(assignee);
                break;
            }
            if(assignee == null) {
                throw new IllegalArgumentException("User unauthorized");
            }
        }

        return ticketRepository.save(ticket);
    }

}
