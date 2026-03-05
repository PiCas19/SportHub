package ch.supsi.sporthub.backend.service.impl;

import ch.supsi.sporthub.backend.dto.response.CompetitionSummaryResponse;
import ch.supsi.sporthub.backend.dto.response.LeaderboardEntryResponse;
import ch.supsi.sporthub.backend.model.*;
import ch.supsi.sporthub.backend.repository.jpa.*;
import ch.supsi.sporthub.backend.service.api.ICompetitionReadService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Service implementation for handling competition read operations.
 * This service provides methods to fetch competitions, participants, leaderboard entries, and goals.
 */
@Service
public class CompetitionReadServiceImpl implements ICompetitionReadService {

    private final CompetitionRepository competitionRepository;
    private final CompetitionGoalRepository competitionGoalRepository;
    private final CompetitionParticipantRepository competitionParticipantRepository;
    private final CompetitionLeaderboardEntryRepository competitionLeaderboardEntryRepository;
    private final ChatRepository chatRepository;

    /**
     * Constructs a CompetitionReadServiceImpl with the necessary repositories.
     *
     * @param competitionRepository Repository for competition entities
     * @param competitionGoalRepository Repository for competition goal entities
     * @param competitionParticipantRepository Repository for competition participant entities
     * @param competitionLeaderboardEntryRepository Repository for leaderboard entries
     * @param chatRepository Repository for chat-related entities
     */
    public CompetitionReadServiceImpl(CompetitionRepository competitionRepository,
                                      CompetitionGoalRepository competitionGoalRepository,
                                      CompetitionParticipantRepository competitionParticipantRepository,
                                      CompetitionLeaderboardEntryRepository competitionLeaderboardEntryRepository,
                                      ChatRepository chatRepository) {
        this.competitionRepository = competitionRepository;
        this.competitionGoalRepository = competitionGoalRepository;
        this.competitionParticipantRepository = competitionParticipantRepository;
        this.competitionLeaderboardEntryRepository = competitionLeaderboardEntryRepository;
        this.chatRepository = chatRepository;
    }

    /**
     * Retrieves a competition by its ID.
     *
     * @param competitionId the ID of the competition
     * @return the competition if found
     * @throws ResponseStatusException if the competition is not found
     */
    @Override
    public Competition getCompetitionById(Long competitionId) {
        return competitionRepository.findById(competitionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Competition not found"));
    }

    /**
     * Retrieves all participants for a specific competition.
     *
     * @param competition the competition entity
     * @return list of participants
     */
    @Override
    public List<CompetitionParticipant> getParticipantsForCompetition(Competition competition) {
        return competitionParticipantRepository.findByCompetition(competition);
    }

    /**
     * Retrieves leaderboard entries for a specific competition, sorted by score in descending order.
     *
     * @param competition the competition entity
     * @return sorted list of leaderboard entry responses
     */
    @Override
    public List<LeaderboardEntryResponse> getLeaderboardEntriesForCompetition(Competition competition) {
        return competitionLeaderboardEntryRepository.findByCompetition(competition)
                .stream()
                .sorted(Comparator.comparing(CompetitionLeaderboardEntry::getScore).reversed())
                .map(entry -> {
                    User user = entry.getUser();
                    Optional<Chat> chatOpt = chatRepository.findAllByChatId(competition.getChatId())
                            .stream()
                            .filter(chat -> chat.getUser().getId().equals(user.getId()))
                            .findFirst();

                    String telegramUsername = chatOpt.map(Chat::getTelegramUserName).orElse(null);

                    LeaderboardEntryResponse response = new LeaderboardEntryResponse(
                            entry.getScore(),
                            user.getUsername(),
                            telegramUsername
                    );
                    response.setMessage("Competition leaderboard");
                    return response;
                })
                .toList();
    }

    /**
     * Retrieves a summary of competitions associated with a user.
     *
     * @param user the user entity
     * @return list of competition summary responses
     */
    @Override
    public List<CompetitionSummaryResponse> getCompetitionsForUser(User user) {
        List<String> chatIds = chatRepository.findByUser(user).stream()
                .map(Chat::getChatId)
                .filter(Objects::nonNull)
                .filter(chatId -> !chatId.isEmpty())
                .toList();

        if (chatIds.isEmpty()) {
            return List.of();
        }

        return competitionRepository.findByChatIdIn(chatIds).stream()
                .map(competition -> convertToDto(competition, user))
                .toList();
    }

    /**
     * Converts a competition entity to a competition summary DTO.
     *
     * @param competition the competition entity
     * @param user the user entity to check registration status
     * @return competition summary response DTO
     */
    private CompetitionSummaryResponse convertToDto(Competition competition, User user) {
        Optional<CompetitionGoal> goalOpt = competitionGoalRepository.findByCompetition(competition);
        boolean isUserRegistered = competitionParticipantRepository.existsByCompetitionAndUser(competition, user);
        int currentParticipants = competitionParticipantRepository.findByCompetition(competition).size();

        CompetitionSummaryResponse dto = new CompetitionSummaryResponse();
        dto.setCompetitionId(competition.getId());
        dto.setName(competition.getName());
        dto.setMaxParticipants(competition.getMaxParticipants());
        dto.setCurrentParticipants(currentParticipants);
        dto.setUserRegistered(isUserRegistered);
        dto.setStarted(competition.isStarted());
        dto.setFinished(competition.isFinished());
        dto.setChatId(competition.getChatId());
        dto.setDeadline(competition.getDeadline());
        dto.setEndDate(competition.getEndDate());
        dto.setMessage(isUserRegistered
                ? "You are registered to this competition."
                : "You are not registered to this competition.");

        goalOpt.ifPresent(goal -> {
            dto.setGoalType(goal.getGoalType());
            dto.setSportType(goal.getSportType());
        });

        return dto;
    }

    /**
     * Retrieves the goal associated with a specific competition.
     *
     * @param competition the competition entity
     * @return an Optional containing the goal if found
     */
    @Override
    public Optional<CompetitionGoal> getGoalForCompetition(Competition competition) {
        return competitionGoalRepository.findByCompetition(competition);
    }

    /**
     * Retrieves all competitions.
     *
     * @return list of all competitions
     */
    @Override
    public List<Competition> getAllCompetitions() {
        return competitionRepository.findAll();
    }

    /**
     * Finds competitions by their associated chat ID.
     *
     * @param chatId the chat ID
     * @return list of competitions associated with the chat ID
     */
    @Override
    public List<Competition> findCompetitionsByChatId(String chatId) {
        return competitionRepository.findAll().stream()
                .filter(c -> chatId.equals(c.getChatId()))
                .toList();
    }
}