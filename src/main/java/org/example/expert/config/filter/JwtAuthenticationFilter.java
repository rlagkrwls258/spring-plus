package org.example.expert.config.filter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.config.authuser.AuthUserDetails;
import org.example.expert.config.authuser.AuthUserDetailsService;
import org.example.expert.config.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil; // JWT 토큰을 처리하는 클래스

    private final AuthUserDetailsService authUserDetailsService;

    @Autowired
    public JwtAuthenticationFilter(JwtUtil jwtUtil, AuthUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.authUserDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String token = jwtUtil.resolveToken(request);
        if (token != null && jwtUtil.validateToken(token)) {
            Claims claims = jwtUtil.extractClaims(token);
            String email = claims.get("email", String.class);

            UserDetails userDetails = authUserDetailsService.loadUserByUsername(email);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        chain.doFilter(request, response);
    }
}