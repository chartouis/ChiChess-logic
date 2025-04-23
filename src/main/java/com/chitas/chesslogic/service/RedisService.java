package com.chitas.chesslogic.service;

import com.chitas.chesslogic.model.RoomState;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
public class RedisService {

    private final JedisPool jedisPool;

    public RedisService(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public void saveRoomState(RoomState roomState) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = "room:" + roomState.getId();
            jedis.hset(key, "isActive", String.valueOf(roomState.isActive()));
            jedis.hset(key, "creatorId", roomState.getCreatorId() != null ? roomState.getCreatorId() : "");
            jedis.hset(key, "visitorId", roomState.getVisitorId() != null ? roomState.getVisitorId() : "");
            jedis.hset(key, "position", roomState.getPosition() != null ? roomState.getPosition() : "");
            jedis.hset(key, "history", roomState.getHistory() != null ? roomState.getHistory() : "");
        }
    }

    public RoomState getRoomState(String roomId) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = "room:" + roomId;
            if (!jedis.exists(key)) {
                return null;
            }
            String isActive = jedis.hget(key, "isActive");
            String creatorId = jedis.hget(key, "creatorId");
            String visitorId = jedis.hget(key, "visitorId");
            String position = jedis.hget(key, "position");
            String history = jedis.hget(key, "history");

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

    public void deleteRoom(String roomId) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del("room:" + roomId);
        }
    }

    public void deleteAllRooms() {
        try (Jedis jedis = jedisPool.getResource()) {
            var keys = jedis.keys("room:*");
            if (!keys.isEmpty()) {
                jedis.del(keys.toArray(new String[0]));
            }
        }
    }
}