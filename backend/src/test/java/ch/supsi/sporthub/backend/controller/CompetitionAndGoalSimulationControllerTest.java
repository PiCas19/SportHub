package ch.supsi.sporthub.backend.controller;

import ch.supsi.sporthub.backend.dto.request.SimulationRequest;
import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.security.JwtUtil;
import ch.supsi.sporthub.backend.service.api.ISimulationService;
import ch.supsi.sporthub.backend.service.api.IUserChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompetitionAndGoalSimulationController.class)
public class CompetitionAndGoalSimulationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @TestConfiguration
    static class MockConfig {
        @Bean
        IUserChatService userChatService() {
            return mock(IUserChatService.class);
        }

        @Bean
        ISimulationService simulationService() {
            return mock(ISimulationService.class);
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .build();
        }

        @Bean
        JwtUtil jwtUtil() {
            return mock(JwtUtil.class);
        }
    }

    @Autowired
    private IUserChatService userChatService;
    @Autowired
    private CompetitionAndGoalSimulationController controller;

    @Test
    void simulate_userNotFound() throws Exception {
        SimulationRequest request = new SimulationRequest();
        request.setChatId("123");
        request.setSimulationType("Goal");
        when(userChatService.getUserFromChat("123")).thenReturn(null);

        mockMvc.perform(post("/api/simulation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"chatId\":\"123\", \"simulationType\":\"Goal\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("User not found!"));
    }

    @Test
    void simulate_goalInPrivateChat() throws Exception {
        String chatId = "123";
        SimulationRequest request = new SimulationRequest();
        request.setChatId("123");
        request.setSimulationType("Private");
        User user = new User();
        user.setUsername("john");

        when(userChatService.getUserFromChat(chatId)).thenReturn(user);
        when(userChatService.hasPrivateChat(chatId, user)).thenReturn(true);

        mockMvc.perform(post("/api/simulation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"chatId\":\"123\", \"simulationType\":\"Goal\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Goal simulation completed for the user john"));
    }

    @Test
    void simulate_goalInGroupChat() throws Exception {
        String chatId = "-456";
        SimulationRequest request = new SimulationRequest();
        request.setChatId(chatId);
        request.setSimulationType("Goal");
        User user = new User();
        user.setUsername("john");

        when(userChatService.getUserFromChat(chatId)).thenReturn(user);
        when(userChatService.hasPrivateChat(chatId, user)).thenReturn(false);

        mockMvc.perform(post("/api/simulation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"chatId\":\"-456\", \"simulationType\":\"Goal\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Goal simulation can only be done in a private chat."));
    }

    @Test
    void simulate_competitionInPrivateChat() throws Exception {
        String chatId = "123";
        SimulationRequest request = new SimulationRequest();
        request.setChatId(chatId);
        request.setSimulationType("Competition");
        User user = new User();
        user.setUsername("john");

        when(userChatService.getUserFromChat(chatId)).thenReturn(user);
        when(userChatService.hasPrivateChat(chatId, user)).thenReturn(true);

        mockMvc.perform(post("/api/simulation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"chatId\":\"123\", \"simulationType\":\"Competition\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Competition simulation cannot be done in a private chat."));
    }

    @Test
    void simulate_competitionInGroupChat() throws Exception {
        String chatId = "-456";
        SimulationRequest request = new SimulationRequest();
        request.setChatId(chatId);
        request.setSimulationType("Competition");
        User user = new User();
        user.setUsername("john");

        when(userChatService.getUserFromChat(chatId)).thenReturn(user);
        when(userChatService.hasPrivateChat(chatId, user)).thenReturn(false);

        mockMvc.perform(post("/api/simulation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"chatId\":\"-456\", \"simulationType\":\"Competition\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Competition simulation completed for the user john"));
    }

    @Test
    void simulate_invalidSimulationType() throws Exception {
        String chatId = "123";
        String invalidSimulationType = "InvalidType";
        String requestBody = "{\"chatId\":\"" + chatId + "\", \"simulationType\":\"" + invalidSimulationType + "\"}";
        mockMvc.perform(post("/api/simulation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().string("Simulation type not recognized or invalid chat type!"));
    }

    @Test
    void testIsPrivateChat_validChatId() throws Exception {
        Method method = CompetitionAndGoalSimulationController.class.getDeclaredMethod("isPrivateChat", String.class);
        method.setAccessible(true);
        String chatId = "123";
        Object result = method.invoke(controller, chatId);
        assertTrue((Boolean) result);
    }

    @Test
    void testIsPrivateChat_invalidChatId() throws Exception {
        Method method = CompetitionAndGoalSimulationController.class.getDeclaredMethod("isPrivateChat", String.class);
        method.setAccessible(true);
        String chatId = "invalid-chat-id";
        Object result = method.invoke(controller, chatId);
        assertFalse((Boolean) result);
    }

    @Test
    void testIsPrivateChat_zeroChatId() throws Exception {
        Method method = CompetitionAndGoalSimulationController.class.getDeclaredMethod("isPrivateChat", String.class);
        method.setAccessible(true);
        String chatId = "0";
        Object result = method.invoke(controller, chatId);
        assertFalse((Boolean) result);
    }

}
