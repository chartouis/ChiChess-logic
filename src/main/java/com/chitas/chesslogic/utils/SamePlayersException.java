package com.chitas.chesslogic.utils;

public class SamePlayersException extends RuntimeException {
    public SamePlayersException(String roomId, String player) {
        super("Room has the same player: " + player + " as white and black: " + roomId);
    }
}
