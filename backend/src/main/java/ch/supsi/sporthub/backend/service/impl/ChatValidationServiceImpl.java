package ch.supsi.sporthub.backend.service.impl;

import ch.supsi.sporthub.backend.config.TelegramProperties;
import ch.supsi.sporthub.backend.model.Leaderboard;
import ch.supsi.sporthub.backend.repository.jpa.LeaderboardRepository;
import ch.supsi.sporthub.backend.service.api.IBaseApiService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service class responsible for validating chat IDs and updating leaderboards in a group chat.
 * It interacts with the Telegram API to verify if a chat ID is valid, and it handles leaderboard updates
 * by adding users to existing leaderboards based on group chat IDs.
 */
@Service
public class ChatValidationServiceImpl {
    private final IBaseApiService baseApiService;
    private final TelegramProperties telegramProperties;
    private final LeaderboardRepository leaderboardRepository;

    /**
     * Constructs a {@link ChatValidationServiceImpl} with the necessary services and properties.
     *
     * @param telegramProperties The properties for interacting with the Telegram API, such as the bot token and base URL.
     * @param baseApiService The service used to make API calls to Telegram.
     * @param leaderboardRepository The repository for accessing and managing leaderboards in the database.
     */
    public ChatValidationServiceImpl(TelegramProperties telegramProperties, IBaseApiService baseApiService, LeaderboardRepository leaderboardRepository) {
        this.baseApiService = baseApiService;
        this.telegramProperties = telegramProperties;
        this.leaderboardRepository = leaderboardRepository;
    }


    /**
     * Verifies if the provided chat ID is valid by making an API call to the Telegram "getChat" method.
     * The chat ID is checked by sending a request to the Telegram API and verifying the response status.
     *
     * @param chatId The chat ID to verify.
     * @return True if the chat ID is valid, false otherwise.
     */
    public boolean verifyChatId(String chatId) {
        try {
            String endpoint = "/bot" + telegramProperties.getBotToken() + "/getChat?chat_id=" + chatId;
            ResponseEntity<String> response = baseApiService.get(telegramProperties.getBaseUrl(), endpoint, String.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Updates the leaderboard for a specific group chat by adding the user to the leaderboard if they are not already present.
     * If the leaderboard for the group does not exist, no update will occur.
     *
     * @param groupId The group ID of the chat.
     * @param username The username of the user to add to the leaderboard.
     * @return A list of leaderboard names where the user was added.
     */
    public List<String> updateLeaderboards(String groupId, String username) {
        List<String> addedLeaderboards = new ArrayList<>();
        Optional<Leaderboard> leaderboardOpt = leaderboardRepository.findByChatId(groupId);
        if (leaderboardOpt.isPresent()) {
            Leaderboard leaderboard = leaderboardOpt.get();
            if (!leaderboard.getScores().containsKey(username)) {
                leaderboard.getScores().put(username, 0);
                leaderboardRepository.save(leaderboard);
                addedLeaderboards.add(leaderboard.getName());
            }
        }

        return addedLeaderboards;
    }

}