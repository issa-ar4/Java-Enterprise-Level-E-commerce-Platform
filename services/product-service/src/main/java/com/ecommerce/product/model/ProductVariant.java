package com.ecommerce.product.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Product variant (e.g., different sizes, colors).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant {

    private String variantId;
    private String sku;
    private String name;
    private Map<String, String> attributes; // e.g., {"size": "M", "color": "Blue"}
    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private Integer stockQuantity;
    private String imageUrl;
    private Boolean available;
}
