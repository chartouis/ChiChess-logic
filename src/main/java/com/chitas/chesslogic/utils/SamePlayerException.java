package com.chitas.chesslogic.utils;

import com.chitas.chesslogic.model.RoomState;

public class SamePlayerException extends RuntimeException {
    public SamePlayerException(RoomState room, String player) {
        super("Room has the same player: " + player + " as white and black: \n" + room);
    }
}
