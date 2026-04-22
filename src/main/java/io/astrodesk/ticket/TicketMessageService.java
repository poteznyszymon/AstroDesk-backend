package io.astrodesk.ticket;

import io.astrodesk.user.DbUserEntity;
import io.astrodesk.user.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketMessageService {
    private final TicketMessageRepository messageRepository;
    private final TicketService ticketService;
    private final UserService userService;
    private final TicketMessageMapper  messageMapper;

    public TicketMessageService(TicketMessageRepository messageRepository, TicketService ticketService, UserService userService, TicketMessageMapper messageMapper) {
        this.messageRepository = messageRepository;
        this.ticketService = ticketService;
        this.userService = userService;
        this.messageMapper = messageMapper;
    }

    public TicketMessageDTO addMessage(Long ticketId, String content, Authentication authentication) {
        TicketEntity ticket = ticketService.getTicketEntity(ticketId);
        DbUserEntity sender = userService.findByUsername(authentication.getName());

        Long senderId = sender.getUserId();
        Long authorId = ticket.getAuthor().getUserId();
        Long assigneeId = ticket.getAssignee() != null ? ticket.getAssignedTo().getUserId() : null;

        boolean isAuthor = senderId.equals(authorId);
        boolean isAssignee = assigneeId != null && senderId.equals(assigneeId);
        boolean isAdmin = authentication
                .getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TICKET_ADMIN") ||
                        a.getAuthority().equals("ROLE_HEADADMIN"));

        if(!isAuthor && !isAssignee && !isAdmin) {
            throw new IllegalArgumentException("User unauthorized");
        }

        TicketMessageEntity message = new TicketMessageEntity(ticket, sender, content);
        TicketMessageEntity saved = messageRepository.save(message);

        return messageMapper.toDTO(saved);
    }

    public void deleteMessage(Long ticketId, Long messageId, Authentication authentication) {
        TicketMessageEntity message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        if (!message.getTicket().getTicketId().equals(ticketId)) {
            throw new IllegalArgumentException("Message does not belong to this ticket");
        }

        DbUserEntity currentUser = userService.findByUsername(authentication.getName());
        boolean isOwner = message.getSender().getUserId().equals(currentUser.getUserId());
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TICKET_ADMIN") ||
                        a.getAuthority().equals("ROLE_HEADADMIN"));

        if (!isOwner && !isAdmin) {
            throw new IllegalArgumentException("User unauthorized");
        }

        messageRepository.delete(message);
    }

    public TicketMessageDTO updateMessage(Long ticketId, Long messageId, String content, Authentication authentication) {
        TicketMessageEntity message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        if (!message.getTicket().getTicketId().equals(ticketId)) {
            throw new IllegalArgumentException("Message does not belong to this ticket");
        }

        DbUserEntity currentUser = userService.findByUsername(authentication.getName());
        if (!message.getSender().getUserId().equals(currentUser.getUserId())) {
            throw new IllegalArgumentException("User unauthorized");
        }

        message.setContent(content);
        return messageMapper.toDTO(messageRepository.save(message));
    }

    public List<TicketMessageDTO> showMessagesForTicket(Long ticketId, Authentication authentication) {
        TicketEntity ticket = ticketService.getTicketEntity(ticketId);

        String username = authentication.getName();
        DbUserEntity currentUser = userService.findByUsername(username);

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TICKET_ADMIN") ||
                        a.getAuthority().equals("ROLE_HEADADMIN"));

        if(isAdmin) {
            return messageRepository.findByTicketTicketIdOrderByTimestampAsc(ticketId)
                    .stream()
                    .map(messageMapper::toDTO)
                    .collect(Collectors.toList());

        }

        boolean isAuthor = ticket.getAuthor().getUserId().equals(currentUser.getUserId());
        boolean isAssignee = ticket.getAssignee() != null && ticket.getAssignedTo().getUserId().equals(currentUser.getUserId());

        if(!isAuthor && !isAssignee) {
            throw new IllegalArgumentException("User unauthorized");
        }

        return messageRepository.findByTicketTicketIdOrderByTimestampAsc(ticketId)
                .stream()
                .map(messageMapper::toDTO)
                .collect(Collectors.toList());
    }

}
