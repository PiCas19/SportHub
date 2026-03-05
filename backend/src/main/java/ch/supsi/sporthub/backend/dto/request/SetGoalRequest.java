package ch.supsi.sporthub.backend.dto.request;

import ch.supsi.sporthub.backend.model.Period;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Request object used to set a personal goal for a user.
 * <p>
 * This request includes the target value to achieve, the type of goal,
 * the reference period (e.g., daily, weekly), and the associated sport type.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SetGoalRequest extends Request {
    private Double targetValue;
    private String goalType;
    private Period period;
    private String sportType;
}