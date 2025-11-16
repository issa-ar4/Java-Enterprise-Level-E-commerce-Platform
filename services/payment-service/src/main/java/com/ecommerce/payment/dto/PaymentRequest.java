package com.ecommerce.payment.dto;

import com.ecommerce.payment.model.Payment;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Payment request DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotBlank(message = "Order ID is required")
    private String orderId;

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    private String currency;

    @NotNull(message = "Payment method is required")
    private Payment.PaymentMethod paymentMethod;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @Email(message = "Invalid email format")
    private String customerEmail;

    private String customerName;

    // Billing address
    private String billingStreet;
    private String billingCity;
    private String billingState;
    private String billingPostalCode;
    private String billingCountry;

    // Success and cancel URLs for PayPal
    private String successUrl;
    private String cancelUrl;

    // Additional metadata
    private String metadata;
}
