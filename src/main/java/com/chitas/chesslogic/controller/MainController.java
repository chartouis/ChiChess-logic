package com.chitas.chesslogic.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chitas.chesslogic.service.ChessService;



@RestController
@RequestMapping
public class MainController {
    private final ChessService chess;

    public MainController(ChessService chess){
        this.chess = chess;

    }

    // @PostMapping("/move")
    // public String doMove(@RequestBody String entity) {
    //     return chess.move(entity);
    // }
    

}
