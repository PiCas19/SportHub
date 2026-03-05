package ch.supsi.sporthub.backend.service.domain.strava;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.MultiValueMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StravaTokenRequestTest {

    private static class TestStravaTokenRequest extends StravaTokenRequest {
        private final String grantType;
        public TestStravaTokenRequest(String clientId, String clientSecret, String grantType) {
            super(clientId, clientSecret);
            this.grantType = grantType;
        }
        @Override
        public String getGrantType() {
            return grantType;
        }
        @Override
        public void addTokenParameter(MultiValueMap<String, String> requestBody) {
            requestBody.add("test-param", "test-value");
        }
    }

    @Test
    void testBuildRequest() {
        String clientId = "test-client-id";
        String clientSecret = "test-client-secret";
        String grantType = "test-grant-type";
        TestStravaTokenRequest request = spy(new TestStravaTokenRequest(clientId, clientSecret, grantType));
        doNothing().when(request).addTokenParameter(any());
        MultiValueMap<String, String> result = request.buildRequest();
        assertEquals(clientId, result.getFirst("client_id"));
        assertEquals(clientSecret, result.getFirst("client_secret"));
        assertEquals(grantType, result.getFirst("grant_type"));
        verify(request).addTokenParameter(any());
    }
}