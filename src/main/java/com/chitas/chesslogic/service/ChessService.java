package com.chitas.chesslogic.service;

import java.util.HashMap;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.chitas.chesslogic.interfaces.ChessGameService;
import com.chitas.chesslogic.interfaces.RoomManager;
import com.chitas.chesslogic.model.GameStatus;
import com.chitas.chesslogic.model.RoomState;
import com.chitas.chesslogic.utils.AnnoyingConstants;
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
    private final AnnoyingConstants acost;

    public ChessService(RedisService redisService, AnnoyingConstants acost) {
        this.redisService = redisService;
        this.acost = acost;
    }

    private HashMap<String, Board> roomBoards = new HashMap<>();
    private HashMap<String, MoveList> roomMoves = new HashMap<>();

    @Override
    public boolean doMove(String roomId, String from, String to, String promotion) {
        Move move;
        if (!promotion.equals("")) {
            move = new Move(Square.fromValue(from), Square.fromValue(to), Piece.fromValue(promotion)); // possible
                                                                                                       // exception
                                                                                                       // abuse. because
                                                                                                       // im not
                                                                                                       // checking
        } else {                                                                                        // of the piece with that promotion exists
            move = new Move(Square.fromValue(from), Square.fromValue(to));
        }
        RoomState state = getRoomState(roomId);
        Board board = roomBoards.get(roomId);
        MoveList moveList = roomMoves.get(roomId);
        String player = acost.getCurrentUserUsername();
        if (board == null || moveList == null || state == null) {
            return false;
        }

        String playerToMove = board.getSideToMove() == Side.WHITE ? state.getWhite() : state.getBlack();

        if (player.equals(playerToMove) && board.doMove(move, true)) {
            moveList.add(move);
            log.info("Move : {} on room : {}", move, roomId);
            state.setPosition(board.getFen());
            state.setHistory(moveList.toSan());
        
            // NOW check the status AFTER the move
            GameStatus status = checkBoardStatus(roomId);
            state.setStatus(status);
        
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
        
        return false;
    }

    private GameStatus checkBoardStatus(String roomId) {
        Board board = roomBoards.get(roomId);
        if(board == null){
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
    public RoomState createRoom(String creator, String white, String black) {
        log.info("Creating new room for creator: {}", creator);

        String roomId = generateRoomUUID();
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
        return redisService.getRoomState(roomId); // right now just uses redis, but it should also access postgress and
                                                  // look there if it couldn find it on redis
    }

    @Override
    public boolean offerDraw(String roomId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'offerDraw'");
    }

    @Override
    public boolean resign(String roomId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'resign'");
    }

}
