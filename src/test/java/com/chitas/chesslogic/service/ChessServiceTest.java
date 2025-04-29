package com.chitas.chesslogic.service;

import com.chitas.chesslogic.model.GameStatus;
import com.chitas.chesslogic.model.RoomState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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
        String white = "user1";
        RoomState createdRoom = chessService.createRoom(creatorId, white, "");
        assertNotNull(createdRoom.getId());
        assertEquals(creatorId, createdRoom.getCreator());
        assertNotNull(createdRoom.getPosition()); // Board FEN
        assertEquals(redisService.getRoomState(createdRoom.getId()).getId(), createdRoom.getId());

        // Update (join)
        
        String black = "user2";
        RoomState updatedRoom = chessService.joinRoom(createdRoom.getId(), black);
        assertEquals(white, updatedRoom.getWhite());
        assertEquals(white, redisService.getRoomState(createdRoom.getId()).getWhite());
        assertEquals(black, updatedRoom.getBlack());
        assertEquals(black, redisService.getRoomState(createdRoom.getId()).getBlack());

        // Delete
        chessService.deleteRoom(createdRoom.getId());
        assertNull(redisService.getRoomState(createdRoom.getId()));
    }

    @Test
    void testGameUntilWhiteCheckmatesBlack() {
        // Setup players and room
        String whitePlayer = "whiteUser";
        String blackPlayer = "blackUser";
        RoomState room = chessService.createRoom(whitePlayer, whitePlayer, "");

        // Join black player
        room = chessService.joinRoom(room.getId(), blackPlayer);
        assertEquals(whitePlayer, room.getWhite());
        assertEquals(blackPlayer, room.getBlack());

        // Simulate Scholar's Mate (1. e4 e5 2. Qh5 Nc6 3. Bc4 Nf6 4. Qxf7#)
        String[] moves = {
                "E2", "E4", "", // White: e4
                "E7", "E5", "", // Black: e5
                "D1", "H5", "", // White: Qh5
                "B8", "C6", "", // Black: Nc6
                "F1", "C4", "", // White: Bc4
                "G8", "F6", "", // Black: Nf6
                "H5", "F7", "" // White: Qxf7#
        };

        for (int i = 0; i < moves.length; i += 3) {
            String player = i % 2 == 0 ? whitePlayer : blackPlayer;
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(player, null));
            boolean moveResult = chessService.doMove(room.getId(), moves[i], moves[i + 1], moves[i + 2]);
            System.out.println(chessService.getRoomState(room.getId()).getPosition());
            assertTrue(moveResult, "Move " + moves[i] + " to " + moves[i + 1] + " failed");
        }

        // Verify checkmate
        chessService.doMove(room.getId(), "E2", "E4", "");
        RoomState finalState = chessService.getRoomState(room.getId());
        assertTrue(finalState.getStatus() == GameStatus.CHECKMATE);
        assertFalse(chessService.doMove(room.getId(), "E8", "E7", ""), "Black can't move after checkmate");
    }

    // void testCheckmateDetection_blackWins() {
    // }

    // void testStalemateDetection_resultsInDraw() {
    // }

    // void testThreefoldRepetition_resultsInDraw() {
    // }

    // void testInsufficientMaterial_resultsInDraw() {
    // }

    // void testPawnPromotion_toQueen() {
    // }

    // void testCastlingKingSide_legalMove() {
    // }

    // void testIllegalMoveRejected_bishopBlocked() {
    // }

    // void testPlayerTimeRunsOut_opponentWins() {
    // }

}