package com.chitas.chesslogic.utils;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AnnoyingConstants {

    public String getCurrentUserUsername(){
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

}
