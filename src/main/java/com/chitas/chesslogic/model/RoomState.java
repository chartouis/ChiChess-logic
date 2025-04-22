package com.chitas.chesslogic.model;

import lombok.Data;

@Data
public class RoomState {
    // room id
    private String id;

    // if the game is live
    private boolean isActive;

    // id of the player who created the room
    private String creatorId;

    // id of the player who entered the room
    private String visitorId;

    // position in FEN format
    private String position;
    
    // history of moves in SAN format
    private String history;

    // Private constructor to enforce the use of Builder
    private RoomState(Builder builder) {
        this.id = builder.id;
        this.isActive = builder.isActive;
        this.creatorId = builder.creatorId;
        this.visitorId = builder.visitorId;
        this.position = builder.position;
        this.history = builder.history;
    }

    // Static Builder class to create RoomState instances
    public static class Builder {
        private String id;
        private boolean isActive;
        private String creatorId;
        private String visitorId;
        private String position;
        private String history;

        // Set room ID
        public Builder id(String id) {
            this.id = id;
            return this;
        }

        // Set game status (active/inactive)
        public Builder isActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        // Set creator's player ID
        public Builder creatorId(String creatorId) {
            this.creatorId = creatorId;
            return this;
        }

        // Set visitor's player ID
        public Builder visitorId(String visitorId) {
            this.visitorId = visitorId;
            return this;
        }

        // Set the current position in FEN format
        public Builder position(String position) {
            this.position = position;
            return this;
        }

        // Set the move history in SAN format
        public Builder history(String history) {
            this.history = history;
            return this;
        }

        // Build and return the RoomState object
        public RoomState build() {
            return new RoomState(this);
        }
    }
}
