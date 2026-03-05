package ch.supsi.sporthub.backend.controller;

import ch.supsi.sporthub.backend.dto.request.SetGoalRequest;
import ch.supsi.sporthub.backend.dto.response.GoalItemResponse;
import ch.supsi.sporthub.backend.exception.StravaTokenUnavailableException;
import ch.supsi.sporthub.backend.model.*;
import ch.supsi.sporthub.backend.security.JwtUtil;
import ch.supsi.sporthub.backend.service.EmailService;
import ch.supsi.sporthub.backend.service.JwtTokenService;
import ch.supsi.sporthub.backend.service.UserService;
import ch.supsi.sporthub.backend.service.api.IGoalService;
import ch.supsi.sporthub.backend.service.StravaAuthService;
import ch.supsi.sporthub.backend.service.domain.strava.StravaTokenResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GoalController.class)
public class GoalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;



    @TestConfiguration
    static class MockConfig {
        @Bean
        public JwtUtil jwtUtil() {
            return mock(JwtUtil.class);
        }
        @Bean
        UserDetailsService userDetailsService() {
            return mock(UserDetailsService.class);
        }

        @Bean
        EmailService emailService() {
            return mock(EmailService.class);
        }

        @Bean
        AuthenticationManager authenticationManager() {
            return mock(AuthenticationManager.class);
        }

        @Bean
        JwtTokenService jwtTokenService() {
            return mock(JwtTokenService.class);
        }

        @Bean
        StravaAuthService stravaAuthService() {
            return mock(StravaAuthService.class);
        }

        @Bean
        UserService userService() {
            return mock(UserService.class);
        }

        @Bean
        IGoalService goalService() {
            return mock(IGoalService.class);
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .build();
        }
    }

    @Autowired private JwtTokenService jwtTokenService;
    @Autowired private UserService userService;
    @Autowired private IGoalService goalService;
    @Autowired private StravaAuthService stravaAuthService;

    @Test
    void createGoal() throws Exception {
        String username = "john";
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";

        User user = new User();
        user.setUsername(username);
        SetGoalRequest request = new SetGoalRequest();
        request.setTargetValue(100.0);
        request.setGoalType("distance");
        request.setPeriod(Period.MONTHLY);
        request.setSportType("RUN");

        Goal createdGoal = new Goal();
        createdGoal.setId(1L);
        createdGoal.setTargetValue(100.0);
        createdGoal.setGoalType(GoalType.DISTANCE);
        createdGoal.setPeriod(Period.MONTHLY);
        createdGoal.setSportType(SportType.RUN);

        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(jwtTokenService.extractUsername("Bearer " + token)).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(user);
        when(jwtTokenService.isTokenValid(anyString(), any())).thenReturn(true);
        when(goalService.createGoal(eq(request), eq(user))).thenReturn(createdGoal);

        mockMvc.perform(post("/api/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Goal created 1"));

        verify(goalService).createGoal(eq(request), eq(user));
    }

    @Test
    void getGoals() throws Exception {
        String username = "john";
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";

        User user = new User();
        user.setUsername(username);

        GoalItemResponse goalResponse = new GoalItemResponse();
        goalResponse.setId(1L);
        goalResponse.setName("Run 100km");
        goalResponse.setGoalType(GoalType.DISTANCE);
        goalResponse.setSportType(SportType.RUN);
        goalResponse.setCurrent(50.0);
        goalResponse.setRemaining(50.0);
        goalResponse.setPercentage(50.0);
        goalResponse.setTarget(100.0);
        goalResponse.setPeriod(Period.MONTHLY);
        goalResponse.setUnit("km");
        List<GoalItemResponse> goals = List.of(goalResponse);

        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(jwtTokenService.extractUsername("Bearer " + token)).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(user);
        when(jwtTokenService.isTokenValid(anyString(), any())).thenReturn(true);

        StravaTokenResponse stravaTokenResponse = new StravaTokenResponse(
                "valid_access_token",
                "bearer",
                "valid_refresh_token",
                3600,
                System.currentTimeMillis() + 3600 * 1000,
                null
        );

        when(stravaAuthService.getValidAccessToken(username)).thenReturn(Optional.of(stravaTokenResponse));
        when(stravaAuthService.getValidRefreshToken(username)).thenReturn(Optional.empty());

        when(goalService.getGoalItemResponses(any(User.class), anyString())).thenReturn(goals);
        mockMvc.perform(get("/api/goals")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Run 100km"))
                .andExpect(jsonPath("$[0].goalType").value("DISTANCE"))
                .andExpect(jsonPath("$[0].sportType").value("RUN"))
                .andExpect(jsonPath("$[0].current").value(50.0))
                .andExpect(jsonPath("$[0].remaining").value(50.0))
                .andExpect(jsonPath("$[0].percentage").value(50.0))
                .andExpect(jsonPath("$[0].target").value(100.0))
                .andExpect(jsonPath("$[0].period").value("MONTHLY"))
                .andExpect(jsonPath("$[0].unit").value("km"));
        verify(goalService).getGoalItemResponses(any(User.class), anyString());
    }

    @Test
    void deleteGoal() throws Exception {
        String username = "john";
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";
        Long goalId = 1L;
        User user = new User();
        user.setUsername(username);
        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(jwtTokenService.extractUsername("Bearer " + token)).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(user);
        when(jwtTokenService.isTokenValid(anyString(), any())).thenReturn(true);
        lenient().when(goalService.deleteGoalForUser(goalId, user)).thenReturn(true);
        mockMvc.perform(delete("/api/goals/{id}", goalId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Goal deleted successfully."));
    }

    @Test
    void deleteGoal_shouldThrowGoalNotFoundException_whenGoalNotFound() throws Exception {
        String username = "john";
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";
        Long goalId = 1L;
        User user = new User();
        user.setUsername(username);
        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(jwtTokenService.extractUsername("Bearer " + token)).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(user);
        when(jwtTokenService.isTokenValid(anyString(), any())).thenReturn(true);

        lenient().when(goalService.deleteGoalForUser(goalId, user)).thenReturn(false);
        mockMvc.perform(delete("/api/goals/{id}", goalId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Goal not found for id: " + goalId));
    }

    @Test
    void getValidAccessTokenOrThrow_shouldReturnAccessToken_whenValidTokenPresent() throws Exception {
        String username = "john";
        String validAccessToken = "valid-access-token";

        StravaTokenResponse stravaTokenResponse = new StravaTokenResponse(
                validAccessToken,
                "bearer",
                "valid-refresh-token",
                3600,
                System.currentTimeMillis() + 3600 * 1000,
                null
        );

        when(stravaAuthService.getValidAccessToken(username)).thenReturn(Optional.of(stravaTokenResponse));

        GoalController controller = new GoalController(goalService, userService, jwtTokenService, stravaAuthService);

        String token = reflectionInvokeGetValidAccessTokenOrThrow(controller, username);

        assertEquals(validAccessToken, token);
    }

    @Test
    void getValidAccessTokenOrThrow_shouldReturnAccessToken_whenUsingRefreshToken() throws Exception {
        String username = "john";
        String newAccessToken = "new-access-token";
        String refreshToken = "refresh-token";
        StravaToken stravaToken = new StravaToken();
        stravaToken.setRefreshToken(refreshToken);

        when(stravaAuthService.getValidAccessToken(username)).thenReturn(Optional.empty());
        when(stravaAuthService.getValidRefreshToken(username)).thenReturn(Optional.of(stravaToken));

        StravaTokenResponse stravaTokenResponse = new StravaTokenResponse();
        stravaTokenResponse.setAccessToken(newAccessToken);
        when(stravaAuthService.refreshAccessToken(refreshToken, username))
                .thenReturn(stravaTokenResponse);

        GoalController controller = new GoalController(goalService, userService, jwtTokenService, stravaAuthService);

        String token = reflectionInvokeGetValidAccessTokenOrThrow(controller, username);

        assertEquals(newAccessToken, token);
    }

    @Test
    void getValidAccessTokenOrThrow_shouldThrowStravaTokenUnavailableException_whenNoTokensAvailable() throws Exception {
        String username = "john";
        when(stravaAuthService.getValidAccessToken(username)).thenReturn(Optional.empty());
        when(stravaAuthService.getValidRefreshToken(username)).thenReturn(Optional.empty());

        GoalController controller = new GoalController(goalService, userService, jwtTokenService, stravaAuthService);
        Exception exception = assertThrows(InvocationTargetException.class, () -> {
            reflectionInvokeGetValidAccessTokenOrThrow(controller, username);
        });
        Throwable cause = exception.getCause();
        assertInstanceOf(StravaTokenUnavailableException.class, cause, "Expected StravaTokenUnavailableException");
        assertEquals("Strava token not available", cause.getMessage());
    }


    private String reflectionInvokeGetValidAccessTokenOrThrow(GoalController controller, String username) throws Exception {
        Method method = GoalController.class.getDeclaredMethod("getValidAccessTokenOrThrow", String.class);
        method.setAccessible(true);
        return (String) method.invoke(controller, username);
    }
}
