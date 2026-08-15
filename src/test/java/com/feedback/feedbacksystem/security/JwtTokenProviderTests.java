package com.feedback.feedbacksystem.security;

import com.feedback.feedbacksystem.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JwtTokenProviderTests {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void testTokenGenerationAndValidation() {
        Authentication auth = new UsernamePasswordAuthenticationToken("student@college.edu", "password");
        String token = jwtTokenProvider.generateToken(auth);

        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getEmailFromToken(token)).isEqualTo("student@college.edu");
    }

    @Test
    void testTokenWithClaimsGeneration() {
        String token = jwtTokenProvider.generateTokenForUser("faculty@college.edu", 5L, "ROLE_FACULTY");

        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getEmailFromToken(token)).isEqualTo("faculty@college.edu");
    }

    @Test
    void testInvalidTokenValidation() {
        assertThat(jwtTokenProvider.validateToken("invalid.jwt.token")).isFalse();
    }
}
