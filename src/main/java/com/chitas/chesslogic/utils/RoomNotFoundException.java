package com.chitas.chesslogic.utils;

import com.chitas.chesslogic.model.RoomState;

public class RoomNotFoundException extends RuntimeException {
    public RoomNotFoundException(RoomState room) {
        super("Room not found: " + room);
    }
}
