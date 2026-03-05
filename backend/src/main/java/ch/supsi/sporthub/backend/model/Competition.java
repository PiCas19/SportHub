package ch.supsi.sporthub.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


/**
 * Represents a competition entity within the system.
 * A competition includes metadata such as name, participation limits,
 * relevant dates, and Telegram chat information for communication.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "competitions")
public class Competition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int maxParticipants;
    private LocalDate deadline;
    private LocalDate endDate;
    private boolean started = false;
    private boolean finished = false;

    @Column(name = "chat_id", nullable = false)
    private String chatId;
}