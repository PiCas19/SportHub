package ch.supsi.sporthub.backend.service.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class IBaseApiServiceDefaultMethodsTest {

    private static class TestBaseApiService implements IBaseApiService {
        @Override
        public <T> ResponseEntity<T> get(String baseUrl, String endpoint, String accessToken, Class<T> responseType) {
            return null;
        }

        @Override
        public <T> ResponseEntity<T> get(String baseUrl, String endpoint, Class<T> responseType) {
            return null;
        }

        @Override
        public <T> ResponseEntity<T> post(String baseUrl, String endpoint, Object requestBody, HttpHeaders headers, Class<T> responseType) {
            return null;
        }
    }

    @Spy
    private TestBaseApiService baseApiService;

    @Mock
    private ResponseEntity<String> mockResponse;

    @Captor
    private ArgumentCaptor<HttpHeaders> headersCaptor;

    @BeforeEach
    public void setup() {
    }

    @Test
    public void testPostJson() {
        String baseUrl = "https://api.example.com";
        String endpoint = "/test";
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("key1", "value1");
        requestBody.put("key2", "value2");
        doReturn(mockResponse).when(baseApiService).post(
                anyString(), anyString(), any(), any(HttpHeaders.class), any());
        ResponseEntity<String> result = baseApiService.postJson(baseUrl, endpoint, requestBody, String.class);
        assertEquals(mockResponse, result);
        verify(baseApiService).post(
                eq(baseUrl),
                eq(endpoint),
                eq(requestBody),
                headersCaptor.capture(),
                eq(String.class)
        );
        assertEquals(MediaType.APPLICATION_JSON, headersCaptor.getValue().getContentType());
    }

    @Test
    public void testPostForm() {
        String baseUrl = "https://api.example.com";
        String endpoint = "/form";
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("field1", "value1");
        formData.add("field2", "value2");
        doReturn(mockResponse).when(baseApiService).post(
                anyString(), anyString(), any(), any(HttpHeaders.class), any());
        ResponseEntity<String> result = baseApiService.postForm(baseUrl, endpoint, formData, String.class);
        assertEquals(mockResponse, result);
        verify(baseApiService).post(
                eq(baseUrl),
                eq(endpoint),
                eq(formData),
                headersCaptor.capture(),
                eq(String.class)
        );
        assertEquals(MediaType.APPLICATION_FORM_URLENCODED, headersCaptor.getValue().getContentType());
    }
}