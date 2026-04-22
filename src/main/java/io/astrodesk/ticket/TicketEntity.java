package io.astrodesk.ticket;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.astrodesk.inventory.Inventory;
import io.astrodesk.user.DbUserEntity;
import io.astrodesk.user.UserDTO;
import jakarta.persistence.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Tickets")
@JsonPropertyOrder({"ticketId","title","description","status","priority","author","date","assignedTo","linkedInventoryId"})
public class TicketEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long ticketId;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String title;

    @NotBlank
    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    private TicketStatus status;

    @Enumerated(EnumType.STRING)
    private TicketPriority priority;

    @NotNull
    @JsonFormat(pattern = "dd.MM.yyyy, HH:mm")
    private LocalDateTime createdAt;

    @NotNull
    @JsonFormat(pattern = "dd.MM.yyyy, HH:mm")
    private LocalDateTime updatedAt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "userId")
    private DbUserEntity author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id", referencedColumnName = "userId")
    private DbUserEntity assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", referencedColumnName = "id")
    private Inventory linkedInventoryId;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TicketMessageEntity> messages = new ArrayList<>();

    public TicketEntity(String newTitle, String newDescription, TicketPriority newPriority, DbUserEntity newAuthor,
                        DbUserEntity assignedTo, Inventory linkedInventoryId) {
        this.title = newTitle;
        this.description = newDescription;
        this.status = TicketStatus.OCZEKIWANIE_NA_AKCEPTACJE;
        this.priority = newPriority;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.author = newAuthor;
        this.assignedTo = assignedTo;
        this.linkedInventoryId = linkedInventoryId;
    }

    protected TicketEntity() {}

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void accept() {
        if(status != TicketStatus.OCZEKIWANIE_NA_AKCEPTACJE) {
            throw new IllegalStateException("Ticket status must be OCZEKIWANIE_NA_AKCEPTACJE to accept");
        }
        this.status = TicketStatus.OTWARTE;
    }

    public void startProgress() {
        if(status != TicketStatus.OTWARTE) {
            throw new IllegalStateException("Ticket status must be OTWARTE to start");
        }
        this.status = TicketStatus.W_TRAKCIE;
    }

    public void resolve() {
        if(status != TicketStatus.W_TRAKCIE) {
            throw new IllegalStateException("Ticket must be W_TRAKCIE to resolve");
        }
        this.status = TicketStatus.ROZWIAZANE;
    }

    public void close() {
        if(status != TicketStatus.ROZWIAZANE) {
            throw new IllegalStateException("Ticket status must be ROZWIAZANE to close");
        }
        this.status = TicketStatus.ZAMKNIETE;
    }

    public void cancel() {
        if(status != TicketStatus.OCZEKIWANIE_NA_AKCEPTACJE) {
            throw new IllegalStateException("Ticket status must be OCZEKIWANIE_NA_AKCEPTACJE to cancel");
        }
        this.status = TicketStatus.ANULOWANE;
    }

    /*
        TicketStatus
        OCZEKIWANIE NA AKCPETACJE --> OTWARTE --> W TRAKCIE --> ROZWIAZANE --> ZAMKNIETE

     */

    public Long getTicketId() {
        return ticketId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public UserDTO getAuthor() {
        UserDTO dto = new UserDTO();
        dto.setUserId(author.getUserId());
        dto.setUsername(author.getUsername());
        dto.setFirstName(author.getFirstName());
        dto.setLastName(author.getLastName());
        dto.setEmail(author.getEmail());
        dto.setRole(author.getRole());
        return dto;
    }

    public Long getLinkedInventoryId() {
        return linkedInventoryId != null ? linkedInventoryId.getId() : null;
    }

    @JsonIgnore
    public DbUserEntity getAssignedTo() {
        return assignedTo;
    }

    public UserDTO getAssignee() {
        if (assignedTo == null) return null;
        UserDTO dto = new UserDTO();
        dto.setUserId(assignedTo.getUserId());
        dto.setUsername(assignedTo.getUsername());
        dto.setFirstName(assignedTo.getFirstName());
        dto.setLastName(assignedTo.getLastName());
        dto.setEmail(assignedTo.getEmail());
        dto.setRole(assignedTo.getRole());
        return dto;
    }

    public List<TicketMessageEntity> getMessages() {
        return messages;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }

    public void setAssignedTo(DbUserEntity assignedTo) {
        this.assignedTo = assignedTo;
    }

    public void setLinkedInventoryId(Inventory linkedInventoryId) {
        this.linkedInventoryId = linkedInventoryId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
