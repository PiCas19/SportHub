package ch.supsi.sporthub.backend.dto.response;

import ch.supsi.sporthub.backend.model.Competition;
import ch.supsi.sporthub.backend.model.CompetitionGoal;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Response class returned after a competition creation operation.
 * <p>
 * Contains the created {@link Competition} entity along with the associated
 * {@link CompetitionGoal}, and a message indicating the result of the operation.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CompetitionCreationResult extends Response {
    private Competition competition;
    private CompetitionGoal goal;

    /**
     * Constructs a new {@code CompetitionCreationResult} containing the created competition and its goal.
     *
     * @param saved              the {@link Competition} entity that was persisted
     * @param goal               the {@link CompetitionGoal} linked to the competition
     * @param createCompetition  a response message describing the outcome
     */
    public CompetitionCreationResult(Competition saved, CompetitionGoal goal, String createCompetition) {
        super(createCompetition);
        this.competition = saved;
        this.goal = goal;
    }
}