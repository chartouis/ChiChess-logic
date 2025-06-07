package com.chitas.chesslogic.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chitas.chesslogic.model.MoveRequest;
import com.chitas.chesslogic.model.MoveResponse;
import com.chitas.chesslogic.service.ChessService;

import jakarta.validation.Valid;

@RestController
@RequestMapping
public class MainController {
    private final ChessService chess;

    public MainController(ChessService chess) {
        this.chess = chess;

    }

    @PostMapping("/api/move")
    public ResponseEntity<MoveResponse> doMove(@RequestBody @Valid MoveRequest move) {
        MoveResponse resp = new MoveResponse(chess.doMove(move.getRoomId(),move.getFrom(), move.getTo(), move.getPromotion()), chess.getRoomState(move.getRoomId()));
        return ResponseEntity.ok(resp);
    }

}
