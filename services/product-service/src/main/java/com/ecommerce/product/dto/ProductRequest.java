package com.ecommerce.product.dto;

import com.ecommerce.product.model.ProductVariant;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Product request DTO for creating/updating products.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @NotBlank(message = "SKU is required")
    private String sku;

    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 200, message = "Product name must be between 3 and 200 characters")
    private String name;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @NotBlank(message = "Category ID is required")
    private String categoryId;

    private String categoryName;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    private BigDecimal compareAtPrice;

    @NotBlank(message = "Currency is required")
    private String currency;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    private Integer lowStockThreshold;

    private String brand;

    private List<String> tags;

    private List<String> imageUrls;

    private String primaryImageUrl;

    private List<ProductVariant> variants;

    private Map<String, String> specifications;

    private Boolean featured;
}
