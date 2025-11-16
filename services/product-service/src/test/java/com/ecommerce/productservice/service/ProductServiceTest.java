package com.ecommerce.productservice.service;

import com.ecommerce.productservice.dto.ProductRequest;
import com.ecommerce.productservice.dto.ProductResponse;
import com.ecommerce.productservice.event.ProductEvent;
import com.ecommerce.productservice.exception.ProductNotFoundException;
import com.ecommerce.productservice.model.Product;
import com.ecommerce.productservice.model.ProductVariant;
import com.ecommerce.productservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Product Service Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private KafkaTemplate<String, ProductEvent> kafkaTemplate;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private ProductRequest testRequest;
    private String productId;

    @BeforeEach
    void setUp() {
        productId = "673e8f9a1234567890abcdef";
        
        // Setup test product
        testProduct = new Product();
        testProduct.setId(productId);
        testProduct.setSku("TEST-001");
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Description");
        testProduct.setCategoryId("cat-001");
        testProduct.setCategoryName("Test Category");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setCurrency("USD");
        testProduct.setStockQuantity(100);
        testProduct.setBrand("TestBrand");
        testProduct.setTags(Arrays.asList("test", "product"));
        testProduct.setActive(true);
        testProduct.setFeatured(false);

        // Setup test request
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
    void testCreateProduct() {
        // Given
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        ProductResponse response = productService.createProduct(testRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(productId);
        assertThat(response.getSku()).isEqualTo("TEST-001");
        assertThat(response.getName()).isEqualTo("Test Product");
        assertThat(response.getPrice()).isEqualByComparingTo(new BigDecimal("99.99"));

        verify(productRepository, times(1)).save(any(Product.class));
        verify(kafkaTemplate, times(1)).send(eq("product-events"), any(ProductEvent.class));
    }

    @Test
    @DisplayName("Should get product by ID successfully")
    void testGetProductById() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        // When
        ProductResponse response = productService.getProductById(productId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(productId);
        assertThat(response.getName()).isEqualTo("Test Product");

        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    @DisplayName("Should throw exception when product not found by ID")
    void testGetProductByIdNotFound() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> productService.getProductById(productId))
            .isInstanceOf(ProductNotFoundException.class)
            .hasMessageContaining("Product not found with id: " + productId);

        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    @DisplayName("Should get product by SKU successfully")
    void testGetProductBySku() {
        // Given
        when(productRepository.findBySku("TEST-001")).thenReturn(Optional.of(testProduct));

        // When
        ProductResponse response = productService.getProductBySku("TEST-001");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getSku()).isEqualTo("TEST-001");

        verify(productRepository, times(1)).findBySku("TEST-001");
    }

    @Test
    @DisplayName("Should throw exception when product not found by SKU")
    void testGetProductBySkuNotFound() {
        // Given
        when(productRepository.findBySku("INVALID-SKU")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> productService.getProductBySku("INVALID-SKU"))
            .isInstanceOf(ProductNotFoundException.class)
            .hasMessageContaining("Product not found with sku: INVALID-SKU");

        verify(productRepository, times(1)).findBySku("INVALID-SKU");
    }

    @Test
    @DisplayName("Should get all products with pagination")
    void testGetAllProducts() {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        Page<Product> productPage = new PageImpl<>(products);
        Pageable pageable = PageRequest.of(0, 20);

        when(productRepository.findAll(pageable)).thenReturn(productPage);

        // When
        Page<ProductResponse> response = productService.getAllProducts(pageable);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getId()).isEqualTo(productId);

        verify(productRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should get products by category")
    void testGetProductsByCategory() {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        Page<Product> productPage = new PageImpl<>(products);
        Pageable pageable = PageRequest.of(0, 20);

        when(productRepository.findByCategoryId("cat-001", pageable)).thenReturn(productPage);

        // When
        Page<ProductResponse> response = productService.getProductsByCategory("cat-001", pageable);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getCategoryId()).isEqualTo("cat-001");

        verify(productRepository, times(1)).findByCategoryId("cat-001", pageable);
    }

    @Test
    @DisplayName("Should search products by query")
    void testSearchProducts() {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        Page<Product> productPage = new PageImpl<>(products);
        Pageable pageable = PageRequest.of(0, 20);

        when(productRepository.searchByNameOrDescription("test", pageable)).thenReturn(productPage);

        // When
        Page<ProductResponse> response = productService.searchProducts("test", pageable);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);

        verify(productRepository, times(1)).searchByNameOrDescription("test", pageable);
    }

    @Test
    @DisplayName("Should get featured products")
    void testGetFeaturedProducts() {
        // Given
        testProduct.setFeatured(true);
        List<Product> products = Arrays.asList(testProduct);
        Page<Product> productPage = new PageImpl<>(products);
        Pageable pageable = PageRequest.of(0, 10);

        when(productRepository.findByFeaturedTrue(pageable)).thenReturn(productPage);

        // When
        Page<ProductResponse> response = productService.getFeaturedProducts(pageable);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).isFeatured()).isTrue();

        verify(productRepository, times(1)).findByFeaturedTrue(pageable);
    }

    @Test
    @DisplayName("Should update product successfully")
    void testUpdateProduct() {
        // Given
        ProductRequest updateRequest = new ProductRequest();
        updateRequest.setName("Updated Product");
        updateRequest.setDescription("Updated Description");
        updateRequest.setPrice(new BigDecimal("149.99"));
        updateRequest.setStockQuantity(150);

        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        ProductResponse response = productService.updateProduct(productId, updateRequest);

        // Then
        assertThat(response).isNotNull();
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).save(any(Product.class));
        verify(kafkaTemplate, times(1)).send(eq("product-events"), any(ProductEvent.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent product")
    void testUpdateProductNotFound() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> productService.updateProduct(productId, testRequest))
            .isInstanceOf(ProductNotFoundException.class)
            .hasMessageContaining("Product not found with id: " + productId);

        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, never()).save(any(Product.class));
        verify(kafkaTemplate, never()).send(anyString(), any(ProductEvent.class));
    }

    @Test
    @DisplayName("Should delete product successfully")
    void testDeleteProduct() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        doNothing().when(productRepository).deleteById(productId);

        // When
        productService.deleteProduct(productId);

        // Then
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).deleteById(productId);
        verify(kafkaTemplate, times(1)).send(eq("product-events"), any(ProductEvent.class));
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent product")
    void testDeleteProductNotFound() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> productService.deleteProduct(productId))
            .isInstanceOf(ProductNotFoundException.class)
            .hasMessageContaining("Product not found with id: " + productId);

        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, never()).deleteById(anyString());
        verify(kafkaTemplate, never()).send(anyString(), any(ProductEvent.class));
    }

    @Test
    @DisplayName("Should update stock successfully")
    void testUpdateStock() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        productService.updateStock(productId, 200);

        // Then
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).save(productCaptor.capture());
        
        Product savedProduct = productCaptor.getValue();
        assertThat(savedProduct.getStockQuantity()).isEqualTo(200);

        ArgumentCaptor<ProductEvent> eventCaptor = ArgumentCaptor.forClass(ProductEvent.class);
        verify(kafkaTemplate, times(1)).send(eq("product-events"), eventCaptor.capture());
        
        ProductEvent event = eventCaptor.getValue();
        assertThat(event.getEventType()).isEqualTo(ProductEvent.EventType.PRODUCT_STOCK_UPDATED);
    }

    @Test
    @DisplayName("Should throw exception when updating stock for non-existent product")
    void testUpdateStockNotFound() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> productService.updateStock(productId, 200))
            .isInstanceOf(ProductNotFoundException.class)
            .hasMessageContaining("Product not found with id: " + productId);

        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should map product to response correctly")
    void testMapToResponse() {
        // Given - product with variants
        ProductVariant variant = new ProductVariant();
        variant.setVariantId("v1");
        variant.setSku("TEST-001-VAR");
        variant.setName("Variant 1");
        variant.setPrice(new BigDecimal("109.99"));
        variant.setStockQuantity(50);
        variant.setAvailable(true);
        
        Map<String, String> attributes = new HashMap<>();
        attributes.put("color", "Red");
        attributes.put("size", "Large");
        variant.setAttributes(attributes);
        
        testProduct.setVariants(Collections.singletonList(variant));

        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        // When
        ProductResponse response = productService.getProductById(productId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(productId);
        assertThat(response.getSku()).isEqualTo("TEST-001");
        assertThat(response.getName()).isEqualTo("Test Product");
        assertThat(response.getDescription()).isEqualTo("Test Description");
        assertThat(response.getPrice()).isEqualByComparingTo(new BigDecimal("99.99"));
        assertThat(response.getCurrency()).isEqualTo("USD");
        assertThat(response.getStockQuantity()).isEqualTo(100);
        assertThat(response.getBrand()).isEqualTo("TestBrand");
        assertThat(response.getTags()).containsExactly("test", "product");
        assertThat(response.isActive()).isTrue();
        assertThat(response.isFeatured()).isFalse();
        
        // Verify variants
        assertThat(response.getVariants()).hasSize(1);
        assertThat(response.getVariants().get(0).getVariantId()).isEqualTo("v1");
        assertThat(response.getVariants().get(0).getName()).isEqualTo("Variant 1");
        assertThat(response.getVariants().get(0).getAttributes()).containsEntry("color", "Red");
    }

    @Test
    @DisplayName("Should handle product creation with variants")
    void testCreateProductWithVariants() {
        // Given
        ProductVariant variant = new ProductVariant();
        variant.setVariantId("v1");
        variant.setSku("TEST-001-VAR");
        variant.setName("Variant 1");
        variant.setPrice(new BigDecimal("109.99"));
        variant.setStockQuantity(50);
        variant.setAvailable(true);
        
        testRequest.setVariants(Collections.singletonList(variant));
        testProduct.setVariants(Collections.singletonList(variant));

        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        ProductResponse response = productService.createProduct(testRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getVariants()).hasSize(1);
        assertThat(response.getVariants().get(0).getVariantId()).isEqualTo("v1");

        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should handle empty search results")
    void testSearchProductsNoResults() {
        // Given
        Page<Product> emptyPage = new PageImpl<>(Collections.emptyList());
        Pageable pageable = PageRequest.of(0, 20);

        when(productRepository.searchByNameOrDescription("nonexistent", pageable)).thenReturn(emptyPage);

        // When
        Page<ProductResponse> response = productService.searchProducts("nonexistent", pageable);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEmpty();
        assertThat(response.getTotalElements()).isZero();

        verify(productRepository, times(1)).searchByNameOrDescription("nonexistent", pageable);
    }
}
