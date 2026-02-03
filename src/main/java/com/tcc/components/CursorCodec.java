package com.tcc.components;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
public class CursorCodec {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String encode(Object cursor){
        try {
            String json = objectMapper.writeValueAsString(cursor);
            return Base64.getUrlEncoder().encodeToString(json.getBytes());
        }
        catch (Exception e){
            throw new IllegalArgumentException("Invalid cursor");
        }
    }


    public <T> T decode(String token, Class<T> clazz){
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(token);
            return objectMapper.readValue(decoded,clazz);

        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid cursor token");
        }
    }
}
