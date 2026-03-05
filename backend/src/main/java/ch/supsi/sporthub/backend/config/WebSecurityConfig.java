package ch.supsi.sporthub.backend.config;

import ch.supsi.sporthub.backend.security.JwtAuthenticationFilter;
import ch.supsi.sporthub.backend.service.CustomUserDetailService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * WebSecurityConfig configures the security settings for the application,
 * including HTTP request authorization, CORS configuration, JWT authentication, and session management.
 * This class integrates Spring Security to secure the application's endpoints and handle user authentication.
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private final CustomUserDetailService customUserDetailService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Constructs an instance of WebSecurityConfig with the necessary services.
     *
     * @param customUserDetailService The service for loading user details for authentication.
     * @param jwtAuthenticationFilter The filter responsible for JWT-based authentication.
     */
    public WebSecurityConfig(CustomUserDetailService customUserDetailService, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.customUserDetailService = customUserDetailService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }


    /**
     * Configures the HTTP security settings, including CORS, CSRF, session management, and request authorization.
     * It defines which routes are public, which require authentication, and sets up the stateless session policy.
     *
     * @param http The HttpSecurity instance used for configuring security settings.
     * @return The SecurityFilterChain with the defined security settings.
     * @throws Exception If any error occurs during the configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/auth/login", "/auth/register", "/auth/refresh", "/auth/activate", "/auth/forgot-password", "/auth/reset-password", "/api/telegram/update", "/api/strava/webhook", "/auth/validate-token", "/api/map/**", "/api/simulation/**").permitAll()
                        .requestMatchers("/auth/logout").authenticated()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Configures the authentication provider used to authenticate users via username and password.
     * It integrates the custom user details service and BCrypt password encoder for user authentication.
     *
     * @return The AuthenticationProvider instance for authenticating users.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Configures the password encoder used to encode and verify passwords.
     * This method uses BCryptPasswordEncoder, a secure hashing algorithm for password storage.
     *
     * @return The PasswordEncoder instance used for encoding passwords.
     */
    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the AuthenticationManager bean used for authentication in the application.
     *
     * @param config The AuthenticationConfiguration instance used to configure authentication settings.
     * @return The AuthenticationManager instance.
     * @throws Exception If an error occurs while getting the AuthenticationManager.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Configures the CORS (Cross-Origin Resource Sharing) settings for the application.
     * This allows specific origins to make requests to the server, enabling access from the frontend application.
     *
     * @return The CorsConfigurationSource instance that defines the CORS configuration.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://192.168.1.112:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization", "Set-Cookie"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}