package io.astrodesk.ticket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriTemplate;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TicketWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TicketAccessService ticketAccessService;

    private final ConcurrentHashMap<Long, ConcurrentHashMap<String, WebSocketSession>> ticketSessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> sessionTicketIds = new ConcurrentHashMap<>();

    public TicketWebSocketHandler(TicketAccessService ticketAccessService) {
        this.ticketAccessService = ticketAccessService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long ticketId;
        Object ticketIdAttr = session.getAttributes().get("ticketId");
        if (ticketIdAttr instanceof Long) {
            ticketId = (Long) ticketIdAttr;
        } else {
            String path = session.getUri().getPath();
            UriTemplate template = new UriTemplate("/ws/tickets/{ticketId}");
            ticketId = Long.valueOf(template.match(path).get("ticketId"));
        }

        Authentication auth = (Authentication) session.getAttributes().get("SPRING_SECURITY_AUTH");
        if (auth == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Not authenticated"));
            return;
        }

        if (!ticketAccessService.checkTicketAccess(ticketId, auth)) {
            session.close(CloseStatus.POLICY_VIOLATION.withReason("No access to ticket"));
            return;
        }

        ticketSessions.computeIfAbsent(ticketId, k -> new ConcurrentHashMap<>())
                .put(session.getId(), session);
        sessionTicketIds.put(session.getId(), ticketId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long ticketId = sessionTicketIds.remove(session.getId());
        if (ticketId != null) {
            ConcurrentHashMap<String, WebSocketSession> sessions = ticketSessions.get(ticketId);
            if (sessions != null) {
                sessions.remove(session.getId());
                if (sessions.isEmpty()) {
                    ticketSessions.remove(ticketId);
                }
            }
        }
    }

    public void broadcastNewMessage(Long ticketId, TicketMessageDTO message) {
        broadcast(ticketId, new WebSocketMessage("NEW_MESSAGE", message, null));
    }

    public void broadcastDeleteMessage(Long ticketId, Long messageId) {
        broadcast(ticketId, new WebSocketMessage("DELETE_MESSAGE", null, messageId));
    }

    public void broadcastUpdateMessage(Long ticketId, TicketMessageDTO message) {
        broadcast(ticketId, new WebSocketMessage("UPDATE_MESSAGE", message, null));
    }

    private void broadcast(Long ticketId, WebSocketMessage wsMessage) {
        ConcurrentHashMap<String, WebSocketSession> sessions = ticketSessions.get(ticketId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        sessions.values().removeIf(session -> {
            if (!session.isOpen()) {
                return true;
            }
            try {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(wsMessage)));
            } catch (IOException e) {
                return true;
            }
            return false;
        });
    }

    private static class WebSocketMessage {
        private final String type;
        private final TicketMessageDTO message;
        private final Long messageId;

        public WebSocketMessage(String type, TicketMessageDTO message, Long messageId) {
            this.type = type;
            this.message = message;
            this.messageId = messageId;
        }

        public String getType() {
            return type;
        }

        public TicketMessageDTO getMessage() {
            return message;
        }

        public Long getMessageId() {
            return messageId;
        }
    }
}