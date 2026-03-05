package ch.supsi.sporthub.backend.controller;

import ch.supsi.sporthub.backend.dto.request.SimulationRequest;
import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.service.api.ISimulationService;
import ch.supsi.sporthub.backend.service.api.IUserChatService;
import org.springframework.web.bind.annotation.*;

/**
 * The CompetitionAndGoalSimulationController class handles the simulation of competitions and goals for users in a chat system.
 * It provides API endpoints to simulate these events based on the type of simulation requested and the type of chat (private or group).
 */
@RestController
@RequestMapping("/api/simulation")
public class CompetitionAndGoalSimulationController {

    private final IUserChatService userChatService;
    private final ISimulationService simulationService;

    /**
     * Constructs an instance of CompetitionAndGoalSimulationController with the specified user chat service and simulation service.
     *
     * @param userChatService The service used to fetch user details from a chat.
     * @param simulationService The service responsible for handling simulations for goals and competitions.
     */
    public CompetitionAndGoalSimulationController(IUserChatService userChatService, ISimulationService simulationService) {
        this.userChatService = userChatService;
        this.simulationService = simulationService;
    }

    /**
     * Simulates either a "Goal" or a "Competition" event for a user, based on the simulation request.
     * It checks if the chat is private or not and triggers the appropriate simulation for the user.
     *
     * @param simulationRequest The request containing the simulation type and chat ID.
     * @return A message indicating the result of the simulation process.
     */
    @PostMapping
    public String simulate(@RequestBody SimulationRequest simulationRequest) {
        User user = userChatService.getUserFromChat(simulationRequest.getChatId());

        if (user == null) {
            return "User not found!";
        }

        boolean isPrivate = isPrivateChat(simulationRequest.getChatId());

        if ("Goal".equalsIgnoreCase(simulationRequest.getSimulationType())) {
            if (isPrivate) {
                simulationService.simulateGoals(user, simulationRequest.getChatId());
                return "Goal simulation completed for the user " + user.getUsername();
            } else {
                return "Goal simulation can only be done in a private chat.";
            }
        }

        if ("Competition".equalsIgnoreCase(simulationRequest.getSimulationType())) {
            if (!isPrivate) {
                simulationService.simulateCompetition(user, simulationRequest.getChatId());
                return "Competition simulation completed for the user " + user.getUsername();
            } else {
                return "Competition simulation cannot be done in a private chat.";
            }
        }

        return "Simulation type not recognized or invalid chat type!";
    }

    /**
     * Checks if the provided chat ID corresponds to a private chat.
     * Private chats are identified by a positive chat ID.
     *
     * @param chatId The ID of the chat to check.
     * @return true if the chat is private, false otherwise.
     */
    private boolean isPrivateChat(String chatId) {
        try {
            long chatIdLong = Long.parseLong(chatId);
            return chatIdLong > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}