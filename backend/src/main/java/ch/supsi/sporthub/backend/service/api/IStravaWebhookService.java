package ch.supsi.sporthub.backend.service.api;

import ch.supsi.sporthub.backend.dto.response.strava.StravaWebhookResponse;

/**
 * Service interface for handling incoming Strava webhook events.
 * This includes validation of verification tokens and processing
 * of activity creation events triggered by Strava.
 */
public interface IStravaWebhookService {

    /**
     * Handles a new activity creation event sent by Strava via webhook.
     *
     * @param activityId the ID of the newly created activity
     * @param athleteId the ID of the athlete who performed the activity
     * @param eventTime the timestamp of the event (in epoch seconds)
     * @return a response indicating the result of the processing
     */
    StravaWebhookResponse handleActivityCreation(Long activityId, Long athleteId, Long eventTime);

    /**
     * Verifies whether the received token matches the expected webhook verification token.
     *
     * @param token the verification token sent by Strava
     * @return true if the token is valid, false otherwise
     */
    boolean isVerifyTokenValid(String token);
}