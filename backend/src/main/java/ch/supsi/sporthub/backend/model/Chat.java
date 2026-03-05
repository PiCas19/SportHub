package ch.supsi.sporthub.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a Telegram chat associated with a user.
 * Each chat is linked to a specific user and contains metadata
 * such as chat ID, type, and the Telegram username.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "chats")
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String chatId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private
    ChatType chatType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String telegramUserName;
}