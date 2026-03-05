package ch.supsi.sporthub.backend.controller;

import ch.supsi.sporthub.backend.dto.request.strava.StravaWebhookEvent;
import ch.supsi.sporthub.backend.dto.response.strava.StravaWebhookResponse;
import ch.supsi.sporthub.backend.exception.InvalidVerifyTokenException;
import ch.supsi.sporthub.backend.exception.UnsupportedEventTypeException;
import ch.supsi.sporthub.backend.service.api.IStravaWebhookService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * StravaWebhookController handles incoming webhook requests from Strava.
 * It processes events related to Strava activities and validates the webhook subscription.
 */
@RestController
@RequestMapping("/api/strava/webhook")
public class StravaWebhookController {

    private final IStravaWebhookService stravaWebhookService;

    /**
     * Constructs an instance of StravaWebhookController with the specified StravaWebhookService.
     *
     * @param stravaWebhookService The service responsible for handling Strava webhook events.
     */
    public StravaWebhookController(IStravaWebhookService stravaWebhookService) {
        this.stravaWebhookService = stravaWebhookService;
    }

    /**
     * Handles incoming webhook events from Strava.
     * It processes events related to the creation of Strava activities and invokes the corresponding service method.
     * If the event type is unsupported, it throws an UnsupportedEventTypeException.
     *
     * @param event The Strava webhook event containing details about the activity.
     * @return A response entity containing the result of the event processing.
     * @throws UnsupportedEventTypeException If the event type is not "create" for an "activity".
     */
    @PostMapping
    public ResponseEntity<StravaWebhookResponse> handleWebhook(@RequestBody StravaWebhookEvent event) {
        if (!"create".equals(event.getAspectType()) || !"activity".equals(event.getObjectType())) {
            throw new UnsupportedEventTypeException("Unsupported event type: " + event.getAspectType());
        }

        StravaWebhookResponse response = stravaWebhookService.handleActivityCreation(
                event.getObjectId(), event.getOwnerId(), event.getEventTime());

        HttpStatus status = response.isNotified() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Verifies the webhook subscription by checking the challenge token sent by Strava.
     * This method is used for the initial verification of the webhook subscription.
     *
     * @param mode        The mode of the webhook subscription (should be "subscribe").
     * @param challenge   The challenge token provided by Strava.
     * @param verifyToken The verification token to validate the request.
     * @return A response entity containing the challenge token if the verification is successful.
     * @throws InvalidVerifyTokenException If the verification token or mode is invalid.
     */
    @GetMapping
    public ResponseEntity<Map<String, String>> verifyWebhook(@RequestParam("hub.mode") String mode,
                                                             @RequestParam("hub.challenge") String challenge,
                                                             @RequestParam("hub.verify_token") String verifyToken) {
        if (!"subscribe".equals(mode) || !stravaWebhookService.isVerifyTokenValid(verifyToken)) {
            throw new InvalidVerifyTokenException("Invalid verify token or mode");
        }

        return ResponseEntity.ok(Map.of("hub.challenge", challenge));
    }
}