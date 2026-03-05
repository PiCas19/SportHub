package ch.supsi.sporthub.backend.service.api;

import ch.supsi.sporthub.backend.dto.request.CreateCompetitionRequest;
import ch.supsi.sporthub.backend.dto.response.CompetitionSummaryResponse;
import ch.supsi.sporthub.backend.dto.response.LeaderboardEntryResponse;
import ch.supsi.sporthub.backend.model.*;

import java.util.List;

/**
 * Service interface for managing competitions.
 * Includes creation, participation, goal management, leaderboard updates, and retrieval of competition data.
 */
public interface ICompetitionService {

    /**
     * Creates a new competition based on the provided request and user.
     *
     * @param req  the competition creation request
     * @param user the user initiating the creation
     * @return a summary of the newly created competition
     */
    CompetitionSummaryResponse createCompetition(CreateCompetitionRequest req, User user);

    /**
     * Registers a user to participate in a given competition.
     *
     * @param competitionId the ID of the competition
     * @param user          the user joining the competition
     */
    void joinCompetition(Long competitionId, User user);

    /**
     * Removes a user from a competition.
     *
     * @param competitionId the ID of the competition
     * @param user          the user leaving the competition
     */
    void leaveCompetition(Long competitionId, User user);

    /**
     * Persists the goal configuration associated with a competition.
     *
     * @param competitionGoal the competition goal entity
     */
    void saveCompetitionGoal(CompetitionGoal competitionGoal);

    /**
     * Saves or updates a competition entity.
     *
     * @param competition the competition entity to be saved
     */
    void saveCompetition(Competition competition);

    /**
     * Deletes a competition by its ID.
     *
     * @param competitionId the ID of the competition to be deleted
     */
    void deleteCompetition(Long competitionId);

    /**
     * Retrieves competitions relevant to a specific user.
     *
     * @param user the user entity
     * @return list of competition summaries
     */
    List<CompetitionSummaryResponse> getCompetitionsForUser(User user);

    /**
     * Retrieves the leaderboard entries for a given competition.
     *
     * @param competition the competition entity
     * @return list of leaderboard entries
     */
    List<LeaderboardEntryResponse>  getLeaderboardEntriesForCompetition(Competition competition);

    /**
     * Retrieves a competition by its unique identifier.
     *
     * @param competitionId the ID of the competition
     * @return the competition entity
     */
    Competition getCompetitionById(Long competitionId);

    /**
     * Retrieves the goal associated with a specific competition.
     *
     * @param competition the competition entity
     * @return the associated competition goal
     */
    CompetitionGoal getGoalForCompetition(Competition competition);

    /**
     * Retrieves all competitions in the system.
     *
     * @return list of all competitions
     */
    List<Competition> getAllCompetitions();

    /**
     * Updates the score of a user in the leaderboard of a given competition.
     *
     * @param competition the competition entity
     * @param username    the username of the user whose score is to be updated
     * @param score       the score to be assigned
     */
    void updateLeaderboardEntryScore(Competition competition, String username, double score);
}