package com.chitas.chesslogic.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.chitas.chesslogic.model.User;

@Repository
public interface UsersRepo extends JpaRepository<User, Long> {
    User findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);


}

