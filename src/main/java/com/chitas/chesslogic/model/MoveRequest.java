package com.chitas.chesslogic.model;

import lombok.Data;

@Data
public class MoveRequest {
    private String roomId;
    private String from;   // e.g., "e7"
    private String to;     // e.g., "e8"
    private String promotion; // optional: "q", "r", "b", "n"
}

