package com.scaler.EcomUserService.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
public class SecretKeyConfig {

    @Value("${jwt.secret}")
    private String secret;
    @Bean
    public SecretKey getSecretKey(){
        byte[] encodedKey     = Base64.getDecoder().decode(secret);
        SecretKey key = new SecretKeySpec(encodedKey, 0, encodedKey.length, "HmacSHA256");
        return key;
    }
}
