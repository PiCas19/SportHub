package ch.supsi.sporthub.backend.service.api;

import ch.supsi.sporthub.backend.dto.request.CreateCompetitionRequest;
import ch.supsi.sporthub.backend.dto.response.CompetitionCreationResult;
import ch.supsi.sporthub.backend.model.Competition;
import ch.supsi.sporthub.backend.model.CompetitionGoal;
import ch.supsi.sporthub.backend.model.User;

import java.util.Optional;


/**
 * Interface for services that handle writing operations related to competitions,
 * such as creation, participant management, leaderboard updates, and persistence.
 */
public interface ICompetitionWriteService {

    /**
     * Creates a new competition with the specified request data and user.
     *
     * @param req  the request containing competition details
     * @param user the user creating the competition
     * @return a result object containing the created competition and its goal
     */
    CompetitionCreationResult createCompetition(CreateCompetitionRequest req, User user);

    /**
     * Adds the specified user as a participant in the given competition.
     *
     * @param competitionId the ID of the competition
     * @param user          the user joining the competition
     */
    void joinCompetition(Long competitionId, User user);

    /**
     * Removes the specified user from the given competition.
     *
     * @param competitionId the ID of the competition
     * @param user          the user leaving the competition
     */
    void leaveCompetition(Long competitionId, User user);

    /**
     * Persists the goal object associated with a competition.
     *
     * @param competitionGoal the goal to save
     */
    void saveCompetitionGoal(CompetitionGoal competitionGoal);

    /**
     * Saves or updates a competition entity in the database.
     *
     * @param competition the competition to persist
     */
    void saveCompetition(Competition competition);

    /**
     * Deletes the competition identified by the given ID.
     *
     * @param competitionId the ID of the competition to delete
     */
    void deleteCompetition(Long competitionId);

    /**
     * Finds a competition by its name and chat ID.
     *
     * @param name    the name of the competition
     * @param chatId  the chat ID associated with the competition
     * @return an Optional containing the found competition, or empty if not found
     */
    Optional<Competition> findCompetitionByNameAndChatId(String name, String chatId);

    /**
     * Updates the score of a user in the leaderboard for a given competition.
     *
     * @param competition the competition entity
     * @param username    the username of the participant
     * @param score       the new score to assign
     */
    void updateLeaderboardEntryScore(Competition competition, String username, double score);

    /**
     * Checks whether a competition with the given name and chat ID exists.
     *
     * @param name    the name of the competition
     * @param chatId  the associated chat ID
     * @return true if such a competition exists, false otherwise
     */
    boolean existsCompetitionByNameAndChatId(String name, String chatId);
}