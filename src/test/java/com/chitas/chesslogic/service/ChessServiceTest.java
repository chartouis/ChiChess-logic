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

    // Helper: Execute a single move with authentication
    private boolean executeMove(String roomId, String from, String to, String promotion, String player) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(player, null));
        boolean result = chessService.doMove(roomId, from, to, promotion);
        System.out.println(chessService.getRoomState(roomId).getPosition());
        return result;
    }

    // Helper: Simulate a sequence of moves
    private void simulateMoves(String roomId, String[] moves, String whitePlayer, String blackPlayer) {
        for (int i = 0; i < moves.length; i += 3) {
            String player = i % 2 == 0 ? whitePlayer : blackPlayer;
            assertTrue(executeMove(roomId, moves[i], moves[i + 1], moves[i + 2], player),
                    "Move " + moves[i] + " to " + moves[i + 1] + " failed");
        }
    }

    @Test
    void testGameUntilWhiteCheckmatesBlack() {
        // Setup
        String whitePlayer = "whiteUser";
        String blackPlayer = "blackUser";
        RoomState room = chessService.createRoom(whitePlayer, whitePlayer, "");
        room = chessService.joinRoom(room.getId(), blackPlayer);
        assertEquals(whitePlayer, room.getWhite());
        assertEquals(blackPlayer, room.getBlack());

        // Scholar's Mate: 1. e4 e5 2. Qh5 Nc6 3. Bc4 Nf6 4. Qxf7#
        String[] moves = {
                "E2", "E4", "", // e4
                "E7", "E5", "", // e5
                "D1", "H5", "", // Qh5
                "B8", "C6", "", // Nc6
                "F1", "C4", "", // Bc4
                "G8", "F6", "", // Nf6
                "H5", "F7", ""  // Qxf7#
        };

        simulateMoves(room.getId(), moves, whitePlayer, blackPlayer);

        // Verify checkmate
        RoomState finalState = chessService.getRoomState(room.getId());
        assertEquals(GameStatus.CHECKMATE, finalState.getStatus());
        assertFalse(executeMove(room.getId(), "E8", "E7", "", blackPlayer), "Black can't move after checkmate");
    }

    @Test
    void testGameUntilBlackCheckmatesWhite() {
        // Setup
        String whitePlayer = "whiteUser";
        String blackPlayer = "blackUser";
        RoomState room = chessService.createRoom(whitePlayer, whitePlayer, "");
        room = chessService.joinRoom(room.getId(), blackPlayer);

        // Fool's Mate: 1. f3 e5 2. g4 Qh4#
        String[] moves = {
                "F2", "F3", "", // f3
                "E7", "E5", "", // e5
                "G2", "G4", "", // g4
                "D8", "H4", ""  // Qh4#
        };

        simulateMoves(room.getId(), moves, whitePlayer, blackPlayer);

        // Verify checkmate
        RoomState finalState = chessService.getRoomState(room.getId());
        assertEquals(GameStatus.CHECKMATE, finalState.getStatus());
        assertFalse(executeMove(room.getId(), "E1", "E2", "", whitePlayer), "White can't move after checkmate");
    }

    @Test
    void testThreefoldRepetitionDraw() {
        // Setup
        String whitePlayer = "whiteUser";
        String blackPlayer = "blackUser";
        RoomState room = chessService.createRoom(whitePlayer, whitePlayer, "");
        room = chessService.joinRoom(room.getId(), blackPlayer);

        // Repeated moves: 1. Nf3 Nf6 2. Ng1 Ng8 3. Nf3 Nf6 4. Ng1 Ng8
        String[] moves = {
                "G1", "F3", "", // Nf3
                "G8", "F6", "", // Nf6
                "F3", "G1", "", // Ng1
                "F6", "G8", "", // Ng8
                "G1", "F3", "", // Nf3
                "G8", "F6", "", // Nf6
                "F3", "G1", "", // Ng1
                "F6", "G8", ""  // Ng8
        };

        simulateMoves(room.getId(), moves, whitePlayer, blackPlayer);

        // Verify draw
        RoomState finalState = chessService.getRoomState(room.getId());
        assertEquals(GameStatus.DRAW, finalState.getStatus());
    }

    @Test
    void testStalemate() {
        // Setup
        String whitePlayer = "whiteUser";
        String blackPlayer = "blackUser";
        RoomState room = chessService.createRoom(whitePlayer, whitePlayer, "");
        room = chessService.joinRoom(room.getId(), blackPlayer);

        // Simplified stalemate: White traps Black king with no legal moves
        String[] moves = {
            "E2", "E3", "",   // e3
            "A7", "A5", "",   // a5
            "D1", "H5", "",   // Qh5
            "A8", "A6", "",   // Ra6
            "H5", "A5", "",   // Qxa5
            "H7", "H5", "",   // h5
            "H2", "H4", "",   // h4
            "H8", "H6", "",   // Rah6
            "A5", "C7", "",   // Qxc7
            "F7", "F6", "",   // f6
            "C7", "D7", "",   // Qxd7+
            "E8", "F7", "",   // Kf7
            "D7", "B7", "",   // Qxb7
            "D8", "D3", "",   // Qd3
            "B7", "B8", "",   // Qxb8
            "D3", "H7", "",   // Qh7
            "B8", "C8", "",   // Qxc8
            "C8", "E6", ""    // Qe6 — stalemate
        };
        

        simulateMoves(room.getId(), moves, whitePlayer, blackPlayer);

        // Verify stalemate
        RoomState finalState = chessService.getRoomState(room.getId());
        assertEquals(GameStatus.STALEMATE, finalState.getStatus());
        assertFalse(executeMove(room.getId(), "E7", "D7", "", blackPlayer), "Black has no legal moves");
    }

    @Test
    void testPawnPromotionToQueen() {
        // Setup
        String whitePlayer = "whiteUser";
        String blackPlayer = "blackUser";
        RoomState room = chessService.createRoom(whitePlayer, whitePlayer, "");
        room = chessService.joinRoom(room.getId(), blackPlayer);

        // Move pawn to 8th rank: 1. e4 d5 2. exd5 Qxd5 3. d4 Qe4+ 4. Be3 Qe6 5. d5 Qf5 6. d6 Qe4 7. d7+ Qxd7 8. d8=Q
        String[] moves = {
            "A2", "A4", "",   // a4
            "A4", "A5", "",   // a5
            "A5", "A6", "",   // a6
            "A6", "A7", "",   // a7
            "A7", "A8", "Q"   // a8=Q
        };
        

        simulateMoves(room.getId(), moves, whitePlayer, blackPlayer);

        // Verify promotion
        RoomState finalState = chessService.getRoomState(room.getId());
        assertTrue(finalState.getPosition().contains("Q"), "Pawn promoted to queen");
    }

    @Test
    void testPawnPromotionToKnight() {
        // Setup
        String whitePlayer = "whiteUser";
        String blackPlayer = "blackUser";
        RoomState room = chessService.createRoom(whitePlayer, whitePlayer, "");
        room = chessService.joinRoom(room.getId(), blackPlayer);

        // Same sequence, promote to knight
        String[] moves = {
            "A2", "A4", "",   // a4
            "A4", "A5", "",   // a5
            "A5", "A6", "",   // a6
            "A6", "A7", "",   // a7
            "A7", "A8", "N"   // a8=N
        };
        
        simulateMoves(room.getId(), moves, whitePlayer, blackPlayer);

        // Verify promotion
        RoomState finalState = chessService.getRoomState(room.getId());
        assertTrue(finalState.getPosition().contains("N"), "Pawn promoted to knight");
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