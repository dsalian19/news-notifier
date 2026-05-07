package com.newsnotifier.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        String testSecret = Base64.getEncoder().encodeToString(new byte[64]);
        jwtUtil = new JwtUtil(testSecret, 86400000);
    }

    @Test
    void generateAndExtractEmail() {
        String token = jwtUtil.generateToken("test@example.com");
        assertEquals("test@example.com", jwtUtil.extractEmail(token));
    }

    @Test
    void validTokenPassesValidation() {
        String token = jwtUtil.generateToken("test@example.com");
        assertTrue(jwtUtil.isTokenValid(token));
    }

    @Test
    void garbageTokenFailsValidation() {
        assertFalse(jwtUtil.isTokenValid("not.a.token"));
    }
}
