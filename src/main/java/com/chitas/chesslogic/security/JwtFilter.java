package com.chitas.chesslogic.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;

import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.chitas.chesslogic.service.JWTService;

import java.io.IOException;

@Component
@Log4j2
public class JwtFilter extends OncePerRequestFilter {

    private final JWTService jwtService;
    private final ApplicationContext context;

    public JwtFilter(JWTService jwtService, ApplicationContext context){
        this.jwtService = jwtService;
        this.context = context;

    }

    @SuppressWarnings("null")
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = getToken(request);
        String username = jwtService.extractUserName(token); 

        if (username != null || (SecurityContextHolder.getContext().getAuthentication() == null && token != null)){

            UserDetails userDetails = context.getBean(CUserDetailsService.class).loadUserByUsername(username);

            if(jwtService.validateToken(token, userDetails)){
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request,response);
    }
    public String getToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            log.trace("No cookies found in request" );
            return null;
        }
    
        for (Cookie ck : cookies) {
            String name = ck.getName();
            if ("ACCESS-TOKEN-JWTAUTH".equals(name)) {
                log.trace("Found token in cookie: {}", name);
                return ck.getValue();
            }
        }
        log.warn("No matching token found in cookies");
        return null;
    }
}
