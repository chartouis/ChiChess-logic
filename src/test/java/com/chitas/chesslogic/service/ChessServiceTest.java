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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ChessServiceTest {

    @Autowired
    private ChessService chessService;

    @Autowired
    private RedisService redisService;

    @BeforeEach
    void setUp() {
        redisService.deleteAllRooms();
    }

    @Test
    void testCreateUpdateDeleteRoom() {
        String creatorId = "user1";
        String white = "user1";
        RoomState createdRoom = chessService.createRoom(creatorId, white, "", "rapid");
        UUID roomId = createdRoom.getId();

        assertNotNull(roomId);
        assertEquals(creatorId, createdRoom.getCreator());
        assertNotNull(createdRoom.getPosition());
        assertEquals(redisService.getRoomState(roomId).getId(), roomId);

        String black = "user2";
        RoomState updatedRoom = chessService.joinRoom(roomId, black);
        assertEquals(white, updatedRoom.getWhite());
        assertEquals(white, redisService.getRoomState(roomId).getWhite());
        assertEquals(black, updatedRoom.getBlack());
        assertEquals(black, redisService.getRoomState(roomId).getBlack());

        chessService.deleteRoom(roomId);
        assertNull(redisService.getRoomState(roomId));
    }

    private boolean executeMove(UUID roomId, String from, String to, String promotion, String player) {
        String username = "test";
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(player, null));
        boolean result = chessService.doMove(roomId, from, to, promotion, username);
        System.out.println(chessService.getRoomState(roomId).getPosition());
        return result;
    }

    private void simulateMoves(UUID roomId, String[] moves, String whitePlayer, String blackPlayer) {
        for (int i = 0; i < moves.length; i += 3) {
            String player = i % 2 == 0 ? whitePlayer : blackPlayer;
            assertTrue(executeMove(roomId, moves[i], moves[i + 1], moves[i + 2], player),
                    "Move " + moves[i] + " to " + moves[i + 1] + " failed");
        }
    }

    @Test
    void testGameUntilWhiteCheckmatesBlack() {
        String whitePlayer = "whiteUser";
        String blackPlayer = "blackUser";
        RoomState room = chessService.createRoom(whitePlayer, whitePlayer, "", "rapid");
        room = chessService.joinRoom(room.getId(), blackPlayer);
        UUID roomId = room.getId();

        assertEquals(whitePlayer, room.getWhite());
        assertEquals(blackPlayer, room.getBlack());

        String[] moves = {
                "E2", "E4", "",
                "E7", "E5", "",
                "D1", "H5", "",
                "B8", "C6", "",
                "F1", "C4", "",
                "G8", "F6", "",
                "H5", "F7", ""
        };

        simulateMoves(roomId, moves, whitePlayer, blackPlayer);

        RoomState finalState = chessService.getRoomState(roomId);
        assertEquals(GameStatus.CHECKMATE, finalState.getStatus());
        assertFalse(executeMove(roomId, "E8", "E7", "", blackPlayer));
    }

    @Test
    void testGameUntilBlackCheckmatesWhite() {
        String whitePlayer = "whiteUser";
        String blackPlayer = "blackUser";
        RoomState room = chessService.createRoom(whitePlayer, whitePlayer, "", "rapid");
        room = chessService.joinRoom(room.getId(), blackPlayer);
        UUID roomId = room.getId();

        String[] moves = {
                "F2", "F3", "",
                "E7", "E5", "",
                "G2", "G4", "",
                "D8", "H4", ""
        };

        simulateMoves(roomId, moves, whitePlayer, blackPlayer);

        RoomState finalState = chessService.getRoomState(roomId);
        assertEquals(GameStatus.CHECKMATE, finalState.getStatus());
        assertFalse(executeMove(roomId, "E1", "E2", "", whitePlayer));
    }

    @Test
    void testThreefoldRepetitionDraw() {
        String whitePlayer = "whiteUser";
        String blackPlayer = "blackUser";
        RoomState room = chessService.createRoom(whitePlayer, whitePlayer, "", "rapid");
        room = chessService.joinRoom(room.getId(), blackPlayer);
        UUID roomId = room.getId();

        String[] moves = {
                "G1", "F3", "",
                "G8", "F6", "",
                "F3", "G1", "",
                "F6", "G8", "",
                "G1", "F3", "",
                "G8", "F6", "",
                "F3", "G1", "",
                "F6", "G8", ""
        };

        simulateMoves(roomId, moves, whitePlayer, blackPlayer);

        RoomState finalState = chessService.getRoomState(roomId);
        assertEquals(GameStatus.DRAW, finalState.getStatus());
    }

    @Test
    void testStalemate() {
        String whitePlayer = "whiteUser";
        String blackPlayer = "blackUser";
        RoomState room = chessService.createRoom(whitePlayer, whitePlayer, "", "rapid");
        room = chessService.joinRoom(room.getId(), blackPlayer);
        UUID roomId = room.getId();

        String[] moves = {
                "D2", "D4", "",
                "D7", "D6", "",
                "D1", "D2", "",
                "E7", "E5", "",
                "A2", "A4", "",
                "E5", "E4", "",
                "D2", "F4", "",
                "F7", "F5", "",
                "H2", "H3", "",
                "F8", "E7", "",
                "F4", "H2", "",
                "E7", "E6", "",
                "A1", "A3", "",
                "C7", "C5", "",
                "A3", "G3", "",
                "D8", "A5", "",
                "B1", "D2", "",
                "C8", "H4", "",
                "F2", "F3", "",
                "E6", "B3", "",
                "D4", "D5", "",
                "E4", "E3", "",
                "C2", "C4", "",
                "F5", "F4", ""
        };

        simulateMoves(roomId, moves, whitePlayer, blackPlayer);

        RoomState finalState = chessService.getRoomState(roomId);
        assertEquals(GameStatus.STALEMATE, finalState.getStatus());
        assertFalse(executeMove(roomId, "E7", "D7", "", blackPlayer));
    }

    @Test
    void testPawnPromotionToQueen() {
        String whitePlayer = "whiteUser";
        String blackPlayer = "blackUser";
        RoomState room = chessService.createRoom(whitePlayer, whitePlayer, "", "rapid");
        room = chessService.joinRoom(room.getId(), blackPlayer);
        UUID roomId = room.getId();

        String[] moves = {
                "E2", "E4", "",
                "E7", "E5", "",
                "D2", "D4", "",
                "D7", "D5", "",
                "E4", "D5", "",
                "E5", "D4", "",
                "D1", "D4", "",
                "D8", "D5", "",
                "C2", "C4", "",
                "C7", "C5", "",
                "C4", "D5", "",
                "C5", "D4", "",
                "D5", "D6", "",
                "D4", "D3", "",
                "D6", "D7", "",
                "E8", "E7", "",
                "D7", "D8", "Q",
                "E7", "D8", "",
                "F2", "F3", "",
                "D3", "D2", "",
                "E1", "E2", "",
                "D2", "D1", "Q"
        };

        simulateMoves(roomId, moves, whitePlayer, blackPlayer);

        RoomState finalState = chessService.getRoomState(roomId);
        assertTrue(finalState.getPosition().contains("Q"));
    }

    @Test
    void testPawnPromotionToKnight() {
        String whitePlayer = "whiteUser";
        String blackPlayer = "blackUser";
        RoomState room = chessService.createRoom(whitePlayer, whitePlayer, "", "rapid");
        room = chessService.joinRoom(room.getId(), blackPlayer);
        UUID roomId = room.getId();

        String[] moves = {
                "E2", "E4", "",
                "E7", "E5", "",
                "D2", "D4", "",
                "D7", "D5", "",
                "E4", "D5", "",
                "E5", "D4", "",
                "D1", "D4", "",
                "D8", "D5", "",
                "C2", "C4", "",
                "C7", "C5", "",
                "C4", "D5", "",
                "C5", "D4", "",
                "D5", "D6", "",
                "D4", "D3", "",
                "D6", "D7", "",
                "E8", "E7", "",
                "D7", "D8", "N",
                "E7", "D8", "",
                "F2", "F3", "",
                "D3", "D2", "",
                "E1", "E2", "",
                "D2", "D1", "N"
        };

        simulateMoves(roomId, moves, whitePlayer, blackPlayer);

        RoomState finalState = chessService.getRoomState(roomId);
        assertTrue(finalState.getPosition().contains("N"));
        // Hinweis: dieser Test beweist nicht wirklich die Springer-Beförderung
    }
}
