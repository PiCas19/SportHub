package ch.supsi.sporthub.backend.service.domain.telegram.commands;

import ch.supsi.sporthub.backend.dto.response.LeaderboardEntryResponse;
import ch.supsi.sporthub.backend.model.Competition;
import ch.supsi.sporthub.backend.service.api.ICompetitionReadService;
import ch.supsi.sporthub.backend.service.api.ITelegramCommand;
import ch.supsi.sporthub.backend.service.domain.telegram.TelegramCommandInput;
import ch.supsi.sporthub.backend.utils.ResponseUtils;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Command for displaying the competition history for a specific group.
 * This command retrieves past competition details and their associated leaderboard entries.
 */
@Component
public class CompetitionHistoryCommand implements ITelegramCommand {

    private final ICompetitionReadService competitionReadService;

    public CompetitionHistoryCommand(ICompetitionReadService competitionReadService) {
        this.competitionReadService = competitionReadService;
    }

    /**
     * Executes the command to retrieve and display the competition history for a specific chat.
     * It fetches all competitions for the chat and their respective leaderboard entries.
     *
     * @param input The input containing the chat ID and arguments for the command.
     * @return A formatted string containing the competition history and leaderboard details for the group.
     */
    @Override
    @Transactional
    public String execute(TelegramCommandInput input) {
        String chatId = input.getChatId();
        List<Competition> competitions = competitionReadService.findCompetitionsByChatId(chatId);

        if (competitions.isEmpty()) {
            return "ℹ️ <b>No competitions found</b> for this group.";
        }

        StringBuilder response = new StringBuilder("📜 <b>Competition history for this group:</b>\n\n");

        for (Competition competition : competitions) {
            List<LeaderboardEntryResponse> leaderboardEntries = competitionReadService.getLeaderboardEntriesForCompetition(competition);

            response.append("🏁 <b>").append(escapeHtml(competition.getName())).append("</b>\n");
            response.append("• Status: ").append(competition.isFinished() ? "✅ Finished" : "⏳ In Progress").append("\n");
            response.append("• Max Participants: ").append(competition.getMaxParticipants()).append("\n");
            response.append("• Deadline: ").append(competition.getDeadline()).append("\n");

            response.append("📊 <b>Leaderboard:</b>\n");
            response.append(formatLeaderboard(competition, leaderboardEntries));
            response.append("\n");
        }

        return response.toString();
    }

    /**
     * Formats the leaderboard for a competition based on the leaderboard entries.
     * If there are no entries, a message is provided depending on whether the competition is finished.
     *
     * @param competition The competition for which the leaderboard is being formatted.
     * @param leaderboardEntries The list of leaderboard entries for the competition.
     * @return A formatted string of the leaderboard or a message indicating no entries.
     */
    private String formatLeaderboard(Competition competition, List<LeaderboardEntryResponse> leaderboardEntries) {
        if (!leaderboardEntries.isEmpty()) {
            return ResponseUtils.formatLeaderboardResponse(leaderboardEntries);
        } else if (competition.isFinished()) {
            return "• No activity completed during this competition.\n❗ <i>No points awarded.</i>\n";
        } else {
            return "• No activity from participants\n";
        }
    }


    /**
     * Returns the command name for this command, which is used for identifying it in the Telegram bot.
     *
     * @return The command name, "/competition_history".
     */
    @Override
    public String getCommandName() {
        return "/competition_history";
    }

    /**
     * Determines whether the command can be used in the given chat.
     * For this command, it is allowed in any chat.
     *
     * @param chatId The ID of the chat where the command is being used.
     * @return Always returns true, allowing the command in any chat.
     */
    @Override
    public boolean isAllowedInChat(String chatId) {
        return true;
    }

    /**
     * Escapes HTML special characters in a string to prevent HTML injection.
     * This method is used for safely displaying competition names in Telegram messages.
     *
     * @param text The text to escape.
     * @return The escaped text with HTML special characters replaced.
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}