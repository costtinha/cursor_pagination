package com.tcc.controller;

import com.tcc.dtos.authDto.*;
import com.tcc.entity.User;
import com.tcc.exception.AuthException;
import com.tcc.exception.ConflictException;
import com.tcc.persistance.UserRepository;
import com.tcc.service.authService.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/public")
public class AuthController {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UserRepository repository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }
    @PostMapping
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request){
        if (repository.existsByUsername(request.username())){
            throw new ConflictException("Username: " + request.username() +" already exists");
        }
        User user = new User();
        user.setUsername(request.username());
        String encodedPassword = passwordEncoder.encode(request.password());
        user.setPassword(encodedPassword);

        repository.save(user);
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(accessToken,refreshToken));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest body){
        User user = repository.findByUsername(body.username()).orElseThrow(() -> new AuthException("Invalid credentials"));
        if(!passwordEncoder.matches(body.password(),user.getPassword())){
            throw new AuthException("Invalid credentials");
        }
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return ResponseEntity.ok(new AuthResponse(accessToken,refreshToken));

    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest body){
        String username = jwtService.extractUsername(body.refreshToken());
        User user = repository.findByUsername(username).orElseThrow(() -> new AuthException("Invalid token"));
        if(!jwtService.isTokenValid(body.refreshToken(), user)){
            throw new AuthException("Refresh token invalid or expired");
        }
        String newAccessToken = jwtService.generateAccessToken(user);
        return ResponseEntity.ok(new TokenResponse(newAccessToken));

    }


}
