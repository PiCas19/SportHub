package ch.supsi.sporthub.backend.controller;

import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.security.JwtUtil;
import ch.supsi.sporthub.backend.service.EmailService;
import ch.supsi.sporthub.backend.service.JwtTokenService;
import ch.supsi.sporthub.backend.service.UserService;
import ch.supsi.sporthub.backend.service.api.ITelegramService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TelegramController.class)
public class TelegramControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ITelegramService telegramService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @TestConfiguration
    static class MockConfig {
        @Bean
        public ITelegramService telegramService() {
            return mock(ITelegramService.class);
        }

        @Bean
        public EmailService emailService() {
            return mock(EmailService.class);
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
            JwtUtil jwtUtil = mock(JwtUtil.class);
            when(jwtUtil.validateToken(anyString(), any(UserDetails.class))).thenReturn(true);
            return jwtUtil;
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
    void sendMessage_success() throws Exception {
        String token = "Bearer valid.token";
        String chatId = "123";
        String message = "Test Message";

        when(telegramService.sendMessage(chatId, message)).thenReturn("Message sent");

        mockMvc.perform(post("/api/telegram/send")
                        .header("Authorization", token)
                        .param("chatId", chatId)
                        .param("text", message))
                .andExpect(status().isOk());
    }

    @Test
    void getChatUpdates_success() throws Exception {
        String token = "Bearer valid.token";

        when(telegramService.getChatUpdates()).thenReturn("Updates fetched");

        mockMvc.perform(get("/api/telegram/getUpdates")
                        .header("Authorization", token))
                .andExpect(status().isOk());
    }

    @Test
    void sendGroupInviteEmail_success() throws Exception {
        String token = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";
        String username = "john";
        User user = new User();
        user.setUsername(username);
        user.setEmail("john@example.com");
        String inviteLink = "http://telegram.com/group-invite";

        when(jwtTokenService.extractUsername(anyString())).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(user);
        when(telegramService.getGroupInviteLink()).thenReturn(inviteLink);

        mockMvc.perform(post("/api/telegram/invite/group")
                        .header("Authorization", token))
                .andExpect(status().isAccepted())
                .andExpect(content().string("Invitation to add bot to a group successfully sent to john@example.com"));

        verify(emailService, times(1)).sendInviteTelegramBot(user.getEmail(), inviteLink);
        verify(telegramService, times(1)).getGroupInviteLink();
    }

    @Test
    void sendUserInviteEmail_success() throws Exception {
        String token = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";
        String username = "john";
        User user = new User();
        user.setUsername(username);
        user.setEmail("john@example.com");
        String botLink = "https://t.me/your_bot_link";
        when(jwtTokenService.extractUsername(anyString())).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(user);
        when(telegramService.getBotLink()).thenReturn(botLink);
        mockMvc.perform(post("/api/telegram/invite/user")
                        .header("Authorization", token))
                .andExpect(status().isAccepted())
                .andExpect(content().string("Telegram bot invitation successfully sent to john@example.com"));
        verify(emailService, times(1)).sendInviteTelegramBot(user.getEmail(), botLink);
        verify(telegramService, times(1)).getBotLink();
    }

    @Test
    void handleUpdate_success() throws Exception {
        String update = "{\"key\":\"value\"}";
        when(telegramService.handleUpdate(update)).thenReturn("Update handled");

        mockMvc.perform(post("/api/telegram/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(update))
                .andExpect(status().isOk());
    }
}