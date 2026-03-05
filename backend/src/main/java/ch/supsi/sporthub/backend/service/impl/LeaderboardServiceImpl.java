package ch.supsi.sporthub.backend.service.impl;

import ch.supsi.sporthub.backend.model.Leaderboard;
import ch.supsi.sporthub.backend.repository.jpa.LeaderboardRepository;
import ch.supsi.sporthub.backend.service.api.ILeaderboardService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of the ILeaderboardService interface that manages leaderboards.
 * This service interacts with the LeaderboardRepository to handle operations such as creating, retrieving,
 * updating, and removing users from leaderboards.
 */
@Service
public class LeaderboardServiceImpl implements ILeaderboardService {
    private final LeaderboardRepository leaderboardRepository;

    /**
     * Constructor for the LeaderboardServiceImpl class.
     * Initializes the service with the provided leaderboard repository for managing leaderboard data.
     *
     * @param leaderboardRepository The repository for accessing leaderboard data.
     */
    public LeaderboardServiceImpl(LeaderboardRepository leaderboardRepository) {
        this.leaderboardRepository = leaderboardRepository;
    }


    /**
     * Creates a new leaderboard with the given chat ID and name, and saves it in the repository.
     *
     * @param chatId The chat ID associated with the leaderboard.
     * @param name   The name of the leaderboard.
     */
    @Override
    public void createLeaderboard(String chatId, String name) {
        Leaderboard leaderboard = new Leaderboard(chatId, name);
        leaderboardRepository.save(leaderboard);
    }

    /**
     * Retrieves a leaderboard by its associated chat ID.
     *
     * @param chatId The chat ID associated with the leaderboard.
     * @return An Optional containing the leaderboard if found, or empty if not.
     */
    @Override
    public Optional<Leaderboard> getLeaderboardById(String chatId) {
        return leaderboardRepository.findByChatId(chatId);
    }

    /**
     * Checks if a leaderboard with the specified name already exists.
     *
     * @param name The name of the leaderboard to check.
     * @return true if a leaderboard with the given name exists, false otherwise.
     */
    @Override
    public boolean existsByName(String name) {
        return leaderboardRepository.existsByName(name);
    }

    /**
     * Saves the provided leaderboard to the repository.
     *
     * @param leaderboard The leaderboard to save.
     */
    @Override
    public void save(Leaderboard leaderboard) {
        leaderboardRepository.save(leaderboard);
    }

    /**
     * Removes a user from the leaderboard identified by the specified chat ID and username.
     *
     * @param chatId  The chat ID associated with the leaderboard.
     * @param username The username of the user to remove from the leaderboard.
     */
    @Override
    public void removeUserFromLeaderboard(String chatId, String username) {
        Optional<Leaderboard> leaderboardOpt = leaderboardRepository.findByChatId(chatId);

        if (leaderboardOpt.isPresent()) {
            Leaderboard leaderboard = leaderboardOpt.get();
            leaderboard.getScores().remove(username);
            leaderboardRepository.save(leaderboard);
        }
    }

    /**
     * Retrieves all leaderboards stored in the repository.
     *
     * @return A list of all leaderboards.
     */
    @Override
    public List<Leaderboard> getAllLeaderboards() {
        return leaderboardRepository.findAll();
    }


}