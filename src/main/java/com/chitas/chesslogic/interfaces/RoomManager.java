package com.chitas.chesslogic.interfaces;

import com.chitas.chesslogic.model.RoomState;

public interface RoomManager {

        RoomState createRoom(String creator, String white, String black, String gameType);

        RoomState joinRoom(String roomId, String visitorId);

        RoomState getRoomState(String roomId);

        void deleteRoom(String roomId);

        void loadRooms();

}
