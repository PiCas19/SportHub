package ch.supsi.sporthub.backend.controller;

import ch.supsi.sporthub.backend.dto.request.auth.AddChatRequest;
import ch.supsi.sporthub.backend.dto.response.auth.ChatManagementResponse;
import ch.supsi.sporthub.backend.dto.response.telegram.ChatInfoResponse;
import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.security.JwtUtil;
import ch.supsi.sporthub.backend.service.JwtTokenService;
import ch.supsi.sporthub.backend.service.UserService;
import ch.supsi.sporthub.backend.service.api.ITelegramChatManagementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TelegramChatManagementController.class)
public class TelegramChatManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class MockConfig {
        @Bean
        public JwtTokenService jwtTokenService() {
            return mock(JwtTokenService.class);
        }

        @Bean
        public UserService userService() {
            return mock(UserService.class);
        }

        @Bean
        public ITelegramChatManagementService chatManagementService() {
            return mock(ITelegramChatManagementService.class);
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

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private UserService userService;

    @Autowired
    private ITelegramChatManagementService chatManagementService;

    @Test
    void addChat() throws Exception {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";
        String username = "john";
        User user = new User();
        user.setUsername(username);

        AddChatRequest request = new AddChatRequest();
        request.setChatId("12345");

        ChatManagementResponse response = new ChatManagementResponse("Chat added successfully", true, 123);

        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(user);
        when(chatManagementService.addChat(user, "12345")).thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(post("/api/telegram/chats")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Chat added successfully"));

        verify(chatManagementService).addChat(user, "12345");
    }

    @Test
    void getUserChats() throws Exception {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";
        String username = "john";
        User user = new User();
        user.setUsername(username);

        ChatInfoResponse chatInfoResponse = new ChatInfoResponse(
                "Chats retrieved successfully",
                "Test Chat",
                10,
                "12345",
                1L,
                "A test chat",
                "small.jpg",
                "big.jpg",
                List.of("admin1", "admin2"),
                "group"
        );

        List<ChatInfoResponse> chats = List.of(chatInfoResponse);
        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(user);
        when(chatManagementService.getUserGroupChatIds(user)).thenReturn(chats);
        mockMvc.perform(get("/api/telegram/chats")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chats[0].chatId").value("12345"))
                .andExpect(jsonPath("$.chats[0].title").value("Test Chat"))
                .andExpect(jsonPath("$.chats[0].memberCount").value(10));

        verify(chatManagementService).getUserGroupChatIds(user);
    }


    @Test
    void removeChat() throws Exception {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";
        String username = "john";
        User user = new User();
        user.setUsername(username);

        Long chatId = 12345L;
        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(user);
        doNothing().when(chatManagementService).removeChat(user, chatId);
        mockMvc.perform(delete("/api/telegram/chats/{id}", chatId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Chat successfully removed"));

        verify(chatManagementService).removeChat(user, chatId);
    }
}
