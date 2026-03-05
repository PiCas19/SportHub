package ch.supsi.sporthub.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JwtProperties holds the configuration properties related to JWT (JSON Web Token) security.
 * It contains the necessary properties for configuring JWT authentication, such as secret key and expiration times.
 * These properties are fetched from the application's configuration file using the "security.jwt" prefix.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {
    private String secretKey;
    private long expirationTime;
    private long refreshExpirationTime;
    private long resetExpirationTime;
    private long activationExpirationTime;
}