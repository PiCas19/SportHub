package ch.supsi.sporthub.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * StravaProperties holds the configuration properties related to the Strava client integration.
 * These properties are used for interacting with the Strava API, handling authentication, and webhook verification.
 * The properties are fetched from the application's configuration file using the "strava.client" prefix.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "strava.client")
public class StravaProperties {
    private String clientId;
    private String secret;
    private String redirectUri;
    private String scopes;
    private String baseUrl;
    private String webhookVerifyToken;
    private String jasyptEncryptorPassword;
}