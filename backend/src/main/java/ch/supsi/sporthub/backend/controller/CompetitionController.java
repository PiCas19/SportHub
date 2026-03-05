package ch.supsi.sporthub.backend.controller;

import ch.supsi.sporthub.backend.dto.request.CreateCompetitionRequest;
import ch.supsi.sporthub.backend.dto.response.CompetitionSummaryResponse;
import ch.supsi.sporthub.backend.dto.response.LeaderboardEntryResponse;
import ch.supsi.sporthub.backend.model.Competition;
import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.service.JwtTokenService;
import ch.supsi.sporthub.backend.service.UserService;
import ch.supsi.sporthub.backend.service.api.ICompetitionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CompetitionController provides the API endpoints for managing competitions.
 * It allows users to create, join, leave, and view competitions as well as get leaderboard entries.
 */
@RestController
@RequestMapping("/api/competitions")
public class CompetitionController extends BaseController {

    private final ICompetitionService competitionService;

    /**
     * Constructs an instance of CompetitionController with the specified competition service, JWT token service, and user service.
     *
     * @param competitionService The service responsible for managing competitions.
     * @param jwtTokenService    The service responsible for handling JWT token generation and validation.
     * @param userService        The service responsible for user-related operations.
     */
    public CompetitionController(ICompetitionService competitionService,
                                 JwtTokenService jwtTokenService,
                                 UserService userService) {
        super(jwtTokenService, userService);
        this.competitionService = competitionService;
    }

    /**
     * Creates a new competition.
     * This method requires the user to be authenticated, as their information is needed to create the competition.
     *
     * @param request    The request containing the competition details.
     * @param authHeader The authorization header containing the user's JWT token.
     * @return A response entity containing the summary of the created competition.
     */
    @PostMapping
    public ResponseEntity<CompetitionSummaryResponse> createCompetition(@RequestBody CreateCompetitionRequest request,
                                                         @RequestHeader("Authorization") String authHeader) {
        User user = getUserFromHeader(authHeader);
        CompetitionSummaryResponse created = competitionService.createCompetition(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Retrieves all competitions for the authenticated user.
     * This method requires the user to be authenticated to fetch their associated competitions.
     *
     * @param authHeader The authorization header containing the user's JWT token.
     * @return A response entity containing a list of competitions that the user is associated with.
     */
    @GetMapping
    public ResponseEntity<List<CompetitionSummaryResponse>> getUserCompetitions(@RequestHeader("Authorization") String authHeader) {
        User user = getUserFromHeader(authHeader);
        return ResponseEntity.ok(competitionService.getCompetitionsForUser(user));
    }

    /**
     * Allows a user to join a competition.
     * This method requires the user to be authenticated and the competition to exist.
     *
     * @param id         The ID of the competition to join.
     * @param authHeader The authorization header containing the user's JWT token.
     * @return A response entity confirming the success of the operation.
     */
    @PostMapping("/{id}/join")
    public ResponseEntity<?> joinCompetition(@PathVariable Long id,
                                             @RequestHeader("Authorization") String authHeader) {
        User user = getUserFromHeader(authHeader);
        competitionService.joinCompetition(id, user);
        return ResponseEntity.ok("Joined competition successfully.");
    }

    /**
     * Allows a user to leave a competition.
     * This method requires the user to be authenticated and the user to be currently participating in the competition.
     *
     * @param id         The ID of the competition to leave.
     * @param authHeader The authorization header containing the user's JWT token.
     * @return A response entity confirming the success of the operation.
     */
    @PostMapping("/{id}/leave")
    public ResponseEntity<?> leaveCompetition(@PathVariable Long id,
                                              @RequestHeader("Authorization") String authHeader) {
        User user = getUserFromHeader(authHeader);
        competitionService.leaveCompetition(id, user);
        return ResponseEntity.ok("Left competition successfully.");
    }

    /**
     * Retrieves the leaderboard entries for a specific competition.
     * This method requires the user to be authenticated and the competition to exist.
     *
     * @param id         The ID of the competition for which to retrieve leaderboard entries.
     * @param authHeader The authorization header containing the user's JWT token.
     * @return A response entity containing a list of leaderboard entries for the competition.
     */
    @GetMapping("/{id}/leaderboard")
    public ResponseEntity<List<LeaderboardEntryResponse>> getLeaderboard(@PathVariable Long id,
                                                                         @RequestHeader("Authorization") String authHeader) {
        User user = getUserFromHeader(authHeader);
        Competition competition = competitionService.getCompetitionById(id);
        List<LeaderboardEntryResponse> leaderboardEntries = competitionService.getLeaderboardEntriesForCompetition(competition);
        return ResponseEntity.ok(leaderboardEntries);
    }
}