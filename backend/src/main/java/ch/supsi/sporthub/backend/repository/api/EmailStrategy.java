package ch.supsi.sporthub.backend.repository.api;

/**
 * Strategy interface for generating email content.
 * <p>
 * Implementations of this interface define the subject and content
 * of specific types of emails (e.g., account activation, password reset).
 */
public interface EmailStrategy {

    /**
     * Returns the subject line of the email.
     *
     * @return the email subject as a String
     */
    String getSubject();

    /**
     * Returns the main body content of the email.
     *
     * @return the email content as a String
     */
    String getContent();
}