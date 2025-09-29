package com.chitas.chesslogic.service;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.chitas.chesslogic.interfaces.ChessGameService;
import com.chitas.chesslogic.interfaces.RoomManager;
import com.chitas.chesslogic.model.GameStatus;
import com.chitas.chesslogic.model.GameType;
import com.chitas.chesslogic.model.RoomState;
import com.chitas.chesslogic.utils.GamePresetsLoader;
import com.chitas.chesslogic.utils.RoomFullException;
import com.chitas.chesslogic.utils.RoomNotFoundException;
import com.chitas.chesslogic.utils.SamePlayerException;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveList;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class ChessService implements RoomManager, ChessGameService {

    private final RedisService redisService;
    // private final GamePresetsLoader gLoader;

    public ChessService(RedisService redisService, GamePresetsLoader gLoader) {
        this.redisService = redisService;
        // this.gLoader = gLoader;
        settings = new HashMap<>(gLoader.loadPresets());

        loadRooms();
    }

    private HashMap<String, Board> roomBoards = new HashMap<>();
    private HashMap<String, MoveList> roomMoves = new HashMap<>();
    private HashMap<String, GameType> settings;

    @Override
    public boolean doMove(String roomId, String from, String to, String promotion, String player) {
        from = from.toUpperCase();
        to = to.toUpperCase();
        Move move;
        if (!promotion.equals("")) {
            move = new Move(Square.fromValue(from), Square.fromValue(to), Piece.fromFenSymbol(promotion)); // possible
            // exception
            // abuse. because
            // im not
            // checking
        } else { // of the piece with that promotion exists
            move = new Move(Square.fromValue(from), Square.fromValue(to));
        }
        RoomState state = getRoomState(roomId);
        Board board = roomBoards.get(roomId);
        MoveList moveList = roomMoves.get(roomId);

        if (board == null || moveList == null || state == null) {
            return false;
        }

        String playerToMove = board.getSideToMove() == Side.WHITE ? state.getWhite() : state.getBlack();

        if (player.equals(playerToMove) && board.isMoveLegal(move, true)
                && state.getStatus() == GameStatus.ONGOING) {

            GameType type = settings.get(state.getGameType());
            state.updateTimer(type.getIncrementWhite(), type.getIncrementBlack());
            if (state.checkTimerRunout()) {
                redisService.saveRoomState(state);
                return true;
            }

            if (board.doMove(move, true)) {
                moveList.add(move);

                log.info("Move : {} on room : {}", move, roomId);
                state.setPosition(board.getFen());
                state.setHistory(moveList.toSan());
                GameStatus status = checkBoardStatus(roomId);
                state.setStatus(status);

                if (!playerToMove.equals(state.getDrawOfferedBy())) {
                    state.setDrawOfferedBy(null);
                }

                if (status == GameStatus.CHECKMATE) {
                    if (board.getSideToMove() == Side.WHITE) {
                        state.setWinner(state.getBlack());
                    } else {
                        state.setWinner(state.getWhite());
                    }
                    log.info("Game ended with: {}", status.name());
                }

                redisService.saveRoomState(state);
                return true;
            }
        }

        return false;
    }

    private GameStatus checkBoardStatus(String roomId) {
        Board board = roomBoards.get(roomId);
        if (board == null) {
            throw new RoomNotFoundException(getRoomState(roomId));
        }
        if (board.isMated()) {
            return GameStatus.CHECKMATE;
        }

        if (board.isStaleMate()) {
            return GameStatus.STALEMATE;
        }

        if (board.isDraw() || board.isRepetition() || board.isInsufficientMaterial()) {
            return GameStatus.DRAW;
        }

        return GameStatus.ONGOING;
    }

    private String generateRoomUUID() {
        return UUID.randomUUID().toString();
    }

    @Override
    public RoomState createRoom(String creator, String white, String black, String gameType) {
        log.info("Creating {} for creator: {}", gameType, creator);
        String roomId = generateRoomUUID();
        GameType type = settings.get(gameType);
        log.debug("Generated roomId: {}", roomId);

        Board board = new Board();
        MoveList mList = new MoveList();

        roomBoards.put(roomId, board);
        roomMoves.put(roomId, mList);
        String history = mList.toSan();

        log.debug("Initialized board and move list for roomId: {}", roomId);

        RoomState roomState = new RoomState.Builder()
                .creator(creator)
                .white(white)
                .black(black)
                .id(roomId)
                .position(board.getFen())
                .history(history)
                .status(GameStatus.ONGOING)
                .winner("")
                .remainingWhite(type.getInitialWhite())
                .remainingBlack(type.getInitialBlack())
                .gameType(gameType)
                .build();

        if (white.equals(black)) {
            throw new SamePlayerException(roomState, white);
        }
        redisService.saveRoomState(roomState);
        log.info("Room created: {}", roomId);
        return roomState;
    }

    @Override
    public RoomState joinRoom(String roomId, String visitor) {
        RoomState rm = redisService.getRoomState(roomId);
        if (rm == null) {
            throw new RoomNotFoundException(rm);
        }
        if (rm.getCreator().equals(visitor) || rm.getWhite().equals(rm.getBlack())) {
            throw new SamePlayerException(rm, visitor);
        }
        if (rm.getWhite().equals("")) {
            rm.setWhite(visitor);
            log.info("player: {} joined room: {} as white", visitor, roomId);
        } else if (rm.getBlack().equals("")) {
            rm.setBlack(visitor);
            log.info("player: {} joined room: {} as black", visitor, roomId);
        } else {
            throw new RoomFullException(rm);
        }
        redisService.saveRoomState(rm);

        return rm;
    }

    @Override
    public void deleteRoom(String roomId) {
        redisService.deleteRoom(roomId);
        log.info("Room deleted: {}", roomId);
    }

    @Override
    public RoomState getRoomState(String roomId) {
        log.debug("Getting a roomState of : {}", roomId);
        RoomState state = redisService.getRoomState(roomId);
        if (state == null) {
            return new RoomState.Builder().build();
        }
        return state;

    }

    @Override
    public void loadRooms() {
        log.info("-------------------LOADING STARTED------------------");
        for (RoomState room : redisService.getAllExistingRooms()) {
            try {
                Board board = new Board();
                MoveList mList = new MoveList();

                board.loadFromFen(room.getPosition());
                mList.loadFromSan(room.getHistory());

                roomBoards.put(room.getId(), board);
                roomMoves.put(room.getId(), mList);
                log.info("loaded room : {}", room.getId());
            } catch (Exception e) {
                log.error("Skipping room " + room.getId() + ": corrupted data");
            }
        }
        log.info("------------------LOADING FINISHED------------------");

    }

    // gets every existing RoomState on redis
    public List<RoomState> getAllExistingRooms() {
        return redisService.getAllExistingRooms();
    }

    // gets the id of every existing room
    public List<String> getAllRoomIds() {
        List<String> idList = redisService.getAllExistingRooms().stream()
                .map(room -> room.getId())
                .collect(Collectors.toList());
        return idList;
    }

    public HashMap<String, Board> getBoards() {
        return roomBoards;
    }

    @Override
    public boolean acceptDraw(String roomId, String username) {
        RoomState state = getRoomState(roomId);
        if (state == null) {
            return false;
        }
        if (!Objects.equals(state.getDrawOfferedBy(), username)
                && state.hasPlayer(username) && !Objects.equals(state.getDrawOfferedBy(), null)) {
            closeRoom(state, GameStatus.DRAW, null);
            return true;
        }
        return false;
    }

    @Override
    public boolean offerDraw(String roomId, String username) {
        RoomState state = getRoomState(roomId);
        if (state == null) {
            return false;
        }
        if (Objects.equals(state.getDrawOfferedBy(), null)
                && state.hasPlayer(username)) {
            state.setDrawOfferedBy(username);
            redisService.saveRoomState(state);
            return true;
        }
        return false;
    }

    // The username means the player who resigns. So the winner is the opposite
    // player
    @Override
    public boolean resign(String roomId, String username) {
        RoomState state = getRoomState(roomId);
        Board board = roomBoards.get(roomId);
        if (board == null || state == null) {
            return false;
        }
        if (state.getBlack().equals(username)) {
            closeRoom(state, GameStatus.RESIGNED, state.getWhite());
            return true;
        }
        if (state.getWhite().equals(username)) {
            closeRoom(state, GameStatus.RESIGNED, state.getBlack());
            return true;
        }
        return false;

    }

    private void closeRoom(RoomState state, GameStatus status, String winner) {
        state.setWinner(winner);
        state.setStatus(status);
        state.setActive(false);
        roomBoards.remove(state.getId());
        redisService.saveRoomState(state);
    }

}
