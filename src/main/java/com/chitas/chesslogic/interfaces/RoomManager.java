package com.chitas.chesslogic.interfaces;

import com.chitas.chesslogic.model.RoomState;

public interface RoomManager {

        RoomState createRoom(String creatorId);
        RoomState joinRoom(String roomId, String visitorId);
        void deleteRoom(String roomId);
    
}
