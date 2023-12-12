package com.twitterclone.demo.auth

import groovy.util.logging.Slf4j
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys

import java.security.Key
import java.time.Instant
import java.time.temporal.ChronoUnit

@Slf4j
class JwtTokenUtils {

    private static final String SECRET_KEY = "suPEr-hjGEFG*&_secret%for*super-keyAaBbFgE&&&%*#"
    private static final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes("UTF-8"))
    private static final long EXPIRATION_TIME = ChronoUnit.MINUTES.getDuration().toMillis() * 30

    static String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setExpiration(Date.from(Instant.now().plusMillis(EXPIRATION_TIME)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact()
    }

    static String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
            return claims.getSubject()
        } catch (JwtException ex) {
            log.warn("Getting username from jwt was failed", ex)
            return null
        }
    }

    static boolean isTokenValid(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
            return claims.getExpiration().after(Date.from(Instant.now()))
        } catch (Exception ex) {
            log.warn("Invalid token. Value is '%'", token, ex)
            return false
        }
    }
}