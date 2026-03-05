package ch.supsi.sporthub.backend.service.impl;

import ch.supsi.sporthub.backend.service.api.IBaseApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BaseApiServiceTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private IBaseApiService baseApiService;

    @BeforeEach
    void setUp() {
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        baseApiService = new BaseApiServiceImpl(webClientBuilder);
    }

    @Test
    void testGet() {
        String baseUrl = "https://api.example.com";
        String endpoint = "/users";
        String accessToken = "test-token";
        String expectedResponse = "Response data";
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(baseUrl + endpoint)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header("Authorization", "Bearer " + accessToken)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(expectedResponse));
        ResponseEntity<String> response = baseApiService.get(baseUrl, endpoint, accessToken, String.class);
        assertNotNull(response);
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    void testGetWithoutAccessToken() {
        String baseUrl = "https://api.example.com";
        String endpoint = "/users";
        String expectedResponse = "Response data";
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(baseUrl + endpoint)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(expectedResponse));
        ResponseEntity<String> response = baseApiService.get(baseUrl, endpoint, String.class);
        assertNotNull(response);
        assertEquals(expectedResponse, response.getBody());
    }


    @Test
    void testPostJson() {
        String baseUrl = "https://api.example.com";
        String endpoint = "/users";
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("username", "testuser");
        requestBody.put("email", "test@example.com");
        String expectedResponse = "Response data";
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(baseUrl + endpoint)).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(requestBody)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(expectedResponse));
        ResponseEntity<String> response = baseApiService.postJson(baseUrl, endpoint, requestBody, String.class);
        assertNotNull(response);
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    void testPostForm() {
        String baseUrl = "https://api.example.com";
        String endpoint = "/login";
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("username", "testuser");
        requestBody.add("password", "password123");
        String expectedResponse = "Response data";
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(baseUrl + endpoint)).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(requestBody)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(expectedResponse));
        ResponseEntity<String> response = baseApiService.postForm(baseUrl, endpoint, requestBody, String.class);
        assertNotNull(response);
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    void testPost() {
        String baseUrl = "https://api.example.com";
        String endpoint = "/data";
        Object requestBody = new HashMap<String, Object>();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String expectedResponse = "Response data";
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(baseUrl + endpoint)).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(requestBody)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(expectedResponse));
        ResponseEntity<String> response = baseApiService.post(baseUrl, endpoint, requestBody, headers, String.class);
        assertNotNull(response);
        assertEquals(expectedResponse, response.getBody());
    }
}