package ch.supsi.sporthub.backend.service.api;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import java.util.Map;


/**
 * Interface that defines common operations for interacting with external APIs,
 * supporting GET and POST requests with flexible request body and header configurations.
 */
public interface IBaseApiService {

    /**
     * Performs a GET request to the specified endpoint with an access token for authorization.
     *
     * @param baseUrl      the base URL of the API
     * @param endpoint     the endpoint to call
     * @param accessToken  the bearer token for authorization
     * @param responseType the expected response type
     * @param <T>          the response type
     * @return a {@link ResponseEntity} containing the response
     */
    <T> ResponseEntity<T> get(String baseUrl, String endpoint, String accessToken, Class<T> responseType);

    /**
     * Performs a GET request to the specified endpoint without authorization.
     *
     * @param baseUrl      the base URL of the API
     * @param endpoint     the endpoint to call
     * @param responseType the expected response type
     * @param <T>          the response type
     * @return a {@link ResponseEntity} containing the response
     */
    <T> ResponseEntity<T> get(String baseUrl, String endpoint, Class<T> responseType);

    /**
     * Performs a POST request to the specified endpoint with custom headers and a request body.
     *
     * @param baseUrl      the base URL of the API
     * @param endpoint     the endpoint to call
     * @param requestBody  the body of the POST request
     * @param headers      the headers to include in the request
     * @param responseType the expected response type
     * @param <T>          the response type
     * @return a {@link ResponseEntity} containing the response
     */
    <T> ResponseEntity<T> post(String baseUrl, String endpoint, Object requestBody, HttpHeaders headers, Class<T> responseType);

    /**
     * Convenience method to perform a POST request with JSON content type.
     *
     * @param baseUrl      the base URL of the API
     * @param endpoint     the endpoint to call
     * @param requestBody  the request body as a map of key-value pairs
     * @param responseType the expected response type
     * @param <T>          the response type
     * @return a {@link ResponseEntity} containing the response
     */
    default <T> ResponseEntity<T> postJson(String baseUrl, String endpoint, Map<String, String> requestBody, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        return post(baseUrl, endpoint, requestBody, headers, responseType);
    }

    /**
     * Convenience method to perform a POST request with form URL-encoded content type.
     *
     * @param baseUrl      the base URL of the API
     * @param endpoint     the endpoint to call
     * @param requestBody  the request body as a {@link MultiValueMap}
     * @param responseType the expected response type
     * @param <T>          the response type
     * @return a {@link ResponseEntity} containing the response
     */
    default <T> ResponseEntity<T> postForm(String baseUrl, String endpoint, MultiValueMap<String, String> requestBody, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);
        return post(baseUrl, endpoint, requestBody, headers, responseType);
    }
}