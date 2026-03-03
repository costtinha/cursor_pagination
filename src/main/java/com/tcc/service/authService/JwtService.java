package com.tcc.service.authService;

import com.tcc.dtos.authDto.*;
import com.tcc.entity.User;
import com.tcc.exception.AuthException;
import com.tcc.exception.ConflictException;
import com.tcc.persistance.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.Date;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secretKey;
    private static final long ACCESS_EXPIRATION = 1000 * 60 * 15;
    private static final long REFRESH_EXPIRATION = 1000 * 60 * 60 * 7;
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public JwtService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public String generateAccessToken(UserDetails user){
        return generateToken(user, ACCESS_EXPIRATION);
    }

    public String generateRefreshToken(UserDetails user){
        return generateToken(user,REFRESH_EXPIRATION);
    }


    public AuthResponse register(RegisterRequest request){
        if (repository.existsByUsername(request.username())){
            throw new ConflictException("Username: " + request.username() + " already exists");
        }
        User user = new  User();
        user.setUsername(request.username());
        String encodedPassword = passwordEncoder.encode(request.password());
        user.setPassword(encodedPassword);
        repository.save(user);
        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);
        return new AuthResponse(accessToken,refreshToken);

    }
    public AuthResponse login(LoginRequest body){
        User user = repository.findByUsername(body.username()).orElseThrow(() -> new AuthException("Invalid credentials"));
        if(!passwordEncoder.matches(body.password(), user.getPassword())){
            throw  new AuthException("Invalid credentials");
        }
        String accessToken  = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);
        return new AuthResponse(accessToken,refreshToken);
    }

    public TokenResponse refresh(RefreshRequest body){
        String username = extractUsername(body.refreshToken());
        User user = repository.findByUsername(username).orElseThrow(() -> new AuthException("Invalid token"));
        if(!isTokenValid(body.refreshToken(),user)){
            throw new AuthException("Refresh token invalid or expired");
        }
        String newAccessToken = generateAccessToken(user);
        return new TokenResponse(newAccessToken);

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
