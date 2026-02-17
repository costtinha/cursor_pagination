package com.tcc.service.authService;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;


import java.util.Date;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secretKey;
    private static final long ACCESS_EXPIRATION = 1000 * 60 * 15;
    private static final long REFRESH_EXPIRATION = 1000 * 60 * 60 * 7;

    public String generateAccessToken(UserDetails user){
        return generateToken(user, ACCESS_EXPIRATION);
    }

    public String generateRefreshToken(UserDetails user){
        return generateToken(user,REFRESH_EXPIRATION);
    }



    public String generateToken(UserDetails user, long expiration){
        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()),SignatureAlgorithm.HS256)
                .compact();

    }

    public String extractUsername(String token){
        return Jwts.parserBuilder()
                .setSigningKey(secretKey.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean isTokenValid(String token,UserDetails user){
        try {
            String username = extractUsername(token);
            return username.equals(user.getUsername()) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token){
        return Jwts.parserBuilder()
                .setSigningKey(secretKey.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration()
                .before(new Date());
    }
}
