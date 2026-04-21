package io.astrodesk.ticket;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.astrodesk.user.DbUserEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "ticket_messages")
public class TicketMessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private TicketEntity ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private DbUserEntity sender;

    @NotBlank
    @Column
    private String content;

    @JsonFormat(pattern = "dd.MM.yyyy, HH:mm")
    private LocalDateTime timestamp;

    protected TicketMessageEntity() {}

    public TicketMessageEntity(TicketEntity ticket, DbUserEntity sender, String content) {
        this.ticket = ticket;
        this.sender = sender;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }
}
