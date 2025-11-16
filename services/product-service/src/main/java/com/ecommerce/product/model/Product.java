package com.ecommerce.product.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Product entity stored in MongoDB.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
public class Product {

    @Id
    private String id;

    @Indexed
    private String sku;

    @Indexed
    private String name;

    private String description;

    @Indexed
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

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private String createdBy;

    private String updatedBy;
}
