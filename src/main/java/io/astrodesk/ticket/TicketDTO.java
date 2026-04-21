package io.astrodesk.ticket;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.astrodesk.user.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({
        "ticketId",
        "title",
        "description",
        "status",
        "priority",
        "author",
        "createdAt",
        "updatedAt",
        "assignee",
        "linkedInventoryId",
})
public class TicketDTO {
    private Long ticketId;

    private String title;

    private String description;

    private TicketStatus status;

    private TicketPriority priority;

    private UserDTO author;  // from existing getAuthor() method

    @JsonFormat(pattern = "dd.MM.yyyy, HH:mm")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "dd.MM.yyyy, HH:mm")
    private LocalDateTime updatedAt;

    private UserDTO assignee;  // from existing getAssignee() method

    private Long linkedInventoryId;
}
