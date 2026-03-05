package ch.supsi.sporthub.backend.controller;

import ch.supsi.sporthub.backend.dto.request.strava.ActivityFilterRequest;
import ch.supsi.sporthub.backend.dto.request.strava.AddActivityRequest;
import ch.supsi.sporthub.backend.dto.request.strava.AuthorizationStravaCodeRequest;
import ch.supsi.sporthub.backend.exception.StravaTokenUnavailableException;
import ch.supsi.sporthub.backend.model.StravaToken;
import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.security.JwtUtil;
import ch.supsi.sporthub.backend.service.JwtTokenService;
import ch.supsi.sporthub.backend.service.StravaAuthService;
import ch.supsi.sporthub.backend.service.UserService;
import ch.supsi.sporthub.backend.service.api.INotificationService;
import ch.supsi.sporthub.backend.service.api.IStravaService;
import ch.supsi.sporthub.backend.service.domain.strava.Athlete;
import ch.supsi.sporthub.backend.service.domain.strava.StravaActivity;
import ch.supsi.sporthub.backend.service.domain.strava.StravaTokenResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StravaController.class)
public class StravaControllerTest {

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
        public StravaAuthService stravaAuthService() {
            return mock(StravaAuthService.class);
        }

        @Bean
        public IStravaService stravaService() {
            return mock(IStravaService.class);
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
        public INotificationService notificationService() {
            return mock(INotificationService.class);
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
    private StravaAuthService stravaAuthService;
    @Autowired
    private IStravaService stravaService;
    @Autowired
    private UserService userService;
    @Autowired
    private INotificationService notificationService;


    @InjectMocks
    private StravaController stravaController;

    @Test
    void login_userAlreadyAuthenticated() throws Exception {
        String username = "john";
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";

        User user = new User();
        user.setUsername(username);

        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(stravaAuthService.refreshAccessTokenIfNeeded(username)).thenReturn(Optional.empty());
        when(stravaAuthService.getValidAccessToken(username)).thenReturn(Optional.of(new StravaTokenResponse("valid_access_token", "bearer", "valid_refresh_token", 3600, System.currentTimeMillis() + 3600 * 1000, null)));

        mockMvc.perform(get("/api/strava/login")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User already authenticated with Strava.".trim()));
    }


    @Test
    void login_stravaAuthorizationRequired() throws Exception {
        String username = "john";
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";

        User user = new User();
        user.setUsername(username);

        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(stravaAuthService.refreshAccessTokenIfNeeded(username)).thenReturn(Optional.empty());
        when(stravaAuthService.getValidAccessToken(username)).thenReturn(Optional.empty());
        when(stravaAuthService.getAuthorizationUrl()).thenReturn("http://strava.com/authorize");

        mockMvc.perform(get("/api/strava/login")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Strava Authorization Required"))
                .andExpect(jsonPath("$.authorizationUrl").value("http://strava.com/authorize"));
    }

    @Test
    void getAccessToken_success() throws Exception {
        String username = "john";
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";

        AuthorizationStravaCodeRequest request = new AuthorizationStravaCodeRequest();
        request.setCode("auth_code");

        User user = new User();
        user.setUsername(username);

        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(stravaAuthService.getAccessToken(username, "auth_code")).thenReturn(new StravaTokenResponse("access_token", "bearer", "refresh_token", 3600, System.currentTimeMillis() + 3600 * 1000, null));

        mockMvc.perform(post("/api/strava/token")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Authentication successful!"));
    }

    @Test
    void refreshAccessToken_invalidRefreshToken() throws Exception {
        String token = "invalid-refresh-token";
        String username = "john";

        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(stravaAuthService.getValidRefreshToken(username)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/strava/refresh-token")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Strava refresh token is missing or expired."));
    }

    @Test
    void getActivities_success() throws Exception {
        String token = "valid-token";
        String username = "john";
        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(stravaAuthService.getValidAccessToken(username)).thenReturn(Optional.of(new StravaTokenResponse("valid_access_token", "bearer", "refresh_token", 3600, System.currentTimeMillis() + 3600 * 1000, null)));

        StravaActivity activity = new StravaActivity();
        activity.setId(1L);
        activity.setName("Run 10km");

        when(stravaService.getActivities("valid_access_token")).thenReturn(List.of(activity));

        mockMvc.perform(get("/api/strava/activities")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Run 10km"));
    }

    @Test
    void addActivity_success() throws Exception {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";
        String username = "john";
        AddActivityRequest request = new AddActivityRequest();
        request.setName("Run 10km");

        User user = new User();
        user.setUsername(username);
        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(user);
        when(stravaAuthService.getValidAccessToken(username)).thenReturn(Optional.of(new StravaTokenResponse("valid_access_token", "bearer", "refresh_token", 3600, System.currentTimeMillis() + 3600 * 1000, null)));
        when(stravaService.saveActivity(eq(request), eq("valid_access_token"))).thenReturn(new StravaActivity());

        mockMvc.perform(post("/api/strava/activities")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void getActivityById_success() throws Exception {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";
        String activityId = "12345";
        String username = "john";

        StravaActivity activity = new StravaActivity();
        activity.setId(12345L);
        activity.setName("Run 10km");

        User user = new User();
        user.setUsername(username);
        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(user);
        when(stravaAuthService.getValidAccessToken(username)).thenReturn(Optional.of(new StravaTokenResponse("valid_access_token", "bearer", "refresh_token", 3600, System.currentTimeMillis() + 3600 * 1000, null)));
        when(stravaService.getActivityById("valid_access_token", activityId)).thenReturn(activity);

        mockMvc.perform(get("/api/strava/activities/{id}", activityId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(12345))
                .andExpect(jsonPath("$.name").value("Run 10km"));
    }

    @Test
    void getAthlete_success() throws Exception {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";
        String username = "john";

        Athlete athlete = new Athlete();
        athlete.setId(12345L);
        athlete.setUsername("john");

        User user = new User();
        user.setUsername(username);

        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(user);
        when(stravaAuthService.getValidAccessToken(username)).thenReturn(Optional.of(new StravaTokenResponse("valid_access_token", "bearer", "refresh_token", 3600, System.currentTimeMillis() + 3600 * 1000, null)));
        when(stravaService.getAthlete("valid_access_token")).thenReturn(athlete);

        mockMvc.perform(get("/api/strava/athlete")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(12345))
                .andExpect(jsonPath("$.username").value("john"));
    }

    @Test
    void validateStravaToken_validToken() throws Exception {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";
        String username = "john";

        User user = new User();
        user.setUsername(username);
        when(jwtTokenService.extractUsername("valid-token")).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(user);

        when(stravaAuthService.isUserTokenValid(username)).thenReturn(true);
        mockMvc.perform(get("/api/strava/validate-token")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Token is valid"));
    }

    @Test
    void filterActivities_success() throws Exception {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";
        String username = "john";
        ActivityFilterRequest filter = new ActivityFilterRequest();
        filter.setSportType("RUN");

        User user = new User();
        user.setUsername(username);

        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(user);
        when(stravaService.getActivities(anyString())).thenReturn(List.of(new StravaActivity()));
        when(stravaService.filterActivities(anyList(), any(ActivityFilterRequest.class))).thenReturn(List.of(new StravaActivity()));

        mockMvc.perform(post("/api/strava/activities/filter")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getWeekly_success() throws Exception {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";
        String username = "john";
        String sportType = "RUN";

        User user = new User();
        user.setUsername(username);

        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(user);
        when(stravaService.getActivities(anyString())).thenReturn(List.of(new StravaActivity()));
        when(stravaService.calculateWeeklyPerformance(any(), anyList(), anyString())).thenReturn(new HashMap<>());

        mockMvc.perform(get("/api/strava/performance/weekly")
                        .header("Authorization", "Bearer " + token)
                        .param("sportType", sportType))
                .andExpect(status().isOk());
    }


    @Test
    void getDaily_success() throws Exception {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";
        String username = "john";
        String sportType = "RUN";

        User user = new User();
        user.setUsername(username);

        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(user);
        when(stravaService.getActivities(anyString())).thenReturn(List.of(new StravaActivity()));
        when(stravaService.calculateDailyPerformance(any(), anyList(), anyString())).thenReturn(new HashMap<>());

        mockMvc.perform(get("/api/strava/performance/daily")
                        .header("Authorization", "Bearer " + token)
                        .param("sportType", sportType))
                .andExpect(status().isOk());
    }

    @Test
    void getYearly_success() throws Exception {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";
        String username = "john";
        String sportType = "RUN";

        User user = new User();
        user.setUsername(username);

        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(user);
        when(stravaService.getActivities(anyString())).thenReturn(List.of(new StravaActivity()));
        when(stravaService.calculateYearlyPerformance(any(), anyList(), anyString())).thenReturn(new HashMap<>());

        mockMvc.perform(get("/api/strava/performance/yearly")
                        .header("Authorization", "Bearer " + token)
                        .param("sportType", sportType))
                .andExpect(status().isOk());
    }


    @Test
    void getLastSixMonths_success() throws Exception {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";
        String username = "john";
        String sportType = "RUN";
        User user = new User();
        user.setUsername(username);

        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(user);
        when(stravaService.getActivities(anyString())).thenReturn(List.of(new StravaActivity()));
        when(stravaService.calculateLastSixMonthsPerformance(any(), anyList(), anyString())).thenReturn(new HashMap<>());

        mockMvc.perform(get("/api/strava/performance/last-6-months")
                        .header("Authorization", "Bearer " + token)
                        .param("sportType", sportType))
                .andExpect(status().isOk());
    }


    @Test
    void getSportDistribution_success() throws Exception {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";
        String username = "john";

        User user = new User();
        user.setUsername(username);

        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(user);
        when(stravaService.getActivities(anyString())).thenReturn(List.of(new StravaActivity()));
        when(stravaService.calculateSportTypeDistribution(anyList())).thenReturn(new HashMap<>());

        mockMvc.perform(get("/api/strava/performance/sport-distribution")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }


    @Test
    void getTotalKm_success() throws Exception {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";
        String username = "john";
        String sportType = "RUN";

        User user = new User();
        user.setUsername(username);

        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(user);
        when(stravaService.getActivities(anyString())).thenReturn(List.of(new StravaActivity()));
        when(stravaService.calculateKm(anyList(), anyString())).thenReturn(new HashMap<>());

        mockMvc.perform(get("/api/strava/total-km")
                        .header("Authorization", "Bearer " + token)
                        .param("sportType", sportType))
                .andExpect(status().isOk());
    }

    @Test
    void getSummary_success() throws Exception {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";
        String username = "john";
        String sportType = "RUN";

        User user = new User();
        user.setUsername(username);

        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(user);
        when(stravaAuthService.getValidAccessToken(username))
                .thenReturn(Optional.of(new StravaTokenResponse(
                        "access_token", "Bearer", "refresh_token", 3600, System.currentTimeMillis() + 3600 * 1000, null
                )));
        when(stravaService.getActivities(anyString())).thenReturn(List.of(new StravaActivity()));
        when(stravaService.calculatePerformanceSummary(any(), anyList(), anyString()))
                .thenReturn(new HashMap<>());
        mockMvc.perform(get("/api/strava/performance/summary")
                        .header("Authorization", "Bearer " + token)
                        .param("sportType", sportType))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }


    @Test
    void getMonthly_success() throws Exception {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";
        String username = "john";
        String sportType = "RUN";

        User user = new User();
        user.setUsername(username);

        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(user);

        when(stravaAuthService.getValidAccessToken(username))
                .thenReturn(Optional.of(new StravaTokenResponse(
                        "access_token", "Bearer", "refresh_token", 3600, System.currentTimeMillis() + 3600 * 1000, null
                )));

        when(stravaService.getActivities(anyString())).thenReturn(List.of(new StravaActivity()));

        when(stravaService.calculateMonthlyPerformance(any(), anyList(), anyString()))
                .thenReturn(new HashMap<>());
        mockMvc.perform(get("/api/strava/performance/monthly")
                        .header("Authorization", "Bearer " + token)
                        .param("sportType", sportType))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void login_tokenRefreshedSuccessfully() throws Exception {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";
        String username = "john";

        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(stravaAuthService.refreshAccessTokenIfNeeded(username)).thenReturn(Optional.of(new StravaTokenResponse(
                "new_access_token", "bearer", "refresh_token", 3600, System.currentTimeMillis() + 3600 * 1000, null
        )));
        mockMvc.perform(get("/api/strava/login")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Access token refreshed successfully."));
    }


    @Test
    void getAccessToken_missingAuthorizationCode() throws Exception {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";
        AuthorizationStravaCodeRequest request = new AuthorizationStravaCodeRequest();
        request.setCode("");
        when(jwtTokenService.extractUsername(token)).thenReturn("john");
        mockMvc.perform(post("/api/strava/token")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Missing authorization code"));
    }


    @Test
    void getAccessToken_nullAuthorizationCode() throws Exception {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";
        AuthorizationStravaCodeRequest request = new AuthorizationStravaCodeRequest();
        request.setCode(null);

        when(jwtTokenService.extractUsername(token)).thenReturn("john");
        mockMvc.perform(post("/api/strava/token")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Missing authorization code"));
    }

    @Test
    void refreshAccessToken_refreshTokenIsNull() throws Exception {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";
        String username = "john";
        when(jwtTokenService.extractUsername(token)).thenReturn(username);

        StravaToken stravaToken = mock(StravaToken.class);
        when(stravaToken.getRefreshToken()).thenReturn(null);
        when(stravaAuthService.getValidRefreshToken(username)).thenReturn(Optional.of(stravaToken));
        mockMvc.perform(post("/api/strava/refresh-token")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Strava refresh token is missing or expired."));
    }

    @Test
    void refreshAccessToken_refreshTokenValid() throws Exception {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";
        String username = "john";
        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        String validRefreshToken = "valid-refresh-token";
        StravaToken stravaToken = mock(StravaToken.class);
        when(stravaToken.getRefreshToken()).thenReturn(validRefreshToken);
        when(stravaAuthService.getValidRefreshToken(username)).thenReturn(Optional.of(stravaToken));
        StravaTokenResponse mockResponse = new StravaTokenResponse("newAccessToken", "Bearer", "newRefreshToken", 3600, System.currentTimeMillis() + 3600 * 1000, null);
        when(stravaAuthService.refreshAccessToken(validRefreshToken, username)).thenReturn(mockResponse);
        mockMvc.perform(post("/api/strava/refresh-token")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Token refreshed successfully!"));
    }

    @Test
    void validateStravaToken_invalidToken() throws Exception {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";
        String username = "john";
        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(stravaAuthService.isUserTokenValid(username)).thenReturn(false);

        mockMvc.perform(get("/api/strava/validate-token")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Token is expired"));
    }


    @Test
    void getActivities_noActivitiesFound() throws Exception {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";
        String username = "john";
        when(jwtTokenService.extractUsername(token)).thenReturn(username);

        when(stravaService.getActivities(anyString())).thenReturn(new ArrayList<>());
        mockMvc.perform(get("/api/strava/activities")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("No activities found"));
    }

    @Test
    void getValidAccessTokenOrThrow_shouldThrowStravaTokenUnavailableException_whenNoTokensAvailable() {
        String username = "john";
        String token = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";
        when(stravaAuthService.getValidAccessToken(username)).thenReturn(Optional.empty());
        when(stravaAuthService.getValidRefreshToken(username)).thenReturn(Optional.empty());

        StravaController controller = new StravaController(stravaAuthService, stravaService, jwtTokenService, userService, notificationService);
        Exception exception = assertThrows(InvocationTargetException.class, () -> {
            reflectionInvokeGetValidAccessTokenOrThrow(controller, username, token);
        });
        Throwable cause = exception.getCause();
        assertInstanceOf(StravaTokenUnavailableException.class, cause, "Expected StravaTokenUnavailableException");
        assertEquals("Strava token is missing or expired.", cause.getMessage());
    }

    private String reflectionInvokeGetValidAccessTokenOrThrow(StravaController controller, String username, String authHeader) throws Exception {
        Method method = StravaController.class.getDeclaredMethod("getValidAccessTokenOrThrow", String.class);
        method.setAccessible(true);
        return (String) method.invoke(controller, authHeader);
    }


}
