package com.ecommerce.payment.controller;

import com.ecommerce.payment.dto.PaymentRequest;
import com.ecommerce.payment.dto.PaymentResponse;
import com.ecommerce.payment.model.Payment;
import com.ecommerce.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST controller for payment operations.
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment", description = "Payment management APIs")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Create a new payment", description = "Initiates a payment transaction with Stripe or PayPal")
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody PaymentRequest request) {
        log.info("Received payment request for order: {}", request.getOrderId());
        PaymentResponse response = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirm payment", description = "Confirms and captures a pending payment")
    public ResponseEntity<PaymentResponse> confirmPayment(
            @Parameter(description = "Payment ID") @PathVariable Long id) {
        log.info("Received payment confirmation request for: {}", id);
        PaymentResponse response = paymentService.confirmPayment(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel payment", description = "Cancels a pending payment")
    public ResponseEntity<PaymentResponse> cancelPayment(
            @Parameter(description = "Payment ID") @PathVariable Long id) {
        log.info("Received payment cancellation request for: {}", id);
        PaymentResponse response = paymentService.cancelPayment(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/refund")
    @Operation(summary = "Refund payment", description = "Processes a full or partial refund")
    public ResponseEntity<PaymentResponse> refundPayment(
            @Parameter(description = "Payment ID") @PathVariable Long id,
            @Parameter(description = "Refund amount") @RequestParam BigDecimal amount,
            @Parameter(description = "Refund reason") @RequestParam(required = false) String reason) {
        log.info("Received refund request for payment: {} with amount: {}", id, amount);
        PaymentResponse response = paymentService.refundPayment(id, amount, reason);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID", description = "Retrieves payment details by payment ID")
    public ResponseEntity<PaymentResponse> getPayment(
            @Parameter(description = "Payment ID") @PathVariable Long id) {
        log.info("Received request to get payment: {}", id);
        PaymentResponse response = paymentService.getPayment(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payment by order ID", description = "Retrieves payment details by order ID")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(
            @Parameter(description = "Order ID") @PathVariable String orderId) {
        log.info("Received request to get payment for order: {}", orderId);
        PaymentResponse response = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get payments by user", description = "Retrieves all payments for a specific user")
    public ResponseEntity<List<PaymentResponse>> getUserPayments(
            @Parameter(description = "User ID") @PathVariable String userId) {
        log.info("Received request to get payments for user: {}", userId);
        List<PaymentResponse> response = paymentService.getUserPayments(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get payments by status", description = "Retrieves all payments with a specific status")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByStatus(
            @Parameter(description = "Payment status") @PathVariable Payment.PaymentStatus status) {
        log.info("Received request to get payments with status: {}", status);
        List<PaymentResponse> response = paymentService.getPaymentsByStatus(status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Checks if the payment service is running")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Payment Service is running");
    }
}
