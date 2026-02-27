package com.tcc.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

public abstract class RateLimitedController {
    protected ResponseEntity<List<?>> listFallBack(Throwable t){
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Collections.emptyList());
    }


    protected ResponseEntity<?> itemFallBack(Throwable t){
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("{\"error\": \"Too many requests. Try again later.\"}");
    }

    protected <T>ResponseEntity<?> itemFallBack(T dto,Throwable t){
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("{\"error\": \"Too many requests. Try again later.\"}");
    }
    protected ResponseEntity<Void> voidFallBack(Throwable t){
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    }

    protected <T>ResponseEntity<Void> voidFallBack(T dto, Throwable t){
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    }

}
