package com.chitas.chesslogic.service;

import org.springframework.stereotype.Service;

import com.github.bhlangonijr.chesslib.Board;

@Service
public class ChessService {
    public static final Board board = new Board();

    public ChessService() {
        
    }

    public String move(String move) {
        System.out.println(move);
        board.doMove(move);
        return board.getFen();
    }
}
