package ch.supsi.sporthub.backend.service.impl;

import ch.supsi.sporthub.backend.dto.response.CompetitionSummaryResponse;
import ch.supsi.sporthub.backend.dto.response.LeaderboardEntryResponse;
import ch.supsi.sporthub.backend.model.*;
import ch.supsi.sporthub.backend.repository.jpa.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CompetitionReadServiceImplTest {

    @Mock private CompetitionRepository competitionRepository;
    @Mock private CompetitionGoalRepository competitionGoalRepository;
    @Mock private CompetitionParticipantRepository competitionParticipantRepository;
    @Mock private CompetitionLeaderboardEntryRepository leaderboardEntryRepository;
    @Mock private ChatRepository chatRepository;

    @InjectMocks private CompetitionReadServiceImpl competitionReadService;

    private User testUser;
    private Competition testCompetition;
    private CompetitionGoal testGoal;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");

        testCompetition = new Competition();
        testCompetition.setId(1L);
        testCompetition.setName("Test Competition");
        testCompetition.setChatId("group-123");
        testCompetition.setMaxParticipants(10);
        testCompetition.setDeadline(LocalDate.now().plusDays(5));
        testCompetition.setEndDate(LocalDate.now().plusDays(10));

        testGoal = new CompetitionGoal();
        testGoal.setId(1L);
        testGoal.setCompetition(testCompetition);
        testGoal.setGoalType(GoalType.DISTANCE);
        testGoal.setSportType(SportType.RUN);
        testGoal.setTargetValue(100.0);
    }

    @Test
    void getCompetitionById_Exists_ReturnsCompetition() {
        when(competitionRepository.findById(1L)).thenReturn(Optional.of(testCompetition));

        Competition result = competitionReadService.getCompetitionById(1L);

        assertEquals(testCompetition, result);
    }

    @Test
    void getCompetitionById_NotExists_ThrowsException() {
        when(competitionRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> competitionReadService.getCompetitionById(1L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Competition not found", ex.getReason());
    }

    @Test
    void getParticipantsForCompetition_ReturnsParticipants() {
        CompetitionParticipant participant = new CompetitionParticipant();
        participant.setUser(testUser);

        when(competitionParticipantRepository.findByCompetition(testCompetition))
                .thenReturn(List.of(participant));

        List<CompetitionParticipant> result =
                competitionReadService.getParticipantsForCompetition(testCompetition);

        assertEquals(1, result.size());
        assertEquals(testUser, result.getFirst().getUser());
    }

    @Test
    void getLeaderboardEntriesForCompetition_ReturnsSortedEntries() {
        CompetitionLeaderboardEntry entry1 = new CompetitionLeaderboardEntry();
        entry1.setScore(50.0);
        entry1.setUser(testUser);

        User user2 = new User();
        user2.setUsername("user2");
        CompetitionLeaderboardEntry entry2 = new CompetitionLeaderboardEntry();
        entry2.setScore(100.0);
        entry2.setUser(user2);

        Chat chat = new Chat();
        chat.setTelegramUserName("testTelegram");
        chat.setUser(testUser);

        when(leaderboardEntryRepository.findByCompetition(testCompetition))
                .thenReturn(List.of(entry1, entry2));
        when(chatRepository.findAllByChatId(testCompetition.getChatId()))
                .thenReturn(List.of(chat));

        List<LeaderboardEntryResponse> result =
                competitionReadService.getLeaderboardEntriesForCompetition(testCompetition);

        assertEquals(2, result.size());
        assertEquals(100.0, result.get(0).getScore());
        assertEquals("user2", result.get(0).getSporthubUsername());
        assertEquals("testUser", result.get(1).getSporthubUsername());
        assertEquals("testTelegram", result.get(1).getTelegramUsername());
    }

    @Test
    void getCompetitionsForUser_WithChats_ReturnsCompetitions() {
        Chat chat = new Chat();
        chat.setChatId("group-123");
        chat.setUser(testUser);

        when(chatRepository.findByUser(testUser)).thenReturn(List.of(chat));
        when(competitionRepository.findByChatIdIn(List.of("group-123")))
                .thenReturn(List.of(testCompetition));
        when(competitionGoalRepository.findByCompetition(testCompetition))
                .thenReturn(Optional.of(testGoal));
        when(competitionParticipantRepository.existsByCompetitionAndUser(testCompetition, testUser))
                .thenReturn(true);
        when(competitionParticipantRepository.findByCompetition(testCompetition))
                .thenReturn(List.of(new CompetitionParticipant()));

        List<CompetitionSummaryResponse> result =
                competitionReadService.getCompetitionsForUser(testUser);

        assertEquals(1, result.size());
        CompetitionSummaryResponse dto = result.getFirst();
        assertEquals(testCompetition.getId(), dto.getCompetitionId());
        assertEquals(testCompetition.getName(), dto.getName());
        assertEquals(1, dto.getCurrentParticipants());
        assertTrue(dto.isUserRegistered());
        assertEquals(GoalType.DISTANCE, dto.getGoalType());
    }

    @Test
    void getCompetitionsForUser_NoChats_ReturnsEmptyList() {
        when(chatRepository.findByUser(testUser)).thenReturn(List.of());

        List<CompetitionSummaryResponse> result =
                competitionReadService.getCompetitionsForUser(testUser);

        assertTrue(result.isEmpty());
    }

    @Test
    void findCompetitionsByChatId_ReturnsFilteredCompetitions() {
        Competition comp1 = new Competition();
        comp1.setChatId("group-123");
        Competition comp2 = new Competition();
        comp2.setChatId("group-456");

        when(competitionRepository.findAll()).thenReturn(List.of(comp1, comp2));

        List<Competition> result =
                competitionReadService.findCompetitionsByChatId("group-123");

        assertEquals(1, result.size());
        assertEquals("group-123", result.getFirst().getChatId());
    }

    @Test
    void getLeaderboardEntriesForCompetition_NoTelegramUsername_ReturnsNull() {
        CompetitionLeaderboardEntry entry = new CompetitionLeaderboardEntry();
        entry.setScore(50.0);
        entry.setUser(testUser);

        when(leaderboardEntryRepository.findByCompetition(testCompetition))
                .thenReturn(List.of(entry));
        when(chatRepository.findAllByChatId(testCompetition.getChatId()))
                .thenReturn(List.of());

        List<LeaderboardEntryResponse> result =
                competitionReadService.getLeaderboardEntriesForCompetition(testCompetition);

        assertEquals(1, result.size());
        assertNull(result.getFirst().getTelegramUsername());
    }

    @Test
    void convertToDto_WithGoal_ReturnsCompleteDto() throws Exception {
        when(competitionGoalRepository.findByCompetition(testCompetition))
                .thenReturn(Optional.of(testGoal));
        when(competitionParticipantRepository.existsByCompetitionAndUser(testCompetition, testUser))
                .thenReturn(true);
        when(competitionParticipantRepository.findByCompetition(testCompetition))
                .thenReturn(List.of(new CompetitionParticipant(), new CompetitionParticipant()));

        Method method = CompetitionReadServiceImpl.class.getDeclaredMethod(
                "convertToDto", Competition.class, User.class);
        method.setAccessible(true);

        CompetitionSummaryResponse dto = (CompetitionSummaryResponse)
                method.invoke(competitionReadService, testCompetition, testUser);

        assertNotNull(dto);
        assertEquals(testCompetition.getId(), dto.getCompetitionId());
        assertEquals(testCompetition.getName(), dto.getName());
        assertEquals(testCompetition.getMaxParticipants(), dto.getMaxParticipants());
        assertEquals(2, dto.getCurrentParticipants());
        assertTrue(dto.isUserRegistered());
        assertEquals(GoalType.DISTANCE, dto.getGoalType());
        assertEquals(SportType.RUN, dto.getSportType());
        assertEquals(testCompetition.isStarted(), dto.isStarted());
        assertEquals(testCompetition.isFinished(), dto.isFinished());
        assertEquals(testCompetition.getChatId(), dto.getChatId());
        assertEquals(testCompetition.getDeadline(), dto.getDeadline());
        assertEquals(testCompetition.getEndDate(), dto.getEndDate());
        assertEquals("You are registered to this competition.", dto.getMessage());
    }

    @Test
    void convertToDto_WithoutGoal_ReturnsPartialDto() throws Exception {
        when(competitionGoalRepository.findByCompetition(testCompetition))
                .thenReturn(Optional.empty());
        when(competitionParticipantRepository.existsByCompetitionAndUser(testCompetition, testUser))
                .thenReturn(false);
        when(competitionParticipantRepository.findByCompetition(testCompetition))
                .thenReturn(List.of());

        Method method = CompetitionReadServiceImpl.class.getDeclaredMethod(
                "convertToDto", Competition.class, User.class);
        method.setAccessible(true);

        CompetitionSummaryResponse dto = (CompetitionSummaryResponse)
                method.invoke(competitionReadService, testCompetition, testUser);
        assertNotNull(dto);
        assertNull(dto.getGoalType());
        assertNull(dto.getSportType());
        assertFalse(dto.isUserRegistered());
        assertEquals(0, dto.getCurrentParticipants());
        assertEquals("You are not registered to this competition.", dto.getMessage());
    }

    @Test
    void getGoalForCompetition_WhenGoalExists_ReturnsGoal() {
        Competition competition = new Competition();
        competition.setId(1L);

        CompetitionGoal expectedGoal = new CompetitionGoal();
        expectedGoal.setId(1L);
        expectedGoal.setCompetition(competition);

        when(competitionGoalRepository.findByCompetition(competition))
                .thenReturn(Optional.of(expectedGoal));

        Optional<CompetitionGoal> result = competitionReadService.getGoalForCompetition(competition);

        assertTrue(result.isPresent());
        assertEquals(expectedGoal, result.get());
        verify(competitionGoalRepository).findByCompetition(competition);
    }

    @Test
    void getGoalForCompetition_WhenNoGoalExists_ReturnsEmpty() {
        Competition competition = new Competition();

        when(competitionGoalRepository.findByCompetition(competition))
                .thenReturn(Optional.empty());

        Optional<CompetitionGoal> result = competitionReadService.getGoalForCompetition(competition);

        assertFalse(result.isPresent());
        verify(competitionGoalRepository).findByCompetition(competition);
    }


    @Test
    void getAllCompetitions_WhenCompetitionsExist_ReturnsList() {
        Competition competition1 = new Competition();
        competition1.setId(1L);
        Competition competition2 = new Competition();
        competition2.setId(2L);

        when(competitionRepository.findAll())
                .thenReturn(Arrays.asList(competition1, competition2));

        List<Competition> result = competitionReadService.getAllCompetitions();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
        verify(competitionRepository).findAll();
    }

    @Test
    void getAllCompetitions_WhenNoCompetitionsExist_ReturnsEmptyList() {
        when(competitionRepository.findAll())
                .thenReturn(Collections.emptyList());
        List<Competition> result = competitionReadService.getAllCompetitions();
        assertTrue(result.isEmpty());
        verify(competitionRepository).findAll();
    }
}