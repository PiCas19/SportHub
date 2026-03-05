package ch.supsi.sporthub.backend.service.domain.telegram.commands;

import ch.supsi.sporthub.backend.dto.response.MapDataResponse;
import ch.supsi.sporthub.backend.model.ChatType;
import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.service.StravaAuthService;
import ch.supsi.sporthub.backend.service.api.*;
import ch.supsi.sporthub.backend.service.domain.strava.ActivitySegment;
import ch.supsi.sporthub.backend.service.domain.strava.StravaActivity;
import ch.supsi.sporthub.backend.service.domain.strava.StravaTokenResponse;
import ch.supsi.sporthub.backend.service.domain.telegram.ChatTypeFactory;
import ch.supsi.sporthub.backend.service.domain.telegram.TelegramCommandInput;
import ch.supsi.sporthub.backend.utils.ArgumentParser;
import ch.supsi.sporthub.backend.utils.PolylineUtils;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Command for checking the location of Strava activities in a specified radius from a target location.
 * This command retrieves activities from users in a chat, checks if their activities are near a target location,
 * and then generates a map of matched activities.
 */
@Component
public class CheckLocationCommand implements ITelegramCommand {
    private final StravaAuthService stravaAuthService;
    private final IStravaService stravaService;
    private final IGeocodingService geocodingService;
    private final IMapCacheService mapCacheService;
    private final IUserChatService userChatService;
    private final ChatTypeFactory chatTypeFactory;

    private static final double DEFAULT_RADIUS_KM = 2.0;

    /**
     * Constructs a new CheckLocationCommand with the required dependencies.
     *
     * @param stravaAuthService The service responsible for Strava authentication.
     * @param stravaService The service for interacting with Strava activities.
     * @param geocodingService The service for geocoding and reverse geocoding.
     * @param mapCacheService The service for caching map data.
     * @param userChatService The service for retrieving users in the chat.
     * @param chatTypeFactory The factory for determining chat types.
     */
    public CheckLocationCommand(StravaAuthService stravaAuthService,
                                IStravaService stravaService,
                                IGeocodingService geocodingService,
                                IMapCacheService mapCacheService,
                                IUserChatService userChatService,
                                ChatTypeFactory chatTypeFactory) {
        this.stravaAuthService = stravaAuthService;
        this.stravaService = stravaService;
        this.geocodingService = geocodingService;
        this.mapCacheService = mapCacheService;
        this.userChatService = userChatService;
        this.chatTypeFactory = chatTypeFactory;
    }

    /**
     * Executes the command to check for Strava activities near a specified location and radius.
     * It retrieves users from the chat, fetches their Strava activities, and checks if they are within the target area.
     *
     * @param input The input containing the command arguments and chat ID.
     * @return A message containing the results of the location check or an error message.
     */
    @Override
    public String execute(TelegramCommandInput input) {
        String chatId = input.getChatId();

        Map<String, String> params = ArgumentParser.parseArgs(input.getArgs());

        double targetLat;
        double targetLon;

        if (params.containsKey("lat") ^ params.containsKey("lon")) {
            return "❌ Please provide both 'lat' and 'lon'.";
        } else if (params.containsKey("lat") && params.containsKey("lon")) {
            try {
                targetLat = Double.parseDouble(params.get("lat"));
                targetLon = Double.parseDouble(params.get("lon"));
            } catch (NumberFormatException e) {
                return "❌ Invalid coordinates. Please provide valid numbers for 'lat' and 'lon'.";
            }
        } else if (params.containsKey("location")) {
            double[] coords = geocodingService.geocode(params.get("location"));
            if (coords == null) {
                return "❌ Unable to geocode the location provided.";
            }
            targetLat = coords[0];
            targetLon = coords[1];
        } else {
            return "❌ <b>Usage:</b> <code>/check_location lat=&lt;latitude&gt; lon=&lt;longitude&gt; [radius=&lt;km&gt;]</code> OR <code>/check_location location='Location Name' [radius=&lt;km&gt;]</code>";
        }

        double radiusKm = DEFAULT_RADIUS_KM;
        if (params.containsKey("radius")) {
            try {
                radiusKm = Double.parseDouble(params.get("radius"));
            } catch (NumberFormatException e) {
                return "❌ Invalid radius. Please provide a valid number.";
            }
        }

        String locationDescription = geocodingService.reverseGeocode(targetLat, targetLon);
        String header = String.format("\uD83D\uDCCD Target Location: (%.5f, %.5f) - %s\n\uD83D\uDD35 Radius: %.2f km\n\n",
                targetLat, targetLon, locationDescription, radiusKm);

        StringBuilder activitiesBuilder = new StringBuilder();
        List<ActivitySegment> matchedSegments = new ArrayList<>();

        List<User> users = userChatService.getUsersInChat(chatId);
        if (users.isEmpty()) return "❌ No users found in this chat.";

        for (User user : users) {
            Optional<StravaTokenResponse> tokenResponseOpt = stravaAuthService.getOrRefreshAccessToken(user.getUsername());
            if (tokenResponseOpt.isEmpty()) continue;

            String accessToken = tokenResponseOpt.get().getAccessToken();
            List<StravaActivity> activities;
            try {
                activities = stravaService.getActivities(accessToken);
            } catch (Exception e) {
                continue;
            }

            for (StravaActivity activity : activities) {
                if (activity.getStartLatlng() == null || activity.getStartLatlng().size() < 2 ||
                        activity.getEndLatlng() == null || activity.getEndLatlng().size() < 2) continue;

                double startLat = activity.getStartLatlng().get(0);
                double startLon = activity.getStartLatlng().get(1);
                double endLat = activity.getEndLatlng().get(0);
                double endLon = activity.getEndLatlng().get(1);

                double distanceKm = geocodingService.distancePointToSegment(targetLat, targetLon, startLat, startLon, endLat, endLon);

                if (distanceKm <= radiusKm) {
                    ActivitySegment segment = new ActivitySegment(startLat, startLon, endLat, endLon, activity.getName());
                    Map<String, Object> map = activity.getMap();
                    if (map != null && map.get("summary_polyline") instanceof String polylineStr) {
                        List<List<Double>> decoded = PolylineUtils.decodePolyline(polylineStr);
                        for (List<Double> point : decoded) {
                            if (point.size() >= 2) {
                                segment.addPoint(point.get(0), point.get(1));
                            }
                        }
                    }
                    matchedSegments.add(segment);
                    activitiesBuilder.append(formatActivity(activity, user, distanceKm));
                }
            }
        }

        if (activitiesBuilder.isEmpty()) {
            return header + "❌ No activity found near this location";
        }

        MapDataResponse mapData = new MapDataResponse(targetLat, targetLon, matchedSegments);
        String token = mapCacheService.saveMapData(mapData);
        String mapUrl = "http://192.168.1.112:5173/map/" + token;
        activitiesBuilder.append(String.format("\uD83D\uDDFA <a href=\"%s\">View all activities on the interactive map</a>\n", mapUrl));

        return header + activitiesBuilder;
    }

    /**
     * Formats a Strava activity into a human-readable string with details like athlete, activity type,
     * distance, calories, and more.
     *
     * @param activity The Strava activity to format.
     * @param user The user who performed the activity.
     * @param distanceKm The distance between the target location and the activity's route.
     * @return A formatted string representing the activity details.
     */
    private String formatActivity(StravaActivity activity, User user, double distanceKm) {
        return String.format("""
                        🏃‍♂️ Athlete: %s
                        📍 Activity Start: (%.5f, %.5f)
                        🗺 Route Distance from target: %.2f km
                        🏋 Activity: %s
                        💪 Sport: %s
                        📏 Distance: %.2f km
                        🔥 Calories: %.0f
                        
                        """,
                user.getUsername(),
                activity.getStartLatlng().get(0),
                activity.getStartLatlng().get(1),
                distanceKm,
                activity.getName(),
                activity.getSportType(),
                activity.getDistanceMeters() / 1000.0,
                activity.getCalories()
        );
    }

    /**
     * Returns the command name for this command, which is used for identification in Telegram.
     *
     * @return The command name, "/check_location".
     */
    @Override
    public String getCommandName() {
        return "/check_location";
    }

    /**
     * Determines if the command is allowed in the specified chat.
     * This command can only be used in group chats.
     *
     * @param chatId The ID of the chat.
     * @return True if the command is allowed in the chat, false otherwise.
     */
    @Override
    public boolean isAllowedInChat(String chatId) {
        return chatTypeFactory.isChatType(chatId, ChatType.GROUP);
    }
}