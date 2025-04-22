package com.chitas.chesslogic.service;

import java.util.HashMap;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.chitas.chesslogic.interfaces.RoomManager;
import com.chitas.chesslogic.model.RoomState;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.MoveList;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class ChessService implements RoomManager {

    public ChessService() {

    }

    private HashMap<String, Board> roomBoards = new HashMap<>();
    private HashMap<String, MoveList> roomMoves = new HashMap<>();

    public void doMove(String roomId, String move) {
        Board board = roomBoards.get(roomId);
        MoveList moveList = roomMoves.get(roomId);

        if (board != null && moveList != null) {
            board.doMove(move);

        }
    }

    private String generateRoomUUID() {
        return UUID.randomUUID().toString();
    }

    @Override
    public RoomState createRoom(String creatorId) {
        log.info("Creating new room for creatorId: {}", creatorId);

        String roomId = generateRoomUUID();
        log.debug("Generated roomId: {}", roomId);

        Board board = new Board();
        MoveList mList = new MoveList();

        roomBoards.put(roomId, board);
        roomMoves.put(roomId, mList);

        log.debug("Initialized board and move list for roomId: {}", roomId);

        RoomState roomState = new RoomState.Builder()
                .creatorId(creatorId)
                .id(roomId)
                .position(board.getFen())
                .history(mList.toSan())
                .build();

        log.info("Room created: {}", roomId);

        // TODO save this roomState in redis
        return roomState;
    }

    @Override
    public RoomState joinRoom(String roomId, String visitorId) {
        // has to fetch a room from redis and then save it back there but with a
        // visitorID
        throw new UnsupportedOperationException("Unimplemented method 'ashdua'");
    }

    @Override
    public void deleteRoom(String roomId) {
        //has to delete a room with the given roomId from redis
        throw new UnsupportedOperationException("Unimplemented method 'ashdua'");    }

}
