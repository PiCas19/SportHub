package ch.supsi.sporthub.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a personal goal set by a user.
 * A goal is defined by a target value, goal type, sport type, and time period.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "goals")
public class Goal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double targetValue;

    @Enumerated(EnumType.STRING)
    private GoalType goalType;

    @Enumerated(EnumType.STRING)
    private SportType sportType;

    @Enumerated(EnumType.STRING)
    private Period period;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}