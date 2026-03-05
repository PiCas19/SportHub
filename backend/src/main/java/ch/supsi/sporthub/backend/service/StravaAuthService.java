package ch.supsi.sporthub.backend.service;

import ch.supsi.sporthub.backend.config.StravaProperties;
import ch.supsi.sporthub.backend.model.StravaToken;
import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.repository.jpa.StravaTokenRepository;
import ch.supsi.sporthub.backend.repository.jpa.UserRepository;
import ch.supsi.sporthub.backend.service.api.IBaseApiService;
import ch.supsi.sporthub.backend.service.domain.strava.*;
import ch.supsi.sporthub.backend.service.impl.EncryptionServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.time.Instant;
import java.util.Optional;

/**
 * Service class responsible for handling authentication and token management with the Strava API.
 * This service interacts with the Strava API to obtain, refresh, and validate access tokens and other related actions.
 * It also provides methods for retrieving user and athlete information from Strava.
 */
@Service
public class StravaAuthService {
    private final StravaProperties stravaProperties;
    private final IBaseApiService baseApiService;
    private final StravaTokenRepository stravaTokenRepository;
    private final UserRepository userRepository;
    private final EncryptionServiceImpl encryptionService;

    /**
     * Constructor for the StravaAuthService class.
     * Initializes the service with the provided dependencies.
     *
     * @param stravaProperties         The properties configuration for Strava API.
     * @param baseApiService           The service used for making HTTP requests to the Strava API.
     * @param stravaTokenRepository    Repository for managing Strava tokens.
     * @param userRepository           Repository for managing user data.
     * @param encryptionService        Service for encrypting and decrypting tokens.
     */
    public StravaAuthService(StravaProperties stravaProperties, IBaseApiService baseApiService,
                             StravaTokenRepository stravaTokenRepository, UserRepository userRepository,
                             EncryptionServiceImpl encryptionService) {
        this.stravaProperties = stravaProperties;
        this.baseApiService = baseApiService;
        this.stravaTokenRepository = stravaTokenRepository;
        this.userRepository = userRepository;
        this.encryptionService = encryptionService;
    }

    /**
     * Retrieves the Strava token associated with a specific username.
     *
     * @param username The username of the user whose Strava token is being retrieved.
     * @return An optional Strava token if found.
     */
    private Optional<StravaToken> getStravaToken(String username) {
        return userRepository.findByUsername(username)
                .flatMap(stravaTokenRepository::findByUser);
    }

    /**
     * Checks if the provided Strava token is valid by comparing its expiration time with the current time.
     *
     * @param token The Strava token to be validated.
     * @return true if the token is valid, false otherwise.
     */
    private boolean isTokenValid(StravaToken token) {
        return token != null && Instant.now().isBefore(token.getExpiresAt());
    }

    /**
     * Checks if a user's Strava token is valid.
     *
     * @param username The username whose Strava token is being validated.
     * @return true if the user's token is valid, false otherwise.
     */
    public boolean isUserTokenValid(String username) {
        return getStravaToken(username).map(this::isTokenValid).orElse(false);
    }

    /**
     * Checks if the provided refresh token is valid.
     *
     * @param refreshToken The refresh token to be validated.
     * @return true if the refresh token is valid, false otherwise.
     */
    private boolean isRefreshTokenValid(String refreshToken) {
        return refreshToken != null && !refreshToken.isEmpty();
    }

    /**
     * Finds a user by their Strava athlete ID.
     *
     * @param athleteId The athlete ID to search for.
     * @return An optional User if found.
     */
    public Optional<User> findUserByAthleteId(Long athleteId) {
        Optional<StravaToken> stravaToken = stravaTokenRepository.findByAthleteId(athleteId);
        if (stravaToken.isPresent()) {
            User user = stravaToken.get().getUser();
            return userRepository.findById(user.getId());
        }
        return Optional.empty();
    }

    /**
     * Retrieves a Strava activity by its ID using the Strava API and the provided access token.
     *
     * @param activityId The ID of the Strava activity to fetch.
     * @param accessToken The access token used to authenticate the request.
     * @return The Strava activity corresponding to the provided ID.
     */
    public StravaActivity getActivityById(String activityId, String accessToken) {
        try {
            ResponseEntity<StravaActivity> response = baseApiService.get(
                    stravaProperties.getBaseUrl(),
                    "/activities/" + activityId,
                    encryptionService.decrypt(accessToken),
                    StravaActivity.class
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Failed to fetch activity: " + e.getResponseBodyAsString());
        }
    }

    /**
     * Generates the URL for the Strava authorization page.
     * The URL is used to authorize an app with Strava and obtain an authorization code.
     *
     * @return The authorization URL for Strava.
     */
    public String getAuthorizationUrl() {
        return "https://www.strava.com/oauth/authorize" +
                "?client_id=" + stravaProperties.getClientId() +
                "&response_type=code" +
                "&redirect_uri=" + stravaProperties.getRedirectUri() +
                "&approval_prompt=force" +
                "&scope=" + stravaProperties.getScopes();
    }

    /**
     * Retrieves an access token from Strava using an authorization code.
     * This token is used to authenticate future API requests for the user.
     *
     * @param username The username of the user requesting the token.
     * @param code The authorization code obtained from Strava.
     * @return The Strava token response containing the access and refresh tokens.
     */
    public StravaTokenResponse getAccessToken(String username, String code) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        StravaTokenRequest request = TokenRequestFactory.createAuthorizationCodeRequest(
                stravaProperties.getClientId(), stravaProperties.getSecret(), code
        );

        StravaTokenResponse response = requestToken(request);
        saveOrUpdateStravaToken(user, response);
        return response;
    }

    /**
     * Refreshes the access token for a user using a valid refresh token.
     *
     * @param refreshToken The refresh token used to obtain a new access token.
     * @param username The username of the user requesting the token refresh.
     * @return The refreshed Strava token response containing the new access token.
     */
    public StravaTokenResponse refreshAccessToken(String refreshToken, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        StravaTokenRequest request = TokenRequestFactory.createRefreshTokenRequest(
                stravaProperties.getClientId(), stravaProperties.getSecret(), refreshToken
        );

        StravaTokenResponse response = requestToken(request);

        if (response == null || response.getAccessToken() == null) {
            throw new RuntimeException("Errore durante il refresh del token per utente: " + username);
        }

        if (response.getRefreshToken() == null || response.getRefreshToken().isEmpty()) {
            response.setRefreshToken(refreshToken);
        }

        saveOrUpdateStravaToken(user, response);
        return response;
    }

    /**
     * Refreshes the access token for a user if the current token is expired or invalid.
     *
     * @param username The username of the user whose token needs to be refreshed.
     * @return An optional containing the refreshed access token response if refreshed, otherwise empty.
     */
    public Optional<StravaTokenResponse> refreshAccessTokenIfNeeded(String username) {
        Optional<StravaToken> tokenOpt = getStravaToken(username);
        if (tokenOpt.isPresent()) {
            StravaToken token = tokenOpt.get();

            if (isTokenValid(token)) {
                return Optional.empty();
            }

            String refreshToken = encryptionService.decrypt(token.getRefreshToken());
            if (isRefreshTokenValid(refreshToken)) {
                return Optional.of(refreshAccessToken(refreshToken, username));
            }
        }
        return Optional.empty();
    }

    /**
     * Retrieves a valid access token for a user, refreshing it if necessary.
     *
     * @param username The username of the user for whom the access token is to be retrieved.
     * @return An optional containing the valid access token response, or empty if no valid token is available.
     */
    public Optional<StravaTokenResponse> getValidAccessToken(String username) {
        Optional<StravaToken> tokenOpt = getStravaToken(username);
        if (tokenOpt.isPresent() && isTokenValid(tokenOpt.get())) {
            StravaToken token = tokenOpt.get();

            return Optional.of(new StravaTokenResponse(
                    encryptionService.decrypt(token.getAccessToken()),
                    "Bearer",
                    encryptionService.decrypt(token.getRefreshToken()),
                    (int) (token.getExpiresAt().getEpochSecond() - Instant.now().getEpochSecond()),
                    token.getExpiresAt().getEpochSecond(),
                    getAthleteById(token.getAthleteId())
            ));
        }
        return Optional.empty();
    }

    /**
     * Retrieves a valid refresh token for a user.
     *
     * @param username The username of the user for whom the refresh token is to be retrieved.
     * @return An optional containing the valid refresh token, or empty if no valid refresh token is available.
     */
    public Optional<StravaToken> getValidRefreshToken(String username) {
        Optional<StravaToken> tokenOpt = getStravaToken(username);

        if (tokenOpt.isPresent()) {
            StravaToken token = tokenOpt.get();

            if (token.getRefreshToken() != null && !token.getRefreshToken().isEmpty()) {
                return Optional.of(token);
            }
        }

        return Optional.empty();
    }

    /**
     * Retrieves athlete details from Strava by the athlete ID.
     *
     * @param athleteId The Strava athlete ID.
     * @return The athlete details.
     * @throws RuntimeException if no Strava token is found for the given athlete ID or the token has expired.
     */
    private Athlete getAthleteById(Long athleteId) {
        Optional<StravaToken> tokenOpt = stravaTokenRepository.findByAthleteId(athleteId);
        if (tokenOpt.isEmpty()) {
            throw new RuntimeException("No Strava token found for athlete ID: " + athleteId);
        }

        StravaToken token = tokenOpt.get();
        if (!isTokenValid(token)) {
            throw new RuntimeException("Strava token expired for athlete ID: " + athleteId);
        }

        try {
            ResponseEntity<Athlete> response = baseApiService.get(
                    stravaProperties.getBaseUrl(),
                    "/athlete",
                    encryptionService.decrypt(token.getAccessToken()),
                    Athlete.class
            );

            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Failed to fetch athlete data: " + e.getResponseBodyAsString());
        }
    }

    /**
     * Makes a token request to the Strava API using the provided token request.
     *
     * @param request The Strava token request to be sent to the Strava API.
     * @return The Strava token response containing the access token and other information.
     */
    private StravaTokenResponse requestToken(StravaTokenRequest request) {
        try {
            ResponseEntity<StravaTokenResponse> response = baseApiService.postForm(
                    stravaProperties.getBaseUrl(),
                    "/oauth/token",
                    request.buildRequest(),
                    StravaTokenResponse.class
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Strava API Error: " + e.getResponseBodyAsString());
        }
    }

    /**
     * Saves or updates the Strava token for a given user.
     * This method ensures the latest token information is saved to the database.
     *
     * @param user The user for whom the Strava token is to be saved.
     * @param response The Strava token response containing the latest token data.
     */
    private void saveOrUpdateStravaToken(User user, StravaTokenResponse response) {
        StravaToken token = stravaTokenRepository.findByUser(user).orElse(new StravaToken());
        token.setUser(user);

        if (response.getAthlete() != null) {
            token.setAthleteId(response.getAthlete().getId());
        }

        token.setAccessToken(encryptionService.encrypt(response.getAccessToken()));
        token.setRefreshToken(encryptionService.encrypt(response.getRefreshToken()));
        token.setExpiresAt(Instant.ofEpochSecond(response.getExpiresAt()));
        stravaTokenRepository.save(token);
        stravaTokenRepository.flush();
    }

    /**
     * Removes the Strava token for a given user.
     *
     * @param username The username of the user whose Strava token is to be removed.
     * @return true if the token was removed successfully, false if the token was not found.
     */
    public boolean removeStravaToken(String username) {
        Optional<StravaToken> tokenOpt = getStravaToken(username);
        if (tokenOpt.isPresent()) {
            stravaTokenRepository.delete(tokenOpt.get());
            return true;
        }
        return false;
    }

    /**
     * Retrieves or refreshes the access token for a given user.
     * If the token is expired, it will be refreshed using the refresh token.
     *
     * @param username The username of the user whose access token is to be retrieved or refreshed.
     * @return An optional containing the valid Strava token response, or empty if no token is available.
     */
    public Optional<StravaTokenResponse> getOrRefreshAccessToken(String username) {
        Optional<StravaToken> tokenOpt = getStravaToken(username);
        if (tokenOpt.isPresent()) {
            StravaToken token = tokenOpt.get();
            if (isTokenValid(token)) {
                return Optional.of(buildTokenResponse(token));
            }

            String refreshToken = encryptionService.decrypt(token.getRefreshToken());
            if (isRefreshTokenValid(refreshToken)) {
                return Optional.of(refreshAccessToken(refreshToken, username));
            }
        }
        return Optional.empty();
    }

    /**
     * Builds a Strava token response from the provided token.
     *
     * @param token The Strava token from which the response is to be built.
     * @return The Strava token response containing the access token, refresh token, and expiration details.
     */
    private StravaTokenResponse buildTokenResponse(StravaToken token) {
        return new StravaTokenResponse(
                encryptionService.decrypt(token.getAccessToken()),
                "Bearer",
                encryptionService.decrypt(token.getRefreshToken()),
                (int) (token.getExpiresAt().getEpochSecond() - Instant.now().getEpochSecond()),
                token.getExpiresAt().getEpochSecond(),
                getAthleteById(token.getAthleteId())
        );
    }

}