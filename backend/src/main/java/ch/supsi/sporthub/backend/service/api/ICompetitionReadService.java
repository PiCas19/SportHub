package ch.supsi.sporthub.backend.service.api;

import ch.supsi.sporthub.backend.dto.response.CompetitionSummaryResponse;
import ch.supsi.sporthub.backend.dto.response.LeaderboardEntryResponse;
import ch.supsi.sporthub.backend.model.*;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for read-only operations related to competitions.
 * Provides methods to retrieve competitions, participants, leaderboard data, and associated goals.
 */
public interface ICompetitionReadService {

    /**
     * Retrieves a competition by its unique identifier.
     *
     * @param competitionId the ID of the competition
     * @return the corresponding {@link Competition}
     */
    Competition getCompetitionById(Long competitionId);

    /**
     * Retrieves all participants registered in a given competition.
     *
     * @param competition the competition entity
     * @return list of {@link CompetitionParticipant}
     */
    List<CompetitionParticipant> getParticipantsForCompetition(Competition competition);

    /**
     * Retrieves the leaderboard entries for a given competition.
     *
     * @param competition the competition entity
     * @return list of {@link LeaderboardEntryResponse}
     */
    List<LeaderboardEntryResponse>  getLeaderboardEntriesForCompetition(Competition competition);

    /**
     * Returns a list of competitions that a user is participating in or eligible for.
     *
     * @param user the user entity
     * @return list of {@link CompetitionSummaryResponse}
     */
    List<CompetitionSummaryResponse> getCompetitionsForUser(User user);

    /**
     * Fetches the goal configuration associated with a specific competition, if present.
     *
     * @param competition the competition entity
     * @return optional {@link CompetitionGoal}
     */
    Optional<CompetitionGoal> getGoalForCompetition(Competition competition);

    /**
     * Retrieves all competitions from the system.
     *
     * @return list of all {@link Competition} entities
     */
    List<Competition> getAllCompetitions();

    /**
     * Finds competitions associated with a specific chat ID.
     *
     * @param chatId the chat identifier
     * @return list of {@link Competition} matching the chat ID
     */
    List<Competition> findCompetitionsByChatId(String chatId);
}