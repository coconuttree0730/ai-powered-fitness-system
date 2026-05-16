package com.fitness.integration.security;

import com.fitness.common.config.JwtProperties;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtTokenProviderTest {

    @Test
    void generateTokenShouldPreserveLegacyClaims() {
        JwtProperties properties = mock(JwtProperties.class);
        when(properties.getSecret()).thenReturn("01234567890123456789012345678901");
        when(properties.getAccessExpiration()).thenReturn(3600000L);
        when(properties.getIssuer()).thenReturn("fitness");
        when(properties.getAudience()).thenReturn("fitness-web");

        JwtTokenProvider provider = new JwtTokenProvider(properties);

        String token = provider.generateAccessToken(1L, "coach001", List.of("COACH"));
        Claims claims = provider.parseToken(token);

        assertEquals("coach001", claims.getSubject());
        assertEquals(1L, ((Number) claims.get("userId")).longValue());
        assertEquals(List.of("COACH"), claims.get("roles"));
        assertEquals("access", claims.get("type"));
        assertTrue(provider.validateAccessToken(token));
    }
}