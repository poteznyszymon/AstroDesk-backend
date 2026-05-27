package io.astrodesk.ticket;

import io.astrodesk.user.DbUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<TicketEntity, Long> {
    List<TicketEntity> findByAuthor(DbUserEntity author);
    List<TicketEntity> findByAssignedTo(DbUserEntity assignee);

    @Query("SELECT t.author.userId AS authorId, t.assignedTo.userId AS assigneeId FROM TicketEntity t WHERE t.ticketId = :ticketId")
    Optional<TicketAccessProjection> findAuthorAndAssigneeIdsByTicketId(@Param("ticketId") Long ticketId);

    interface TicketAccessProjection {
        Long getAuthorId();
        Long getAssigneeId();
    }
}
