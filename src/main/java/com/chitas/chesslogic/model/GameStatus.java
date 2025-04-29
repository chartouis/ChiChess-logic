package com.chitas.chesslogic.model;

public enum GameStatus {
    WAITING, // lobby created
    ONGOING, // game in progress
    CHECKMATE,
    STALEMATE,
    DRAW,
    RESIGNED,
    TIMEOUT
}
