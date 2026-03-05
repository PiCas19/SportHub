package ch.supsi.sporthub.backend.controller;

import ch.supsi.sporthub.backend.dto.request.strava.StravaWebhookEvent;
import ch.supsi.sporthub.backend.dto.response.strava.StravaWebhookResponse;
import ch.supsi.sporthub.backend.security.JwtUtil;
import ch.supsi.sporthub.backend.service.JwtTokenService;
import ch.supsi.sporthub.backend.service.UserService;
import ch.supsi.sporthub.backend.service.api.IStravaWebhookService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StravaWebhookController.class)
public class StravaWebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private IStravaWebhookService stravaWebhookService;


    @TestConfiguration
    static class MockConfig {

        @Bean
        public IStravaWebhookService stravaWebhookService() {
            return mock(IStravaWebhookService.class);
        }

        @Bean
        public JwtTokenService jwtTokenService() {
            return mock(JwtTokenService.class);
        }

        @Bean
        public UserService userService() {
            return mock(UserService.class);
        }

        @Bean
        public JwtUtil jwtUtil() {
            return mock(JwtUtil.class);
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .build();
        }
    }

    @Test
    void handleWebhook_invalidEventType() throws Exception {
        StravaWebhookEvent event = new StravaWebhookEvent();
        event.setAspectType("delete");
        event.setObjectType("activity");
        event.setObjectId(12345L);
        event.setOwnerId(67890L);
        event.setEventTime(System.currentTimeMillis());
        mockMvc.perform(post("/api/strava/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"aspect_type\":\"delete\",\"object_type\":\"activity\",\"object_id\":12345,\"owner_id\":67890,\"event_time\":1628276800000}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unsupported event type: delete"));

        verifyNoInteractions(stravaWebhookService);
    }

    @Test
    void verifyWebhook_invalidToken() throws Exception {
        String mode = "subscribe";
        String challenge = "challenge123";
        String verifyToken = "kdf8js92lHdFz4m";
        when(stravaWebhookService.isVerifyTokenValid(verifyToken)).thenReturn(false);
        mockMvc.perform(get("/api/strava/webhook")
                        .param("hub.mode", mode)
                        .param("hub.challenge", challenge)
                        .param("hub.verify_token", verifyToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Invalid verify token or mode"));
    }


    @Test
    void handleWebhook_InvalidAspectType_ShouldThrowException() throws Exception {
        StravaWebhookEvent event = new StravaWebhookEvent();
        event.setAspectType("update");
        event.setObjectType("activity");

        mockMvc.perform(post("/api/strava/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"aspect_type\":\"update\",\"object_type\":\"activity\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unsupported event type: update"));

        verifyNoInteractions(stravaWebhookService);
    }

    @Test
    void handleWebhook_InvalidObjectType_ShouldThrowException() throws Exception {
        StravaWebhookEvent event = new StravaWebhookEvent();
        event.setAspectType("create");
        event.setObjectType("athlete");

        mockMvc.perform(post("/api/strava/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"aspect_type\":\"create\",\"object_type\":\"athlete\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unsupported event type: create"));

        verifyNoInteractions(stravaWebhookService);
    }

    @Test
    void verifyWebhook_InvalidMode_ShouldThrowException() throws Exception {
        String mode = "unsubscribe";
        String challenge = "challenge123";
        String verifyToken = "validToken123";

        mockMvc.perform(get("/api/strava/webhook")
                        .param("hub.mode", mode)
                        .param("hub.challenge", challenge)
                        .param("hub.verify_token", verifyToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Invalid verify token or mode"));

        verifyNoInteractions(stravaWebhookService);
    }

    @Test
    void verifyWebhook_InvalidToken_ShouldThrowException() throws Exception {
        String mode = "subscribe";
        String challenge = "challenge123";
        String verifyToken = "invalidToken";

        when(stravaWebhookService.isVerifyTokenValid(verifyToken)).thenReturn(false);

        mockMvc.perform(get("/api/strava/webhook")
                        .param("hub.mode", mode)
                        .param("hub.challenge", challenge)
                        .param("hub.verify_token", verifyToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Invalid verify token or mode"));
    }
}
