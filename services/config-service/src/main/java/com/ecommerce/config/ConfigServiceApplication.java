package com.ecommerce.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Config Service Application using Spring Cloud Config Server.
 * Provides centralized configuration management for all microservices.
 * 
 * @author Enterprise E-commerce Platform Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableConfigServer
public class ConfigServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServiceApplication.class, args);
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║     Config Service Started Successfully                       ║");
        System.out.println("║     Server: http://localhost:8888                             ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
    }
}
