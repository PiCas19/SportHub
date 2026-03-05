package ch.supsi.sporthub.backend.service;

import ch.supsi.sporthub.backend.config.StravaProperties;
import ch.supsi.sporthub.backend.model.StravaToken;
import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.repository.jpa.StravaTokenRepository;
import ch.supsi.sporthub.backend.repository.jpa.UserRepository;
import ch.supsi.sporthub.backend.service.api.IBaseApiService;
import ch.supsi.sporthub.backend.service.domain.strava.*;

import ch.supsi.sporthub.backend.service.impl.EncryptionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StravaAuthServiceTest {

    private IBaseApiService baseApiService;
    private StravaProperties stravaProperties;
    private StravaTokenRepository stravaTokenRepository;
    private UserRepository userRepository;
    private StravaAuthService authService;
    private EncryptionServiceImpl encryptionService;

    @BeforeEach
    void setup() {
        baseApiService = mock(IBaseApiService.class);
        stravaProperties = mock(StravaProperties.class);
        stravaTokenRepository = mock(StravaTokenRepository.class);
        userRepository = mock(UserRepository.class);
        encryptionService = mock(EncryptionServiceImpl.class);
        authService = new StravaAuthService(stravaProperties, baseApiService, stravaTokenRepository, userRepository, encryptionService);

        when(stravaProperties.getClientId()).thenReturn("client-id");
        when(stravaProperties.getSecret()).thenReturn("secret");
        when(stravaProperties.getRedirectUri()).thenReturn("http://localhost/callback");
        when(stravaProperties.getScopes()).thenReturn("read,activity:write");
        when(stravaProperties.getBaseUrl()).thenReturn("http://mock.strava.api");
    }

    @Test
    void testGetAuthorizationUrl() {
        String url = authService.getAuthorizationUrl();
        assertTrue(url.contains("client_id=client-id"));
        assertTrue(url.contains("scope=read,activity:write"));
    }


    @Test
    void testIsUserTokenValid_TokenValid() {
        User user = new User();
        user.setUsername("john");
        StravaToken token = new StravaToken();
        token.setExpiresAt(Instant.now().plusSeconds(1000));
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(stravaTokenRepository.findByUser(user)).thenReturn(Optional.of(token));

        assertTrue(authService.isUserTokenValid("john"));
    }

    @Test
    void testIsUserTokenValid_NoToken() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());
        assertFalse(authService.isUserTokenValid("john"));
    }

    @Test
    void testGetAccessToken_UserNotFound() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> authService.getAccessToken("john", "code"));
    }


    @Test
    void testRefreshAccessToken_ResponseNull() {
        User user = new User();
        user.setUsername("john");
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(baseApiService.postForm(any(), any(), any(), eq(StravaTokenResponse.class)))
                .thenReturn(null);

        assertThrows(RuntimeException.class, () -> authService.refreshAccessToken("r", "john"));
    }

    @Test
    void testRefreshAccessToken_ResponseAccessTokenNull() {
        User user = new User();
        user.setUsername("john");
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        StravaTokenResponse response = new StravaTokenResponse(null, null, "refresh", 3600, Instant.now().getEpochSecond() + 3600, null);
        when(baseApiService.postForm(any(), any(), any(), eq(StravaTokenResponse.class)))
                .thenReturn(ResponseEntity.ok(response));

        assertThrows(RuntimeException.class, () -> authService.refreshAccessToken("refresh", "john"));
    }


    @Test
    void testRefreshAccessTokenIfNeeded_TokenStillValid() {
        User user = new User();
        user.setUsername("john");
        StravaToken token = new StravaToken();
        token.setRefreshToken("refresh-token");
        token.setExpiresAt(Instant.now().plusSeconds(1000));
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(stravaTokenRepository.findByUser(user)).thenReturn(Optional.of(token));

        Optional<StravaTokenResponse> result = authService.refreshAccessTokenIfNeeded("john");
        assertFalse(result.isPresent());
    }

    @Test
    void testRefreshAccessTokenIfNeeded_NoToken() {
        User user = new User();
        when(userRepository.findByUsername("noone")).thenReturn(Optional.of(user));
        when(stravaTokenRepository.findByUser(user)).thenReturn(Optional.empty());

        Optional<StravaTokenResponse> result = authService.refreshAccessTokenIfNeeded("noone");
        assertFalse(result.isPresent());
    }

    @Test
    void testGetValidRefreshToken_Present() {
        User user = new User();
        user.setUsername("u");
        StravaToken token = new StravaToken();
        token.setRefreshToken("refresh");
        when(userRepository.findByUsername("u")).thenReturn(Optional.of(user));
        when(stravaTokenRepository.findByUser(user)).thenReturn(Optional.of(token));

        assertTrue(authService.getValidRefreshToken("u").isPresent());
    }

    @Test
    void testGetValidRefreshToken_Empty() {
        User user = new User();
        StravaToken token = new StravaToken();
        token.setRefreshToken(null);
        when(userRepository.findByUsername("u")).thenReturn(Optional.of(user));
        when(stravaTokenRepository.findByUser(user)).thenReturn(Optional.of(token));

        assertFalse(authService.getValidRefreshToken("u").isPresent());
    }

    @Test
    void testGetAthleteById_TokenInvalid() throws Exception {
        Long athleteId = 321L;
        StravaToken token = new StravaToken();
        token.setExpiresAt(Instant.now().minusSeconds(10));

        when(stravaTokenRepository.findByAthleteId(athleteId)).thenReturn(Optional.of(token));

        Method method = StravaAuthService.class.getDeclaredMethod("getAthleteById", Long.class);
        method.setAccessible(true);

        InvocationTargetException ex = assertThrows(InvocationTargetException.class, () ->
                method.invoke(authService, athleteId)
        );

        Throwable cause = ex.getCause();
        assertInstanceOf(RuntimeException.class, cause);
        assertEquals("Strava token expired for athlete ID: " + athleteId, cause.getMessage());
    }

    @Test
    void testRequestToken_ThrowsException() throws Exception {
        StravaTokenRequest request = mock(StravaTokenRequest.class);

        MultiValueMap<String, String> mockMap = new LinkedMultiValueMap<>();
        mockMap.add("client_id", "test");

        when(request.buildRequest()).thenReturn(mockMap);

        when(baseApiService.postForm(any(), any(), any(), eq(StravaTokenResponse.class)))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.BAD_REQUEST, "Bad request", null, new byte[0], null
                ));

        Method method = StravaAuthService.class.getDeclaredMethod("requestToken", StravaTokenRequest.class);
        method.setAccessible(true);

        InvocationTargetException ex = assertThrows(InvocationTargetException.class, () ->
                method.invoke(authService, request)
        );

        Throwable cause = ex.getCause();
        assertInstanceOf(RuntimeException.class, cause);
        assertTrue(cause.getMessage().contains("Strava API Error"));
    }


    @Test
    void testRemoveStravaToken_Success() {
        User user = new User();
        user.setUsername("john");
        StravaToken token = new StravaToken();
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(stravaTokenRepository.findByUser(user)).thenReturn(Optional.of(token));

        assertTrue(authService.removeStravaToken("john"));
        verify(stravaTokenRepository).delete(token);
    }

    @Test
    void testRemoveStravaToken_NotFound() {
        when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());
        assertFalse(authService.removeStravaToken("nobody"));
    }

    @Test
    void testPrivate_isTokenValid() throws Exception {
        StravaToken token = new StravaToken();
        token.setExpiresAt(Instant.now().plusSeconds(1000));

        Method method = StravaAuthService.class.getDeclaredMethod("isTokenValid", StravaToken.class);
        method.setAccessible(true);

        assertTrue((boolean) method.invoke(authService, token));
    }

    @Test
    void testPrivate_isRefreshTokenValid() throws Exception {
        Method method = StravaAuthService.class.getDeclaredMethod("isRefreshTokenValid", String.class);
        method.setAccessible(true);

        assertTrue((boolean) method.invoke(authService, "refresh"));
        assertFalse((boolean) method.invoke(authService, ""));
    }

    @Test
    void testPrivate_getStravaToken() throws Exception {
        User user = new User();
        user.setUsername("john");
        StravaToken token = new StravaToken();
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(stravaTokenRepository.findByUser(user)).thenReturn(Optional.of(token));

        Method method = StravaAuthService.class.getDeclaredMethod("getStravaToken", String.class);
        method.setAccessible(true);

        Optional<StravaToken> result = (Optional<StravaToken>) method.invoke(authService, "john");
        assertTrue(result.isPresent());
    }

    @Test
    void testFindUserByAthleteId_Success() {
        Long athleteId = 123L;
        StravaToken token = new StravaToken();
        User user = new User();
        user.setId(1L);
        token.setUser(user);

        when(stravaTokenRepository.findByAthleteId(athleteId)).thenReturn(Optional.of(token));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Optional<User> result = authService.findUserByAthleteId(athleteId);
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    void testFindUserByAthleteId_NotFound() {
        Long athleteId = 123L;
        when(stravaTokenRepository.findByAthleteId(athleteId)).thenReturn(Optional.empty());

        Optional<User> result = authService.findUserByAthleteId(athleteId);
        assertFalse(result.isPresent());
    }

    @Test
    void testFindUserByAthleteId_UserNotFound() {
        Long athleteId = 123L;
        StravaToken token = new StravaToken();
        User user = new User();
        user.setId(1L);
        token.setUser(user);

        when(stravaTokenRepository.findByAthleteId(athleteId)).thenReturn(Optional.of(token));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<User> result = authService.findUserByAthleteId(athleteId);
        assertFalse(result.isPresent());
    }


    @Test
    void testGetValidAccessToken_InvalidToken() {
        User user = new User();
        user.setUsername("john");
        StravaToken token = new StravaToken();
        token.setExpiresAt(Instant.now().minusSeconds(300));

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(stravaTokenRepository.findByUser(user)).thenReturn(Optional.of(token));

        Optional<StravaTokenResponse> result = authService.getValidAccessToken("john");
        assertFalse(result.isPresent());
    }


    @Test
    void testGetAthleteById_TokenNotFound() throws Exception {
        Long athleteId = 123L;
        when(stravaTokenRepository.findByAthleteId(athleteId)).thenReturn(Optional.empty());

        Method method = StravaAuthService.class.getDeclaredMethod("getAthleteById", Long.class);
        method.setAccessible(true);

        InvocationTargetException ex = assertThrows(InvocationTargetException.class, () ->
                method.invoke(authService, athleteId));

        Throwable cause = ex.getCause();
        assertInstanceOf(RuntimeException.class, cause);
        assertEquals("No Strava token found for athlete ID: " + athleteId, cause.getMessage());
    }

    @Test
    void testIsTokenValid_True() throws Exception {
        StravaToken token = new StravaToken();
        token.setExpiresAt(Instant.now().plusSeconds(300));

        Method method = StravaAuthService.class.getDeclaredMethod("isTokenValid", StravaToken.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(authService, token);
        assertTrue(result);
    }

    @Test
    void testIsTokenValid_False_NullToken() throws Exception {
        Method method = StravaAuthService.class.getDeclaredMethod("isTokenValid", StravaToken.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(authService, (Object) null);
        assertFalse(result);
    }

    @Test
    void testIsTokenValid_False_Expired() throws Exception {
        StravaToken token = new StravaToken();
        token.setExpiresAt(Instant.now().minusSeconds(300));

        Method method = StravaAuthService.class.getDeclaredMethod("isTokenValid", StravaToken.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(authService, token);
        assertFalse(result);
    }

    @Test
    void testIsRefreshTokenValid_True() throws Exception {
        Method method = StravaAuthService.class.getDeclaredMethod("isRefreshTokenValid", String.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(authService, "validRefreshToken");
        assertTrue(result);
    }

    @Test
    void testIsRefreshTokenValid_False_Null() throws Exception {
        Method method = StravaAuthService.class.getDeclaredMethod("isRefreshTokenValid", String.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(authService, (Object) null);
        assertFalse(result);
    }

    @Test
    void testIsRefreshTokenValid_False_Empty() throws Exception {
        Method method = StravaAuthService.class.getDeclaredMethod("isRefreshTokenValid", String.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(authService, "");
        assertFalse(result);
    }

    @Test
    void testRefreshAccessToken_ResponseNull_Throws() {
        User user = new User();
        user.setUsername("john");

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(baseApiService.postForm(any(), any(), any(), eq(StravaTokenResponse.class))).thenReturn(null);

        assertThrows(RuntimeException.class, () -> authService.refreshAccessToken("refresh", "john"));
    }

    @Test
    void testRefreshAccessToken_ResponseAccessTokenNull_Throws() {
        User user = new User();
        user.setUsername("john");

        StravaTokenResponse response = new StravaTokenResponse(null, null, "refresh", 3600, Instant.now().getEpochSecond() + 3600, null);

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(baseApiService.postForm(any(), any(), any(), eq(StravaTokenResponse.class))).thenReturn(ResponseEntity.ok(response));

        assertThrows(RuntimeException.class, () -> authService.refreshAccessToken("refresh", "john"));
    }

    @Test
    void testGetValidAccessToken_TokenInvalid_ReturnsEmpty() {
        User user = new User();
        user.setUsername("john");
        StravaToken token = new StravaToken();
        token.setExpiresAt(Instant.now().minusSeconds(300));

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(stravaTokenRepository.findByUser(user)).thenReturn(Optional.of(token));

        Optional<StravaTokenResponse> result = authService.getValidAccessToken("john");
        assertFalse(result.isPresent());
    }

    @Test
    void testGetValidRefreshToken_ValidTokenPresent() {
        User user = new User();
        user.setUsername("john");
        StravaToken token = new StravaToken();
        token.setRefreshToken("refresh");

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(stravaTokenRepository.findByUser(user)).thenReturn(Optional.of(token));

        Optional<StravaToken> result = authService.getValidRefreshToken("john");
        assertTrue(result.isPresent());
    }

    @Test
    void testGetValidRefreshToken_TokenMissingOrInvalid() {
        User user = new User();
        user.setUsername("john");
        StravaToken token = new StravaToken();
        token.setRefreshToken(null);

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(stravaTokenRepository.findByUser(user)).thenReturn(Optional.of(token));

        Optional<StravaToken> result = authService.getValidRefreshToken("john");
        assertFalse(result.isPresent());
    }

    @Test
    void testRefreshAccessToken_ResponseBodyIsNull() {
        User user = new User();
        user.setUsername("john");
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        ResponseEntity<StravaTokenResponse> mockResponse = mock(ResponseEntity.class);
        when(mockResponse.getBody()).thenReturn(null);
        when(baseApiService.postForm(any(), any(), any(), eq(StravaTokenResponse.class)))
                .thenReturn(mockResponse);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                authService.refreshAccessToken("refresh-token", "john")
        );

        assertTrue(ex.getMessage().contains("Errore durante il refresh del token"));
    }


    @Test
    void testGetValidAccessToken_TokenNotPresent() {
        User user = new User();
        user.setUsername("john");

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(stravaTokenRepository.findByUser(user)).thenReturn(Optional.empty());

        Optional<StravaTokenResponse> result = authService.getValidAccessToken("john");

        assertFalse(result.isPresent());
    }

    @Test
    void testRefreshAccessTokenIfNeeded_TokenNotFoundForUser() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(new User()));
        when(stravaTokenRepository.findByUser(any())).thenReturn(Optional.empty());

        Optional<StravaTokenResponse> result = authService.refreshAccessTokenIfNeeded("john");

        assertFalse(result.isPresent());
    }

    @Test
    void testGetActivityById_Success() {
        String activityId = "12345";
        String accessToken = "validAccessToken";
        StravaActivity mockActivity = new StravaActivity();
        mockActivity.setId(12345L);
        when(encryptionService.decrypt(accessToken)).thenReturn("decryptedAccessToken");
        when(baseApiService.get(
                eq(stravaProperties.getBaseUrl()),
                eq("/activities/" + activityId),
                eq("decryptedAccessToken"),
                eq(StravaActivity.class)
        )).thenReturn(ResponseEntity.ok(mockActivity));
        StravaActivity result = authService.getActivityById(activityId, accessToken);
        assertNotNull(result);
        assertEquals(mockActivity.getId(), result.getId());
    }

    @Test
    void testGetActivityById_Failure() {
        String activityId = "12345";
        String accessToken = "validAccessToken";
        String decryptedAccessToken = "decryptedAccessToken";

        when(encryptionService.decrypt(accessToken)).thenReturn(decryptedAccessToken);

        when(baseApiService.get(
                eq(stravaProperties.getBaseUrl()),
                eq("/activities/" + activityId),
                eq(decryptedAccessToken),
                eq(StravaActivity.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.getActivityById(activityId, accessToken);
        });
        assertTrue(exception.getMessage().contains("Failed to fetch activity"));
    }

    @Test
    void testGetAccessToken_Success() {
        String username = "john";
        String code = "authcode";
        User user = new User();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        StravaTokenResponse mockResponse = new StravaTokenResponse("accessToken", "Bearer", "refreshToken", 3600,
                Instant.now().getEpochSecond() + 3600, null);
        when(baseApiService.postForm(any(), any(), any(), eq(StravaTokenResponse.class)))
                .thenReturn(ResponseEntity.ok(mockResponse));
        StravaTokenResponse result = authService.getAccessToken(username, code);
        verify(baseApiService).postForm(any(), any(), any(), eq(StravaTokenResponse.class));
        verify(stravaTokenRepository).save(any());
        verify(stravaTokenRepository).flush();
        assertEquals("accessToken", result.getAccessToken());
        assertEquals("refreshToken", result.getRefreshToken());
    }

    @Test
    void testRefreshAccessToken_NullAccessToken() {
        String username = "john";
        User user = new User();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        StravaTokenResponse mockResponse = new StravaTokenResponse(null, "Bearer", "refreshToken", 3600,
                Instant.now().getEpochSecond() + 3600, null);
        when(baseApiService.postForm(any(), any(), any(), eq(StravaTokenResponse.class)))
                .thenReturn(ResponseEntity.ok(mockResponse));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.refreshAccessToken("refreshToken", username);
        });

        assertTrue(exception.getMessage().contains("Errore durante il refresh del token per utente: john"));
    }


    @Test
    void testRefreshAccessToken_ValidResponse() {
        String username = "john";
        User user = new User();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        StravaTokenResponse mockResponse = new StravaTokenResponse("accessToken", "Bearer", "refreshToken", 3600,
                Instant.now().getEpochSecond() + 3600, null);
        when(baseApiService.postForm(any(), any(), any(), eq(StravaTokenResponse.class)))
                .thenReturn(ResponseEntity.ok(mockResponse));
        StravaTokenResponse result = authService.refreshAccessToken("refreshToken", username);
        verify(stravaTokenRepository).save(any());
        verify(stravaTokenRepository).flush();
        assertEquals("accessToken", result.getAccessToken());
        assertEquals("refreshToken", result.getRefreshToken());
    }

    @Test
    void testRefreshAccessToken_NoChangeInRefreshToken() {
        String username = "john";
        User user = new User();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        StravaTokenResponse mockResponse = new StravaTokenResponse("accessToken", "Bearer", "existingRefreshToken", 3600,
                Instant.now().getEpochSecond() + 3600, null);
        when(baseApiService.postForm(any(), any(), any(), eq(StravaTokenResponse.class)))
                .thenReturn(ResponseEntity.ok(mockResponse));
        StravaTokenResponse result = authService.refreshAccessToken("refreshToken", username);
        assertEquals("existingRefreshToken", result.getRefreshToken());
        verify(stravaTokenRepository).save(any());
        verify(stravaTokenRepository).flush();
    }


    @Test
    void testRefreshAccessToken_SetRefreshToken() {
        String username = "john";
        User user = new User();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        StravaTokenResponse mockResponse = new StravaTokenResponse("accessToken", "Bearer", null, 3600,
                Instant.now().getEpochSecond() + 3600, null);
        when(baseApiService.postForm(any(), any(), any(), eq(StravaTokenResponse.class)))
                .thenReturn(ResponseEntity.ok(mockResponse));
        StravaTokenResponse result = authService.refreshAccessToken("refreshToken", username);
        assertEquals("refreshToken", result.getRefreshToken());
        verify(stravaTokenRepository).save(any());
        verify(stravaTokenRepository).flush();
    }

    @Test
    void testRefreshAccessToken_EmptyRefreshToken() {
        String username = "john";
        User user = new User();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        StravaTokenResponse mockResponse = new StravaTokenResponse("accessToken", "Bearer", "", 3600,
                Instant.now().getEpochSecond() + 3600, null);
        when(baseApiService.postForm(any(), any(), any(), eq(StravaTokenResponse.class)))
                .thenReturn(ResponseEntity.ok(mockResponse));
        StravaTokenResponse result = authService.refreshAccessToken("refreshToken", username);
        assertEquals("refreshToken", result.getRefreshToken());
        verify(stravaTokenRepository).save(any());
        verify(stravaTokenRepository).flush();
    }

    @Test
    void testBuildTokenResponse() throws Exception {
        StravaToken token = new StravaToken();
        token.setAccessToken("encryptedAccessToken");
        token.setRefreshToken("encryptedRefreshToken");
        token.setExpiresAt(Instant.now().plusSeconds(3600));
        token.setAthleteId(123L);
        when(encryptionService.decrypt("encryptedAccessToken")).thenReturn("accessToken");
        when(encryptionService.decrypt("encryptedRefreshToken")).thenReturn("refreshToken");
        when(stravaTokenRepository.findByAthleteId(123L)).thenReturn(Optional.of(token));

        Athlete mockAthlete = new Athlete();
        mockAthlete.setId(123L);
        ResponseEntity<Athlete> mockResponse = ResponseEntity.ok(mockAthlete);
        when(baseApiService.get(eq(stravaProperties.getBaseUrl()), eq("/athlete"), anyString(), eq(Athlete.class)))
                .thenReturn(mockResponse);
        Method buildTokenResponseMethod = StravaAuthService.class.getDeclaredMethod("buildTokenResponse", StravaToken.class);
        buildTokenResponseMethod.setAccessible(true);
        StravaTokenResponse response = (StravaTokenResponse) buildTokenResponseMethod.invoke(authService, token);
        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("refreshToken", response.getRefreshToken());
        assertEquals(3600, response.getExpiresIn());
        assertEquals(Instant.now().plusSeconds(3600).getEpochSecond(), response.getExpiresAt());
        assertEquals(mockAthlete, response.getAthlete());
    }

    @Test
    void testGetOrRefreshAccessToken_NoToken() {
        String username = "john";
        when(stravaTokenRepository.findByUser(any(User.class))).thenReturn(Optional.empty());
        Optional<StravaTokenResponse> result = authService.getOrRefreshAccessToken(username);
        assertFalse(result.isPresent());
    }

    @Test
    void testGetValidAccessToken_TokenPresentAndValid_WithReflectionFixed() {
        String username = "testuser";
        User user = new User();
        user.setUsername(username);

        StravaToken stravaToken = new StravaToken();
        stravaToken.setAccessToken("encryptedAccessToken");
        stravaToken.setRefreshToken("encryptedRefreshToken");
        stravaToken.setExpiresAt(Instant.now().plusSeconds(3600));
        stravaToken.setAthleteId(123L);

        Athlete mockAthlete = new Athlete();
        mockAthlete.setId(123L);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(stravaTokenRepository.findByUser(user)).thenReturn(Optional.of(stravaToken));
        when(stravaTokenRepository.findByAthleteId(123L)).thenReturn(Optional.of(stravaToken));

        when(encryptionService.decrypt("encryptedAccessToken")).thenReturn("decryptedAccessToken");
        when(encryptionService.decrypt("encryptedRefreshToken")).thenReturn("decryptedRefreshToken");
        when(baseApiService.get(
                eq(stravaProperties.getBaseUrl()),
                eq("/athlete"),
                eq("decryptedAccessToken"),
                eq(Athlete.class)
        )).thenReturn(ResponseEntity.ok(mockAthlete));

        Optional<StravaTokenResponse> result = authService.getValidAccessToken(username);

        assertTrue(result.isPresent());
        StravaTokenResponse response = result.get();
        assertEquals("decryptedAccessToken", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("decryptedRefreshToken", response.getRefreshToken());
        assertTrue(response.getExpiresIn() > 0);
        assertEquals(stravaToken.getExpiresAt().getEpochSecond(), response.getExpiresAt());
        assertEquals(mockAthlete, response.getAthlete());

        verify(userRepository, times(1)).findByUsername(username);
        verify(stravaTokenRepository, times(1)).findByUser(user);
        verify(stravaTokenRepository, times(1)).findByAthleteId(123L);
        verify(encryptionService, times(2)).decrypt("encryptedAccessToken");
        verify(encryptionService, times(1)).decrypt("encryptedRefreshToken");
        verify(baseApiService, times(1)).get(
                eq(stravaProperties.getBaseUrl()),
                eq("/athlete"),
                eq("decryptedAccessToken"),
                eq(Athlete.class)
        );
    }

    @Test
    void testGetValidAccessToken_TokenExpired_RefreshTokenInvalid() {
        User user = new User();
        user.setUsername("expiredUser");

        StravaToken token = new StravaToken();
        token.setExpiresAt(Instant.now().minusSeconds(1000));
        token.setRefreshToken("encryptedRefreshToken");

        when(userRepository.findByUsername("expiredUser")).thenReturn(Optional.of(user));
        when(stravaTokenRepository.findByUser(user)).thenReturn(Optional.of(token));
        when(encryptionService.decrypt("encryptedRefreshToken")).thenReturn("");

        Optional<StravaTokenResponse> result = authService.getValidAccessToken("expiredUser");

        assertFalse(result.isPresent());
    }

    @Test
    void testGetAthleteById_HttpClientErrorException() throws Exception {
        Long athleteId = 123L;
        StravaToken token = new StravaToken();
        token.setAccessToken("encryptedAccessToken");
        token.setExpiresAt(Instant.now().plusSeconds(3600));

        when(stravaTokenRepository.findByAthleteId(athleteId)).thenReturn(Optional.of(token));
        when(encryptionService.decrypt("encryptedAccessToken")).thenReturn("decryptedAccessToken");
        when(baseApiService.get(
                eq(stravaProperties.getBaseUrl()),
                eq("/athlete"),
                eq("decryptedAccessToken"),
                eq(Athlete.class)
        )).thenThrow(HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST, "Bad Request", null, new byte[0], null
        ));

        Method method = StravaAuthService.class.getDeclaredMethod("getAthleteById", Long.class);
        method.setAccessible(true);

        InvocationTargetException ex = assertThrows(InvocationTargetException.class, () ->
                method.invoke(authService, athleteId)
        );

        Throwable cause = ex.getCause();
        assertInstanceOf(RuntimeException.class, cause);
        assertTrue(cause.getMessage().contains("Failed to fetch athlete data"));
    }


    @Test
    void testBuildTokenResponse_NullAthlete() throws Exception {
        StravaToken token = new StravaToken();
        token.setAccessToken("encryptedAccessToken");
        token.setRefreshToken("encryptedRefreshToken");
        token.setExpiresAt(Instant.now().plusSeconds(3600));
        token.setAthleteId(123L);

        when(encryptionService.decrypt("encryptedAccessToken")).thenReturn("accessToken");
        when(encryptionService.decrypt("encryptedRefreshToken")).thenReturn("refreshToken");
        when(stravaTokenRepository.findByAthleteId(123L)).thenReturn(Optional.of(token));

        when(baseApiService.get(
                eq(stravaProperties.getBaseUrl()),
                eq("/athlete"),
                eq("accessToken"),
                eq(Athlete.class)
        )).thenReturn(ResponseEntity.ok(null));

        Method buildTokenResponseMethod = StravaAuthService.class.getDeclaredMethod("buildTokenResponse", StravaToken.class);
        buildTokenResponseMethod.setAccessible(true);
        StravaTokenResponse response = (StravaTokenResponse) buildTokenResponseMethod.invoke(authService, token);

        assertNotNull(response);
        assertNull(response.getAthlete());
    }

    @Test
    void testGetOrRefreshAccessToken_TokenPresentButInvalidRefreshTokenInvalid() {
        User user = new User();
        user.setUsername("expiredUser");

        StravaToken token = new StravaToken();
        token.setExpiresAt(Instant.now().minusSeconds(1000));
        token.setRefreshToken("encryptedRefreshToken");

        when(userRepository.findByUsername("expiredUser")).thenReturn(Optional.of(user));
        when(stravaTokenRepository.findByUser(user)).thenReturn(Optional.of(token));
        when(encryptionService.decrypt("encryptedRefreshToken")).thenReturn("");

        Optional<StravaTokenResponse> result = authService.getOrRefreshAccessToken("expiredUser");

        assertFalse(result.isPresent());
    }

    @Test
    void testRefreshAccessTokenIfNeeded_TokenExpired_RefreshSuccess() {
        User user = new User();
        user.setUsername("john");

        StravaToken expiredToken = new StravaToken();
        expiredToken.setRefreshToken("encryptedRefreshToken");
        expiredToken.setExpiresAt(Instant.now().minusSeconds(1000));

        StravaTokenResponse refreshedTokenResponse = new StravaTokenResponse(
                "newAccessToken", "Bearer", "newRefreshToken", 3600, Instant.now().plusSeconds(3600).getEpochSecond(), null
        );

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(stravaTokenRepository.findByUser(user)).thenReturn(Optional.of(expiredToken));
        when(encryptionService.decrypt("encryptedRefreshToken")).thenReturn("validRefreshToken");

        when(baseApiService.postForm(
                eq(stravaProperties.getBaseUrl()),
                eq("/oauth/token"),
                any(),
                eq(StravaTokenResponse.class))
        ).thenReturn(ResponseEntity.ok(refreshedTokenResponse));

        Optional<StravaTokenResponse> result = authService.refreshAccessTokenIfNeeded("john");

        assertTrue(result.isPresent());
        assertEquals("newAccessToken", result.get().getAccessToken());
    }

    @Test
    void testRefreshAccessTokenIfNeeded_RefreshTokenInvalid() {
        User user = new User();
        user.setUsername("john");

        StravaToken expiredToken = new StravaToken();
        expiredToken.setRefreshToken("encryptedInvalidRefreshToken");
        expiredToken.setExpiresAt(Instant.now().minusSeconds(1000));

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(stravaTokenRepository.findByUser(user)).thenReturn(Optional.of(expiredToken));
        when(encryptionService.decrypt("encryptedInvalidRefreshToken")).thenReturn("");

        Optional<StravaTokenResponse> result = authService.refreshAccessTokenIfNeeded("john");

        assertFalse(result.isPresent());
    }

    @Test
    void testGetValidRefreshToken_TokenEmptyRefreshToken() {
        User user = new User();
        user.setUsername("john");

        StravaToken token = new StravaToken();
        token.setRefreshToken("");

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(stravaTokenRepository.findByUser(user)).thenReturn(Optional.of(token));

        Optional<StravaToken> result = authService.getValidRefreshToken("john");

        assertFalse(result.isPresent());
    }

    @Test
    void testRefreshAccessTokenIfNeeded_TokenValid_BranchTrue() {
        String username = "john";
        User user = new User();
        user.setUsername(username);

        StravaToken token = new StravaToken();
        token.setExpiresAt(Instant.now().plusSeconds(3600));

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(stravaTokenRepository.findByUser(user)).thenReturn(Optional.of(token));

        Optional<StravaTokenResponse> result = authService.refreshAccessTokenIfNeeded(username);

        assertFalse(result.isPresent());
    }


    @Test
    void testSaveOrUpdateStravaToken_AthletePresent_BranchTrue() throws Exception {
        User user = new User();
        user.setUsername("john");

        Athlete athlete = new Athlete();
        athlete.setId(123L);

        StravaTokenResponse response = new StravaTokenResponse(
                "accessToken", "Bearer", "refreshToken",
                3600, Instant.now().plusSeconds(3600).getEpochSecond(), athlete
        );

        StravaToken token = new StravaToken();
        when(stravaTokenRepository.findByUser(user)).thenReturn(Optional.of(token));
        when(encryptionService.encrypt("accessToken")).thenReturn("encryptedAccessToken");
        when(encryptionService.encrypt("refreshToken")).thenReturn("encryptedRefreshToken");

        Method saveMethod = StravaAuthService.class.getDeclaredMethod("saveOrUpdateStravaToken", User.class, StravaTokenResponse.class);
        saveMethod.setAccessible(true);
        saveMethod.invoke(authService, user, response);

        assertEquals(123L, token.getAthleteId());
        verify(stravaTokenRepository).save(token);
        verify(stravaTokenRepository).flush();
    }


}
