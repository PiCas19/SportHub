package ch.supsi.sporthub.backend.service;

import ch.supsi.sporthub.backend.repository.impl.email.ActivationEmailStrategy;
import ch.supsi.sporthub.backend.repository.impl.email.BotInviteEmailStrategy;
import ch.supsi.sporthub.backend.repository.impl.email.PasswordResetEmailStrategy;
import ch.supsi.sporthub.backend.service.api.IEmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private IEmailService emailService;

    @InjectMocks
    private EmailService emailServiceUnderTest;

    @Test
    void testSendActivationCode() {
        String to = "test@example.com";
        String activationLink = "http://example.com/activate";
        emailServiceUnderTest.sendActivationCode(to, activationLink);
        verify(emailService).sendEmail(eq(to), any(ActivationEmailStrategy.class));
    }

    @Test
    void testSendPasswordResetEmail() {
        String to = "test@example.com";
        String resetLink = "http://example.com/reset";
        emailServiceUnderTest.sendPasswordResetEmail(to, resetLink);
        verify(emailService).sendEmail(eq(to), any(PasswordResetEmailStrategy.class));
    }

    @Test
    void testSendInviteTelegramBot() {
        String to = "test@example.com";
        String inviteLink = "http://example.com/invite";
        emailServiceUnderTest.sendInviteTelegramBot(to, inviteLink);
        verify(emailService).sendEmail(eq(to), any(BotInviteEmailStrategy.class));
    }
}
