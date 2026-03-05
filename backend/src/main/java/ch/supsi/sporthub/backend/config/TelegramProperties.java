package ch.supsi.sporthub.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * TelegramProperties holds the configuration properties related to the Telegram bot integration.
 * These properties are used for interacting with the Telegram Bot API, such as sending messages and setting up bot details.
 * The properties are fetched from the application's configuration file using the "telegram" prefix.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "telegram")
public class TelegramProperties {
    private String baseUrl;
    private String botToken;
    private String botUsername;
    private String botUrlLink;
}