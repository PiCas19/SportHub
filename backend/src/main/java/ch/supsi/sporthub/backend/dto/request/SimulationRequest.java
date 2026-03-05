package ch.supsi.sporthub.backend.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Request object used to trigger a simulation within a specific chat context.
 * <p>
 * This request includes the ID of the chat and the type of simulation to perform.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SimulationRequest extends Request {
    private String chatId;
    private String simulationType;  // Ti
}