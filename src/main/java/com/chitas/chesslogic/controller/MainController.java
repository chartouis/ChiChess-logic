package com.chitas.chesslogic.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
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

    @PostMapping("/api/game/{roomId}")
    public ResponseEntity<MoveResponse> doMove(@RequestBody @Valid MoveRequest move, @PathVariable String roomId) {
        MoveResponse resp = new MoveResponse(chess.doMove(roomId,move.getFrom(), move.getTo(), move.getPromotion()), chess.getRoomState(roomId));
        return ResponseEntity.ok(resp);
    }

}
