package com.example.demo.security;

import com.example.demo.jwtUtils.JwtTokenUtil;
import com.example.demo.service.AccountUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.text.html.Option;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Component
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {
    private final AccountUserDetailsService accountUserDetailsService;
    private final JwtTokenUtil jwtTokenUtil;

    @Autowired
    public JwtRequestFilter(AccountUserDetailsService accountUserDetailsService,
                            JwtTokenUtil jwtTokenUtil) {
        this.accountUserDetailsService = accountUserDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = getToken(request);
            String username = getUsername(request);
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.accountUserDetailsService.loadUserByUsername(username);
                if (jwtTokenUtil.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                }
            }
        } catch (UsernameNotFoundException e) {
            log.info("Username not found");
        } catch (ExpiredJwtException e) {
            log.info("JWT Token has expired");
        } catch (IllegalArgumentException e) {
            log.info("Unable to get JWT Token");
        } catch (Exception e) {
            e.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }

    private String getToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getCookies())
                .map(Arrays::stream)
                .map(cookieStream -> cookieStream
                        .filter(c -> c.getName().equals("token")))
                .flatMap(Stream::findAny)
                .map(Cookie::getValue)
                .orElse("");
    }

    private String getUsername(HttpServletRequest request) throws IllegalArgumentException {
        return Optional.ofNullable(request.getCookies())
                .map(Arrays::stream)
                .map(cookieStream -> cookieStream.filter(c -> c.getName().equals("token")))
                .flatMap(Stream::findAny)
                .map(Cookie::getValue)
                .map(this.jwtTokenUtil::getUsernameFromToken)
                .orElse("");
    }
}
