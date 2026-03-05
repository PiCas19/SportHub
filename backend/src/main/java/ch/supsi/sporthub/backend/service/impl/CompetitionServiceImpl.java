package ch.supsi.sporthub.backend.service.impl;

import ch.supsi.sporthub.backend.dto.request.CreateCompetitionRequest;
import ch.supsi.sporthub.backend.dto.response.CompetitionCreationResult;
import ch.supsi.sporthub.backend.dto.response.CompetitionSummaryResponse;
import ch.supsi.sporthub.backend.dto.response.LeaderboardEntryResponse;
import ch.supsi.sporthub.backend.model.Competition;
import ch.supsi.sporthub.backend.model.CompetitionGoal;
import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.service.api.ICompetitionReadService;
import ch.supsi.sporthub.backend.service.api.ICompetitionService;
import ch.supsi.sporthub.backend.service.api.ICompetitionWriteService;
import ch.supsi.sporthub.backend.service.api.INotificationService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service implementation class combining read and write operations related to Competitions.
 * This service facilitates the creation, joining, leaving, updating, and retrieval of competition details and leaderboard entries.
 */
@Service
public class CompetitionServiceImpl implements ICompetitionService {

    private final ICompetitionReadService competitionReadService;
    private final ICompetitionWriteService competitionWriteService;
    private final INotificationService notificationService;

    /**
     * Constructs a CompetitionServiceImpl with the necessary services for handling competition operations.
     *
     * @param competitionReadService  Service for reading competition-related data
     * @param competitionWriteService Service for writing competition-related data
     * @param notificationService     Service for sending notifications
     */
    public CompetitionServiceImpl(ICompetitionReadService competitionReadService,
                                  ICompetitionWriteService competitionWriteService,
                                  INotificationService notificationService) {
        this.competitionReadService = competitionReadService;
        this.competitionWriteService = competitionWriteService;
        this.notificationService = notificationService;
    }

    /**
     * Creates a new competition using provided details and sends notifications upon successful creation.
     *
     * @param req  Request object containing details for the competition creation
     * @param user User who initiates the creation
     * @return Summary response containing the details of the newly created competition
     */
    @Override
    public CompetitionSummaryResponse createCompetition(CreateCompetitionRequest req, User user) {
        CompetitionCreationResult result = competitionWriteService.createCompetition(req, user);
        notificationService.notifyCompetitionCreated(result.getCompetition(), result.getGoal());
        CompetitionSummaryResponse competitionSummaryResponse = new CompetitionSummaryResponse();
        competitionSummaryResponse.setCompetitionId(result.getCompetition().getId());
        competitionSummaryResponse.setName(result.getCompetition().getName());
        competitionSummaryResponse.setMaxParticipants(result.getCompetition().getMaxParticipants());
        competitionSummaryResponse.setCurrentParticipants(0);
        competitionSummaryResponse.setUserRegistered(false);
        competitionSummaryResponse.setFinished(result.getCompetition().isFinished());
        competitionSummaryResponse.setStarted(result.getCompetition().isStarted());
        competitionSummaryResponse.setChatId(result.getCompetition().getChatId());
        competitionSummaryResponse.setGoalType(result.getGoal().getGoalType());
        competitionSummaryResponse.setSportType(result.getGoal().getSportType());
        competitionSummaryResponse.setDeadline(result.getCompetition().getDeadline());
        competitionSummaryResponse.setEndDate(result.getCompetition().getEndDate());
        competitionSummaryResponse.setMessage("Competition created");
        return competitionSummaryResponse;
    }

    /**
     * Allows a user to join a competition.
     *
     * @param competitionId ID of the competition to join
     * @param user          User who wishes to join
     */
    @Override
    public void joinCompetition(Long competitionId, User user) {
        competitionWriteService.joinCompetition(competitionId, user);
    }

    /**
     * Allows a user to leave a competition.
     *
     * @param competitionId ID of the competition to leave
     * @param user          User who wishes to leave
     */
    @Override
    public void leaveCompetition(Long competitionId, User user) {
        competitionWriteService.leaveCompetition(competitionId, user);
    }

    /**
     * Saves a competition goal.
     *
     * @param competitionGoal Goal object to be saved
     */
    @Override
    public void saveCompetitionGoal(CompetitionGoal competitionGoal) {
        competitionWriteService.saveCompetitionGoal(competitionGoal);
    }

    /**
     * Saves competition details.
     *
     * @param competition Competition object to be saved
     */
    @Override
    public void saveCompetition(Competition competition) {
        competitionWriteService.saveCompetition(competition);
    }

    /**
     * Deletes a competition based on its ID.
     *
     * @param competitionId ID of the competition to delete
     */
    @Override
    public void deleteCompetition(Long competitionId) {
        competitionWriteService.deleteCompetition(competitionId);
    }

    /**
     * Retrieves a list of competitions associated with a given user.
     *
     * @param user User whose competitions are to be retrieved
     * @return List of competition summaries
     */
    @Override
    public List<CompetitionSummaryResponse> getCompetitionsForUser(User user) {
        return competitionReadService.getCompetitionsForUser(user);
    }

    /**
     * Retrieves leaderboard entries for a specific competition.
     *
     * @param competition Competition for which leaderboard entries are requested
     * @return List of leaderboard entry responses
     */
    @Override
    public List<LeaderboardEntryResponse>  getLeaderboardEntriesForCompetition(Competition competition) {
        return competitionReadService.getLeaderboardEntriesForCompetition(competition);
    }


    /**
     * Retrieves a competition by its ID.
     *
     * @param competitionId ID of the competition
     * @return Competition entity
     */
    @Override
    public Competition getCompetitionById(Long competitionId) {
        return competitionReadService.getCompetitionById(competitionId);
    }

    /**
     * Retrieves the goal for a specific competition.
     *
     * @param competition Competition whose goal is to be retrieved
     * @return Competition goal entity
     * @throws RuntimeException if the goal is not found
     */
    @Override
    public CompetitionGoal getGoalForCompetition(Competition competition) {
        return competitionReadService.getGoalForCompetition(competition)
                .orElseThrow(() -> new RuntimeException("Goal not found for competition: " + competition.getId()));
    }

    /**
     * Retrieves all competitions.
     *
     * @return List of all competitions
     */
    @Override
    public List<Competition> getAllCompetitions() {
        return competitionReadService.getAllCompetitions();
    }

    /**
     * Updates leaderboard entry score.
     *
     * @param competition Competition whose leaderboard entry needs updating
     * @param username    Username of the participant
     * @param score       New score to be set
     */
    @Override
    public void updateLeaderboardEntryScore(Competition competition, String username, double score) {
        competitionWriteService.updateLeaderboardEntryScore(competition, username, score);
    }
}