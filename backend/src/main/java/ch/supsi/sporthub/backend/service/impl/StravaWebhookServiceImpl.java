package ch.supsi.sporthub.backend.service.impl;

import ch.supsi.sporthub.backend.config.StravaProperties;
import ch.supsi.sporthub.backend.dto.response.strava.StravaWebhookResponse;
import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.service.StravaAuthService;
import ch.supsi.sporthub.backend.service.api.INotificationService;
import ch.supsi.sporthub.backend.service.api.IStravaWebhookService;
import ch.supsi.sporthub.backend.service.domain.strava.StravaActivity;
import ch.supsi.sporthub.backend.service.domain.strava.StravaTokenResponse;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Implementation of the IStravaWebhookService interface that handles Strava webhook events.
 * This service processes incoming webhook events from Strava, such as activity creation,
 * and sends notifications to users about their activities.
 */
@Service
public class StravaWebhookServiceImpl implements IStravaWebhookService {
    private final StravaAuthService stravaAuthService;
    private final INotificationService notificationService;
    private final StravaProperties stravaConfig;

    /**
     * Constructor for the StravaWebhookServiceImpl class.
     * Initializes the service with the provided Strava authentication service, notification service, and Strava configuration.
     *
     * @param stravaAuthService  The Strava authentication service to handle token validation and activity fetching.
     * @param notificationService The service to send notifications to users.
     * @param stravaConfig        The Strava configuration for webhook token validation.
     */
    public StravaWebhookServiceImpl(StravaAuthService stravaAuthService, INotificationService notificationService, StravaProperties stravaConfig) {
        this.stravaAuthService = stravaAuthService;
        this.notificationService = notificationService;
        this.stravaConfig = stravaConfig;
    }


    /**
     * Handles the creation of a new activity based on a webhook event from Strava.
     * It retrieves the user's data, validates the access token, fetches the activity details,
     * and sends a notification if the event is within the allowed time window.
     *
     * @param activityId The ID of the activity that was created.
     * @param athleteId  The ID of the athlete who performed the activity.
     * @param eventTime  The time when the event was triggered in Unix epoch seconds.
     * @return A StravaWebhookResponse containing the result of the operation and any relevant messages.
     */
    @Override
    public StravaWebhookResponse handleActivityCreation(Long activityId, Long athleteId, Long eventTime) {
        Optional<User> userOpt = stravaAuthService.findUserByAthleteId(athleteId);
        if (userOpt.isEmpty()) {
            return new StravaWebhookResponse("User not found for athlete ID: " + athleteId, false, null);
        }

        User user = userOpt.get();
        Optional<StravaTokenResponse> tokenOpt = stravaAuthService.getValidAccessToken(user.getUsername());

        if (tokenOpt.isEmpty()) {
            Optional<StravaTokenResponse> refreshedTokenOpt = stravaAuthService.refreshAccessTokenIfNeeded(user.getUsername());
            if (refreshedTokenOpt.isPresent()) {
                tokenOpt = refreshedTokenOpt;
            } else {
                return new StravaWebhookResponse("Access token invalid and could not be refreshed for user: " + user.getUsername(), false, null);
            }
        }

        String accessToken = tokenOpt.get().getAccessToken();

        try {
            StravaActivity activity = stravaAuthService.getActivityById(activityId.toString(), accessToken);

            Instant eventInstant = Instant.ofEpochSecond(eventTime);
            Instant now = Instant.now();

            boolean notified = false;
            if (Duration.between(eventInstant, now).abs().toMinutes() <= 2) {
                notificationService.notifyActivityCompleted(user, activity);
                notified = true;
            }

            return new StravaWebhookResponse(
                    notified ? "Notification sent successfully." : "Activity not notified due to timing constraints.",
                    notified,
                    activity.getName()
            );
        } catch (RuntimeException e) {
            return new StravaWebhookResponse("Failed to fetch activity: " + e.getMessage(), false, null);
        }
    }

    /**
     * Verifies the token sent during the Strava webhook verification process.
     * This is used to validate that the webhook request is coming from Strava.
     *
     * @param token The token sent by Strava during the webhook verification.
     * @return true if the token is valid, false otherwise.
     */
    @Override
    public boolean isVerifyTokenValid(String token) {
        return stravaConfig.getWebhookVerifyToken().equals(token);
    }
}