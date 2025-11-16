package com.ecommerce.payment.controller;

import com.ecommerce.payment.model.Payment;
import com.ecommerce.payment.repository.PaymentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * PayPal webhook handler.
 */
@RestController
@RequestMapping("/api/webhooks/paypal")
@RequiredArgsConstructor
@Slf4j
public class PayPalWebhookController {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String PAYMENT_EVENTS_TOPIC = "payment-events";

    @PostMapping
    public ResponseEntity<String> handlePayPalWebhook(@RequestBody String payload) {
        log.info("Received PayPal webhook");

        try {
            JsonNode event = objectMapper.readTree(payload);
            String eventType = event.get("event_type").asText();
            
            log.info("Processing PayPal event: {}", eventType);

            switch (eventType) {
                case "PAYMENT.CAPTURE.COMPLETED":
                    handlePaymentCaptureCompleted(event);
                    break;
                case "PAYMENT.CAPTURE.DENIED":
                    handlePaymentCaptureDenied(event);
                    break;
                case "PAYMENT.CAPTURE.REFUNDED":
                    handlePaymentCaptureRefunded(event);
                    break;
                case "CHECKOUT.ORDER.APPROVED":
                    handleCheckoutOrderApproved(event);
                    break;
                default:
                    log.info("Unhandled PayPal event type: {}", eventType);
            }
            
            return ResponseEntity.ok("Webhook processed");
        } catch (Exception e) {
            log.error("Error processing PayPal webhook", e);
            return ResponseEntity.internalServerError().body("Error processing webhook");
        }
    }

    private void handlePaymentCaptureCompleted(JsonNode event) {
        try {
            JsonNode resource = event.get("resource");
            String orderId = resource.has("supplementary_data") 
                    ? resource.get("supplementary_data").get("related_ids").get("order_id").asText()
                    : null;

            if (orderId != null) {
                log.info("Payment capture completed for order: {}", orderId);
                
                paymentRepository.findByTransactionId(orderId)
                        .ifPresent(payment -> {
                            payment.setStatus(Payment.PaymentStatus.COMPLETED);
                            payment.setCompletedAt(LocalDateTime.now());
                            paymentRepository.save(payment);
                            
                            kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, "payment.completed", payment);
                            log.info("Payment completed: {}", payment.getId());
                        });
            }
        } catch (Exception e) {
            log.error("Error handling PayPal payment capture completed", e);
        }
    }

    private void handlePaymentCaptureDenied(JsonNode event) {
        try {
            JsonNode resource = event.get("resource");
            String orderId = resource.has("supplementary_data") 
                    ? resource.get("supplementary_data").get("related_ids").get("order_id").asText()
                    : null;

            if (orderId != null) {
                log.info("Payment capture denied for order: {}", orderId);
                
                paymentRepository.findByTransactionId(orderId)
                        .ifPresent(payment -> {
                            payment.setStatus(Payment.PaymentStatus.FAILED);
                            payment.setFailureReason("Payment capture denied by PayPal");
                            paymentRepository.save(payment);
                            
                            kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, "payment.failed", payment);
                            log.info("Payment failed: {}", payment.getId());
                        });
            }
        } catch (Exception e) {
            log.error("Error handling PayPal payment capture denied", e);
        }
    }

    private void handlePaymentCaptureRefunded(JsonNode event) {
        try {
            JsonNode resource = event.get("resource");
            log.info("Payment capture refunded: {}", resource);
            
            // Update payment refund status
            // Implementation depends on your refund tracking requirements
        } catch (Exception e) {
            log.error("Error handling PayPal payment capture refunded", e);
        }
    }

    private void handleCheckoutOrderApproved(JsonNode event) {
        try {
            JsonNode resource = event.get("resource");
            String orderId = resource.get("id").asText();
            
            log.info("Checkout order approved: {}", orderId);
            
            paymentRepository.findByTransactionId(orderId)
                    .ifPresent(payment -> {
                        payment.setStatus(Payment.PaymentStatus.PROCESSING);
                        paymentRepository.save(payment);
                        
                        kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, "payment.approved", payment);
                        log.info("Payment approved: {}", payment.getId());
                    });
        } catch (Exception e) {
            log.error("Error handling PayPal checkout order approved", e);
        }
    }
}
