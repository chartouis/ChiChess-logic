package com.chitas.chesslogic.model;

import lombok.Data;

@Data
public class RoomState {
    private String id;
    private boolean isActive;
    private String creator;
    private String black;
    private String white;
    private String position; // FEN
    private String history; // SAN moves

    private RoomState(Builder builder) {
        this.id = builder.id;
        this.isActive = builder.isActive;
        this.creator = builder.creator;
        this.black = builder.black;
        this.white = builder.white;
        this.position = builder.position;
        this.history = builder.history;
    }

    public static class Builder {
        private String id;
        private boolean isActive;
        private String creator;
        private String black;
        private String white;
        private String position;
        private String history;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder isActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public Builder creator(String creator) {
            this.creator = creator;
            return this;
        }

        public Builder black(String black) {
            this.black = black;
            return this;
        }

        public Builder white(String white) {
            this.white = white;
            return this;
        }

        public Builder position(String position) {
            this.position = position;
            return this;
        }

        public Builder history(String history) {
            this.history = history;
            return this;
        }

        public RoomState build() {
            return new RoomState(this);
        }
    }
}
