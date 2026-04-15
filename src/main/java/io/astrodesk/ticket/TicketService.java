package io.astrodesk.ticket;

import io.astrodesk.history.HistoryService;
import io.astrodesk.history.HistoryTargetType;
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
    private final HistoryService historyService;

    public TicketService(TicketRepository ticketRepository, UserRepository userRepository, UserService userService,
                         InventoryRepository inventoryRepository, HistoryService historyService) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.inventoryRepository = inventoryRepository;
        this.historyService = historyService;
    }

    public TicketEntity saveTicket(String title, String description, TicketPriority priority, DbUserEntity author, DbUserEntity assignedTo, Inventory asset) {
        TicketEntity ticketEntity = new TicketEntity(title, description, priority, author, assignedTo, asset);
        ticketRepository.save(ticketEntity);
        return ticketEntity;
    }

    public TicketEntity acceptTicket(long ticketId, Authentication authentication) {
        TicketEntity ticket = getTicket(ticketId);
        TicketStatus oldStatus = ticket.getStatus();
        ticket.accept();
        ticketRepository.save(ticket);

        historyService.saveFieldChange(
                HistoryTargetType.TICKET,
                ticketId,
                "Zmiana statusu",
                oldStatus,
                ticket.getStatus(),
                authentication.getName()
        );
        return ticket;
    }

    public TicketEntity startTicket(long ticketId, Authentication authentication) {
        TicketEntity ticket = getTicket(ticketId);
        TicketStatus oldStatus = ticket.getStatus();
        ticket.startProgress();
        ticketRepository.save(ticket);

        historyService.saveFieldChange(
                HistoryTargetType.TICKET,
                ticketId,
                "Zmiana statusu",
                oldStatus,
                ticket.getStatus(),
                authentication.getName()
        );
        return ticket;
    }

    public TicketEntity resolveTicket(long ticketId, Authentication authentication) {
        TicketEntity ticket = getTicket(ticketId);
        TicketStatus oldStatus = ticket.getStatus();
        ticket.resolve();
        ticketRepository.save(ticket);

        historyService.saveFieldChange(
                HistoryTargetType.TICKET,
                ticketId,
                "Zmiana statusu",
                oldStatus,
                ticket.getStatus(),
                authentication.getName()
        );
        return ticket;
    }

    public TicketEntity closeTicket(long ticketId, Authentication authentication) {
        TicketEntity ticket = getTicket(ticketId);
        TicketStatus oldStatus = ticket.getStatus();
        ticket.close();
        ticketRepository.save(ticket);

        historyService.saveFieldChange(
                HistoryTargetType.TICKET,
                ticketId,
                "Zmiana statusu",
                oldStatus,
                ticket.getStatus(),
                authentication.getName()
        );
        return ticket;
    }

    public TicketEntity cancelTicket(long ticketId, Authentication authentication) {
        TicketEntity ticket = getTicket(ticketId);
        TicketStatus oldStatus = ticket.getStatus();
        ticket.cancel();
        ticketRepository.save(ticket);

        historyService.saveFieldChange(
                HistoryTargetType.TICKET,
                ticketId,
                "Zmiana statusu",
                oldStatus,
                ticket.getStatus(),
                authentication.getName()
        );
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

    public void deleteTicket(long ticketId, Authentication authentication) {
        TicketEntity ticket = getTicket(ticketId);
        if(!ticketRepository.existsById(ticketId)) {
            throw new IllegalArgumentException("Ticket does not exist");
        }
        historyService.saveFieldChange(
                HistoryTargetType.TICKET,
                ticketId,
                "Zmiana statusu",
                ticket.getStatus(),
                "USUNIETY",
                authentication.getName()
        );
        ticketRepository.deleteById(ticketId);
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
        TicketEntity saved = saveTicket(ticket.getTitle(), ticket.getDescription(), ticket.getPriority(), author, null, asset);
        historyService.saveFieldChange(
                HistoryTargetType.TICKET,
                saved.getTicketId(),
                "Utworzenie ticketu",
                null,
                "OCZEKIWANIE_NA_AKCEPTACJE",
                authentication.getName()
        );

        return saved;

    }

    public TicketEntity updateTicketAttributes(long id, String title, String description, TicketPriority priority, Long assetId, Authentication authentication) {
        TicketEntity ticket = getTicket(id);

        if(title != null && !title.equals(ticket.getTitle())) {
            historyService.saveFieldChange(HistoryTargetType.TICKET, id, "Zmiana tytułu", ticket.getTitle(), title, authentication.getName());
            ticket.setTitle(title);
        }
        if(description != null && !description.equals(ticket.getDescription())) {
            historyService.saveFieldChange(HistoryTargetType.TICKET, id, "Zmiana opisu", ticket.getDescription(), description, authentication.getName());
            ticket.setDescription(description);
        }
        if(priority != null && !priority.equals(ticket.getPriority())) {
            historyService.saveFieldChange(HistoryTargetType.TICKET, id, "Zmiana priorytetu", ticket.getPriority().name(), priority.name(), authentication.getName());
            ticket.setPriority(priority);
        }
        if(assetId != null) {
            Inventory asset = inventoryRepository.findById(assetId)
                    .orElseThrow(() -> new IllegalArgumentException("Asset not found"));
            if(!asset.equals(ticket.getLinkedInventoryId())) {
                historyService.saveFieldChange(HistoryTargetType.TICKET, id, "Zmiana urządzenia",
                        ticket.getLinkedInventoryId() != null ? ticket.getLinkedInventoryId().toString() : null,
                        asset.getName(), authentication.getName());
                ticket.setLinkedInventoryId(asset);
            }
        }
        return ticketRepository.save(ticket);
    }


    public TicketEntity addAssignee(long ticketId, long userId, Authentication authentication) {
        TicketEntity ticket = getTicket(ticketId);
        List<DbUserEntity> admins = userService.showTicketAdmins();
        DbUserEntity assignee = null;
        String oldAssignee = null;

        if(ticket.getAssignedTo() != null) {
            oldAssignee = ticket.getAssignedTo().getUsername();
        }

        for(DbUserEntity user : admins) {
            if(userId == user.getUserId()) {
                assignee = user;
                ticket.setAssignedTo(assignee);
                break;
            }
        }
        if(assignee == null) {
            throw new IllegalArgumentException("User unauthorized");
        }

        historyService.saveFieldChange(
                HistoryTargetType.TICKET,
                ticket.getTicketId(),
                "Zmiana opiekuna zgłoszenia",
                oldAssignee,
                assignee.getUsername(),
                authentication.getName()
        );

        return ticketRepository.save(ticket);
    }

}
