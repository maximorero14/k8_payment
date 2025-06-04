package com.maximorero14.payment.rest_client;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Slf4j
public class EnhancedRestClient {

    private static final String REST_CLIENT = "rest_client";
    private static final String HTTP_METHOD = "http_method";
    private static final String METRIC_NAME = "metric_name";
    private static final String RESPONSE_TIME = "response_time";
    private static final String STATUS_CODE = "status_code";
    private static final String RESPONSE_BODY = "response_body";
    private static final String REQUEST_BODY = "request_body";
    private static final String URL = "url";
    private static final String ERROR_MESSAGE = "error_message";

    @Autowired
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper;

    public EnhancedRestClient() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
    }

    public <T> RestClientResponse<T> get(String metricName, String url, Map<String, String> headers, Class<T> responseType) {
        HttpEntity<Object> entity = new HttpEntity<>(null, buildHeaders(headers));
        return execute(metricName, url, HttpMethod.GET, entity, responseType);
    }

    public <T> RestClientResponse<T> post(String metricName, String url, Object request, Map<String, String> headers, Class<T> responseType) {
        HttpEntity<Object> entity = new HttpEntity<>(request, buildHeaders(headers));
        return execute(metricName, url, HttpMethod.POST, entity, responseType);
    }

    public <T> RestClientResponse<T> patch(String metricName, String url, Object request, Map<String, String> headers, Class<T> responseType) {
        HttpEntity<Object> entity = new HttpEntity<>(request, buildHeaders(headers));
        return execute(metricName, url, HttpMethod.PATCH, entity, responseType);
    }

    public <T> RestClientResponse<T> put(String metricName, String url, Object request, Map<String, String> headers, Class<T> responseType) {
        HttpEntity<Object> entity = new HttpEntity<>(request, buildHeaders(headers));
        return execute(metricName, url, HttpMethod.PUT, entity, responseType);
    }

    public <T> RestClientResponse<T> delete(String metricName, String url, Map<String, String> headers, Class<T> responseType) {
        HttpEntity<Object> entity = new HttpEntity<>(null, buildHeaders(headers));
        return execute(metricName, url, HttpMethod.DELETE, entity, responseType);
    }

    private <T> RestClientResponse<T> execute(String metricName, String url, HttpMethod method, HttpEntity<Object> entity, Class<T> responseType) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        RestClientResponse<T> clientResponse = new RestClientResponse<>();
        ResponseEntity<String> rawResponse = null;
        String errorMessage = null;

        try {
            // Always get raw string response first to handle any response type
            rawResponse = restTemplate.exchange(url, method, entity, String.class);

            // Try to parse the response to the expected type
            if (rawResponse.getBody() != null && !rawResponse.getBody().trim().isEmpty()) {
                try {
                    T parsedBody = objectMapper.readValue(rawResponse.getBody(), responseType);
                    clientResponse.setBody(parsedBody);
                    clientResponse.setSuccess(true);
                } catch (JsonProcessingException e) {
                    log.warn("Failed to parse response body to {}: {}", responseType.getSimpleName(), e.getMessage());
                    // Keep the raw response but mark as parsing error
                    clientResponse.setRawBody(rawResponse.getBody());
                    clientResponse.setSuccess(false);
                    clientResponse.setParsingError(true);
                    errorMessage = "Failed to parse response: " + e.getMessage();
                }
            } else {
                clientResponse.setSuccess(true); // Empty body is OK for some operations
            }

            clientResponse.setStatusCode(rawResponse.getStatusCode());
            clientResponse.setHeaders(rawResponse.getHeaders());

        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            // Handle HTTP errors (4xx, 5xx)
            clientResponse.setStatusCode(ex.getStatusCode());
            clientResponse.setRawBody(ex.getResponseBodyAsString());
            clientResponse.setSuccess(false);
            clientResponse.setHttpError(true);
            errorMessage = "HTTP Error: " + ex.getStatusCode() + " - " + ex.getStatusText();

            // Try to parse error response if possible
            if (ex.getResponseBodyAsString() != null && !ex.getResponseBodyAsString().trim().isEmpty()) {
                try {
                    T parsedErrorBody = objectMapper.readValue(ex.getResponseBodyAsString(), responseType);
                    clientResponse.setBody(parsedErrorBody);
                } catch (JsonProcessingException e) {
                    // Error response doesn't match expected type, keep raw
                    log.debug("Error response doesn't match expected type: {}", e.getMessage());
                }
            }

        } catch (ResourceAccessException ex) {
            // Handle connection/timeout errors
            clientResponse.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
            clientResponse.setSuccess(false);
            clientResponse.setConnectionError(true);
            errorMessage = "Connection Error: " + ex.getMessage();

        } catch (Exception ex) {
            // Handle any other unexpected errors
            clientResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            clientResponse.setSuccess(false);
            clientResponse.setUnexpectedError(true);
            errorMessage = "Unexpected Error: " + ex.getMessage();
        }

        stopWatch.stop();
        long processingTime = stopWatch.getTotalTimeMillis();
        clientResponse.setProcessingTimeMs(processingTime);

        // Log the request/response details
        logRequestResponse(metricName, url, method, entity, clientResponse, processingTime, errorMessage);

        return clientResponse;
    }

    private void logRequestResponse(String metricName, String url, HttpMethod method,
                                    HttpEntity<Object> entity, RestClientResponse<?> clientResponse,
                                    long processingTime, String errorMessage) {

        JsonNode requestBody = objectMapper.createObjectNode();
        if (entity.getBody() != null) {
            try {
                String requestBodyString = objectMapper.writeValueAsString(entity.getBody());
                requestBody = objectMapper.readTree(requestBodyString);
            } catch (Exception e) {
                log.warn("Failed to serialize request body: {}", e.getMessage());
            }
        }

        JsonNode responseBody = objectMapper.createObjectNode();
        if (clientResponse.getBody() != null) {
            try {
                String responseBodyString = objectMapper.writeValueAsString(clientResponse.getBody());
                responseBody = objectMapper.readTree(responseBodyString);
            } catch (Exception e) {
                // Use raw body if available
                if (clientResponse.getRawBody() != null) {
                    try {
                        responseBody = objectMapper.readTree(clientResponse.getRawBody());
                    } catch (Exception ex) {
                        log.warn("Failed to parse raw response body: {}", ex.getMessage());
                    }
                }
            }
        }

        String logLevel = clientResponse.isSuccess() ? "INFO" : "WARN";
        String logMessage = String.format("%s [%s: %s] [%s: %s] [%s: %s] [%s: %s] [%s: %s] [%s: %s] [%s: %s]%s",
                REST_CLIENT,
                HTTP_METHOD, method.toString(),
                METRIC_NAME, metricName != null ? metricName : "null",
                STATUS_CODE, clientResponse.getStatusCode() != null ? clientResponse.getStatusCode().value() : "null",
                RESPONSE_TIME, processingTime,
                URL, url != null ? url : "null",
                REQUEST_BODY, requestBody,
                RESPONSE_BODY, responseBody,
                errorMessage != null ? String.format(" [%s: %s]", ERROR_MESSAGE, errorMessage) : ""
        );

        if (clientResponse.isSuccess()) {
            log.info(logMessage);
        } else {
            log.error(logMessage);
        }
    }

    private HttpHeaders buildHeaders(Map<String, String> headers) {
        HttpHeaders httpHeaders = new HttpHeaders();
        if (headers != null && !headers.isEmpty()) {
            headers.forEach(httpHeaders::set);
        }
        return httpHeaders;
    }
}