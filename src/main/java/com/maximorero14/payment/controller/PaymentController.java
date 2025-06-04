package com.maximorero14.payment.controller;

import com.maximorero14.payment.dto.FraudCheckResponse;
import com.maximorero14.payment.dto.PaymentRequest;
import com.maximorero14.payment.dto.PaymentResponse;
import com.maximorero14.payment.rest_client.EnhancedRestClient;
import com.maximorero14.payment.rest_client.RestClientResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/payment")
@Slf4j
public class PaymentController {

	@Autowired
	private EnhancedRestClient restClient;

	@Value("${services.fraud.url}")
	private String fraudServiceUrl;

	@PostMapping("/create")
	public ResponseEntity<?> createPayment(@RequestBody PaymentRequest paymentRequest) {
		String metricName = "create_payment";

		try {
			RestClientResponse<FraudCheckResponse> response = restClient.post(
					metricName,
					fraudServiceUrl+"/fraud/check",
					paymentRequest,
					Map.of("Content-Type", "application/json"),
					FraudCheckResponse.class
			);

			if (!(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError())) {
				FraudCheckResponse fraudCheckResponse = response.getBody();

				if(fraudCheckResponse.isFraud()){

					Map<String, Object> errorResponse = new HashMap<>();
					errorResponse.put("error", "Service unavailable");
					errorResponse.put("message", "Payment service is currently unavailable");
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);

				} else {
					PaymentResponse paymentResponse = new PaymentResponse();

					paymentResponse.setId(UUID.randomUUID().toString());
					paymentResponse.setAmount(paymentRequest.getAmount());
					paymentResponse.setCurrency(paymentRequest.getCurrency());
					paymentResponse.setMethod(paymentRequest.getMethod());
					paymentResponse.setUserId(paymentRequest.getUserId());

					return ResponseEntity.status(HttpStatus.CREATED).body(paymentResponse);
				}

			} else {
				return handleErrorResponse(response);
			}

		} catch (Exception e) {
			log.error("Unexpected error in createPayment: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("error", "Internal server error", "message", "An unexpected error occurred"));
		}
	}

	private ResponseEntity<?> handleErrorResponse(RestClientResponse<FraudCheckResponse> response) {
		Map<String, Object> errorResponse = new HashMap<>();
		errorResponse.put("success", false);
		errorResponse.put("errorType", response.getErrorType());
		errorResponse.put("statusCode", response.getStatusCode().value());

		if (response.isConnectionError()) {
			errorResponse.put("error", "Service unavailable");
			errorResponse.put("message", "Payment service is currently unavailable");
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);

		} else if (response.isParsingError()) {
			errorResponse.put("error", "Response parsing error");
			errorResponse.put("message", "Received unexpected response format");
			errorResponse.put("rawResponse", response.getRawBody());
			return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorResponse);

		} else if (response.isClientError()) {
			// Forward client errors (4xx) as-is
			if (response.getBody() != null) {
				return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
			} else if (response.getRawBody() != null) {
				errorResponse.put("message", response.getRawBody());
				return ResponseEntity.status(response.getStatusCode()).body(errorResponse);
			}
			return ResponseEntity.status(response.getStatusCode()).body(errorResponse);

		} else if (response.isServerError()) {
			errorResponse.put("error", "External service error");
			errorResponse.put("message", "Payment service encountered an error");
			return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorResponse);

		} else {
			errorResponse.put("error", "Unknown error");
			errorResponse.put("message", "An unknown error occurred");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}
	}
}