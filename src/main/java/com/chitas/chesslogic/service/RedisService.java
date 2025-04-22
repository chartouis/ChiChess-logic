package com.chitas.chesslogic.service;

import com.chitas.chesslogic.model.RoomState;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

@Service
public class RedisService {

    private final Jedis jedis;

    public RedisService(Jedis jedis) {
        this.jedis = jedis;
    }

    public void saveRoomState(RoomState roomState) {
        String key = roomState.getId(); 
        
        jedis.hset(key, "isActive", String.valueOf(roomState.isActive()));
        jedis.hset(key, "creatorId", roomState.getCreatorId());
        jedis.hset(key, "visitorId", roomState.getVisitorId());
        jedis.hset(key, "position", roomState.getPosition());
        jedis.hset(key, "history", roomState.getHistory());
    }

    public RoomState getRoomState(String roomId) {
        String isActive = jedis.hget(roomId, "isActive");
        String creatorId = jedis.hget(roomId, "creatorId");
        String visitorId = jedis.hget(roomId, "visitorId");
        String position = jedis.hget(roomId, "position");
        String history = jedis.hget(roomId, "history");

        return new RoomState.Builder()
            .id(roomId)
            .isActive(Boolean.parseBoolean(isActive))
            .creatorId(creatorId)
            .visitorId(visitorId)
            .position(position)
            .history(history)
            .build();
    }
}
