package ch.supsi.sporthub.backend.service.impl;

import ch.supsi.sporthub.backend.service.api.IBaseApiService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Map;

/**
 * Implementation of the {@link IBaseApiService} interface for interacting with external APIs.
 * This service provides methods for making HTTP GET and POST requests to external APIs.
 * It uses the {@link WebClient} class from Spring WebFlux to send requests and retrieve responses.
 * The service supports both JSON and form-encoded POST requests and handles authorization headers for GET requests.
 */
@Service
public class BaseApiServiceImpl implements IBaseApiService {
    private final WebClient webClient;


    /**
     * Constructs a {@link BaseApiServiceImpl} with a configured {@link WebClient}.
     * The WebClient is used to make HTTP requests to external APIs.
     *
     * @param webClientBuilder The builder used to create a WebClient instance.
     */
    public BaseApiServiceImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(" ").build();
    }

    /**
     * Makes an HTTP GET request to the specified endpoint and returns the response as a {@link ResponseEntity}.
     * This version of the GET request includes an authorization token in the request header.
     *
     * @param baseUrl     The base URL of the API.
     * @param endpoint    The endpoint to make the GET request to.
     * @param accessToken The Bearer token for authorization.
     * @param responseType The class type of the response body.
     * @param <T>         The type of the response body.
     * @return A {@link ResponseEntity} containing the response body.
     */
    @Override
    public <T> ResponseEntity<T> get(String baseUrl, String endpoint, String accessToken, Class<T> responseType) {
        T body = webClient.get()
                .uri(baseUrl + endpoint)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(responseType)
                .block();
        return ResponseEntity.ok(body);
    }

    /**
     * Makes an HTTP GET request to the specified endpoint and returns the response as a {@link ResponseEntity}.
     * This version of the GET request does not include an authorization token.
     *
     * @param baseUrl     The base URL of the API.
     * @param endpoint    The endpoint to make the GET request to.
     * @param responseType The class type of the response body.
     * @param <T>         The type of the response body.
     * @return A {@link ResponseEntity} containing the response body.
     */
    @Override
    public <T> ResponseEntity<T> get(String baseUrl, String endpoint, Class<T> responseType) {
        String fullUrl = baseUrl + endpoint;
        T body = webClient.get()
                .uri(fullUrl)
                .retrieve()
                .bodyToMono(responseType)
                .block();
        return ResponseEntity.ok(body);
    }

    /**
     * Makes an HTTP POST request with a JSON body to the specified endpoint and returns the response as a {@link ResponseEntity}.
     * The content type is set to {@link MediaType#APPLICATION_JSON}.
     *
     * @param baseUrl     The base URL of the API.
     * @param endpoint    The endpoint to make the POST request to.
     * @param requestBody The request body to send in the POST request.
     * @param responseType The class type of the response body.
     * @param <T>         The type of the response body.
     * @return A {@link ResponseEntity} containing the response body.
     */
    @Override
    public <T> ResponseEntity<T> postJson(String baseUrl, String endpoint, Map<String, String> requestBody, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return post(baseUrl, endpoint, requestBody, headers, responseType);
    }

    /**
     * Makes an HTTP POST request with a form-urlencoded body to the specified endpoint and returns the response as a {@link ResponseEntity}.
     * The content type is set to {@link MediaType#APPLICATION_FORM_URLENCODED}.
     *
     * @param baseUrl     The base URL of the API.
     * @param endpoint    The endpoint to make the POST request to.
     * @param requestBody The request body to send in the POST request.
     * @param responseType The class type of the response body.
     * @param <T>         The type of the response body.
     * @return A {@link ResponseEntity} containing the response body.
     */
    @Override
    public <T> ResponseEntity<T> postForm(String baseUrl, String endpoint, MultiValueMap<String, String> requestBody, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return post(baseUrl, endpoint, requestBody, headers, responseType);
    }

    /**
     * Makes a generic HTTP POST request with the provided body and headers to the specified endpoint.
     * Returns the response as a {@link ResponseEntity}.
     *
     * @param baseUrl     The base URL of the API.
     * @param endpoint    The endpoint to make the POST request to.
     * @param requestBody The request body to send in the POST request.
     * @param headers     The headers to be sent with the POST request.
     * @param responseType The class type of the response body.
     * @param <T>         The type of the response body.
     * @return A {@link ResponseEntity} containing the response body.
     */
    @Override
    public <T> ResponseEntity<T> post(String baseUrl, String endpoint, Object requestBody, HttpHeaders headers, Class<T> responseType) {
        T body = webClient.post()
                .uri(baseUrl + endpoint)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(responseType)
                .block();
        return ResponseEntity.ok(body);
    }
}