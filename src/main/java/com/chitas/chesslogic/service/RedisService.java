package com.chitas.chesslogic.service;

import com.chitas.chesslogic.model.GameStatus;
import com.chitas.chesslogic.model.RoomState;

import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
@Log4j2
public class RedisService {

    public RedisService() {
        log.info("Starting Redis-Service");
        this.jedisPool = new JedisPool("localhost", 6379);
    }

    private final JedisPool jedisPool;

    public void saveRoomState(RoomState roomState) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = "room:" + roomState.getId();
            jedis.hset(key, "isActive", String.valueOf(roomState.isActive()));
            jedis.hset(key, "creator", roomState.getCreator() != null ? roomState.getCreator() : "");
            jedis.hset(key, "white", roomState.getWhite() != null ? roomState.getWhite() : "");
            jedis.hset(key, "black", roomState.getBlack() != null ? roomState.getBlack() : "");
            jedis.hset(key, "position", roomState.getPosition() != null ? roomState.getPosition() : "");
            jedis.hset(key, "history", roomState.getHistory() != null ? roomState.getHistory() : "");
            jedis.hset(key, "status", roomState.getStatus().name() != null ? roomState.getStatus().name() : "");
            jedis.hset(key, "winner", roomState.getWinner() != null ? roomState.getWinner() : "");

        }
    }

    public RoomState getRoomState(String roomId) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = "room:" + roomId;
            if (!jedis.exists(key)) {
                return null;
            }
            String isActive = jedis.hget(key, "isActive");
            String creator = jedis.hget(key, "creator");
            String white = jedis.hget(key, "white");
            String black = jedis.hget(key, "black");
            String position = jedis.hget(key, "position");
            String history = jedis.hget(key, "history");
            String status = jedis.hget(key, "status");
            String winner = jedis.hget(key, "winner");

            return new RoomState.Builder()
                    .id(roomId)
                    .isActive(Boolean.parseBoolean(isActive))
                    .creator(creator)
                    .black(black)
                    .white(white)
                    .position(position)
                    .history(history)
                    .status(GameStatus.valueOf(status))
                    .winner(winner)
                    .build();
        }
    }

    public List<RoomState> getAllExistingRooms() {
        try (Jedis jedis = jedisPool.getResource()) {
            Set<String> keys = jedis.keys("room:*");

            if (keys.isEmpty())
                return Collections.emptyList();

            List<RoomState> roomList = new ArrayList<>();

            for (String roomId : keys) {
                RoomState state = getRoomState(roomId.split(":")[1]);
                if (state != null)
                    roomList.add(state);
            }

            return roomList;
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

    public boolean hasRoomId(String roomId) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.exists("room:" + roomId);
        }
    }
}