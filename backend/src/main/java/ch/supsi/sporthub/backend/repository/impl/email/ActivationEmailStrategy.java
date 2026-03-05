package ch.supsi.sporthub.backend.repository.impl.email;

import ch.supsi.sporthub.backend.repository.api.EmailStrategy;

/**
 * Strategy for generating email content for account activation.
 * <p>
 * This class implements {@link EmailStrategy} to provide a subject and HTML content
 * for activation emails sent to users after registration.
 */
public class ActivationEmailStrategy implements EmailStrategy {

    private final String activationLink;

    /**
     * Constructs a new {@code ActivationEmailStrategy} with the given activation link.
     *
     * @param activationLink the URL the user must click to activate their account
     */
    public ActivationEmailStrategy( String activationLink) {
        this.activationLink = activationLink;
    }

    /**
     * Returns the subject line for the activation email.
     *
     * @return the subject as a {@code String}
     */
    @Override
    public String getSubject() {
        return "Activate Your SportHub Account";
    }

    /**
     * Returns the HTML content of the activation email, including the activation link.
     *
     * @return the email body as a {@code String}
     */
    @Override
    public String getContent() {
        return """
            <p>Hi,</p>
            <p>Click the link below to activate your account:</p>
            <p><a href="%s" style="color: #1a73e8; font-weight: bold;">Activate Account</a></p>
            <p>The link will expire in 24 hours.</p>
            <p>SportHub Team</p>
        """.formatted(activationLink);
    }
}