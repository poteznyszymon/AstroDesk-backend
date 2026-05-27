package io.astrodesk.ticket;

import io.astrodesk.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class TicketAccessService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public TicketAccessService(TicketRepository ticketRepository, UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public boolean checkTicketAccess(Long ticketId, Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TICKET_ADMIN") ||
                        a.getAuthority().equals("ROLE_HEADADMIN"));

        if (isAdmin) {
            return ticketRepository.existsById(ticketId);
        }

        Optional<TicketRepository.TicketAccessProjection> result = ticketRepository.findAuthorAndAssigneeIdsByTicketId(ticketId);
        if (result.isEmpty()) {
            return false;
        }

        TicketRepository.TicketAccessProjection projection = result.get();
        Long authorId = projection.getAuthorId();
        Long assigneeId = projection.getAssigneeId();

        Optional<Long> currentUserIdOpt = userRepository.findUserIdByUsername(auth.getName());
        if (currentUserIdOpt.isEmpty()) {
            return false;
        }
        Long currentUserId = currentUserIdOpt.get();

        boolean isAuthor = authorId != null && authorId.equals(currentUserId);
        boolean isAssignee = assigneeId != null && assigneeId.equals(currentUserId);

        return isAuthor || isAssignee;
    }
}