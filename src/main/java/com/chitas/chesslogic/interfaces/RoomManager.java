package com.chitas.chesslogic.interfaces;

import java.util.UUID;

import com.chitas.chesslogic.model.RoomState;

public interface RoomManager {

        RoomState createRoom(String creator, String white, String black, String gameType);

        RoomState joinRoom(UUID roomId, String visitorId);

        RoomState getRoomState(UUID roomId);

        void deleteRoom(UUID roomId);

        void loadRooms();

}
