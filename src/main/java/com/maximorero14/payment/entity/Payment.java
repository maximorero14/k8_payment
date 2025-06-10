package com.maximorero14.payment.entity;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "payments")
@Data
public class Payment {
    @Id
    private String id;
    private BigDecimal amount;
    private String currency;
    private String method;
    private String userId;
    private Boolean isFraud;
    private String status;
}