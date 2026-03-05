package ch.supsi.sporthub.backend.dto.response;

import ch.supsi.sporthub.backend.model.GoalType;
import ch.supsi.sporthub.backend.model.SportType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * Response class representing a summary of a competition.
 * <p>
 * Used to display competition data in list views or overviews, including
 * registration status and basic metadata.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CompetitionSummaryResponse extends Response{
    private Long competitionId;
    private String name;
    private int maxParticipants;
    private int currentParticipants;
    private boolean isUserRegistered;
    private GoalType goalType;
    private SportType sportType;
    private boolean finished;
    private boolean started;
    private String chatId;
    private LocalDate deadline;
    private LocalDate endDate;
}