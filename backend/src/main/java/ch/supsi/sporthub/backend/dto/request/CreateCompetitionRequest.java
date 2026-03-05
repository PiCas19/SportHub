package ch.supsi.sporthub.backend.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * CreateCompetitionRequest is a data transfer object (DTO) used to encapsulate
 * the data required to create a new competition within the system.
 * <p>
 * This class is typically used in HTTP requests when an administrator or authorized
 * user wants to define and register a new challenge or competition.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CreateCompetitionRequest extends Request {
    private String name;
    private double targetValue;
    private String goalType;
    private String sportType;
    private int maxParticipants;
    private LocalDate deadline;
    private LocalDate endDate;
    private String chatId;
}