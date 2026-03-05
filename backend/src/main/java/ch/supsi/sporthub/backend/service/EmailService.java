package ch.supsi.sporthub.backend.service;

import ch.supsi.sporthub.backend.repository.impl.email.ActivationEmailStrategy;
import ch.supsi.sporthub.backend.repository.impl.email.BotInviteEmailStrategy;
import ch.supsi.sporthub.backend.repository.impl.email.PasswordResetEmailStrategy;
import ch.supsi.sporthub.backend.service.api.IEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service class that handles sending different types of emails using predefined strategies.
 * This class utilizes the IEmailService to send emails based on specific strategies such as activation, password reset,
 * and Telegram bot invitation.
 */
@Service
@RequiredArgsConstructor
public class EmailService {
    private final IEmailService emailService;

    /**
     * Sends an activation code email to the specified recipient.
     * Uses the ActivationEmailStrategy to generate the email content and sends it via the email service.
     *
     * @param to            The recipient's email address.
     * @param activationLink The activation link to be included in the email.
     */
    public void sendActivationCode(String to, String activationLink) {
        emailService.sendEmail(to, new ActivationEmailStrategy(activationLink));
    }

    /**
     * Sends a password reset email to the specified recipient.
     * Uses the PasswordResetEmailStrategy to generate the email content and sends it via the email service.
     *
     * @param to            The recipient's email address.
     * @param resetLink     The password reset link to be included in the email.
     */
    public void sendPasswordResetEmail(String to, String resetLink) {
        emailService.sendEmail(to, new PasswordResetEmailStrategy(resetLink));
    }

    /**
     * Sends an invitation to a Telegram bot via email to the specified recipient.
     * Uses the BotInviteEmailStrategy to generate the invitation content and sends it via the email service.
     *
     * @param to            The recipient's email address.
     * @param inviteLink    The invitation link for the Telegram bot to be included in the email.
     */
    public  void sendInviteTelegramBot(String to, String inviteLink) {
        emailService.sendEmail(to, new BotInviteEmailStrategy(inviteLink));
    }

}