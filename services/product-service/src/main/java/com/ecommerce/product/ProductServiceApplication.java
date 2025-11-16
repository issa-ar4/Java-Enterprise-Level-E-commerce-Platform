package com.ecommerce.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Product Service Application.
 * Manages product catalog with MongoDB and Redis caching.
 * 
 * @author Enterprise E-commerce Platform Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableCaching
@EnableMongoAuditing
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║     Product Service Started Successfully                      ║");
        System.out.println("║     API: http://localhost:8082                                ║");
        System.out.println("║     Swagger: http://localhost:8082/swagger-ui.html            ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
    }
}
