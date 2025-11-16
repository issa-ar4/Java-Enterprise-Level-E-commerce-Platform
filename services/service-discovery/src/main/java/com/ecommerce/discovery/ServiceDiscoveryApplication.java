package com.ecommerce.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Service Discovery Application using Netflix Eureka Server.
 * This service acts as a registry where all microservices register themselves.
 * 
 * @author Enterprise E-commerce Platform Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableEurekaServer
public class ServiceDiscoveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceDiscoveryApplication.class, args);
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║     Service Discovery (Eureka Server) Started Successfully    ║");
        System.out.println("║     Dashboard: http://localhost:8761                          ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
    }
}
