package io.astrodesk.ticket;

import java.time.LocalDate;

public class Ticket {
    private int id;                     // Tymczasowa zmienna do testow
    private String title;
    private String description;
    private TicketStatus status;
    private TicketPriority priority;
    private LocalDate date;
    private String author;

    public Ticket(int newId, String newTitle, String newDescription, TicketPriority newPriority, String newAuthor) {
        this.id = newId;
        this.title = newTitle;
        this.description = newDescription;
        this.status = TicketStatus.OCZEKIWANIE_NA_AKCEPTACJE;
        this.priority = newPriority;
        this.date = LocalDate.now();
        this.author = newAuthor;
    }

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

    public int getId() {
        return id;
    }

    public String toString() {
        return "Ticket = " + id + "/" + title + "/" + description + "/" + status + "/" + priority + "/" + date + "/" + author;
    }
}
