package com.chitas.chesslogic.utils;

import com.chitas.chesslogic.model.RoomState;

public class RoomFullException extends RuntimeException {
    public RoomFullException(RoomState room) {
        super("Room is full: " + room);
    }
}