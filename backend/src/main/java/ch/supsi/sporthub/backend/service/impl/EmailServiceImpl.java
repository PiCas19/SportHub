package ch.supsi.sporthub.backend.service.impl;

import ch.supsi.sporthub.backend.repository.api.EmailStrategy;
import ch.supsi.sporthub.backend.service.api.IEmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

/**
 * Implementation of the IEmailService interface that handles sending emails via a configured mail server.
 * This service uses the JavaMailSender to send emails with dynamic content.
 */
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements IEmailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String fromEmail;

    /**
     * Sends an email to the specified recipient using the provided email strategy.
     *
     * @param to The recipient email address.
     * @param strategy The strategy that defines the email's subject and content.
     * @throws RuntimeException If an error occurs while sending the email, such as a messaging exception or unsupported encoding.
     */
    @Override
    public void sendEmail(String to, EmailStrategy strategy) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(fromEmail,"SportHub Support");
            helper.setTo(to);
            helper.setSubject(strategy.getSubject());
            helper.setText(strategy.getContent(), true);
            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException("Error sending email to " + to, e);
        }
    }
}