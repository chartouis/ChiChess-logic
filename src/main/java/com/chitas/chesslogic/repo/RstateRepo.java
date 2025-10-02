package com.chitas.chesslogic.repo;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chitas.chesslogic.model.RoomState;

public interface RstateRepo extends JpaRepository<RoomState, UUID> {

}
