package com.ecommerce.payment.service;

import com.ecommerce.payment.dto.PaymentRequest;
import com.ecommerce.payment.model.Payment;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Stripe payment gateway service.
 */
@Service
@Slf4j
public class StripePaymentService {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
        log.info("Stripe API initialized");
    }

    /**
     * Create a Stripe Payment Intent.
     */
    public PaymentIntent createPaymentIntent(PaymentRequest request) throws StripeException {
        log.info("Creating Stripe payment intent for order: {}", request.getOrderId());

        // Convert amount to cents (Stripe works with smallest currency unit)
        long amountInCents = request.getAmount().multiply(BigDecimal.valueOf(100)).longValue();

        // Build metadata
        Map<String, String> metadata = new HashMap<>();
        metadata.put("orderId", request.getOrderId());
        metadata.put("userId", request.getUserId());
        if (request.getMetadata() != null) {
            metadata.put("additionalInfo", request.getMetadata());
        }

        // Create payment intent parameters
        PaymentIntentCreateParams.Builder paramsBuilder = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency(request.getCurrency().toLowerCase())
                .setDescription(request.getDescription())
                .putAllMetadata(metadata)
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build()
                );

        // Add customer email if provided
        if (request.getCustomerEmail() != null) {
            paramsBuilder.setReceiptEmail(request.getCustomerEmail());
        }

        PaymentIntentCreateParams params = paramsBuilder.build();

        try {
            PaymentIntent paymentIntent = PaymentIntent.create(params);
            log.info("Stripe payment intent created: {} for order: {}", 
                    paymentIntent.getId(), request.getOrderId());
            return paymentIntent;
        } catch (StripeException e) {
            log.error("Failed to create Stripe payment intent for order: {}", 
                    request.getOrderId(), e);
            throw e;
        }
    }

    /**
     * Retrieve a Payment Intent by ID.
     */
    public PaymentIntent retrievePaymentIntent(String paymentIntentId) throws StripeException {
        log.info("Retrieving Stripe payment intent: {}", paymentIntentId);
        try {
            return PaymentIntent.retrieve(paymentIntentId);
        } catch (StripeException e) {
            log.error("Failed to retrieve Stripe payment intent: {}", paymentIntentId, e);
            throw e;
        }
    }

    /**
     * Confirm a Payment Intent.
     */
    public PaymentIntent confirmPaymentIntent(String paymentIntentId) throws StripeException {
        log.info("Confirming Stripe payment intent: {}", paymentIntentId);
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            return paymentIntent.confirm();
        } catch (StripeException e) {
            log.error("Failed to confirm Stripe payment intent: {}", paymentIntentId, e);
            throw e;
        }
    }

    /**
     * Cancel a Payment Intent.
     */
    public PaymentIntent cancelPaymentIntent(String paymentIntentId) throws StripeException {
        log.info("Cancelling Stripe payment intent: {}", paymentIntentId);
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            return paymentIntent.cancel();
        } catch (StripeException e) {
            log.error("Failed to cancel Stripe payment intent: {}", paymentIntentId, e);
            throw e;
        }
    }

    /**
     * Create a refund for a payment.
     */
    public Refund createRefund(String paymentIntentId, BigDecimal amount, String reason) throws StripeException {
        log.info("Creating refund for payment intent: {} with amount: {}", paymentIntentId, amount);

        long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();

        RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(paymentIntentId)
                .setAmount(amountInCents)
                .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
                .build();

        try {
            Refund refund = Refund.create(params);
            log.info("Refund created: {} for payment intent: {}", refund.getId(), paymentIntentId);
            return refund;
        } catch (StripeException e) {
            log.error("Failed to create refund for payment intent: {}", paymentIntentId, e);
            throw e;
        }
    }

    /**
     * Map Stripe PaymentIntent status to Payment status.
     */
    public Payment.PaymentStatus mapStripeStatus(String stripeStatus) {
        return switch (stripeStatus) {
            case "requires_payment_method", "requires_confirmation", "requires_action" -> Payment.PaymentStatus.PENDING;
            case "processing" -> Payment.PaymentStatus.PROCESSING;
            case "succeeded" -> Payment.PaymentStatus.COMPLETED;
            case "canceled" -> Payment.PaymentStatus.CANCELLED;
            case "requires_capture" -> Payment.PaymentStatus.PENDING;
            default -> Payment.PaymentStatus.FAILED;
        };
    }
}
