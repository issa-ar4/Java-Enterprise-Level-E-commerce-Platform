package com.ecommerce.product.dto;

import com.ecommerce.product.model.ProductVariant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Product response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private String id;
    private String sku;
    private String name;
    private String description;
    private String categoryId;
    private String categoryName;
    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private String currency;
    private Integer stockQuantity;
    private Integer lowStockThreshold;
    private String brand;
    private List<String> tags;
    private List<String> imageUrls;
    private String primaryImageUrl;
    private List<ProductVariant> variants;
    private Map<String, String> specifications;
    private Double averageRating;
    private Integer reviewCount;
    private Boolean active;
    private Boolean featured;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
