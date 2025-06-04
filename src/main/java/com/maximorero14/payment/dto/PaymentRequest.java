package com.maximorero14.payment.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class PaymentRequest {
    private BigDecimal amount;
    private String currency;
    private String method;
    private String userId;
}
