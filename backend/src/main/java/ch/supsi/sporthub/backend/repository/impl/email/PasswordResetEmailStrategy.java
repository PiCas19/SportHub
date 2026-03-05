package ch.supsi.sporthub.backend.repository.impl.email;

import ch.supsi.sporthub.backend.repository.api.EmailStrategy;


/**
 * Strategy for generating email content to reset a user's password in SportHub.
 * <p>
 * This implementation of {@link EmailStrategy} provides the subject and body of
 * a password reset email, including a time-limited reset link.
 */
public class PasswordResetEmailStrategy implements EmailStrategy {
    private final String resetLink;

    /**
     * Constructs a new {@code PasswordResetEmailStrategy} with the given reset link.
     *
     * @param resetLink the URL that allows the user to reset their password
     */
    public PasswordResetEmailStrategy(String resetLink) {
        this.resetLink = resetLink;
    }

    /**
     * Returns the subject line of the password reset email.
     *
     * @return a {@code String} containing the email subject
     */
    @Override
    public String getSubject() {
        return "SportHub Password Reset Request";
    }

    /**
     * Returns the HTML content of the password reset email, including the reset link.
     *
     * @return a {@code String} containing the email body in HTML format
     */
    @Override
    public String getContent() {
        return """
            <p>Hi,</p>
            <p>We received a request to reset your password.</p>
            <p>Click on the link below to proceed:</p>
            <p><a href="%s" style="color: #1a73e8; font-weight: bold;">Reset your password</a></p>
            <p>If you have not requested a reset, ignore this email.</p>
            <p>The link will expire in 10 minutes.</p>
            <p>Thank you,<br>SportHub Team</p>
        """.formatted(resetLink);
    }
}