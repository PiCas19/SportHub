package ch.supsi.sporthub.backend.controller;

import ch.supsi.sporthub.backend.dto.request.CreateCompetitionRequest;
import ch.supsi.sporthub.backend.dto.response.CompetitionSummaryResponse;
import ch.supsi.sporthub.backend.dto.response.LeaderboardEntryResponse;
import ch.supsi.sporthub.backend.model.Competition;
import ch.supsi.sporthub.backend.model.GoalType;
import ch.supsi.sporthub.backend.model.SportType;
import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.security.JwtUtil;
import ch.supsi.sporthub.backend.service.JwtTokenService;
import ch.supsi.sporthub.backend.service.UserService;
import ch.supsi.sporthub.backend.service.api.ICompetitionService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompetitionController.class)
public class CompetitionControllerTest {

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
        public ICompetitionService competitionService() {
            return mock(ICompetitionService.class);
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
    private ICompetitionService competitionService;

    @Test
    void createCompetition_success() throws Exception {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";
        String username = "john";
        User user = new User();
        user.setUsername(username);
        user.setId(1L);

        CreateCompetitionRequest request = new CreateCompetitionRequest();
        request.setName("Marathon Challenge");
        request.setTargetValue(42.0);
        request.setGoalType("DISTANCE");
        request.setSportType("RUN");
        request.setMaxParticipants(50);
        request.setDeadline(LocalDate.now().plusDays(7));
        request.setEndDate(LocalDate.now().plusDays(30));
        request.setChatId("chat123456");
        CompetitionSummaryResponse competitionSummaryResponse = new CompetitionSummaryResponse();
        competitionSummaryResponse.setCompetitionId(1L);
        competitionSummaryResponse.setName("Marathon Challenge");
        competitionSummaryResponse.setGoalType(GoalType.DISTANCE);
        competitionSummaryResponse.setSportType(SportType.RUN);
        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(user);
        when(competitionService.createCompetition(eq(request), eq(user))).thenReturn(competitionSummaryResponse);
        mockMvc.perform(post("/api/competitions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Marathon Challenge"))
                .andExpect(jsonPath("$.goalType").value("DISTANCE"))
                .andExpect(jsonPath("$.sportType").value("RUN"));

        verify(competitionService).createCompetition(eq(request), eq(user));
    }


    @Test
    void getUserCompetitions_success() throws Exception {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";
        String username = "john";
        User user = new User();
        user.setUsername(username);
        user.setId(1L);

        CompetitionSummaryResponse comp1 = new CompetitionSummaryResponse();
        comp1.setCompetitionId(1L);
        comp1.setName("Running Challenge");
        comp1.setMaxParticipants(50);
        comp1.setCurrentParticipants(10);
        comp1.setUserRegistered(true);
        comp1.setGoalType(GoalType.DISTANCE);
        comp1.setSportType(SportType.RUN);
        comp1.setFinished(false);
        comp1.setStarted(true);
        comp1.setChatId("chat123456");
        comp1.setDeadline(LocalDate.now().plusDays(7));
        comp1.setEndDate(LocalDate.now().plusDays(30));

        CompetitionSummaryResponse comp2 = new CompetitionSummaryResponse();
        comp2.setCompetitionId(2L);
        comp2.setName("Swimming Challenge");
        comp2.setMaxParticipants(30);
        comp2.setCurrentParticipants(5);
        comp2.setUserRegistered(false);
        comp2.setGoalType(GoalType.DURATION);
        comp2.setSportType(SportType.SWIM);
        comp2.setFinished(false);
        comp2.setStarted(false);
        comp2.setChatId("chat654321");
        comp2.setDeadline(LocalDate.now().plusDays(14));
        comp2.setEndDate(LocalDate.now().plusDays(60));

        List<CompetitionSummaryResponse> competitions = Arrays.asList(comp1, comp2);

        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(user);
        when(competitionService.getCompetitionsForUser(user)).thenReturn(competitions);
        mockMvc.perform(get("/api/competitions")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].competitionId").value(1L))
                .andExpect(jsonPath("$[0].name").value("Running Challenge"))
                .andExpect(jsonPath("$[0].maxParticipants").value(50))
                .andExpect(jsonPath("$[0].currentParticipants").value(10))
                .andExpect(jsonPath("$[0].userRegistered").value(true))
                .andExpect(jsonPath("$[0].goalType").value("DISTANCE"))
                .andExpect(jsonPath("$[0].sportType").value("RUN"))
                .andExpect(jsonPath("$[0].finished").value(false))
                .andExpect(jsonPath("$[0].started").value(true))
                .andExpect(jsonPath("$[0].chatId").value("chat123456"))
                .andExpect(jsonPath("$[1].competitionId").value(2L))
                .andExpect(jsonPath("$[1].name").value("Swimming Challenge"));

        verify(competitionService).getCompetitionsForUser(eq(user));
    }

    @Test
    void joinCompetition_success() throws Exception {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";
        String username = "john";
        Long competitionId = 1L;
        User user = new User();
        user.setUsername(username);
        user.setId(1L);

        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(user);
        doNothing().when(competitionService).joinCompetition(eq(competitionId), eq(user));
        mockMvc.perform(post("/api/competitions/{id}/join", competitionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("Joined competition successfully."));

        verify(competitionService).joinCompetition(eq(competitionId), eq(user));
    }

    @Test
    void leaveCompetition_success() throws Exception {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";
        String username = "john";
        Long competitionId = 1L;
        User user = new User();
        user.setUsername(username);
        user.setId(1L);

        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(user);
        doNothing().when(competitionService).leaveCompetition(eq(competitionId), eq(user));
        mockMvc.perform(post("/api/competitions/{id}/leave", competitionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("Left competition successfully."));

        verify(competitionService).leaveCompetition(eq(competitionId), eq(user));
    }

    @Test
    void getLeaderboard_success() throws Exception {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";
        String username = "john";
        Long competitionId = 1L;
        User user = new User();
        user.setUsername(username);
        user.setId(1L);

        Competition competition = new Competition();
        competition.setId(competitionId);
        competition.setName("Running Challenge");

        LeaderboardEntryResponse entry1 = new LeaderboardEntryResponse(150.0, "john", "john_telegram");
        LeaderboardEntryResponse entry2 = new LeaderboardEntryResponse(120.0, "jane", "jane_telegram");

        List<LeaderboardEntryResponse> leaderboard = Arrays.asList(entry1, entry2);

        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(user);
        when(competitionService.getCompetitionById(competitionId)).thenReturn(competition);
        when(competitionService.getLeaderboardEntriesForCompetition(competition)).thenReturn(leaderboard);
        mockMvc.perform(get("/api/competitions/{id}/leaderboard", competitionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].score").value(150.0))
                .andExpect(jsonPath("$[0].sporthubUsername").value("john"))
                .andExpect(jsonPath("$[0].telegramUsername").value("john_telegram"))
                .andExpect(jsonPath("$[1].score").value(120.0))
                .andExpect(jsonPath("$[1].sporthubUsername").value("jane"))
                .andExpect(jsonPath("$[1].telegramUsername").value("jane_telegram"));

        verify(competitionService).getCompetitionById(eq(competitionId));
        verify(competitionService).getLeaderboardEntriesForCompetition(eq(competition));
    }
}