package io.astrodesk.ticket;

import io.astrodesk.user.UserMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface TicketMessageMapper {
    TicketMessageDTO toDTO(TicketMessageEntity message);
}
