package ch.supsi.sporthub.backend.service.domain.telegram.commands;

import ch.supsi.sporthub.backend.model.ChatType;
import ch.supsi.sporthub.backend.model.StravaToken;
import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.service.StravaAuthService;
import ch.supsi.sporthub.backend.service.api.*;
import ch.supsi.sporthub.backend.service.domain.strava.StravaActivity;
import ch.supsi.sporthub.backend.service.domain.strava.StravaTokenResponse;
import ch.supsi.sporthub.backend.service.domain.telegram.TelegramCommandInput;
import ch.supsi.sporthub.backend.utils.ArgumentParser;
import ch.supsi.sporthub.backend.utils.DateTimeUtils;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Command that retrieves and displays a user's Strava activities based on provided filters.
 * The command allows the user to filter activities by distance, duration, type, and other parameters.
 * It also supports customizing the format for displaying activity summaries.
 * This command is available only in private chats.
 */
@Component
public class ShowActivitiesCommand implements ITelegramCommand {

    private final StravaAuthService stravaAuthService;
    private final IStravaService stravaService;
    private final IUserChatService userChatService;
    private final IChatTypeFactory chatTypeFactory;

    private static final String DEFAULT_SUMMARY_FORMAT =
            "🏅 {name}\n📅 {date}\n🏃 Distance: {distance} km\n⏱️ Time: {time}\n";

    /**
     * Constructs a {@link ShowActivitiesCommand} with the provided services.
     *
     * @param stravaAuthService service for handling Strava authentication.
     * @param stravaService service for retrieving Strava activities.
     * @param userChatService service for handling user chat interactions.
     * @param chatTypeFactory service for checking chat types.
     */
    public ShowActivitiesCommand(StravaAuthService stravaAuthService,
                                 IStravaService stravaService,
                                 IUserChatService userChatService,
                                 IChatTypeFactory chatTypeFactory) {
        this.stravaAuthService = stravaAuthService;
        this.stravaService = stravaService;
        this.userChatService = userChatService;
        this.chatTypeFactory = chatTypeFactory;
    }

    /**
     * Executes the command to retrieve and display a user's Strava activities.
     * Filters can be applied for distance, duration, type, and other parameters.
     * The activity details are formatted according to the provided format or the default format.
     *
     * @param input The input containing the chat ID and command arguments.
     * @return A message containing the formatted Strava activities or an error message.
     */
    @Override
    public String execute(TelegramCommandInput input) {
        String chatId = input.getChatId();
        User user = userChatService.getUserFromChat(chatId);

        if (user == null) {
            return "❌ No registered user found for this chat. Please register using /start_notification.";
        }

        String username = user.getUsername();
        Map<String, String> params = ArgumentParser.parseArgs(input.getArgs());

        Map<String, String> filters = new HashMap<>();
        String summaryFormat = DEFAULT_SUMMARY_FORMAT;
        Integer count = null;

        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey().toLowerCase();
            String value = entry.getValue();

            switch (key) {
                case "min", "max", "duration", "type" -> filters.put(key, value);
                case "count" -> {
                    try {
                        count = Integer.parseInt(value);
                        if (count <= 0) return "❌ Error: count must be a positive integer.";
                    } catch (NumberFormatException e) {
                        return "❌ Error: Invalid number format for count.";
                    }
                }
                case "format" -> summaryFormat = value;
                default -> {
                    return "❌ Error: Unknown parameter '<code>" + key + "</code>'. "
                            + "Allowed keys: <code>min, max, duration, type, count, format</code>.";
                }
            }
        }

        Optional<StravaTokenResponse> tokenResponseOpt = stravaAuthService.getValidAccessToken(username);
        if (tokenResponseOpt.isEmpty()) {
            StravaToken token = stravaAuthService.getValidRefreshToken(username).orElse(null);
            if (token != null) {
                stravaAuthService.refreshAccessToken(token.getRefreshToken(), username);
            }
            tokenResponseOpt = stravaAuthService.getValidAccessToken(username);
            if (tokenResponseOpt.isEmpty()) {
                return "❌ Could not obtain a valid Strava token.";
            }
        }

        String accessToken = tokenResponseOpt.get().getAccessToken();
        List<StravaActivity> activities = stravaService.getActivities(accessToken);

        if (activities.isEmpty()) {
            return "ℹ️ No activities found for " + username + " on Strava.";
        }

        List<StravaActivity> filtered = activities.stream()
                .filter(a -> passesFilters(a, filters))
                .limit(count != null ? count : Long.MAX_VALUE)
                .toList();

        if (filtered.isEmpty()) {
            return "ℹ️ No activities found matching your filters.";
        }

        StringBuilder response = new StringBuilder("📊 <b>Activities for " + username + ":</b>\n\n");
        for (StravaActivity activity : filtered) {
            response.append(formatActivity(summaryFormat, activity)).append("\n");
        }

        return response.toString();
    }

    /**
     * Filters activities based on the provided filters (min/max distance, duration, type).
     *
     * @param activity The Strava activity to be checked.
     * @param filters The map of filters to apply.
     * @return true if the activity passes the filters, false otherwise.
     */
    private boolean passesFilters(StravaActivity activity, Map<String, String> filters) {
        try {
            double distKm = activity.getDistanceMeters() / 1000.0;
            double durMin = activity.getElapsedTimeSeconds() / 60.0;
            String type = Optional.ofNullable(activity.getSportType()).orElse("").toLowerCase();

            if (filters.containsKey("min") && distKm < Double.parseDouble(filters.get("min"))) return false;
            if (filters.containsKey("max") && distKm > Double.parseDouble(filters.get("max"))) return false;
            if (filters.containsKey("duration") && durMin < Double.parseDouble(filters.get("duration"))) return false;
            if (filters.containsKey("type") && !type.equals(filters.get("type").toLowerCase())) return false;
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * Formats a Strava activity into a string using the provided format.
     *
     * @param format The format string.
     * @param activity The Strava activity to format.
     * @return A formatted string representing the activity.
     */
    private String formatActivity(String format, StravaActivity activity) {
        return format
                .replace("{name}", activity.getName())
                .replace("{date}", activity.getStartDateLocal())
                .replace("{distance}", String.format("%.2f", activity.getDistanceMeters() / 1000.0))
                .replace("{time}", DateTimeUtils.formatElapsedTime(activity.getElapsedTimeSeconds()));
    }

    /**
     * Returns the name of the command, which is used to identify it in the Telegram bot.
     *
     * @return The command name, "/show_activities".
     */
    @Override
    public String getCommandName() {
        return "/show_activities";
    }

    /**
     * Returns the name of the command, which is used to identify it in the Telegram bot.
     *
     * @return The command name, "/show_activities".
     */
    @Override
    public boolean isAllowedInChat(String chatId) {
        return chatTypeFactory.isChatType(chatId, ChatType.PRIVATE);
    }
}