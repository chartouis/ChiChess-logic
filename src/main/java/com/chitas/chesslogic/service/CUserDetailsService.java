package com.chitas.chesslogic.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.chitas.chesslogic.model.User;
import com.chitas.chesslogic.model.UserPrincipal;
import com.chitas.chesslogic.repo.UsersRepo;

@Service
public class CUserDetailsService implements UserDetailsService {

    @Autowired
    private UsersRepo repo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = repo.findByUsername(username);
        if (user == null){
            System.out.println("User not found");
            throw new UsernameNotFoundException(username + " user was not found");
        }
        return new UserPrincipal(user);
    }

}
