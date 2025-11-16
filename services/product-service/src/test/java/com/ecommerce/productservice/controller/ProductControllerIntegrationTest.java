package com.ecommerce.productservice.controller;

import com.ecommerce.productservice.dto.ProductRequest;
import com.ecommerce.productservice.dto.ProductResponse;
import com.ecommerce.productservice.model.Product;
import com.ecommerce.productservice.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {"product-events"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Product Controller Integration Tests")
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    private ProductRequest testRequest;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

        testRequest = new ProductRequest();
        testRequest.setSku("TEST-001");
        testRequest.setName("Test Product");
        testRequest.setDescription("Test Description");
        testRequest.setCategoryId("cat-001");
        testRequest.setCategoryName("Test Category");
        testRequest.setPrice(new BigDecimal("99.99"));
        testRequest.setCurrency("USD");
        testRequest.setStockQuantity(100);
        testRequest.setBrand("TestBrand");
        testRequest.setTags(Arrays.asList("test", "product"));
    }

    @Test
    @DisplayName("Should create product successfully")
    void testCreateProduct() throws Exception {
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.sku").value("TEST-001"))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(99.99))
                .andExpect(jsonPath("$.stockQuantity").value(100))
                .andExpect(jsonPath("$.brand").value("TestBrand"));
    }

    @Test
    @DisplayName("Should return validation error for invalid product")
    void testCreateProductValidationError() throws Exception {
        ProductRequest invalidRequest = new ProductRequest();
        // Missing required fields

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get product by ID")
    void testGetProductById() throws Exception {
        // First create a product
        Product product = createTestProduct();
        String productId = product.getId();

        // Then retrieve it
        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    @DisplayName("Should return 404 for non-existent product")
    void testGetProductByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/products/{id}", "nonexistent-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should get product by SKU")
    void testGetProductBySku() throws Exception {
        // First create a product
        createTestProduct();

        // Then retrieve it by SKU
        mockMvc.perform(get("/api/products/sku/{sku}", "TEST-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("TEST-001"))
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    @DisplayName("Should get all products with pagination")
    void testGetAllProducts() throws Exception {
        // Create multiple products
        createTestProduct();
        createTestProduct("TEST-002", "Test Product 2");
        createTestProduct("TEST-003", "Test Product 3");

        mockMvc.perform(get("/api/products")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @DisplayName("Should get products by category")
    void testGetProductsByCategory() throws Exception {
        // Create products with same category
        createTestProduct();
        createTestProduct("TEST-002", "Test Product 2");

        mockMvc.perform(get("/api/products/category/{categoryId}", "cat-001")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    @DisplayName("Should search products by query")
    void testSearchProducts() throws Exception {
        // Create products
        createTestProduct("LAPTOP-001", "Gaming Laptop");
        createTestProduct("LAPTOP-002", "Business Laptop");
        createTestProduct("PHONE-001", "Smartphone");

        mockMvc.perform(get("/api/products/search")
                .param("query", "laptop")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    @DisplayName("Should get featured products")
    void testGetFeaturedProducts() throws Exception {
        // Create featured and non-featured products
        Product featured1 = createTestProduct("FEATURED-001", "Featured Product 1");
        featured1.setFeatured(true);
        productRepository.save(featured1);

        Product featured2 = createTestProduct("FEATURED-002", "Featured Product 2");
        featured2.setFeatured(true);
        productRepository.save(featured2);

        createTestProduct("REGULAR-001", "Regular Product");

        mockMvc.perform(get("/api/products/featured")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].featured", everyItem(is(true))));
    }

    @Test
    @DisplayName("Should update product")
    void testUpdateProduct() throws Exception {
        // Create product
        Product product = createTestProduct();
        String productId = product.getId();

        // Update request
        ProductRequest updateRequest = new ProductRequest();
        updateRequest.setName("Updated Product Name");
        updateRequest.setPrice(new BigDecimal("149.99"));
        updateRequest.setStockQuantity(150);

        mockMvc.perform(put("/api/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.name").value("Updated Product Name"))
                .andExpect(jsonPath("$.price").value(149.99))
                .andExpect(jsonPath("$.stockQuantity").value(150));
    }

    @Test
    @DisplayName("Should update stock")
    void testUpdateStock() throws Exception {
        // Create product
        Product product = createTestProduct();
        String productId = product.getId();

        mockMvc.perform(patch("/api/products/{id}/stock", productId)
                .param("quantity", "200"))
                .andExpect(status().isOk());

        // Verify stock was updated
        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(200));
    }

    @Test
    @DisplayName("Should delete product")
    void testDeleteProduct() throws Exception {
        // Create product
        Product product = createTestProduct();
        String productId = product.getId();

        // Delete product
        mockMvc.perform(delete("/api/products/{id}", productId))
                .andExpect(status().isNoContent());

        // Verify product was deleted
        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should handle pagination with sorting")
    void testPaginationWithSorting() throws Exception {
        // Create products with different prices
        createTestProductWithPrice("PROD-001", "Product 1", new BigDecimal("50.00"));
        createTestProductWithPrice("PROD-002", "Product 2", new BigDecimal("100.00"));
        createTestProductWithPrice("PROD-003", "Product 3", new BigDecimal("75.00"));

        mockMvc.perform(get("/api/products")
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "price")
                .param("sortDirection", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].price").value(50.00))
                .andExpect(jsonPath("$.content[1].price").value(75.00))
                .andExpect(jsonPath("$.content[2].price").value(100.00));
    }

    @Test
    @DisplayName("Should handle empty search results")
    void testSearchProductsNoResults() throws Exception {
        mockMvc.perform(get("/api/products/search")
                .param("query", "nonexistent")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // Helper methods

    private Product createTestProduct() {
        return createTestProduct("TEST-001", "Test Product");
    }

    private Product createTestProduct(String sku, String name) {
        Product product = new Product();
        product.setSku(sku);
        product.setName(name);
        product.setDescription("Test Description");
        product.setCategoryId("cat-001");
        product.setCategoryName("Test Category");
        product.setPrice(new BigDecimal("99.99"));
        product.setCurrency("USD");
        product.setStockQuantity(100);
        product.setBrand("TestBrand");
        product.setTags(Arrays.asList("test", "product"));
        product.setActive(true);
        product.setFeatured(false);
        
        return productRepository.save(product);
    }

    private Product createTestProductWithPrice(String sku, String name, BigDecimal price) {
        Product product = new Product();
        product.setSku(sku);
        product.setName(name);
        product.setDescription("Test Description");
        product.setCategoryId("cat-001");
        product.setCategoryName("Test Category");
        product.setPrice(price);
        product.setCurrency("USD");
        product.setStockQuantity(100);
        product.setBrand("TestBrand");
        product.setTags(Arrays.asList("test", "product"));
        product.setActive(true);
        product.setFeatured(false);
        
        return productRepository.save(product);
    }
}
