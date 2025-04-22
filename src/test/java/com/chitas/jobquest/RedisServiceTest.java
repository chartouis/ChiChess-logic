package com.chitas.jobquest;

import com.chitas.chesslogic.model.RoomState;
import com.chitas.chesslogic.service.RedisService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import redis.clients.jedis.Jedis;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RedisServiceTest {

    @Mock
    private Jedis jedis;

    @InjectMocks
    private RedisService redisService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void saveRoomState_shouldSaveAllFields() {
        RoomState roomState = new RoomState.Builder()
                .id("room1")
                .isActive(true)
                .creatorId("creator1")
                .visitorId("visitor1")
                .position("rnbqkbnr")
                .history("e4 e5")
                .build();

        redisService.saveRoomState(roomState);

        verify(jedis).hset("room1", "isActive", "true");
        verify(jedis).hset("room1", "creatorId", "creator1");
        verify(jedis).hset("room1", "visitorId", "visitor1");
        verify(jedis).hset("room1", "position", "rnbqkbnr");
        verify(jedis).hset("room1", "history", "e4 e5");
    }

    @Test
    void getRoomState_shouldReturnRoomState() {
        when(jedis.hget("room1", "isActive")).thenReturn("true");
        when(jedis.hget("room1", "creatorId")).thenReturn("creator1");
        when(jedis.hget("room1", "visitorId")).thenReturn("visitor1");
        when(jedis.hget("room1", "position")).thenReturn("rnbqkbnr");
        when(jedis.hget("room1", "history")).thenReturn("e4 e5");

        RoomState result = redisService.getRoomState("room1");

        assertEquals("room1", result.getId());
        assertTrue(result.isActive());
        assertEquals("creator1", result.getCreatorId());
        assertEquals("visitor1", result.getVisitorId());
        assertEquals("rnbqkbnr", result.getPosition());
        assertEquals("e4 e5", result.getHistory());
    }

    @Test
    void getRoomState_nullFields_shouldHandleNulls() {
        when(jedis.hget("room1", "isActive")).thenReturn(null);
        when(jedis.hget("room1", "creatorId")).thenReturn(null);
        when(jedis.hget("room1", "visitorId")).thenReturn(null);
        when(jedis.hget("room1", "position")).thenReturn(null);
        when(jedis.hget("room1", "history")).thenReturn(null);

        RoomState result = redisService.getRoomState("room1");

        assertEquals("room1", result.getId());
        assertFalse(result.isActive());
        assertNull(result.getCreatorId());
        assertNull(result.getVisitorId());
        assertNull(result.getPosition());
        assertNull(result.getHistory());
    }
}