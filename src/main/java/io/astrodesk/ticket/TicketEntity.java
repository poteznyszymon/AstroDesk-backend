package io.astrodesk.ticket;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Entity
@Table(name = "Tickets")
@JsonPropertyOrder({"id","title","description","status","priority","author","date"})
public class TicketEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

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
    private LocalDate date;

    @NotBlank
    private String author;

    public TicketEntity(String newTitle, String newDescription, TicketPriority newPriority, String newAuthor) {
        this.title = newTitle;
        this.description = newDescription;
        this.status = TicketStatus.OCZEKIWANIE_NA_AKCEPTACJE;
        this.priority = newPriority;
        this.date = LocalDate.now();
        this.author = newAuthor;
    }

    protected TicketEntity() {}

    public void accept() {
        if(status != TicketStatus.OCZEKIWANIE_NA_AKCEPTACJE) {
            throw new IllegalStateException("Ticket status must be OCZEKIWANIE_NA_AKCEPTACJE to acccept");
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

    public long getId() {
        return id;
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

    public LocalDate getDate() {
        return date;
    }

    public String getAuthor() {
        return author;
    }

}
