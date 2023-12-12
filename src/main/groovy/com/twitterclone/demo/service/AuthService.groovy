package com.twitterclone.demo.service

import com.twitterclone.demo.auth.JwtTokenUtils
import com.twitterclone.demo.controller.dto.UserDto
import com.twitterclone.demo.exception.exceptions.UnauthorizedException
import com.twitterclone.demo.repo.entities.User
import org.springframework.http.HttpHeaders
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

@Service
class AuthService {

    private final Set<String> jwtCache = Collections.newSetFromMap(new ConcurrentHashMap<>())
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1)
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder()
    private final UsersService usersService

    AuthService(UsersService usersService) {
        this.usersService = usersService
    }

    @PostConstruct
    void init() {
        executorService.scheduleAtFixedRate(() -> {
            jwtCache.removeIf(token -> !JwtTokenUtils.isTokenValid(token))
        }, 0, Duration.ofMinutes(1).toMillis(), TimeUnit.MILLISECONDS)
    }

    @PreDestroy
    void destroy() {
        executorService.shutdownNow()
    }

    void login(HttpServletRequest request, HttpServletResponse response) {
        final String header = request.getHeader(HttpHeaders.AUTHORIZATION)

        if (header == null || !header.startsWith("Basic ")) {
            throw new UnauthorizedException("Basic token is required for authorization")
        }

        final String base64Credentials = header.substring(6)
        byte[] credDecoded = Base64.getDecoder().decode(base64Credentials)
        String[] credentials = new String(credDecoded).split(":", 2)

        if (credentials.length != 2) {
            throw new UnauthorizedException("Invalid Basic token format")
        }

        User user = usersService.findUserByUsername(credentials[0])
                .orElseThrow(() -> new UnauthorizedException("Username and/or password are incorrect"))

        if (!passwordEncoder.matches(credentials[1], user.getPassword())) {
            throw new UnauthorizedException("Username and/or password are incorrect")
        }

        String generatedToken = JwtTokenUtils.generateToken(user.getUsername())
        jwtCache.add(generatedToken)

        response.setContentType("application/json")
        response.setHeader(HttpHeaders.AUTHORIZATION, generatedToken)
    }


    void logout(HttpServletRequest request) {
        final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!header || !header.startsWith("Bearer ")) {
            throw new UnauthorizedException("Bearer token is required for logout request")
        }

        final String token = header.split(" ")[1].trim();
        if (jwtCache.contains(token)) {
            jwtCache.remove(token)
        }
    }

    Map registration(UserDto userDto) {
        return usersService.createUser(userDto)
    }

    boolean existsInCache(String token) {
        return jwtCache.contains(token)
    }
}
