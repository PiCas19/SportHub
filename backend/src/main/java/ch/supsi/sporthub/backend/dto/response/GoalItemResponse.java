package ch.supsi.sporthub.backend.dto.response;

import ch.supsi.sporthub.backend.model.GoalType;
import ch.supsi.sporthub.backend.model.Period;
import ch.supsi.sporthub.backend.model.SportType;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * Response class representing a user's individual goal item.
 * <p>
 * This class is used to communicate goal tracking information to the client,
 * including progress metrics, type details, and the configured target.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GoalItemResponse extends Response {
    private Long id;
    private String name;
    private GoalType goalType;
    private SportType sportType;
    private double current;
    private double remaining;
    private double percentage;
    private double target;
    private Period period;
    private String unit;
}