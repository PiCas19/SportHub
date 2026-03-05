package ch.supsi.sporthub.backend.service.impl;

import ch.supsi.sporthub.backend.repository.api.EmailStrategy;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.UnsupportedEncodingException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class EmailServiceImplTest {
    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @Mock
    private EmailStrategy emailStrategy;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@sporthub.com");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(emailStrategy.getSubject()).thenReturn("Test Subject");
        when(emailStrategy.getContent()).thenReturn("Test Content");
    }

    @Test
    void testSendEmail_success() {
        String recipient = "test@example.com";
        emailService.sendEmail(recipient, emailStrategy);
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }


    @Test
    void testSendEmail_unsupportedEncodingException_setFrom() {
        String recipient = "test@example.com";
        UnsupportedEncodingException expectedException = new UnsupportedEncodingException("Simulated Unsupported Encoding Exception");

        try (MockedConstruction<MimeMessageHelper> mockedConstruction = mockConstruction(MimeMessageHelper.class, (mock, context) -> {
            if (context.arguments().get(0) == mimeMessage && (Boolean) context.arguments().get(1)) {
                try {
                    doThrow(expectedException).when(mock).setFrom(anyString(), anyString());
                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }
            }
        })) {
            RuntimeException actualException = assertThrows(RuntimeException.class,
                    () -> emailService.sendEmail(recipient, emailStrategy));

            assertEquals("Error sending email to " + recipient, actualException.getMessage());
            assertEquals(expectedException, actualException.getCause());
            verify(mailSender).createMimeMessage();
            verify(mailSender, never()).send((MimeMessage) any());
        }
    }
}