package com.maximorero14.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.maximorero14.payment.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, String> {
}