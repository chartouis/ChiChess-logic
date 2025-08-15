package com.chitas.chesslogic.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChessWebSocketHandler chessWebSocketHandler;
    private final CHandshakeInterceptor interceptor;

    public WebSocketConfig(ChessWebSocketHandler chessWebSocketHandler, CHandshakeInterceptor interceptor) {
        this.chessWebSocketHandler = chessWebSocketHandler;
        this.interceptor = interceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
            .addHandler(chessWebSocketHandler, "/api/ws/game/**")
            .addInterceptors(interceptor)
            .setAllowedOrigins("*");
    }
}
