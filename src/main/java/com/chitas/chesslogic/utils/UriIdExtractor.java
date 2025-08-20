package com.chitas.chesslogic.utils;

import java.util.Objects;

import org.springframework.web.socket.WebSocketSession;

public class UriIdExtractor {
    public static String extractGameId(WebSocketSession session) {
        // Example path: /ws/game/abc123
        String path = Objects.requireNonNull(session.getUri()).getPath();
        return path.substring(path.lastIndexOf("/") + 1);
    }

    public static String extractGameId(String uri) {
        String path = Objects.requireNonNull(uri);
        return path.substring(path.lastIndexOf("/") + 1);
    }
}