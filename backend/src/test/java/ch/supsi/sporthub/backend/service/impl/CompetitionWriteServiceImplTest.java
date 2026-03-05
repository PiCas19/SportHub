package ch.supsi.sporthub.backend.service.impl;

import ch.supsi.sporthub.backend.dto.request.CreateCompetitionRequest;
import ch.supsi.sporthub.backend.dto.response.CompetitionCreationResult;
import ch.supsi.sporthub.backend.model.*;
import ch.supsi.sporthub.backend.repository.jpa.*;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CompetitionWriteServiceImplTest {

    @Mock
    private CompetitionRepository competitionRepository;
    @Mock
    private CompetitionGoalRepository competitionGoalRepository;
    @Mock
    private CompetitionParticipantRepository competitionParticipantRepository;
    @Mock
    private CompetitionLeaderboardEntryRepository competitionLeaderboardEntryRepository;
    @Mock
    private ChatRepository chatRepository;

    @InjectMocks
    private CompetitionWriteServiceImpl competitionWriteService;

    private User testUser;
    private Competition testCompetition;
    private Chat testChat;
    private CreateCompetitionRequest validRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");

        testChat = new Chat();
        testChat.setId(1L);
        testChat.setChatId("group-123");
        testChat.setUser(testUser);

        testCompetition = new Competition();
        testCompetition.setId(1L);
        testCompetition.setName("Test Competition");
        testCompetition.setMaxParticipants(5);
        testCompetition.setDeadline(LocalDate.now().plusDays(10));
        testCompetition.setEndDate(LocalDate.now().plusDays(20));
        testCompetition.setChatId("group-123");
        testCompetition.setStarted(false);
        testCompetition.setFinished(false);

        validRequest = new CreateCompetitionRequest();
        validRequest.setName("New Competition");
        validRequest.setMaxParticipants(10);
        validRequest.setDeadline(LocalDate.now().plusDays(5));
        validRequest.setEndDate(LocalDate.now().plusDays(15));
        validRequest.setChatId("group-123");
        validRequest.setGoalType("DISTANCE");
        validRequest.setSportType("RUN");
        validRequest.setTargetValue(100.0);
    }

    @Test
    void createCompetition_validRequest_returnsResult() {
        Page<Chat> chatPage = new PageImpl<>(List.of(testChat));
        when(chatRepository.findByChatId(eq("group-123"), any(PageRequest.class))).thenReturn(chatPage);
        when(competitionRepository.save(any(Competition.class))).thenReturn(testCompetition);

        CompetitionCreationResult result = competitionWriteService.createCompetition(validRequest, testUser);

        assertNotNull(result);
        assertEquals(testCompetition, result.getCompetition());
        verify(competitionGoalRepository).save(any(CompetitionGoal.class));
    }

    @Test
    void createCompetition_invalidChatId_throwsException() {
        validRequest.setChatId("invalidchatid");
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                competitionWriteService.createCompetition(validRequest, testUser));
        assertEquals("Chat ID must contain '-' character (private group).", ex.getReason());
    }

    @Test
    void createCompetition_chatNotFound_throwsException() {
        Page<Chat> emptyPage = new PageImpl<>(List.of());
        when(chatRepository.findByChatId(anyString(), any(PageRequest.class))).thenReturn(emptyPage);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                competitionWriteService.createCompetition(validRequest, testUser));
        assertEquals("Chat not found", ex.getReason());
    }

    @Test
    void joinCompetition_validRequest_succeeds() {
        when(competitionRepository.findById(1L)).thenReturn(Optional.of(testCompetition));
        when(chatRepository.findAllByChatId("group-123")).thenReturn(List.of(testChat));
        when(competitionParticipantRepository.existsByCompetitionAndUser(testCompetition, testUser)).thenReturn(false);
        when(competitionParticipantRepository.findByCompetition(testCompetition)).thenReturn(List.of());

        competitionWriteService.joinCompetition(1L, testUser);

        verify(competitionParticipantRepository).save(any(CompetitionParticipant.class));
        verify(competitionLeaderboardEntryRepository).save(any(CompetitionLeaderboardEntry.class));
    }

    @Test
    void joinCompetition_userNotInChat_throwsException() {
        when(competitionRepository.findById(1L)).thenReturn(Optional.of(testCompetition));
        when(chatRepository.findAllByChatId("group-123")).thenReturn(List.of());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                competitionWriteService.joinCompetition(1L, testUser));
        assertEquals("User is not registered in the chat", ex.getReason());
    }

    @Test
    void joinCompetition_alreadyJoined_throwsException() {
        when(competitionRepository.findById(1L)).thenReturn(Optional.of(testCompetition));
        when(chatRepository.findAllByChatId("group-123")).thenReturn(List.of(testChat));
        when(competitionParticipantRepository.existsByCompetitionAndUser(testCompetition, testUser)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                competitionWriteService.joinCompetition(1L, testUser));
        assertEquals("User already joined this competition", ex.getReason());
    }

    @Test
    void joinCompetition_competitionFull_throwsException() {
        when(competitionRepository.findById(1L)).thenReturn(Optional.of(testCompetition));
        when(chatRepository.findAllByChatId("group-123")).thenReturn(List.of(testChat));
        when(competitionParticipantRepository.existsByCompetitionAndUser(testCompetition, testUser)).thenReturn(false);
        when(competitionParticipantRepository.findByCompetition(testCompetition)).thenReturn(List.of(new CompetitionParticipant(), new CompetitionParticipant(), new CompetitionParticipant(), new CompetitionParticipant(), new CompetitionParticipant()));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                competitionWriteService.joinCompetition(1L, testUser));
        assertEquals("Competition is full", ex.getReason());
    }

    @Test
    void leaveCompetition_validRequest_removesUser() {
        when(competitionRepository.findById(1L)).thenReturn(Optional.of(testCompetition));
        when(competitionParticipantRepository.existsByCompetitionAndUser(testCompetition, testUser)).thenReturn(true);

        competitionWriteService.leaveCompetition(1L, testUser);

        verify(competitionParticipantRepository).deleteByCompetitionAndUser(testCompetition, testUser);
    }

    @Test
    void leaveCompetition_startedCompetition_throwsException() {
        testCompetition.setStarted(true);
        when(competitionRepository.findById(1L)).thenReturn(Optional.of(testCompetition));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                competitionWriteService.leaveCompetition(1L, testUser));
        assertEquals("Cannot leave a started competition", ex.getReason());
    }

    @Test
    void leaveCompetition_userNotParticipant_throwsException() {
        when(competitionRepository.findById(1L)).thenReturn(Optional.of(testCompetition));
        when(competitionParticipantRepository.existsByCompetitionAndUser(testCompetition, testUser)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                competitionWriteService.leaveCompetition(1L, testUser));
        assertEquals("User is not a participant in this competition", ex.getReason());
    }

    @Test
    void saveCompetitionGoal_shouldSaveGoal() {
        CompetitionGoal goal = new CompetitionGoal();
        competitionWriteService.saveCompetitionGoal(goal);
        verify(competitionGoalRepository).save(goal);
    }

    @Test
    void saveCompetition_shouldSaveCompetition() {
        Competition competition = new Competition();
        competitionWriteService.saveCompetition(competition);
        verify(competitionRepository).save(competition);
    }

    @Test
    void findCompetitionByNameAndChatId_shouldReturnCompetition() {
        Competition competition = new Competition();
        when(competitionRepository.findByNameAndChatId("TestComp", "group-123")).thenReturn(Optional.of(competition));

        Optional<Competition> result = competitionWriteService.findCompetitionByNameAndChatId("TestComp", "group-123");

        assertTrue(result.isPresent());
        assertEquals(competition, result.get());
    }

    @Test
    void updateLeaderboardEntryScore_shouldUpdateScoreIfEntryFound() {
        Competition competition = new Competition();
        competition.setId(1L);
        User user = new User();
        user.setUsername("testuser");
        CompetitionLeaderboardEntry entry = new CompetitionLeaderboardEntry();
        entry.setUser(user);

        when(competitionLeaderboardEntryRepository.findByCompetition(competition)).thenReturn(List.of(entry));

        competitionWriteService.updateLeaderboardEntryScore(competition, "testuser", 42.0);

        assertEquals(42.0, entry.getScore());
        verify(competitionLeaderboardEntryRepository).save(entry);
    }

    @Test
    void updateLeaderboardEntryScore_shouldDoNothingIfNoMatchingEntry() {
        Competition competition = new Competition();
        competition.setId(1L);
        User user = new User();
        user.setUsername("anotheruser");
        CompetitionLeaderboardEntry entry = new CompetitionLeaderboardEntry();
        entry.setUser(user);

        when(competitionLeaderboardEntryRepository.findByCompetition(competition)).thenReturn(List.of(entry));

        competitionWriteService.updateLeaderboardEntryScore(competition, "testuser", 42.0);

        verify(competitionLeaderboardEntryRepository, never()).save(any());
    }

    @Test
    void deleteCompetition_shouldDeleteAllRelatedEntities() {
        Competition competition = new Competition();
        competition.setId(1L);

        CompetitionParticipant participant = new CompetitionParticipant();
        CompetitionLeaderboardEntry leaderboardEntry = new CompetitionLeaderboardEntry();
        CompetitionGoal competitionGoal = new CompetitionGoal();

        when(competitionRepository.findById(1L)).thenReturn(Optional.of(competition));
        when(competitionParticipantRepository.findByCompetition(competition)).thenReturn(List.of(participant));
        when(competitionLeaderboardEntryRepository.findByCompetition(competition)).thenReturn(List.of(leaderboardEntry));
        when(competitionGoalRepository.findByCompetition(competition)).thenReturn(Optional.of(competitionGoal));

        competitionWriteService.deleteCompetition(1L);

        verify(competitionParticipantRepository).deleteAll(List.of(participant));
        verify(competitionLeaderboardEntryRepository).deleteAll(List.of(leaderboardEntry));
        verify(competitionGoalRepository).delete(competitionGoal);
        verify(competitionRepository).delete(competition);
    }

    @Test
    void deleteCompetition_notFound_shouldThrowException() {
        when(competitionRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            competitionWriteService.deleteCompetition(99L);
        });

        assertEquals("Competition not found", exception.getReason());
    }


    @Test
    void joinCompetition_deadlinePassed_shouldThrowException() {
        Competition competition = new Competition();
        competition.setId(1L);
        competition.setDeadline(LocalDate.now().minusDays(1));
        competition.setEndDate(LocalDate.now().plusDays(5));
        competition.setMaxParticipants(10);
        competition.setFinished(false);
        competition.setChatId("group-123");

        when(competitionRepository.findById(1L)).thenReturn(Optional.of(competition));
        when(chatRepository.findAllByChatId(anyString())).thenReturn(List.of(new Chat() {{
            setUser(testUser);
        }}));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            competitionWriteService.joinCompetition(1L, testUser);
        });

        assertEquals("Competition deadline passed", exception.getReason());
    }

    @Test
    void joinCompetition_competitionFinished_shouldThrowException() {
        Competition competition = new Competition();
        competition.setId(1L);
        competition.setDeadline(LocalDate.now().plusDays(5));
        competition.setEndDate(LocalDate.now().plusDays(10));
        competition.setMaxParticipants(10);
        competition.setFinished(true);
        competition.setChatId("group-123");

        when(competitionRepository.findById(1L)).thenReturn(Optional.of(competition));
        when(chatRepository.findAllByChatId(anyString())).thenReturn(List.of(new Chat() {{
            setUser(testUser);
        }}));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            competitionWriteService.joinCompetition(1L, testUser);
        });

        assertEquals("Competition has finished", exception.getReason());
    }


    @Test
    void joinCompetition_shouldNotStartCompetition_whenDeadlineNotPassed() {
        Competition competition = new Competition();
        competition.setId(1L);
        competition.setDeadline(LocalDate.now().plusDays(2));
        competition.setEndDate(LocalDate.now().plusDays(5));
        competition.setMaxParticipants(2);
        competition.setFinished(false);
        competition.setChatId("group-123");

        Chat chat = new Chat();
        chat.setUser(testUser);

        when(competitionRepository.findById(1L)).thenReturn(Optional.of(competition));
        when(chatRepository.findAllByChatId(anyString())).thenReturn(List.of(chat));
        when(competitionParticipantRepository.existsByCompetitionAndUser(competition, testUser)).thenReturn(false);
        when(competitionParticipantRepository.findByCompetition(competition))
                .thenReturn(List.of(new CompetitionParticipant()));

        competitionWriteService.joinCompetition(1L, testUser);

        assertFalse(competition.isStarted());
        verify(competitionRepository, never()).save(competition);
    }

    @Test
    void createCompetition_zeroTarget_shouldThrowException() {
        validRequest.setTargetValue(0);
        validRequest.setMaxParticipants(10);

        Page<Chat> chatPage = new PageImpl<>(List.of(testChat));
        when(chatRepository.findByChatId(eq("group-123"), any(PageRequest.class))).thenReturn(chatPage);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                competitionWriteService.createCompetition(validRequest, testUser));
        assertEquals("Target and maxParticipants must be greater than 0", ex.getReason());
    }

    @Test
    void createCompetition_zeroMaxParticipants_shouldThrowException() {
        validRequest.setTargetValue(100);
        validRequest.setMaxParticipants(0);

        Page<Chat> chatPage = new PageImpl<>(List.of(testChat));
        when(chatRepository.findByChatId(eq("group-123"), any(PageRequest.class))).thenReturn(chatPage);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                competitionWriteService.createCompetition(validRequest, testUser));
        assertEquals("Target and maxParticipants must be greater than 0", ex.getReason());
    }

    @Test
    void createCompetition_deadlineInPast_shouldThrowException() {
        validRequest.setDeadline(LocalDate.now().minusDays(1));

        Page<Chat> chatPage = new PageImpl<>(List.of(testChat));
        when(chatRepository.findByChatId(eq("group-123"), any(PageRequest.class))).thenReturn(chatPage);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                competitionWriteService.createCompetition(validRequest, testUser));
        assertEquals("Deadline must be in the future", ex.getReason());
    }

    @Test
    void createCompetition_endDateBeforeDeadline_shouldThrowException() {
        validRequest.setDeadline(LocalDate.now().plusDays(5));
        validRequest.setEndDate(LocalDate.now().plusDays(2));

        Page<Chat> chatPage = new PageImpl<>(List.of(testChat));
        when(chatRepository.findByChatId(eq("group-123"), any(PageRequest.class))).thenReturn(chatPage);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                competitionWriteService.createCompetition(validRequest, testUser));
        assertEquals("End Date must be after the deadline", ex.getReason());
    }

    @Test
    @Transactional
    void existsCompetitionByNameAndChatId_ShouldReturnTrueWhenExists() {
        String name = "Test Competition";
        String chatId = "group-123";
        when(competitionRepository.existsByNameAndChatId(name, chatId)).thenReturn(true);
        boolean result = competitionWriteService.existsCompetitionByNameAndChatId(name, chatId);
        assertTrue(result);
        verify(competitionRepository).existsByNameAndChatId(name, chatId);
    }

    @Test
    @Transactional
    void existsCompetitionByNameAndChatId_ShouldReturnFalseWhenNotExists() {
        String name = "Non-existent Competition";
        String chatId = "group-456";
        when(competitionRepository.existsByNameAndChatId(name, chatId)).thenReturn(false);
        boolean result = competitionWriteService.existsCompetitionByNameAndChatId(name, chatId);
        assertFalse(result);
        verify(competitionRepository).existsByNameAndChatId(name, chatId);
    }

    @Test
    @Transactional
    void existsCompetitionByNameAndChatId_ShouldHandleNullParameters() {
        when(competitionRepository.existsByNameAndChatId(null, null)).thenReturn(false);
        boolean result = competitionWriteService.existsCompetitionByNameAndChatId(null, null);
        assertFalse(result);
        verify(competitionRepository).existsByNameAndChatId(null, null);
    }
}
