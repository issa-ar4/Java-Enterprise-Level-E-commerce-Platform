# Service Discovery (Eureka Server)

Netflix Eureka Server for service registration and discovery in the microservices architecture.

## Overview

This service acts as a service registry where all microservices register themselves and discover other services. It provides:

- **Service Registration**: Microservices automatically register on startup
- **Service Discovery**: Services can find and communicate with each other
- **Health Checking**: Monitors the health of registered services
- **Load Balancing**: Enables client-side load balancing

## Running the Service

### Using Maven
```bash
./mvnw spring-boot:run
```

### Using Docker
```bash
docker build -t service-discovery:1.0.0 .
docker run -p 8761:8761 service-discovery:1.0.0
```

## Accessing the Dashboard

Once started, access the Eureka Dashboard at:
- **URL**: http://localhost:8761
- **Description**: View all registered services and their instances

## Configuration

### Port
- **Default Port**: 8761

### Key Configuration Properties
- `eureka.client.register-with-eureka: false` - Server doesn't register with itself
- `eureka.client.fetch-registry: false` - Server doesn't fetch registry from itself
- `eureka.server.enable-self-preservation: false` - Disabled in development

## Health Check

Check service health:
```bash
curl http://localhost:8761/actuator/health
```

## Metrics

Prometheus metrics endpoint:
```bash
curl http://localhost:8761/actuator/prometheus
```

## Client Configuration

For services that need to register with Eureka:

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```

## Testing

Run tests:
```bash
./mvnw test
```

## Dependencies

- Spring Cloud Netflix Eureka Server
- Spring Boot Actuator
- Micrometer Prometheus Registry

## Production Considerations

1. **High Availability**: Run multiple Eureka instances
2. **Self-Preservation**: Enable in production (`eureka.server.enable-self-preservation: true`)
3. **Security**: Add authentication for the dashboard
4. **Monitoring**: Monitor registered services and instance counts

## Troubleshooting

### Services not showing up
- Check network connectivity
- Verify `eureka.client.service-url.defaultZone` in client configuration
- Check service logs for registration errors

### Self-preservation mode activated
- Normal in production, disabled in development
- Indicates network issues if triggered unexpectedly

## Version

- **Version**: 1.0.0
- **Spring Boot**: 3.2.0
- **Spring Cloud**: 2023.0.0
