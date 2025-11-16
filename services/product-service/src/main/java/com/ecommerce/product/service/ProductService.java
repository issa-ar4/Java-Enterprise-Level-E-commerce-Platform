package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.event.ProductEvent;
import com.ecommerce.product.exception.ProductNotFoundException;
import com.ecommerce.product.model.Product;
import com.ecommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing products.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final KafkaTemplate<String, ProductEvent> kafkaTemplate;
    private static final String PRODUCT_TOPIC = "product-events";

    @Cacheable(value = "products", key = "#id")
    public ProductResponse getProductById(String id) {
        log.info("Fetching product with id: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        return mapToResponse(product);
    }

    public ProductResponse getProductBySku(String sku) {
        log.info("Fetching product with SKU: {}", sku);
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with SKU: " + sku));
        return mapToResponse(product);
    }

    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        log.info("Fetching all products");
        return productRepository.findByActiveTrue(pageable)
                .map(this::mapToResponse);
    }

    public Page<ProductResponse> getProductsByCategory(String categoryId, Pageable pageable) {
        log.info("Fetching products for category: {}", categoryId);
        return productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable)
                .map(this::mapToResponse);
    }

    public Page<ProductResponse> searchProducts(String query, Pageable pageable) {
        log.info("Searching products with query: {}", query);
        return productRepository.findByNameContainingIgnoreCaseAndActiveTrue(query, pageable)
                .map(this::mapToResponse);
    }

    public List<ProductResponse> getFeaturedProducts() {
        log.info("Fetching featured products");
        return productRepository.findByFeaturedTrueAndActiveTrueOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse createProduct(ProductRequest request) {
        log.info("Creating new product: {}", request.getName());
        
        Product product = Product.builder()
                .sku(request.getSku())
                .name(request.getName())
                .description(request.getDescription())
                .categoryId(request.getCategoryId())
                .categoryName(request.getCategoryName())
                .price(request.getPrice())
                .compareAtPrice(request.getCompareAtPrice())
                .currency(request.getCurrency())
                .stockQuantity(request.getStockQuantity())
                .lowStockThreshold(request.getLowStockThreshold())
                .brand(request.getBrand())
                .tags(request.getTags())
                .imageUrls(request.getImageUrls())
                .primaryImageUrl(request.getPrimaryImageUrl())
                .variants(request.getVariants())
                .specifications(request.getSpecifications())
                .averageRating(0.0)
                .reviewCount(0)
                .active(true)
                .featured(request.getFeatured() != null && request.getFeatured())
                .createdBy("system") // TODO: Get from security context
                .build();

        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with id: {}", savedProduct.getId());

        // Publish product created event
        publishProductEvent(savedProduct, "PRODUCT_CREATED");

        return mapToResponse(savedProduct);
    }

    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public ProductResponse updateProduct(String id, ProductRequest request) {
        log.info("Updating product with id: {}", id);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategoryId(request.getCategoryId());
        product.setCategoryName(request.getCategoryName());
        product.setPrice(request.getPrice());
        product.setCompareAtPrice(request.getCompareAtPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setLowStockThreshold(request.getLowStockThreshold());
        product.setBrand(request.getBrand());
        product.setTags(request.getTags());
        product.setImageUrls(request.getImageUrls());
        product.setPrimaryImageUrl(request.getPrimaryImageUrl());
        product.setVariants(request.getVariants());
        product.setSpecifications(request.getSpecifications());
        product.setFeatured(request.getFeatured());
        product.setUpdatedBy("system"); // TODO: Get from security context
        product.setUpdatedAt(LocalDateTime.now());

        Product updatedProduct = productRepository.save(product);
        log.info("Product updated successfully");

        // Publish product updated event
        publishProductEvent(updatedProduct, "PRODUCT_UPDATED");

        return mapToResponse(updatedProduct);
    }

    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(String id) {
        log.info("Deleting product with id: {}", id);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        product.setActive(false);
        productRepository.save(product);
        log.info("Product deleted successfully");

        // Publish product deleted event
        publishProductEvent(product, "PRODUCT_DELETED");
    }

    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public void updateStock(String id, Integer quantity) {
        log.info("Updating stock for product: {} to quantity: {}", id, quantity);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        product.setStockQuantity(quantity);
        productRepository.save(product);

        // Publish stock updated event
        publishProductEvent(product, "PRODUCT_STOCK_UPDATED");
    }

    private void publishProductEvent(Product product, String eventType) {
        try {
            ProductEvent event = ProductEvent.builder()
                    .eventType(eventType)
                    .productId(product.getId())
                    .sku(product.getSku())
                    .name(product.getName())
                    .price(product.getPrice())
                    .stockQuantity(product.getStockQuantity())
                    .timestamp(LocalDateTime.now())
                    .build();

            kafkaTemplate.send(PRODUCT_TOPIC, product.getId(), event);
            log.info("Published {} event for product: {}", eventType, product.getId());
        } catch (Exception e) {
            log.error("Failed to publish event for product: {}", product.getId(), e);
        }
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .categoryId(product.getCategoryId())
                .categoryName(product.getCategoryName())
                .price(product.getPrice())
                .compareAtPrice(product.getCompareAtPrice())
                .currency(product.getCurrency())
                .stockQuantity(product.getStockQuantity())
                .lowStockThreshold(product.getLowStockThreshold())
                .brand(product.getBrand())
                .tags(product.getTags())
                .imageUrls(product.getImageUrls())
                .primaryImageUrl(product.getPrimaryImageUrl())
                .variants(product.getVariants())
                .specifications(product.getSpecifications())
                .averageRating(product.getAverageRating())
                .reviewCount(product.getReviewCount())
                .active(product.getActive())
                .featured(product.getFeatured())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
