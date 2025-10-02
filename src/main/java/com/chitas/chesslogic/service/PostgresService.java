package com.chitas.chesslogic.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.chitas.chesslogic.model.RoomState;
import com.chitas.chesslogic.repo.RstateRepo;

@Service
public class PostgresService {
    private final RstateRepo repo;

    public PostgresService(RstateRepo repo) {
        this.repo = repo;
    }

    public RoomState save(RoomState state) {
        return repo.save(state);
    }

    public boolean has(UUID id) {
        return repo.existsById(id);
    }
}
