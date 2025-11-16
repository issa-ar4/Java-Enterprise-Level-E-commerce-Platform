package com.ecommerce.payment.service;

import com.ecommerce.payment.dto.PaymentRequest;
import com.ecommerce.payment.dto.PaymentResponse;
import com.ecommerce.payment.exception.PaymentFailedException;
import com.ecommerce.payment.exception.PaymentNotFoundException;
import com.ecommerce.payment.model.Payment;
import com.ecommerce.payment.repository.PaymentRepository;
import com.paypal.orders.Order;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Main payment service that orchestrates payment operations.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final StripePaymentService stripePaymentService;
    private final PayPalPaymentService payPalPaymentService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String PAYMENT_EVENTS_TOPIC = "payment-events";

    /**
     * Create a payment based on the payment method.
     */
    @Transactional
    @CacheEvict(value = "payments", allEntries = true)
    public PaymentResponse createPayment(PaymentRequest request) {
        log.info("Creating payment for order: {} with method: {}", 
                request.getOrderId(), request.getPaymentMethod());

        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .paymentMethod(request.getPaymentMethod())
                .status(Payment.PaymentStatus.PENDING)
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail())
                .billingStreet(request.getBillingStreet())
                .billingCity(request.getBillingCity())
                .billingState(request.getBillingState())
                .billingPostalCode(request.getBillingPostalCode())
                .billingCountry(request.getBillingCountry())
                .metadata(request.getMetadata())
                .build();

        try {
            // Route to appropriate payment gateway
            if ("STRIPE".equals(request.getPaymentMethod())) {
                return processStripePayment(payment, request);
            } else if ("PAYPAL".equals(request.getPaymentMethod())) {
                return processPayPalPayment(payment, request);
            } else {
                throw new PaymentFailedException("Unsupported payment method: " + request.getPaymentMethod());
            }
        } catch (Exception e) {
            log.error("Payment creation failed for order: {}", request.getOrderId(), e);
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setFailureReason(e.getMessage());
            paymentRepository.save(payment);
            
            publishPaymentEvent("payment.failed", payment);
            throw new PaymentFailedException("Payment creation failed: " + e.getMessage());
        }
    }

    /**
     * Process Stripe payment.
     */
    private PaymentResponse processStripePayment(Payment payment, PaymentRequest request) throws StripeException {
        PaymentIntent paymentIntent = stripePaymentService.createPaymentIntent(request);
        
        payment.setPaymentIntentId(paymentIntent.getId());
        payment.setTransactionId(paymentIntent.getId());
        payment.setStatus(stripePaymentService.mapStripeStatus(paymentIntent.getStatus()));
        payment.setPaymentUrl(paymentIntent.getClientSecret()); // Client secret for frontend
        
        Payment savedPayment = paymentRepository.save(payment);
        log.info("Stripe payment created: {} for order: {}", savedPayment.getId(), payment.getOrderId());
        
        publishPaymentEvent("payment.created", savedPayment);
        return mapToResponse(savedPayment);
    }

    /**
     * Process PayPal payment.
     */
    private PaymentResponse processPayPalPayment(Payment payment, PaymentRequest request) throws IOException {
        Order order = payPalPaymentService.createOrder(request);
        
        payment.setTransactionId(order.id());
        payment.setStatus(payPalPaymentService.mapPayPalStatus(order.status()));
        payment.setPaymentUrl(payPalPaymentService.getApprovalUrl(order));
        
        Payment savedPayment = paymentRepository.save(payment);
        log.info("PayPal payment created: {} for order: {}", savedPayment.getId(), payment.getOrderId());
        
        publishPaymentEvent("payment.created", savedPayment);
        return mapToResponse(savedPayment);
    }

    /**
     * Confirm a payment (called after customer completes payment).
     */
    @Transactional
    @CacheEvict(value = "payments", allEntries = true)
    public PaymentResponse confirmPayment(Long paymentId) {
        log.info("Confirming payment: {}", paymentId);
        
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentId));

        try {
            if (payment.getPaymentMethod() == Payment.PaymentMethod.STRIPE) {
                PaymentIntent paymentIntent = stripePaymentService.retrievePaymentIntent(payment.getPaymentIntentId());
                payment.setStatus(stripePaymentService.mapStripeStatus(paymentIntent.getStatus()));
                
                if ("succeeded".equals(paymentIntent.getStatus())) {
                    payment.setCompletedAt(LocalDateTime.now());
                    publishPaymentEvent("payment.completed", payment);
                }
            } else if (payment.getPaymentMethod() == Payment.PaymentMethod.PAYPAL) {
                Order order = payPalPaymentService.captureOrder(payment.getTransactionId());
                payment.setStatus(payPalPaymentService.mapPayPalStatus(order.status()));
                
                if ("COMPLETED".equals(order.status())) {
                    payment.setCompletedAt(LocalDateTime.now());
                    publishPaymentEvent("payment.completed", payment);
                }
            }
            
            Payment updatedPayment = paymentRepository.save(payment);
            log.info("Payment confirmed: {}", paymentId);
            return mapToResponse(updatedPayment);
            
        } catch (Exception e) {
            log.error("Payment confirmation failed: {}", paymentId, e);
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setFailureReason(e.getMessage());
            paymentRepository.save(payment);
            
            publishPaymentEvent("payment.failed", payment);
            throw new PaymentFailedException("Payment confirmation failed: " + e.getMessage());
        }
    }

    /**
     * Cancel a payment.
     */
    @Transactional
    @CacheEvict(value = "payments", allEntries = true)
    public PaymentResponse cancelPayment(Long paymentId) {
        log.info("Cancelling payment: {}", paymentId);
        
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentId));

        if (payment.getStatus() == Payment.PaymentStatus.COMPLETED) {
            throw new PaymentFailedException("Cannot cancel completed payment. Use refund instead.");
        }

        try {
            if (payment.getPaymentMethod() == Payment.PaymentMethod.STRIPE && payment.getPaymentIntentId() != null) {
                stripePaymentService.cancelPaymentIntent(payment.getPaymentIntentId());
            }
            
            payment.setStatus(Payment.PaymentStatus.CANCELLED);
            Payment cancelledPayment = paymentRepository.save(payment);
            
            publishPaymentEvent("payment.cancelled", cancelledPayment);
            log.info("Payment cancelled: {}", paymentId);
            return mapToResponse(cancelledPayment);
            
        } catch (Exception e) {
            log.error("Payment cancellation failed: {}", paymentId, e);
            throw new PaymentFailedException("Payment cancellation failed: " + e.getMessage());
        }
    }

    /**
     * Process a refund.
     */
    @Transactional
    @CacheEvict(value = "payments", allEntries = true)
    public PaymentResponse refundPayment(Long paymentId, BigDecimal amount, String reason) {
        log.info("Processing refund for payment: {} with amount: {}", paymentId, amount);
        
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentId));

        if (payment.getStatus() != Payment.PaymentStatus.COMPLETED) {
            throw new PaymentFailedException("Can only refund completed payments");
        }

        if (amount.compareTo(payment.getAmount()) > 0) {
            throw new PaymentFailedException("Refund amount cannot exceed payment amount");
        }

        try {
            if (payment.getPaymentMethod() == Payment.PaymentMethod.STRIPE) {
                stripePaymentService.createRefund(payment.getPaymentIntentId(), amount, reason);
            } else if (payment.getPaymentMethod() == Payment.PaymentMethod.PAYPAL) {
                payPalPaymentService.refundPayment(payment.getTransactionId(), amount, payment.getCurrency());
            }
            
            payment.setRefundedAmount(amount);
            payment.setRefundId(reason); // Store reason in refundId field for now
            
            if (amount.compareTo(payment.getAmount()) == 0) {
                payment.setStatus(Payment.PaymentStatus.REFUNDED);
            } else {
                payment.setStatus(Payment.PaymentStatus.PARTIALLY_REFUNDED);
            }
            
            Payment refundedPayment = paymentRepository.save(payment);
            publishPaymentEvent("payment.refunded", refundedPayment);
            
            log.info("Payment refunded: {}", paymentId);
            return mapToResponse(refundedPayment);
            
        } catch (Exception e) {
            log.error("Refund processing failed: {}", paymentId, e);
            throw new PaymentFailedException("Refund processing failed: " + e.getMessage());
        }
    }

    /**
     * Get payment by ID.
     */
    @Cacheable(value = "payments", key = "#paymentId")
    public PaymentResponse getPayment(Long paymentId) {
        log.info("Retrieving payment: {}", paymentId);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentId));
        return mapToResponse(payment);
    }

    /**
     * Get payment by order ID.
     */
    @Cacheable(value = "payments", key = "'order-' + #orderId")
    public PaymentResponse getPaymentByOrderId(String orderId) {
        log.info("Retrieving payment for order: {}", orderId);
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for order: " + orderId));
        return mapToResponse(payment);
    }

    /**
     * Get all payments for a user.
     */
    public List<PaymentResponse> getUserPayments(String userId) {
        log.info("Retrieving payments for user: {}", userId);
        List<Payment> payments = paymentRepository.findByUserId(userId);
        return payments.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get payments by status.
     */
    public List<PaymentResponse> getPaymentsByStatus(Payment.PaymentStatus status) {
        log.info("Retrieving payments with status: {}", status);
        List<Payment> payments = paymentRepository.findByStatus(status);
        return payments.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Publish payment event to Kafka.
     */
    private void publishPaymentEvent(String eventType, Payment payment) {
        try {
            kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, eventType, payment);
            log.info("Published event: {} for payment: {}", eventType, payment.getId());
        } catch (Exception e) {
            log.error("Failed to publish payment event: {}", eventType, e);
        }
    }

    /**
     * Map Payment entity to PaymentResponse DTO.
     */
    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .paymentIntentId(payment.getPaymentIntentId())
                .paymentUrl(payment.getPaymentUrl())
                .customerName(payment.getCustomerName())
                .customerEmail(payment.getCustomerEmail())
                .refundedAmount(payment.getRefundedAmount())
                .refundId(payment.getRefundId())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .completedAt(payment.getCompletedAt())
                .build();
    }
}
