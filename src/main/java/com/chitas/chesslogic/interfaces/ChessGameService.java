package com.chitas.chesslogic.interfaces;

//This interface assumes that the input that you give the service is TRUSTED
public interface ChessGameService {

    boolean doMove(String roomId, String from, String to, String promotion, String username);

    boolean acceptDraw(String roomId, String username);

    boolean offerDraw(String roomId, String username);

    boolean resign(String roomId, String username);
}
