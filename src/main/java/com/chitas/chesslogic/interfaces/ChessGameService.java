package com.chitas.chesslogic.interfaces;

public interface ChessGameService {

    boolean doMove(String roomId, String from, String to, String promotion);

    boolean offerDraw(String roomId);

    boolean resign(String roomId);
}
