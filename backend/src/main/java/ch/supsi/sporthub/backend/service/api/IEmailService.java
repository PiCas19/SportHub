package ch.supsi.sporthub.backend.service.api;

import ch.supsi.sporthub.backend.repository.api.EmailStrategy;

/**
 * Service interface for sending emails using a specified strategy.
 * Allows different types of emails to be composed and sent dynamically.
 */
public interface IEmailService {

    /**
     * Sends an email to the specified recipient using the provided strategy.
     *
     * @param to       the recipient's email address
     * @param strategy the strategy that defines the email's subject and content
     */
    void sendEmail(String to, EmailStrategy strategy);
}