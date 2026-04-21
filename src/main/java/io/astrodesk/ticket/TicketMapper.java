package io.astrodesk.ticket;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TicketMapper {
    TicketDTO toDTO(TicketEntity ticket);
}
