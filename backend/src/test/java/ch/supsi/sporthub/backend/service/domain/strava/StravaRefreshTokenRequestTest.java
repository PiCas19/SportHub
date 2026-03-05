package ch.supsi.sporthub.backend.service.domain.strava;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.MultiValueMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StravaRefreshTokenRequestTest {

    @Mock
    private MultiValueMap<String, String> mockRequestBody;

    @Test
    void testGetGrantType() {
        StravaRefreshTokenRequest request = new StravaRefreshTokenRequest("id", "secret", "refresh");
        String grantType = request.getGrantType();
        assertEquals("refresh_token", grantType);
    }

    @Test
    void testAddTokenParameter() {
        String refreshToken = "test-refresh-token";
        StravaRefreshTokenRequest request = new StravaRefreshTokenRequest("id", "secret", refreshToken);
        request.addTokenParameter(mockRequestBody);
        verify(mockRequestBody).add("refresh_token", refreshToken);
    }

    @Test
    void testBuildRequest() {
        String clientId = "test-client-id";
        String clientSecret = "test-client-secret";
        String refreshToken = "test-refresh-token";
        StravaRefreshTokenRequest request = spy(new StravaRefreshTokenRequest(clientId, clientSecret, refreshToken));
        doCallRealMethod().when(request).addTokenParameter(any());
        MultiValueMap<String, String> result = request.buildRequest();
        assertEquals(clientId, result.getFirst("client_id"));
        assertEquals(clientSecret, result.getFirst("client_secret"));
        assertEquals("refresh_token", result.getFirst("grant_type"));
        assertEquals(refreshToken, result.getFirst("refresh_token"));
    }
}