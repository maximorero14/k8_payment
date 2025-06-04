package com.maximorero14.payment.rest_client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestClientResponse<T> {
    private T body;
    private String rawBody;
    private HttpStatusCode statusCode;
    private HttpHeaders headers;
    private boolean success;
    private long processingTimeMs;

    // Error flags
    private boolean httpError;        // 4xx, 5xx errors
    private boolean connectionError;  // Network/timeout errors
    private boolean parsingError;     // JSON parsing errors
    private boolean unexpectedError;  // Any other errors

    public boolean isClientError() {
        return statusCode != null && statusCode.is4xxClientError();
    }

    public boolean isServerError() {
        return statusCode != null && statusCode.is5xxServerError();
    }

    public boolean hasError() {
        return !success;
    }

    public String getErrorType() {
        if (httpError) return "HTTP_ERROR";
        if (connectionError) return "CONNECTION_ERROR";
        if (parsingError) return "PARSING_ERROR";
        if (unexpectedError) return "UNEXPECTED_ERROR";
        return "NONE";
    }
}