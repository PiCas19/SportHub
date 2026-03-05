package ch.supsi.sporthub.backend.repository.jpa;

import ch.supsi.sporthub.backend.model.Competition;
import ch.supsi.sporthub.backend.model.CompetitionGoal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for managing {@link CompetitionGoal} entities.
 * Provides methods to access goals associated with competitions.
 */
public interface CompetitionGoalRepository  extends JpaRepository<CompetitionGoal, Long> {

    /**
     * Finds the goal associated with a given competition.
     *
     * @param competition the competition whose goal is to be retrieved
     * @return an optional containing the {@link CompetitionGoal} if found, or empty otherwise
     */
    Optional<CompetitionGoal> findByCompetition(Competition competition);
}