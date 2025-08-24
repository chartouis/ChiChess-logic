package com.chitas.chesslogic.model;

import lombok.Data;

@Data
public class GameType {
    private String name;
    private String initialWhite;
    private String initialBlack;
    private String incrementWhite;
    private String incrementBlack;
}
