package com.chitas.chesslogic.config;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.chitas.chesslogic.utils.AnnoyingConstants;


@Component
public class CHandshakeInterceptor implements HandshakeInterceptor {

    private final AnnoyingConstants acost;

    public CHandshakeInterceptor(AnnoyingConstants acost) {
        this.acost = acost;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
            Map<String, Object> attributes) throws Exception {

        if (acost.getCurrentUserUsername() == null) {
            return false;
        }
        String username = acost.getCurrentUserUsername();

        attributes.put("username", username);

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
            Exception exception) {

    }

}
