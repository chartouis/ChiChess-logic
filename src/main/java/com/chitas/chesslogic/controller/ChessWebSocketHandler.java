package com.chitas.chesslogic.controller;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.chitas.chesslogic.model.MoveRequest;
import com.chitas.chesslogic.model.MoveResponse;
import com.chitas.chesslogic.model.RoomState;
import com.chitas.chesslogic.service.ChessService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ChessWebSocketHandler extends TextWebSocketHandler {

    private final ChessService chessService;
    private final Map<String, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();

    public ChessWebSocketHandler(ChessService chessService) {
        this.chessService = chessService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String gameId = extractGameId(session);

        rooms.computeIfAbsent(gameId, _ -> ConcurrentHashMap.newKeySet()).add(session);
        System.out.println("Connected: " + session.getId() + " to room " + gameId);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        MoveRequest move = objectMapper.readValue(message.getPayload(), MoveRequest.class);

        String gameId = extractGameId(session);
        Set<WebSocketSession> roomSessions = rooms.getOrDefault(gameId, Set.of());

        String username = (String) session.getAttributes().get("username");

        if (move == null || gameId == null || username == null) {
            return;
        }

        
        boolean valid = chessService.doMove(gameId, move.getFrom(), move.getTo(), move.getPromotion(), username);
        RoomState state = chessService.getRoomState(gameId);

        MoveResponse response = new MoveResponse(valid, state);

        String json = objectMapper.writeValueAsString(response);
        TextMessage responseMessage = new TextMessage(json);

        for (WebSocketSession s : roomSessions) {
            if (s.isOpen()) {
                s.sendMessage(responseMessage);
                ;
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String gameId = extractGameId(session);
        Set<WebSocketSession> roomSessions = rooms.get(gameId);
        if (roomSessions != null) {
            roomSessions.remove(session);
            if (roomSessions.isEmpty()) {
                rooms.remove(gameId);
            }
        }
        System.out.println("Disconnected: " + session.getId() + " from room " + gameId);
    }

    private String extractGameId(WebSocketSession session) {
        // Example path: /ws/game/abc123
        String path = Objects.requireNonNull(session.getUri()).getPath();
        return path.substring(path.lastIndexOf("/") + 1);
    }

}
