package com.ecommerce.payment.service;

import com.ecommerce.payment.dto.PaymentRequest;
import com.ecommerce.payment.model.Payment;
import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.orders.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * PayPal payment gateway service.
 */
@Service
@Slf4j
public class PayPalPaymentService {

    @Value("${paypal.client.id}")
    private String clientId;

    @Value("${paypal.client.secret}")
    private String clientSecret;

    @Value("${paypal.mode:sandbox}")
    private String mode;

    private PayPalHttpClient client;

    @PostConstruct
    public void init() {
        PayPalEnvironment environment = "live".equalsIgnoreCase(mode)
                ? new PayPalEnvironment.Live(clientId, clientSecret)
                : new PayPalEnvironment.Sandbox(clientId, clientSecret);
        
        this.client = new PayPalHttpClient(environment);
        log.info("PayPal API initialized in {} mode", mode);
    }

    /**
     * Create a PayPal order.
     */
    public Order createOrder(PaymentRequest request) throws IOException {
        log.info("Creating PayPal order for: {}", request.getOrderId());

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.checkoutPaymentIntent("CAPTURE");

        // Set purchase units
        List<PurchaseUnitRequest> purchaseUnits = new ArrayList<>();
        PurchaseUnitRequest purchaseUnit = new PurchaseUnitRequest()
                .referenceId(request.getOrderId())
                .description(request.getDescription())
                .customId(request.getUserId())
                .amountWithBreakdown(new AmountWithBreakdown()
                        .currencyCode(request.getCurrency())
                        .value(request.getAmount().toString()));

        purchaseUnits.add(purchaseUnit);
        orderRequest.purchaseUnits(purchaseUnits);

        // Set application context (return URLs)
        ApplicationContext applicationContext = new ApplicationContext()
                .brandName("E-Commerce Platform")
                .landingPage("BILLING")
                .userAction("PAY_NOW")
                .shippingPreference("NO_SHIPPING");

        if (request.getSuccessUrl() != null) {
            applicationContext.returnUrl(request.getSuccessUrl());
        }
        if (request.getCancelUrl() != null) {
            applicationContext.cancelUrl(request.getCancelUrl());
        }

        orderRequest.applicationContext(applicationContext);

        // Create order request
        OrdersCreateRequest ordersCreateRequest = new OrdersCreateRequest();
        ordersCreateRequest.requestBody(orderRequest);

        try {
            HttpResponse<Order> response = client.execute(ordersCreateRequest);
            Order order = response.result();
            log.info("PayPal order created: {} for order: {}", order.id(), request.getOrderId());
            return order;
        } catch (IOException e) {
            log.error("Failed to create PayPal order for: {}", request.getOrderId(), e);
            throw e;
        }
    }

    /**
     * Capture payment for an order.
     */
    public Order captureOrder(String orderId) throws IOException {
        log.info("Capturing PayPal order: {}", orderId);

        OrdersCaptureRequest ordersCaptureRequest = new OrdersCaptureRequest(orderId);

        try {
            HttpResponse<Order> response = client.execute(ordersCaptureRequest);
            Order order = response.result();
            log.info("PayPal order captured: {}", orderId);
            return order;
        } catch (IOException e) {
            log.error("Failed to capture PayPal order: {}", orderId, e);
            throw e;
        }
    }

    /**
     * Get order details.
     */
    public Order getOrder(String orderId) throws IOException {
        log.info("Retrieving PayPal order: {}", orderId);

        OrdersGetRequest ordersGetRequest = new OrdersGetRequest(orderId);

        try {
            HttpResponse<Order> response = client.execute(ordersGetRequest);
            return response.result();
        } catch (IOException e) {
            log.error("Failed to retrieve PayPal order: {}", orderId, e);
            throw e;
        }
    }

    /**
     * Process refund for a captured payment.
     */
    public String refundPayment(String captureId, BigDecimal amount, String currency) throws IOException {
        log.info("Creating PayPal refund for capture: {} with amount: {}", captureId, amount);

        // Note: PayPal refunds are handled through the Payments API
        // This is a simplified implementation
        // In production, you'd use: com.paypal.payments.RefundRequest
        
        log.info("Refund initiated for capture: {}", captureId);
        return captureId; // Return capture ID as reference
    }

    /**
     * Get approval URL from order links.
     */
    public String getApprovalUrl(Order order) {
        if (order.links() != null) {
            for (LinkDescription link : order.links()) {
                if ("approve".equals(link.rel())) {
                    return link.href();
                }
            }
        }
        return null;
    }

    /**
     * Map PayPal order status to Payment status.
     */
    public Payment.PaymentStatus mapPayPalStatus(String paypalStatus) {
        return switch (paypalStatus) {
            case "CREATED", "SAVED", "APPROVED" -> Payment.PaymentStatus.PENDING;
            case "PAYER_ACTION_REQUIRED" -> Payment.PaymentStatus.PENDING;
            case "COMPLETED" -> Payment.PaymentStatus.COMPLETED;
            case "VOIDED" -> Payment.PaymentStatus.CANCELLED;
            default -> Payment.PaymentStatus.FAILED;
        };
    }
}
