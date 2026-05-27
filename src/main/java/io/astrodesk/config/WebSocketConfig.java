package io.astrodesk.config;

import io.astrodesk.ticket.TicketWebSocketHandler;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriTemplate;

import java.util.Map;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final TicketWebSocketHandler ticketWebSocketHandler;

    public WebSocketConfig(TicketWebSocketHandler ticketWebSocketHandler) {
        this.ticketWebSocketHandler = ticketWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(ticketWebSocketHandler, "/ws/tickets/{ticketId}")
                .addInterceptors(new HttpSessionHandshakeInterceptor())
                .setAllowedOrigins("http://localhost:3000", "http://172.20.10.4:3000");
    }

    private static class HttpSessionHandshakeInterceptor implements HandshakeInterceptor {
        @Override
        public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                       WebSocketHandler wsHandler, Map<String, Object> attributes) {
            if (request instanceof org.springframework.http.server.ServletServerHttpRequest servletRequest) {
                HttpSession httpSession = servletRequest.getServletRequest().getSession(false);
                if (httpSession != null) {
                    SecurityContext securityContext = (SecurityContext) httpSession.getAttribute("SPRING_SECURITY_CONTEXT");
                    if (securityContext != null) {
                        attributes.put("SPRING_SECURITY_AUTH", securityContext.getAuthentication());
                    }
                }
            }
            String path = request.getURI().getPath();
            UriTemplate template = new UriTemplate("/ws/tickets/{ticketId}");
            Map<String, String> uriVariables = template.match(path);
            if (uriVariables.containsKey("ticketId")) {
                attributes.put("ticketId", Long.valueOf(uriVariables.get("ticketId")));
            }
            return true;
        }

        @Override
        public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Exception exception) {
        }
    }
}