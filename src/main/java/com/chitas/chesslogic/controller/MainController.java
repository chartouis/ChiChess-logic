package com.chitas.chesslogic.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class MainController {
    // private final ChessService chess;
    // private final SimpMessagingTemplate messagingTemplate;

    // public MainController(ChessService chess, SimpMessagingTemplate messagingTemplate) {
    //     this.chess = chess;
    //     this.messagingTemplate = messagingTemplate;

    // }

    // @MessageMapping("/game/{roomId}/move")
    // public void doMove(@RequestBody @Valid MoveRequest move, @PathVariable String roomId) {
    //     MoveResponse resp = new MoveResponse(chess.doMove(roomId, move.getFrom(), move.getTo(), move.getPromotion()),
    //             chess.getRoomState(roomId));
    //     messagingTemplate.convertAndSend("/subscribed/game/" + roomId, resp);
    // }


}
