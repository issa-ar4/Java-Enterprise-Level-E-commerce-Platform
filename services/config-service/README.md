# Config Service

Spring Cloud Config Server for centralized configuration management across all microservices.

## Overview

This service provides:
- **Centralized Configuration**: Single source of truth for all application configurations
- **Environment-Specific Configs**: Different configurations for dev, staging, production
- **Dynamic Configuration**: Configurations can be updated without redeployment
- **Encryption Support**: Sensitive data can be encrypted
- **Version Control**: Configuration history tracked in Git

## Running the Service

### Using Maven
```bash
./mvnw spring-boot:run
```

### Using Docker
```bash
docker build -t config-service:1.0.0 .
docker run -p 8888:8888 config-service:1.0.0
```

## Setup Configuration Repository

### Local Development (File System)
```bash
# Create config repository directory
mkdir ~/config-repo
cd ~/config-repo
git init

# Create application configurations
echo "app.message=Hello from Config Server" > application.yml
git add .
git commit -m "Initial config"
```

### Production (Git Repository)
1. Create a private Git repository
2. Update `application.yml` with repository URL
3. Add credentials for Git access

## Configuration Files Structure

```
config-repo/
├── application.yml              # Common to all services
├── application-dev.yml          # Development environment
├── application-prod.yml         # Production environment
├── user-service.yml             # User Service specific
├── product-service.yml          # Product Service specific
└── ...
```

## Accessing Configurations

### URL Pattern
```
http://localhost:8888/{application}/{profile}/{label}
http://localhost:8888/{application}-{profile}.yml
```

### Examples
```bash
# Get user-service configuration for dev profile
curl -u admin:admin123 http://localhost:8888/user-service/dev

# Get default configuration
curl -u admin:admin123 http://localhost:8888/application/default
```

## Client Configuration

Services that need to use Config Server should include:

```yaml
spring:
  cloud:
    config:
      uri: http://localhost:8888
      username: admin
      password: admin123
      fail-fast: true
  config:
    import: optional:configserver:http://localhost:8888
```

## Security

The Config Server is protected with Basic Authentication:
- **Username**: admin
- **Password**: admin123 (change in production!)

## Encryption

### Encrypt a Value
```bash
curl -u admin:admin123 http://localhost:8888/encrypt -d "mysecret"
```

### Decrypt a Value
```bash
curl -u admin:admin123 http://localhost:8888/decrypt -d "{encrypted-value}"
```

### Use in Configuration
```yaml
datasource:
  password: '{cipher}AQA...'
```

## Refresh Configuration

### Refresh a Single Service
```bash
curl -X POST http://localhost:8081/actuator/refresh
```

### Refresh All Services (with Spring Cloud Bus)
```bash
curl -X POST http://localhost:8888/actuator/bus-refresh
```

## Health Check

```bash
curl http://localhost:8888/actuator/health
```

## Testing

```bash
./mvnw test
```

## Production Considerations

1. **Use Git Repository**: Store configs in version-controlled Git repo
2. **Enable Encryption**: Use encryption for sensitive data
3. **Secure Endpoints**: Use strong passwords and HTTPS
4. **High Availability**: Run multiple instances
5. **Monitoring**: Monitor configuration changes and access

## Troubleshooting

### Configuration not found
- Verify Git repository URL
- Check file names and paths
- Ensure Git repository is accessible

### Authentication failures
- Verify username and password
- Check client configuration

## Version

- **Version**: 1.0.0
- **Spring Boot**: 3.2.0
- **Spring Cloud**: 2023.0.0
