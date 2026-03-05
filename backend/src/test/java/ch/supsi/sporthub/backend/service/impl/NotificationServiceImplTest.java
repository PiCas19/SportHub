package ch.supsi.sporthub.backend.service.impl;

import ch.supsi.sporthub.backend.dto.response.LeaderboardEntryResponse;
import ch.supsi.sporthub.backend.model.*;

import static org.mockito.Mockito.verify;

import ch.supsi.sporthub.backend.service.api.ICompetitionReadService;
import ch.supsi.sporthub.backend.service.api.ITelegramService;
import ch.supsi.sporthub.backend.service.api.IUserChatService;
import ch.supsi.sporthub.backend.service.domain.strava.StravaActivity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;

import static org.mockito.Mockito.*;

public class NotificationServiceImplTest {

    private IUserChatService userChatService;
    private ITelegramService telegramService;
    private ICompetitionReadService competitionReadService;
    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        userChatService = mock(IUserChatService.class);
        telegramService = mock(ITelegramService.class);
        competitionReadService = mock(ICompetitionReadService.class);
        notificationService = new NotificationServiceImpl(competitionReadService, userChatService, telegramService);
    }


    @Test
    void testNotifyActivityCompleted_NoChats() {
        User user = new User();
        when(userChatService.getChatsForUser(user)).thenReturn(Collections.emptyList());

        notificationService.notifyActivityCompleted(user, new StravaActivity());

        verify(telegramService, never()).sendMessage(anyString(), anyString());
    }
    @Test
    void testNotifyActivityCompleted_NullFieldsHandled() {
        User user = new User();
        user.setUsername("nofields");

        Chat chat = new Chat();
        chat.setChatId("999");

        when(userChatService.getChatsForUser(user)).thenReturn(List.of(chat));

        StravaActivity activity = new StravaActivity();
        activity.setDistanceMeters(0);
        activity.setElapsedTimeSeconds(0);

        notificationService.notifyActivityCompleted(user, activity);

        verify(telegramService).sendMessage(eq("999"), contains("Unnamed Activity"));
        verify(telegramService).sendMessage(eq("999"), contains("0.00 km"));
        verify(telegramService).sendMessage(eq("999"), contains("0 min"));
        verify(telegramService).sendMessage(eq("999"), contains("No description"));
    }


    @Test
    void testNotifyGoalReached_NoChats() {
        User user = new User();
        when(userChatService.getChatsForUser(user)).thenReturn(Collections.emptyList());

        Goal goal = new Goal();
        goal.setTargetValue(100.0);
        goal.setGoalType(GoalType.DISTANCE);
        goal.setPeriod(Period.WEEKLY);

        notificationService.notifyGoalReached(user, goal, 101);

        verify(telegramService, never()).sendMessage(anyString(), anyString());
    }

    @Test
    void testNotifyGoalReached_WithChats() {
        User user = new User();
        user.setUsername("john");

        Chat chat = new Chat();
        chat.setChatId("1010");
        when(userChatService.getChatsForUser(user)).thenReturn(List.of(chat));

        Goal goal = new Goal();
        goal.setTargetValue(50.0);
        goal.setGoalType(GoalType.DISTANCE);
        goal.setPeriod(Period.MONTHLY);

        notificationService.notifyGoalReached(user, goal, 55);

        verify(telegramService).sendMessage(eq("1010"), contains("Congratulations"));
        verify(telegramService).sendMessage(eq("1010"), contains("50.00"));
        verify(telegramService).sendMessage(eq("1010"), contains("55.00"));
    }


    @Test
    void testNotifyMilestoneReached_NoChats() {
        Competition competition = new Competition();
        competition.setId(1L);
        competition.setChatId("1234");

        when(competitionReadService.getCompetitionById(1L)).thenReturn(competition);
        when(userChatService.getChatsByChatId("1234")).thenReturn(Collections.emptyList());

        notificationService.notifyMilestoneReached("user", competition, 75);

        verify(telegramService, never()).sendMessage(anyString(), anyString());
    }

    @Test
    void testNotifyMilestoneReached_Progress() {
        Competition competition = new Competition();
        competition.setId(1L);
        competition.setName("Spring Challenge");
        competition.setChatId("555");

        User user = new User();
        user.setUsername("lucy");

        Chat chat = new Chat();
        chat.setChatId("555");
        chat.setUser(user);

        when(competitionReadService.getCompetitionById(1L)).thenReturn(competition);
        when(userChatService.getChatsByChatId("555")).thenReturn(List.of(chat));

        notificationService.notifyMilestoneReached("lucy", competition, 80);

        verify(telegramService).sendMessage(eq("555"), contains("80%"));
    }



    @Test
    void testNotifyActivityCompleted_WithChats() {
        User user = new User();
        user.setUsername("testuser");
        Chat chat = new Chat();
        chat.setChatId("123");
        when(userChatService.getChatsForUser(user)).thenReturn(List.of(chat));

        StravaActivity activity = new StravaActivity();
        activity.setName("Morning Run");
        activity.setDistanceMeters(5000);
        activity.setElapsedTimeSeconds(1800);
        activity.setDescription("Nice workout");

        notificationService.notifyActivityCompleted(user, activity);

        verify(telegramService).sendMessage(eq("123"), contains("Morning Run"));
    }

    @Test
    void testNotifyMilestoneReached_GoalReached() {
        String username = "john";
        Competition competition = new Competition();
        competition.setId(1L);
        competition.setChatId("111");

        User user = new User();
        user.setUsername(username);
        Chat chat = new Chat();
        chat.setChatId("111");
        chat.setUser(user);

        when(competitionReadService.getCompetitionById(1L)).thenReturn(competition);
        when(userChatService.getChatsByChatId("111")).thenReturn(List.of(chat));

        notificationService.notifyMilestoneReached(username, competition, 100);

        verify(telegramService).sendMessage(eq("111"), contains("goal reached"));
    }


    @Test
    void testNotifyCompetitionCreated() {
        Competition competition = new Competition();
        competition.setName("Fun Run");
        competition.setEndDate(LocalDate.of(2025, 5, 1));
        competition.setDeadline(LocalDate.of(2025, 4, 1));
        competition.setMaxParticipants(10);
        competition.setChatId("tele123");

        CompetitionGoal goal = new CompetitionGoal();
        goal.setTargetValue(42.195);
        goal.setGoalType(GoalType.DISTANCE);
        goal.setSportType(SportType.RUN);

        notificationService.notifyCompetitionCreated(competition, goal);

        verify(telegramService).sendMessage(eq("tele123"), contains("New competition created"));
        verify(telegramService).sendMessage(eq("tele123"), contains("42.20"));
    }


    @Test
    void testNotifyCompetitionFinished_WithLeaderboard() {
        Competition competition = new Competition();
        competition.setId(1L);
        competition.setName("Final Sprint");
        competition.setChatId("chat123");

        LeaderboardEntryResponse entry = new LeaderboardEntryResponse(100.0, "runner", "runnerTelegram");

        when(competitionReadService.getCompetitionById(1L)).thenReturn(competition);
        when(competitionReadService.getLeaderboardEntriesForCompetition(competition))
                .thenReturn(List.of(entry));

        notificationService.notifyCompetitionFinished(competition);

        verify(telegramService).sendMessage(eq("chat123"), contains("Leaderboard"));
    }

    @Test
    void testNotifyCompetitionFinished_NoLeaderboard() {
        Competition competition = new Competition();
        competition.setId(2L);
        competition.setName("Empty Race");
        competition.setChatId("chat456");

        when(competitionReadService.getCompetitionById(2L)).thenReturn(competition);
        when(competitionReadService.getLeaderboardEntriesForCompetition(competition))
                .thenReturn(Collections.emptyList());

        notificationService.notifyCompetitionFinished(competition);

        verify(telegramService).sendMessage(eq("chat456"), contains("No activity"));
    }

    @Test
    void testNotifyCompetitionCreated_EscapeHtmlHandlesNulls() {
        Competition competition = new Competition();
        competition.setName(null);
        competition.setEndDate(LocalDate.of(2025, 5, 1));
        competition.setDeadline(LocalDate.of(2025, 4, 1));
        competition.setMaxParticipants(10);
        competition.setChatId("null_test_chat");

        CompetitionGoal goal = new CompetitionGoal();
        goal.setTargetValue(21.0);
        goal.setGoalType(GoalType.DISTANCE);
        goal.setSportType(SportType.RUN);

        notificationService.notifyCompetitionCreated(competition, goal);

        verify(telegramService).sendMessage(eq("null_test_chat"), contains("Goal: 21.00"));
    }

    @Test
    void testNotifyGoalReachedSimulation() {
        User user = new User();
        user.setUsername("john_doe");

        Goal goal = new Goal();
        goal.setGoalType(GoalType.DISTANCE);
        goal.setPeriod(Period.MONTHLY);
        goal.setTargetValue(50.0);

        double currentValue = 55.0;
        String chatId = "12345";
        notificationService.notifyGoalReachedSimulation(user, goal, currentValue, chatId);


        verify(telegramService).sendMessage(eq(chatId), argThat(message ->
                message.contains("john_doe") &&
                        message.contains("50.00") &&
                        message.contains("DISTANCE") &&
                        message.contains("MONTHLY") &&
                        message.contains("55.00")
        ));
    }
    @Test
    void testNotifyCompetitionFinished_NoValidWinner() {
        Competition competition = new Competition();
        competition.setId(2L);
        competition.setName("Race 2");
        competition.setChatId("chat456");

        LeaderboardEntryResponse entry1 = new LeaderboardEntryResponse(null, "user1", null);
        LeaderboardEntryResponse entry2 = new LeaderboardEntryResponse(null, "user2", null);

        when(competitionReadService.getCompetitionById(2L)).thenReturn(competition);
        when(competitionReadService.getLeaderboardEntriesForCompetition(competition))
                .thenReturn(List.of(entry1, entry2));

        notificationService.notifyCompetitionFinished(competition);

        verify(telegramService).sendMessage(eq("chat456"), argThat(message ->
                !message.contains("🏆")
        ));
    }


    @Test
    void testNotifyCompetitionFinished_NoTelegramUsername() {
        Competition competition = new Competition();
        competition.setId(2L);
        competition.setName("Race No Telegram");
        competition.setChatId("chat456");

        LeaderboardEntryResponse winner = new LeaderboardEntryResponse(110.0, "winnerUser", null);

        when(competitionReadService.getCompetitionById(2L)).thenReturn(competition);
        when(competitionReadService.getLeaderboardEntriesForCompetition(competition))
                .thenReturn(List.of(winner));

        notificationService.notifyCompetitionFinished(competition);

        verify(telegramService).sendMessage(eq("chat456"), argThat(message ->
                message.contains("no telegram")
        ));
    }

    @Test
    void testNotifyMilestoneReached_ChatWithoutUser() {
        Competition competition = new Competition();
        competition.setId(1L);
        competition.setName("No User Competition");

        Chat chat = new Chat();
        chat.setChatId("chat001");
        chat.setUser(null);

        when(competitionReadService.getCompetitionById(1L)).thenReturn(competition);
        when(userChatService.getChatsByChatId(competition.getChatId())).thenReturn(List.of(chat));

        notificationService.notifyMilestoneReached("username", competition, 80);
        verify(telegramService, never()).sendMessage(anyString(), anyString());
    }

    @Test
    void testNotifyMilestoneReached_CorrectUser() {
        Competition competition = new Competition();
        competition.setId(2L);
        competition.setName("Correct User Competition");

        User user = new User();
        user.setUsername("targetUser");

        Chat chat = new Chat();
        chat.setChatId("chat002");
        chat.setUser(user);

        when(competitionReadService.getCompetitionById(2L)).thenReturn(competition);
        when(userChatService.getChatsByChatId(competition.getChatId())).thenReturn(List.of(chat));

        notificationService.notifyMilestoneReached("targetUser", competition, 90);
        verify(telegramService).sendMessage(eq("chat002"), contains("90"));
    }
    @Test
    void testNotifyMilestoneReached_WrongUser() {
        Competition competition = new Competition();
        competition.setId(3L);
        competition.setName("Wrong User Competition");

        User user = new User();
        user.setUsername("anotherUser");

        Chat chat = new Chat();
        chat.setChatId("chat003");
        chat.setUser(user);

        when(competitionReadService.getCompetitionById(3L)).thenReturn(competition);
        when(userChatService.getChatsByChatId(competition.getChatId())).thenReturn(List.of(chat));

        notificationService.notifyMilestoneReached("targetUser", competition, 50);

        verify(telegramService, never()).sendMessage(anyString(), anyString());
    }

}
