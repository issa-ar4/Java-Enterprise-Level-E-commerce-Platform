package com.ecommerce.product.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Product category entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "categories")
public class Category {

    @Id
    private String id;

    private String name;

    private String description;

    private String slug;

    private String parentId;

    private List<String> childIds;

    private String imageUrl;

    private Integer displayOrder;

    private Boolean active;

    @CreatedDate
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
