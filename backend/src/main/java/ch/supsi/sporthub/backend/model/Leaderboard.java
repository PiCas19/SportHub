package ch.supsi.sporthub.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Entity representing a leaderboard associated with a chat.
 * Tracks scores per participant within a specific chat context.
 */
@Data
@Entity
@NoArgsConstructor
@Table(name = "leaderboard")
public class Leaderboard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String chatId;
    private String name;

    @ElementCollection
    private Map<String, Integer> scores = new HashMap<>();

    public Leaderboard(String chatId, String name) {
        this.chatId = chatId;
        this.name = name;
    }
}