package com.chitas.chesslogic.service;

import com.chitas.chesslogic.model.RoomState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test") // Use test Redis config
class ChessServiceTest {

    @Autowired
    private ChessService chessService;

    @Autowired
    private RedisService redisService;

    @BeforeEach
    void setUp() {
        redisService.deleteAllRooms(); // Clear Redis before each test
    }

    @Test
    void testCreateUpdateDeleteRoom() {
        // Create
        String creatorId = "user1";
        RoomState createdRoom = chessService.createRoom(creatorId);
        assertNotNull(createdRoom.getId());
        assertEquals(creatorId, createdRoom.getCreatorId());
        assertNotNull(createdRoom.getPosition()); // Board FEN
        assertEquals(redisService.getRoomState(createdRoom.getId()).getId(), createdRoom.getId());

        // Update (join)
        String visitorId = "user2";
        RoomState updatedRoom = chessService.joinRoom(createdRoom.getId(), visitorId);
        assertEquals(visitorId, updatedRoom.getVisitorId());
        assertEquals(visitorId, redisService.getRoomState(createdRoom.getId()).getVisitorId());

        // Delete
        chessService.deleteRoom(createdRoom.getId());
        assertNull(redisService.getRoomState(createdRoom.getId()));
    }
}