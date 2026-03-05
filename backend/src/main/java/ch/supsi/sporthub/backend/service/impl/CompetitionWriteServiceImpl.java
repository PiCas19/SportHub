package ch.supsi.sporthub.backend.service.impl;

import ch.supsi.sporthub.backend.dto.request.CreateCompetitionRequest;
import ch.supsi.sporthub.backend.dto.response.CompetitionCreationResult;
import ch.supsi.sporthub.backend.model.*;
import ch.supsi.sporthub.backend.repository.jpa.*;
import ch.supsi.sporthub.backend.service.api.ICompetitionWriteService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Implementation of the ICompetitionWriteService interface.
 * Provides methods for creating, updating, deleting, and managing competitions.
 */
@Service
public class CompetitionWriteServiceImpl implements ICompetitionWriteService {

    private final CompetitionRepository competitionRepository;
    private final CompetitionGoalRepository competitionGoalRepository;
    private final CompetitionParticipantRepository competitionParticipantRepository;
    private final CompetitionLeaderboardEntryRepository competitionLeaderboardEntryRepository;
    private final ChatRepository chatRepository;

    /**
     * Constructs a CompetitionWriteServiceImpl with the necessary repositories for handling competition write operations.
     *
     * @param competitionRepository Repository for competition entities
     * @param competitionGoalRepository Repository for competition goal entities
     * @param competitionParticipantRepository Repository for competition participant entities
     * @param competitionLeaderboardEntryRepository Repository for leaderboard entry entities
     * @param chatRepository Repository for chat-related entities
     */
    public CompetitionWriteServiceImpl(CompetitionRepository competitionRepository,
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
     * Creates a new competition and validates the request parameters.
     * Throws an exception if any of the validation checks fail.
     *
     * @param req Request object containing competition creation details
     * @param user User who is creating the competition
     * @return Competition creation result containing the competition and goal created
     * @throws ResponseStatusException if validation fails
     */
    @Override
    @Transactional
    public CompetitionCreationResult createCompetition(CreateCompetitionRequest req, User user) {
        if (!req.getChatId().contains("-")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chat ID must contain '-' character (private group).");
        }

        Page<Chat> chatPage = chatRepository.findByChatId(req.getChatId(), PageRequest.of(0, 1));
        if (chatPage.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat not found");
        }

        if (req.getTargetValue() <= 0 || req.getMaxParticipants() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Target and maxParticipants must be greater than 0");
        }
        if (req.getDeadline().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Deadline must be in the future");
        }
        if (req.getEndDate().isBefore(req.getDeadline())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End Date must be after the deadline");
        }

        GoalType goalType = GoalType.valueOf(req.getGoalType().toUpperCase());
        SportType sportType = SportType.valueOf(req.getSportType().toUpperCase());

        Competition competition = new Competition();
        competition.setName(req.getName());
        competition.setMaxParticipants(req.getMaxParticipants());
        competition.setDeadline(req.getDeadline());
        competition.setEndDate(req.getEndDate());
        competition.setChatId(req.getChatId());

        Competition savedCompetition = competitionRepository.save(competition);

        CompetitionGoal goal = new CompetitionGoal();
        goal.setCompetition(savedCompetition);
        goal.setGoalType(goalType);
        goal.setSportType(sportType);
        goal.setTargetValue(req.getTargetValue());

        competitionGoalRepository.save(goal);

        return new CompetitionCreationResult(savedCompetition, goal, "Competition created successfully");
    }

    /**
     * Allows a user to join a competition.
     *
     * @param competitionId The ID of the competition.
     * @param user          The user joining the competition.
     * @throws ResponseStatusException If there are validation errors, such as the user not being in the chat or the competition being full.
     */
    @Override
    @Transactional
    public void joinCompetition(Long competitionId, User user) {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Competition not found"));

        boolean userInChat = chatRepository.findAllByChatId(competition.getChatId())
                .stream()
                .anyMatch(chat -> chat.getUser().getId().equals(user.getId()));

        if (!userInChat) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not registered in the chat");
        }

        if (competitionParticipantRepository.existsByCompetitionAndUser(competition, user)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already joined this competition");
        }

        if (competitionParticipantRepository.findByCompetition(competition).size() >= competition.getMaxParticipants()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Competition is full");
        }

        if (LocalDate.now().isAfter(competition.getDeadline())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Competition deadline passed");
        }

        if (competition.isFinished()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Competition has finished");
        }

        CompetitionParticipant participant = new CompetitionParticipant();
        participant.setCompetition(competition);
        participant.setUser(user);
        participant.setJoinedAt(LocalDateTime.now());
        competitionParticipantRepository.save(participant);

        CompetitionLeaderboardEntry leaderboardEntry = new CompetitionLeaderboardEntry();
        leaderboardEntry.setCompetition(competition);
        leaderboardEntry.setUser(user);
        leaderboardEntry.setScore(0.0);
        competitionLeaderboardEntryRepository.save(leaderboardEntry);

        if (competitionParticipantRepository.findByCompetition(competition).size() == competition.getMaxParticipants()
                && LocalDate.now().isAfter(competition.getDeadline())) {
            competition.setStarted(true);
            competitionRepository.save(competition);
        }
    }

    /**
     * Allows a user to leave a competition.
     *
     * @param competitionId The ID of the competition.
     * @param user          The user leaving the competition.
     * @throws ResponseStatusException If the user is not a participant or the competition has already started.
     */
    @Override
    @Transactional
    public void leaveCompetition(Long competitionId, User user) {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Competition not found"));
        if (competition.isStarted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot leave a started competition");
        }

        if (!competitionParticipantRepository.existsByCompetitionAndUser(competition, user)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not a participant in this competition");
        }

        competitionParticipantRepository.deleteByCompetitionAndUser(competition, user);
        competitionLeaderboardEntryRepository.findByCompetitionAndUser(competition, user)
                .ifPresent(competitionLeaderboardEntryRepository::delete);
    }


    /**
     * Saves a competition goal to the database.
     *
     * @param competitionGoal The goal to be saved.
     */
    @Override
    @Transactional
    public void saveCompetitionGoal(CompetitionGoal competitionGoal) {
        competitionGoalRepository.save(competitionGoal);
    }

    /**
     * Saves a competition to the database.
     *
     * @param competition The competition to be saved.
     */
    @Override
    @Transactional
    public void saveCompetition(Competition competition) {
        competitionRepository.save(competition);
    }

    /**
     * Finds a competition by its name and chat ID.
     *
     * @param name   The name of the competition.
     * @param chatId The chat ID of the competition.
     * @return An Optional containing the competition if found, otherwise empty.
     */
    @Override
    public Optional<Competition> findCompetitionByNameAndChatId(String name, String chatId) {
        return competitionRepository.findByNameAndChatId(name, chatId);
    }

    /**
     * Updates the score of a leaderboard entry in a competition.
     *
     * @param competition The competition containing the leaderboard entry.
     * @param username    The username of the participant.
     * @param score       The new score to set.
     */
    @Override
    @Transactional
    public void updateLeaderboardEntryScore(Competition competition, String username, double score) {
        competitionLeaderboardEntryRepository.findByCompetition(competition).stream()
                .filter(entry -> entry.getUser().getUsername().equals(username))
                .findFirst()
                .ifPresent(entry -> {
                    entry.setScore(score);
                    competitionLeaderboardEntryRepository.save(entry);
                });
    }

    /**
     * Deletes a competition from the database.
     *
     * @param competitionId The ID of the competition to delete.
     * @throws ResponseStatusException If the competition does not exist.
     */
    @Override
    @Transactional
    public void deleteCompetition(Long competitionId) {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Competition not found"));
        competitionParticipantRepository.deleteAll(competitionParticipantRepository.findByCompetition(competition));
        competitionLeaderboardEntryRepository.deleteAll(competitionLeaderboardEntryRepository.findByCompetition(competition));
        competitionGoalRepository.findByCompetition(competition)
                .ifPresent(competitionGoalRepository::delete);
        competitionRepository.delete(competition);
    }

    /**
     * Checks if a competition with the given name and chat ID exists.
     *
     * @param name   The name of the competition.
     * @param chatId The chat ID of the competition.
     * @return true if the competition exists, false otherwise.
     */
    @Override
    @Transactional
    public boolean existsCompetitionByNameAndChatId(String name, String chatId) {
        return competitionRepository.existsByNameAndChatId(name, chatId);
    }
}