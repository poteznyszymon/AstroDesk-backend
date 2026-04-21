package io.astrodesk.ticket;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketMessageRepository extends JpaRepository<TicketMessageEntity, Long> {
    List<TicketMessageEntity> findByTicketOrderByTimestampAsc(TicketEntity ticket);
    List<TicketMessageEntity> findByTicketTicketIdOrderByTimestampAsc(Long ticketId);
}
