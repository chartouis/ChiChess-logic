package com.chitas.chesslogic.utils;

public class RoomFullException extends RuntimeException {
    public RoomFullException(String roomId) {
        super("Room is full: " + roomId);
    }
}