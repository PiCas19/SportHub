package ch.supsi.sporthub.backend.repository.impl.email;

import ch.supsi.sporthub.backend.repository.api.EmailStrategy;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EmailStrategyImplTest {

    @Test
    void testActivationEmailStrategy() {
        String activationLink = "https://sporthub.com/activate?token=12345";
        EmailStrategy strategy = new ActivationEmailStrategy(activationLink);

        assertThat(strategy.getSubject()).isEqualTo("Activate Your SportHub Account");
        assertThat(strategy.getContent()).contains(activationLink);
        assertThat(strategy.getContent()).contains("Activate Account");
    }

    @Test
    void testBotInviteEmailStrategy() {
        String botInviteLink = "https://t.me/sporthub_bot";
        EmailStrategy strategy = new BotInviteEmailStrategy(botInviteLink);

        assertThat(strategy.getSubject()).isEqualTo("Join Our SportHub Bot on Telegram");
        assertThat(strategy.getContent()).contains(botInviteLink);
        assertThat(strategy.getContent()).contains("Join SportHub Bot");
    }

    @Test
    void testPasswordResetEmailStrategy() {
        String resetLink = "https://sporthub.com/reset?token=abcde";
        EmailStrategy strategy = new PasswordResetEmailStrategy(resetLink);

        assertThat(strategy.getSubject()).isEqualTo("SportHub Password Reset Request");
        assertThat(strategy.getContent()).contains(resetLink);
        assertThat(strategy.getContent()).contains("Reset your password");
    }
}
