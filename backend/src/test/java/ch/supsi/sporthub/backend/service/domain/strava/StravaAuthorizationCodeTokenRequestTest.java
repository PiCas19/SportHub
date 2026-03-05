package ch.supsi.sporthub.backend.service.domain.strava;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.MultiValueMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StravaAuthorizationCodeTokenRequestTest {

    @Mock
    private MultiValueMap<String, String> mockRequestBody;

    @Test
    void testGetGrantType() {
        StravaAuthorizationCodeTokenRequest request = new StravaAuthorizationCodeTokenRequest("id", "secret", "code");
        String grantType = request.getGrantType();
        assertEquals("authorization_code", grantType);
    }

    @Test
    void testAddTokenParameter() {
        String authCode = "test-auth-code";
        StravaAuthorizationCodeTokenRequest request = new StravaAuthorizationCodeTokenRequest("id", "secret", authCode);
        request.addTokenParameter(mockRequestBody);
        verify(mockRequestBody).add("code", authCode);
    }

    @Test
    void testBuildRequest() {
        String clientId = "test-client-id";
        String clientSecret = "test-client-secret";
        String authCode = "test-auth-code";
        StravaAuthorizationCodeTokenRequest request = spy(new StravaAuthorizationCodeTokenRequest(clientId, clientSecret, authCode));
        doCallRealMethod().when(request).addTokenParameter(any());
        MultiValueMap<String, String> result = request.buildRequest();
        assertEquals(clientId, result.getFirst("client_id"));
        assertEquals(clientSecret, result.getFirst("client_secret"));
        assertEquals("authorization_code", result.getFirst("grant_type"));
        assertEquals(authCode, result.getFirst("code"));
    }
}
