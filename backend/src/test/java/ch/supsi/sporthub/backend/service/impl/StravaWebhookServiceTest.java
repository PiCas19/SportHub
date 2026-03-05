package ch.supsi.sporthub.backend.service.impl;

import ch.supsi.sporthub.backend.config.StravaProperties;
import ch.supsi.sporthub.backend.dto.response.strava.StravaWebhookResponse;
import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.service.StravaAuthService;
import ch.supsi.sporthub.backend.service.api.INotificationService;
import ch.supsi.sporthub.backend.service.domain.strava.StravaActivity;
import ch.supsi.sporthub.backend.service.domain.strava.StravaTokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StravaWebhookServiceTest {

    private StravaAuthService stravaAuthService;
    private INotificationService notificationService;
    private StravaProperties stravaProperties;
    private StravaWebhookServiceImpl webhookService;
    private User testUser;

    @BeforeEach
    void setUp() {
        stravaAuthService = mock(StravaAuthService.class);
        notificationService = mock(INotificationService.class);
        stravaProperties = mock(StravaProperties.class);
        when(stravaProperties.getWebhookVerifyToken()).thenReturn("verify-token");
        webhookService = new StravaWebhookServiceImpl(stravaAuthService, notificationService, stravaProperties);
        testUser = new User();
        testUser.setUsername("testUser");
    }

    @Test
    void testHandleActivityCreation_UserNotFound() {
        when(stravaAuthService.findUserByAthleteId(123L)).thenReturn(Optional.empty());
        StravaWebhookResponse response = webhookService.handleActivityCreation(1L, 123L, Instant.now().getEpochSecond());
        assertFalse(response.isNotified());
        assertTrue(response.getMessage().contains("User not found"));
        assertNull(response.getActivityName());
    }

    @Test
    void testHandleActivityCreation_AccessTokenNotFound_NotRefreshed() {
        when(stravaAuthService.findUserByAthleteId(456L)).thenReturn(Optional.of(testUser));
        when(stravaAuthService.getValidAccessToken("testUser")).thenReturn(Optional.empty());
        when(stravaAuthService.refreshAccessTokenIfNeeded("testUser")).thenReturn(Optional.empty());
        StravaWebhookResponse response = webhookService.handleActivityCreation(2L, 456L, Instant.now().getEpochSecond());
        assertFalse(response.isNotified());
        assertTrue(response.getMessage().contains("Access token invalid and could not be refreshed"));
        assertNull(response.getActivityName());
    }

    @Test
    void testHandleActivityCreation_NotificationSent() {
        when(stravaAuthService.findUserByAthleteId(789L)).thenReturn(Optional.of(testUser));
        StravaTokenResponse tokenResponse = new StravaTokenResponse("access-token", null, null, 3600, 0, null);
        when(stravaAuthService.getValidAccessToken("testUser")).thenReturn(Optional.of(tokenResponse));
        StravaActivity activity = new StravaActivity();
        activity.setName("Morning Run");
        when(stravaAuthService.getActivityById("1", "access-token")).thenReturn(activity);
        long eventTime = Instant.now().getEpochSecond();
        StravaWebhookResponse response = webhookService.handleActivityCreation(1L, 789L, eventTime);
        verify(notificationService, times(1)).notifyActivityCompleted(testUser, activity);
        assertTrue(response.isNotified());
        assertEquals("Notification sent successfully.", response.getMessage());
        assertEquals("Morning Run", response.getActivityName());
    }

    @Test
    void testHandleActivityCreation_NoNotificationDueToTime() {
        when(stravaAuthService.findUserByAthleteId(111L)).thenReturn(Optional.of(testUser));
        StravaTokenResponse tokenResponse = new StravaTokenResponse("access-token", null, null, 3600, 0, null);
        when(stravaAuthService.getValidAccessToken("testUser")).thenReturn(Optional.of(tokenResponse));
        StravaActivity activity = new StravaActivity();
        activity.setName("Evening Ride");
        when(stravaAuthService.getActivityById("1", "access-token")).thenReturn(activity);
        long eventTime = Instant.now().minus(Duration.ofMinutes(3)).getEpochSecond();
        StravaWebhookResponse response = webhookService.handleActivityCreation(1L, 111L, eventTime);
        verify(notificationService, never()).notifyActivityCompleted(any(), any());
        assertFalse(response.isNotified());
        assertEquals("Activity not notified due to timing constraints.", response.getMessage());
        assertEquals("Evening Ride", response.getActivityName());
    }

    @Test
    void testHandleActivityCreation_FetchActivityException() {
        when(stravaAuthService.findUserByAthleteId(222L)).thenReturn(Optional.of(testUser));
        StravaTokenResponse tokenResponse = new StravaTokenResponse("access-token", null, null, 3600, 0, null);
        when(stravaAuthService.getValidAccessToken("testUser")).thenReturn(Optional.of(tokenResponse));
        when(stravaAuthService.getActivityById("1", "access-token")).thenThrow(new RuntimeException("API error"));
        long eventTime = Instant.now().getEpochSecond();
        StravaWebhookResponse response = webhookService.handleActivityCreation(1L, 222L, eventTime);
        assertFalse(response.isNotified());
        assertTrue(response.getMessage().contains("Failed to fetch activity: API error"));
        assertNull(response.getActivityName());
    }

    @Test
    void testIsVerifyTokenValid() {
        assertTrue(webhookService.isVerifyTokenValid("verify-token"));
        assertFalse(webhookService.isVerifyTokenValid("wrong-token"));
    }

    @Test
    void testHandleActivityCreation_TokenRefreshedSuccessfully() {
        when(stravaAuthService.findUserByAthleteId(333L)).thenReturn(Optional.of(testUser));

        when(stravaAuthService.getValidAccessToken("testUser")).thenReturn(Optional.empty());

        StravaTokenResponse refreshedToken = new StravaTokenResponse("refreshed-token", null, null, 3600, 0, null);
        when(stravaAuthService.refreshAccessTokenIfNeeded("testUser")).thenReturn(Optional.of(refreshedToken));

        StravaActivity activity = new StravaActivity();
        activity.setName("Lunch Ride");

        when(stravaAuthService.getActivityById("10", "refreshed-token")).thenReturn(activity);

        long eventTime = Instant.now().getEpochSecond();

        StravaWebhookResponse response = webhookService.handleActivityCreation(10L, 333L, eventTime);

        verify(notificationService, times(1)).notifyActivityCompleted(testUser, activity);
        assertTrue(response.isNotified());
        assertEquals("Notification sent successfully.", response.getMessage());
        assertEquals("Lunch Ride", response.getActivityName());
    }

}
