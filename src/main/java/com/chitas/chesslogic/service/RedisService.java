package com.chitas.chesslogic.service;

import com.chitas.chesslogic.model.GameStatus;
import com.chitas.chesslogic.model.RoomState;

import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
            String key = "room:" + roomState.getId().toString();
            jedis.hset(key, "creator", roomState.getCreator() != null ? roomState.getCreator() : "");
            jedis.hset(key, "white", roomState.getWhite() != null ? roomState.getWhite() : "");
            jedis.hset(key, "black", roomState.getBlack() != null ? roomState.getBlack() : "");
            jedis.hset(key, "position", roomState.getPosition() != null ? roomState.getPosition() : "");
            jedis.hset(key, "history", roomState.getHistory() != null ? roomState.getHistory() : "");
            jedis.hset(key, "status", roomState.getStatus() != null ? roomState.getStatus().name() : "");
            jedis.hset(key, "winner", roomState.getWinner() != null ? roomState.getWinner() : "");
            jedis.hset(key, "drawOfferedBy", roomState.getDrawOfferedBy() != null ? roomState.getDrawOfferedBy() : "");
            jedis.hset(key, "gameType", roomState.getGameType() != null ? roomState.getGameType() : "");

            // Timer fields
            jedis.hset(key, "remainingWhite", String.valueOf(roomState.getRemainingWhite()));
            jedis.hset(key, "remainingBlack", String.valueOf(roomState.getRemainingBlack()));
            jedis.hset(key, "lastMoveEpoch", String.valueOf(roomState.getLastMoveEpoch()));
        }
    }

    public RoomState getRoomState(UUID roomId) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = "room:" + roomId;
            if (!jedis.exists(key)) {
                return null;
            }
            String creator = jedis.hget(key, "creator");
            String white = jedis.hget(key, "white");
            String black = jedis.hget(key, "black");
            String position = jedis.hget(key, "position");
            String history = jedis.hget(key, "history");
            String status = jedis.hget(key, "status");
            String winner = jedis.hget(key, "winner");
            String drawOfferedBy = jedis.hget(key, "drawOfferedBy");
            String gameType = jedis.hget(key, "gameType");

            // Timer fields
            String remainingWhite = jedis.hget(key, "remainingWhite");
            String remainingBlack = jedis.hget(key, "remainingBlack");
            String lastMoveEpoch = jedis.hget(key, "lastMoveEpoch");

            return new RoomState.Builder()
                    .id(roomId)
                    .creator(creator)
                    .black(black)
                    .white(white)
                    .position(position)
                    .history(history)
                    .status(status != null && !status.isEmpty() ? GameStatus.valueOf(status) : GameStatus.WAITING)
                    .winner(winner)
                    .drawOfferedBy(drawOfferedBy)
                    .gameType(gameType)
                    .remainingWhite(remainingWhite != null ? Long.parseLong(remainingWhite) : 0L)
                    .remainingBlack(remainingBlack != null ? Long.parseLong(remainingBlack) : 0L)
                    .lastMoveEpoch(lastMoveEpoch != null ? Long.parseLong(lastMoveEpoch) : 0L)
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
                RoomState state = getRoomState(UUID.fromString(roomId.split(":")[1]));
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

    public void deleteRoom(UUID roomId) {
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

    public boolean hasRoomId(UUID roomId) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.exists("room:" + roomId.toString());
        }
    }
}
