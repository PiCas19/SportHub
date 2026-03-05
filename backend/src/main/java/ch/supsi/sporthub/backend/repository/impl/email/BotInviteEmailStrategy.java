package ch.supsi.sporthub.backend.repository.impl.email;

import ch.supsi.sporthub.backend.repository.api.EmailStrategy;

/**
 * Strategy for generating email content to invite users to join the SportHub Bot on Telegram.
 * <p>
 * Implements {@link EmailStrategy} to provide a subject and HTML content for the bot invitation email.
 */
public class BotInviteEmailStrategy implements EmailStrategy {

    private final String botInviteLink;

    /**
     * Constructs a new {@code BotInviteEmailStrategy} with the specified invite link.
     *
     * @param botInviteLink the Telegram bot invitation URL
     */
    public BotInviteEmailStrategy(String botInviteLink) {
        this.botInviteLink = botInviteLink;
    }

    /**
     * Returns the subject line for the bot invitation email.
     *
     * @return the subject as a {@code String}
     */
    @Override
    public String getSubject() {
        return "Join Our SportHub Bot on Telegram";
    }

    /**
     * Returns the HTML content of the bot invitation email, including the invitation link.
     *
     * @return the email body as a {@code String}
     */
    @Override
    public String getContent() {
        return """
            <p>Hi,</p>
            <p>Click the link below to start chatting with our SportHub Bot on Telegram:</p>
            <p><a href="%s" style="color: #1a73e8; font-weight: bold;">Join SportHub Bot</a></p>
            <p>SportHub Team</p>
        """.formatted(botInviteLink);
    }
}