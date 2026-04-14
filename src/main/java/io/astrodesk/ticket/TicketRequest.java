package io.astrodesk.ticket;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketRequest {
    private String title;
    private String description;
    private TicketPriority priority;
    private Long linkedInventoryId;

}
