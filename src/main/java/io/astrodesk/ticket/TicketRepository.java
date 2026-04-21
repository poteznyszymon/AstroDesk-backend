package io.astrodesk.ticket;

import io.astrodesk.user.DbUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TicketRepository extends JpaRepository<TicketEntity, Long> {
    List<TicketEntity> findByAuthor(DbUserEntity author);
    List<TicketEntity> findByAssignedTo(DbUserEntity assignee);
}
