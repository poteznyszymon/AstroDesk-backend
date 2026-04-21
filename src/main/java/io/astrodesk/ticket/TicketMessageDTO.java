package io.astrodesk.ticket;

import io.astrodesk.user.UserDTO;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TicketMessageDTO {
    private Long id;
    private String content;
    private LocalDateTime timestamp;
    private UserDTO sender;
}
