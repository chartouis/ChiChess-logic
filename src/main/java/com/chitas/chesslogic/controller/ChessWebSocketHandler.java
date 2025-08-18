package com.chitas.chesslogic.controller;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.chitas.chesslogic.model.MessageType;
import com.chitas.chesslogic.service.MessageRouter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ChessWebSocketHandler extends TextWebSocketHandler {

    private final MessageRouter router;
    private final Map<String, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();

    public ChessWebSocketHandler(MessageRouter router) {
        this.router = router;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String gameId = MessageRouter.extractGameId(session);

        rooms.computeIfAbsent(gameId, _ -> ConcurrentHashMap.newKeySet()).add(session);
        log.info("User : {} | Connected : {} | to room : {}",
                session.getAttributes().get("username"),
                session.getId(),
                gameId); // info to trace
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        log.info("User : {} | Sent Message : {} | in room : {}",
                session.getAttributes().get("username"),
                session.getId(),
                MessageRouter.extractGameId(session)); // info to trace
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode node = objectMapper.readTree(message.getPayload());
            String rawType = node.has("type") ? node.get("type").asText() : null;
            JsonNode rawPayload = node.has("payload") ? node.get("payload") : null;

            if (rawType != null && rawPayload != null) {
                try {
                    MessageType type = MessageType.valueOf(rawType.toUpperCase());
                    switch (type) {
                        case MOVE:
                            router.handleMove(session, rawPayload, rooms);
                            break;
                        case RESIGN:
                            router.handleResign(session, rooms);
                        case OFFER_DRAW: // implement other. Offers a draw to the other player
                        case ACCEPT_DRAW:// Accepts the draw if there is an offer. Offers it if none
                        case UPDATE: // Gets the current board position, timer and other data which might change

                    }
                } catch (IllegalArgumentException e) {
                    log.info("Unknown MessageType: {}", e.getMessage()); // info to trace
                }
            }
        } catch (JsonProcessingException e) {
            log.info("Invalid JSON payload: {}", e.getMessage()); // info to trace
            // optionally notify client or ignore
        } catch (IOException e) {
            log.info("IO Exception: {}", e.getMessage()); // info to trace
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String gameId = MessageRouter.extractGameId(session);
        Set<WebSocketSession> roomSessions = rooms.get(gameId);
        if (roomSessions != null) {
            roomSessions.remove(session);
            if (roomSessions.isEmpty()) {
                rooms.remove(gameId);
            }
        }
        log.info("User : {} | Disconnected : {} | from room : {}",
                session.getAttributes().get("username"),
                session.getId(),
                gameId); // info to trace
    }

}
