package com.ecommerce.payment.controller;

import com.ecommerce.payment.model.Payment;
import com.ecommerce.payment.repository.PaymentRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Stripe webhook handler.
 */
@RestController
@RequestMapping("/api/webhooks/stripe")
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookController {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    private static final String PAYMENT_EVENTS_TOPIC = "payment-events";

    @PostMapping
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        
        log.info("Received Stripe webhook");

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Invalid signature for Stripe webhook", e);
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        log.info("Processing Stripe event: {}", event.getType());

        try {
            switch (event.getType()) {
                case "payment_intent.succeeded":
                    handlePaymentIntentSucceeded(event);
                    break;
                case "payment_intent.payment_failed":
                    handlePaymentIntentFailed(event);
                    break;
                case "payment_intent.canceled":
                    handlePaymentIntentCanceled(event);
                    break;
                case "charge.refunded":
                    handleChargeRefunded(event);
                    break;
                default:
                    log.info("Unhandled Stripe event type: {}", event.getType());
            }
            return ResponseEntity.ok("Webhook processed");
        } catch (Exception e) {
            log.error("Error processing Stripe webhook", e);
            return ResponseEntity.internalServerError().body("Error processing webhook");
        }
    }

    private void handlePaymentIntentSucceeded(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow();

        log.info("Payment intent succeeded: {}", paymentIntent.getId());

        paymentRepository.findByPaymentIntentId(paymentIntent.getId())
                .ifPresent(payment -> {
                    payment.setStatus(Payment.PaymentStatus.COMPLETED);
                    payment.setCompletedAt(LocalDateTime.now());
                    paymentRepository.save(payment);
                    
                    kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, "payment.completed", payment);
                    log.info("Payment completed: {}", payment.getId());
                });
    }

    private void handlePaymentIntentFailed(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow();

        log.info("Payment intent failed: {}", paymentIntent.getId());

        paymentRepository.findByPaymentIntentId(paymentIntent.getId())
                .ifPresent(payment -> {
                    payment.setStatus(Payment.PaymentStatus.FAILED);
                    payment.setFailureReason(paymentIntent.getLastPaymentError() != null 
                            ? paymentIntent.getLastPaymentError().getMessage() 
                            : "Payment failed");
                    paymentRepository.save(payment);
                    
                    kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, "payment.failed", payment);
                    log.info("Payment failed: {}", payment.getId());
                });
    }

    private void handlePaymentIntentCanceled(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow();

        log.info("Payment intent canceled: {}", paymentIntent.getId());

        paymentRepository.findByPaymentIntentId(paymentIntent.getId())
                .ifPresent(payment -> {
                    payment.setStatus(Payment.PaymentStatus.CANCELLED);
                    paymentRepository.save(payment);
                    
                    kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, "payment.cancelled", payment);
                    log.info("Payment cancelled: {}", payment.getId());
                });
    }

    private void handleChargeRefunded(Event event) {
        // Extract charge information and update payment status
        log.info("Charge refunded event received");
        // Implementation depends on your refund tracking requirements
    }
}
