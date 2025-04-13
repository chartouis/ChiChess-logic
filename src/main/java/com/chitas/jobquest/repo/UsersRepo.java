package com.chitas.jobquest.repo;

import com.chitas.jobquest.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepo extends JpaRepository<User, Long> {
    User findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);


}

