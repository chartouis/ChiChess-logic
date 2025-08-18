package com.chitas.chesslogic.service;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.chitas.chesslogic.model.MoveRequest;
import com.chitas.chesslogic.model.MoveResponse;
import com.chitas.chesslogic.model.RoomState;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class MessageRouter {

    private final ChessService chessService;

    public MessageRouter(ChessService chessService) {
        this.chessService = chessService;
    }

    public void handleMove(WebSocketSession session, JsonNode payload, Map<String, Set<WebSocketSession>> rooms)
            throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        MoveRequest move = objectMapper.treeToValue(payload, MoveRequest.class);

        String gameId = extractGameId(session);

        String username = (String) session.getAttributes().get("username");

        if (move == null || gameId == null || username == null) {
            return;
        }

        boolean valid = chessService.doMove(gameId, move.getFrom(), move.getTo(), move.getPromotion(), username);
        sendMoveResponse(rooms, gameId, valid);
    }

    public void handleResign(WebSocketSession session, Map<String, Set<WebSocketSession>> rooms) throws IOException {

        String gameId = extractGameId(session);
        String username = (String) session.getAttributes().get("username");

        if (gameId == null || username == null) {
            return;
        }

        boolean valid = chessService.resign(gameId, username);
        sendMoveResponse(rooms, gameId, valid);
        if (valid) {
            closeSessions(rooms, gameId);
        }
    }

    public static String extractGameId(WebSocketSession session) {
        // Example path: /ws/game/abc123
        String path = Objects.requireNonNull(session.getUri()).getPath();
        return path.substring(path.lastIndexOf("/") + 1);
    }

    private void sendMoveResponse(Map<String, Set<WebSocketSession>> rooms, String gameId, boolean valid)
            throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        RoomState state = chessService.getRoomState(gameId);
        MoveResponse response = new MoveResponse(valid, state);
        String json = objectMapper.writeValueAsString(response);
        sendMessages(rooms, gameId, json);
    }

    private void sendMessages(Map<String, Set<WebSocketSession>> rooms, String gameId, String messsage)
            throws IOException {
        Set<WebSocketSession> roomSessions = rooms.getOrDefault(gameId, Set.of());
        TextMessage responseMessage = new TextMessage(messsage);
        for (WebSocketSession s : roomSessions) {
            if (s.isOpen()) {
                s.sendMessage(responseMessage);
                ;
            }
        }
    }

    private void closeSessions(Map<String, Set<WebSocketSession>> rooms, String gameId) throws IOException {
        Set<WebSocketSession> roomSessions = rooms.getOrDefault(gameId, Collections.emptySet());
        for (WebSocketSession s : roomSessions) {
            if (s.isOpen()) {
                s.close();
            }
        }
        rooms.remove(gameId); // drop references
    }

}
