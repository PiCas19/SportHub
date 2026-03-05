package ch.supsi.sporthub.backend.service.api;

import ch.supsi.sporthub.backend.model.User;

/**
 * Interface for simulation services related to competitions and goals.
 * Used primarily to simulate user progress and send preview notifications.
 */
public interface ISimulationService {

    /**
     * Simulates the user's participation in a competition and evaluates
     * how they would perform based on current or historical data.
     *
     * @param user the user for whom the simulation is run
     * @param chatId the chat ID where the simulation results should be sent
     */
    void simulateCompetition(User user, String chatId);

    /**
     * Simulates the user's progress toward their personal goals and determines
     * if any goals would be achieved based on projected or historical data.
     *
     * @param user the user for whom the simulation is run
     * @param chatId the chat ID where the simulation results should be sent
     */
    void simulateGoals(User user, String chatId);
}