package ch.supsi.sporthub.backend.service.domain.strava;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.MultiValueMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class TokenRequestFactoryTest {

    @Test
    void testCreateAuthorizationCodeRequest() {
        String clientId = "test-client-id";
        String clientSecret = "test-client-secret";
        String code = "test-code";
        StravaTokenRequest request = TokenRequestFactory.createAuthorizationCodeRequest(clientId, clientSecret, code);
        assertTrue(request instanceof StravaAuthorizationCodeTokenRequest);
        assertEquals("authorization_code", request.getGrantType());

        MultiValueMap<String, String> requestBody = request.buildRequest();
        assertEquals(clientId, requestBody.getFirst("client_id"));
        assertEquals(clientSecret, requestBody.getFirst("client_secret"));
        assertEquals(code, requestBody.getFirst("code"));
    }

    @Test
    void testCreateRefreshTokenRequest() {
        String clientId = "test-client-id";
        String clientSecret = "test-client-secret";
        String refreshToken = "test-refresh-token";
        StravaTokenRequest request = TokenRequestFactory.createRefreshTokenRequest(clientId, clientSecret, refreshToken);
        assertTrue(request instanceof StravaRefreshTokenRequest);
        assertEquals("refresh_token", request.getGrantType());
        MultiValueMap<String, String> requestBody = request.buildRequest();
        assertEquals(clientId, requestBody.getFirst("client_id"));
        assertEquals(clientSecret, requestBody.getFirst("client_secret"));
        assertEquals(refreshToken, requestBody.getFirst("refresh_token"));
    }
}