package ch.supsi.sporthub.backend.service.domain.telegram.commands;

import ch.supsi.sporthub.backend.dto.response.LeaderboardEntryResponse;
import ch.supsi.sporthub.backend.model.*;
import ch.supsi.sporthub.backend.service.api.ICompetitionReadService;
import ch.supsi.sporthub.backend.service.domain.telegram.TelegramCommandInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CompetitionHistoryCommandTest {

    @Mock
    private ICompetitionReadService competitionReadService;

    @InjectMocks
    private CompetitionHistoryCommand competitionHistoryCommand;

    private TelegramCommandInput input;
    private final String chatId = "test-chat-id";
    private final String senderName = "test-user";
    private final String messageId = "12345";
    private final String[] args = new String[0];

    @BeforeEach
    void setUp() {
        input = new TelegramCommandInput(chatId, senderName, messageId, args);
    }

    @Test
    void getCommandName_shouldReturnCorrectCommand() {
        assertEquals("/competition_history", competitionHistoryCommand.getCommandName());
    }

    @Test
    void isAllowedInChat_shouldAlwaysReturnTrue() {
        assertTrue(competitionHistoryCommand.isAllowedInChat("any-chat-id"));
    }

    @Test
    void execute_whenNoCompetitionsFound_shouldReturnInfoMessage() {
        when(competitionReadService.findCompetitionsByChatId(chatId)).thenReturn(Collections.emptyList());

        String result = competitionHistoryCommand.execute(input);

        assertEquals("ℹ️ <b>No competitions found</b> for this group.", result);
        verify(competitionReadService).findCompetitionsByChatId(chatId);
    }

    @Test
    void execute_withCompetitionWithScores_shouldIncludeLeaderboard() {
        Competition competition = createTestCompetition(false);
        when(competitionReadService.findCompetitionsByChatId(chatId)).thenReturn(List.of(competition));

        List<LeaderboardEntryResponse> leaderboardEntries = Arrays.asList(
                new LeaderboardEntryResponse(100.0, "user1", "telegramUser1"),
                new LeaderboardEntryResponse(50.0, "user2", "telegramUser2")
        );
        when(competitionReadService.getLeaderboardEntriesForCompetition(competition))
                .thenReturn(leaderboardEntries);

        String result = competitionHistoryCommand.execute(input);

        assertTrue(result.contains("📜 <b>Competition history for this group:</b>"));
        assertTrue(result.contains("🏁 <b>Test Competition</b>"));
        assertTrue(result.contains("• Status: ⏳ In Progress"));
        assertTrue(result.contains("📊 <b>Leaderboard:</b>"));
    }

    @Test
    void execute_withFinishedCompetitionNoScores_shouldIncludeNoActivityMessage() {
        Competition competition = createTestCompetition(true);
        when(competitionReadService.findCompetitionsByChatId(chatId)).thenReturn(List.of(competition));
        when(competitionReadService.getLeaderboardEntriesForCompetition(competition))
                .thenReturn(Collections.emptyList());

        String result = competitionHistoryCommand.execute(input);

        assertTrue(result.contains("🏁 <b>Test Competition</b>"));
        assertTrue(result.contains("• Status: ✅ Finished"));
        assertTrue(result.contains("• No activity completed during this competition."));
        assertTrue(result.contains("❗ <i>No points awarded.</i>"));
    }

    @Test
    void execute_withOngoingCompetitionNoScores_shouldIncludeNoActivityMessage() {
        Competition competition = createTestCompetition(false);
        when(competitionReadService.findCompetitionsByChatId(chatId)).thenReturn(List.of(competition));
        when(competitionReadService.getLeaderboardEntriesForCompetition(competition))
                .thenReturn(Collections.emptyList());

        String result = competitionHistoryCommand.execute(input);

        assertTrue(result.contains("• No activity from participants"));
    }

    @Test
    void execute_withMultipleCompetitions_shouldFormatAllCorrectly() {
        Competition ongoingComp = createTestCompetition(false);
        Competition finishedComp = createTestCompetition(true);

        when(competitionReadService.findCompetitionsByChatId(chatId))
                .thenReturn(List.of(ongoingComp, finishedComp));

        List<LeaderboardEntryResponse> ongoingEntries = List.of(
                new LeaderboardEntryResponse(100.0, "user1", "telegramUser1")
        );
        when(competitionReadService.getLeaderboardEntriesForCompetition(ongoingComp))
                .thenReturn(ongoingEntries);

        when(competitionReadService.getLeaderboardEntriesForCompetition(finishedComp))
                .thenReturn(Collections.emptyList());

        String result = competitionHistoryCommand.execute(input);
        assertTrue(result.contains("🏁 <b>Test Competition</b>"));
        assertTrue(result.contains("• Status: ⏳ In Progress"));
        assertTrue(result.contains("• Status: ✅ Finished"));
        assertTrue(result.contains("• No activity completed during this competition."));
    }

    @Test
    void execute_withArgs_shouldIgnoreArgs() {
        String[] testArgs = {"arg1", "arg2"};
        input = new TelegramCommandInput(chatId, senderName, messageId, testArgs);

        Competition competition = createTestCompetition(false);
        when(competitionReadService.findCompetitionsByChatId(chatId)).thenReturn(List.of(competition));
        when(competitionReadService.getLeaderboardEntriesForCompetition(competition))
                .thenReturn(List.of(new LeaderboardEntryResponse(100.0, "user1", "telegramUser1")));

        String result = competitionHistoryCommand.execute(input);
        assertTrue(result.contains("🏁 <b>Test Competition</b>"));
    }

    @Test
    void escapeHtml_withNullInput_shouldReturnEmptyString() throws Exception {
        Method escapeHtmlMethod = CompetitionHistoryCommand.class
                .getDeclaredMethod("escapeHtml", String.class);
        escapeHtmlMethod.setAccessible(true);
        String result = (String) escapeHtmlMethod.invoke(competitionHistoryCommand, (String) null);
        assertEquals("", result, "Should return empty string for null input");
    }

    private Competition createTestCompetition(boolean isFinished) {
        Competition competition = new Competition();
        competition.setId(1L);
        competition.setName("Test Competition");
        competition.setMaxParticipants(10);
        competition.setDeadline(LocalDate.now().plusDays(7));
        competition.setFinished(isFinished);
        competition.setChatId(chatId);
        return competition;
    }
}