package ch.supsi.sporthub.backend.service.domain.telegram.commands;

import ch.supsi.sporthub.backend.model.ChatType;
import ch.supsi.sporthub.backend.model.StravaToken;
import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.service.StravaAuthService;
import ch.supsi.sporthub.backend.service.api.*;
import ch.supsi.sporthub.backend.service.domain.strava.StravaActivity;
import ch.supsi.sporthub.backend.service.domain.strava.StravaTokenResponse;
import ch.supsi.sporthub.backend.service.domain.telegram.TelegramCommandInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShowActivitiesCommandTest {

    @Mock
    private StravaAuthService stravaAuthService;

    @Mock
    private IStravaService stravaService;

    @Mock
    private IUserChatService userChatService;

    @Mock
    private IChatTypeFactory chatTypeFactory;

    @InjectMocks
    private ShowActivitiesCommand showActivitiesCommand;

    private TelegramCommandInput input;
    private final String chatId = "test-chat-id";
    private final String senderName = "test-user";
    private final String messageId = "12345";

    @BeforeEach
    void setUp() {
        input = new TelegramCommandInput(chatId, senderName, messageId, new String[0]);
    }

    @Test
    void getCommandName_shouldReturnCorrectCommand() {
        assertEquals("/show_activities", showActivitiesCommand.getCommandName());
    }

    @Test
    void isAllowedInChat_whenPrivateChat_shouldReturnTrue() {
        when(chatTypeFactory.isChatType(chatId, ChatType.PRIVATE)).thenReturn(true);
        assertTrue(showActivitiesCommand.isAllowedInChat(chatId));
    }

    @Test
    void isAllowedInChat_whenNotPrivateChat_shouldReturnFalse() {
        when(chatTypeFactory.isChatType(chatId, ChatType.PRIVATE)).thenReturn(false);
        assertFalse(showActivitiesCommand.isAllowedInChat(chatId));
    }

    @Test
    void execute_whenUserNotRegistered_shouldReturnError() {
        when(userChatService.getUserFromChat(chatId)).thenReturn(null);

        String result = showActivitiesCommand.execute(input);

        assertEquals("❌ No registered user found for this chat. Please register using /start_notification.", result);
    }

    @Test
    void execute_whenInvalidCountParameter_shouldReturnError() {
        User user = new User();
        when(userChatService.getUserFromChat(chatId)).thenReturn(user);
        input = new TelegramCommandInput(chatId, senderName, messageId, new String[]{"count=invalid"});

        String result = showActivitiesCommand.execute(input);

        assertEquals("❌ Error: Invalid number format for count.", result);
    }

    @Test
    void execute_whenNegativeCount_shouldReturnError() {
        User user = new User();
        when(userChatService.getUserFromChat(chatId)).thenReturn(user);
        input = new TelegramCommandInput(chatId, senderName, messageId, new String[]{"count=-1"});

        String result = showActivitiesCommand.execute(input);

        assertEquals("❌ Error: count must be a positive integer.", result);
    }

    @Test
    void execute_whenUnknownParameter_shouldReturnError() {
        User user = new User();
        when(userChatService.getUserFromChat(chatId)).thenReturn(user);
        input = new TelegramCommandInput(chatId, senderName, messageId, new String[]{"unknown=param"});

        String result = showActivitiesCommand.execute(input);

        assertTrue(result.contains("❌ Error: Unknown parameter"));
        assertTrue(result.contains("Allowed keys:"));
    }

    @Test
    void execute_whenNoValidToken_shouldReturnError() {
        User user = new User();
        user.setUsername("testuser");
        when(userChatService.getUserFromChat(chatId)).thenReturn(user);
        when(stravaAuthService.getValidAccessToken("testuser")).thenReturn(Optional.empty());
        when(stravaAuthService.getValidRefreshToken("testuser")).thenReturn(Optional.empty());

        String result = showActivitiesCommand.execute(input);

        assertEquals("❌ Could not obtain a valid Strava token.", result);
    }

    @Test
    void execute_whenTokenRefreshNeeded_shouldRefreshToken() {
        User user = new User();
        user.setUsername("testuser");
        StravaToken refreshToken = new StravaToken();
        refreshToken.setRefreshToken("mock-refresh-token");
        StravaTokenResponse newToken = new StravaTokenResponse();

        when(userChatService.getUserFromChat(chatId)).thenReturn(user);
        when(stravaAuthService.getValidAccessToken("testuser"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(newToken));

        when(stravaAuthService.getValidRefreshToken("testuser")).thenReturn(Optional.of(refreshToken));
        when(stravaAuthService.refreshAccessToken(refreshToken.getRefreshToken(), "testuser")).thenReturn(newToken);
        when(stravaService.getActivities(newToken.getAccessToken())).thenReturn(Collections.emptyList());

        String result = showActivitiesCommand.execute(input);

        assertEquals("ℹ️ No activities found for testuser on Strava.", result);
        verify(stravaAuthService).refreshAccessToken(refreshToken.getRefreshToken(), "testuser");
    }

    @Test
    void execute_whenNoActivities_shouldReturnMessage() {
        User user = new User();
        user.setUsername("testuser");
        StravaTokenResponse token = new StravaTokenResponse();

        when(userChatService.getUserFromChat(chatId)).thenReturn(user);
        when(stravaAuthService.getValidAccessToken("testuser")).thenReturn(Optional.of(token));
        when(stravaService.getActivities(token.getAccessToken())).thenReturn(Collections.emptyList());

        String result = showActivitiesCommand.execute(input);

        assertEquals("ℹ️ No activities found for testuser on Strava.", result);
    }

    @Test
    void execute_whenActivitiesFilteredOut_shouldReturnMessage() {
        User user = new User();
        user.setUsername("testuser");
        StravaTokenResponse token = new StravaTokenResponse();
        StravaActivity activity = createTestActivity(5.0, "Run", 1800);

        when(userChatService.getUserFromChat(chatId)).thenReturn(user);
        when(stravaAuthService.getValidAccessToken("testuser")).thenReturn(Optional.of(token));
        when(stravaService.getActivities(token.getAccessToken())).thenReturn(List.of(activity));
        input = new TelegramCommandInput(chatId, senderName, messageId, new String[]{"min=10"});

        String result = showActivitiesCommand.execute(input);

        assertEquals("ℹ️ No activities found matching your filters.", result);
    }

    @Test
    void execute_whenValidActivities_shouldReturnFormattedList() {
        User user = new User();
        user.setUsername("testuser");
        StravaTokenResponse token = new StravaTokenResponse();
        StravaActivity activity1 = createTestActivity(5.0, "Run", 1800);
        StravaActivity activity2 = createTestActivity(10.5, "Ride", 3600);

        when(userChatService.getUserFromChat(chatId)).thenReturn(user);
        when(stravaAuthService.getValidAccessToken("testuser")).thenReturn(Optional.of(token));
        when(stravaService.getActivities(token.getAccessToken())).thenReturn(List.of(activity1, activity2));
        input = new TelegramCommandInput(chatId, senderName, messageId, new String[]{"count=1", "format=Custom {name} {distance}"});

        String result = showActivitiesCommand.execute(input);

        assertTrue(result.contains("📊 <b>Activities for testuser:</b>"));
        assertTrue(result.contains("Custom Morning Run 5.00"));
        assertFalse(result.contains("10.50"));
    }

    @Test
    void execute_shouldFilterActivitiesCorrectly() {
        User user = new User();
        user.setUsername("testuser");
        StravaTokenResponse token = new StravaTokenResponse();

        StravaActivity shortRun = createTestActivity(5.0, "Run", 1800);
        StravaActivity longRun = createTestActivity(15.0, "Run", 3600);
        StravaActivity ride = createTestActivity(40.0, "Ride", 7200);

        when(userChatService.getUserFromChat(chatId)).thenReturn(user);
        when(stravaAuthService.getValidAccessToken("testuser")).thenReturn(Optional.of(token));
        when(stravaService.getActivities(token.getAccessToken())).thenReturn(List.of(shortRun, longRun, ride));

        input = new TelegramCommandInput(chatId, senderName, messageId, new String[]{"min=10"});
        String result = showActivitiesCommand.execute(input);
        assertTrue(result.contains("15.00"));
        assertTrue(result.contains("40.00"));

        input = new TelegramCommandInput(chatId, senderName, messageId, new String[]{"max=20"});
        result = showActivitiesCommand.execute(input);
        assertTrue(result.contains("5.00"));
        assertTrue(result.contains("15.00"));
        assertFalse(result.contains("40.00"));

        input = new TelegramCommandInput(chatId, senderName, messageId, new String[]{"type=ride"});
        result = showActivitiesCommand.execute(input);
        assertFalse(result.contains("Run"));
        assertTrue(result.contains("Ride"));

        input = new TelegramCommandInput(chatId, senderName, messageId, new String[]{"duration=45"});
        result = showActivitiesCommand.execute(input);
        assertFalse(result.contains("30"));
        assertTrue(result.contains("40"));

        input = new TelegramCommandInput(chatId, senderName, messageId, new String[]{"min=invalid"});
        result = showActivitiesCommand.execute(input);
        assertEquals("ℹ️ No activities found matching your filters.", result);
    }

    private StravaActivity createTestActivity(double distanceKm, String type, int durationSeconds) {
        StravaActivity activity = new StravaActivity();
        activity.setName(type.equals("Run") ? "Morning Run" : "Evening Ride");
        activity.setSportType(type);
        activity.setDistanceMeters(distanceKm * 1000);
        activity.setElapsedTimeSeconds(durationSeconds);
        activity.setStartDateLocal("2023-01-01T10:00:00Z");
        return activity;
    }
}