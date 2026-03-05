package ch.supsi.sporthub.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing the goal associated with a competition.
 * Each competition has exactly one goal, which defines the target metric
 * (e.g., distance, time, calories) participants must aim to achieve.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "competition_goals")
public class CompetitionGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double targetValue;

    @Enumerated(EnumType.STRING)
    private GoalType goalType;

    @Enumerated(EnumType.STRING)
    private SportType sportType;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competition_id", nullable = false)
    private Competition competition;
}