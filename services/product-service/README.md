# Product Service

## Overview

The Product Service is a microservice responsible for managing the product catalog in the e-commerce platform. It provides a comprehensive API for creating, updating, retrieving, and deleting products, as well as managing product categories, variants, and stock levels.

## Features

- ✅ **Product Management**: Full CRUD operations for products
- ✅ **Category Management**: Hierarchical category structure
- ✅ **Product Variants**: Support for multiple variants (size, color, etc.)
- ✅ **Search & Filter**: Search products by name, category, tags
- ✅ **Stock Management**: Track and update inventory levels
- ✅ **Redis Caching**: Performance optimization with caching
- ✅ **Event Publishing**: Kafka events for product lifecycle
- ✅ **API Documentation**: OpenAPI/Swagger integration
- ✅ **Monitoring**: Prometheus metrics and health checks
- ✅ **Service Discovery**: Eureka client registration
- ✅ **Centralized Config**: Spring Cloud Config integration

## Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17+
- **Database**: MongoDB
- **Cache**: Redis
- **Messaging**: Apache Kafka
- **Service Discovery**: Netflix Eureka
- **Config Management**: Spring Cloud Config
- **API Documentation**: Swagger/OpenAPI 3.0
- **Monitoring**: Micrometer + Prometheus
- **Build Tool**: Maven 3.9.9

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Product Service                          │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐  │
│  │              │    │              │    │              │  │
│  │  Controller  │───▶│   Service    │───▶│  Repository  │  │
│  │    Layer     │    │    Layer     │    │    Layer     │  │
│  │              │    │              │    │              │  │
│  └──────────────┘    └──────────────┘    └──────────────┘  │
│         │                    │                    │          │
│         │                    │                    │          │
│         ▼                    ▼                    ▼          │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐  │
│  │              │    │              │    │              │  │
│  │  Validation  │    │  Caching     │    │  MongoDB     │  │
│  │   & DTOs     │    │  (Redis)     │    │   Database   │  │
│  │              │    │              │    │              │  │
│  └──────────────┘    └──────────────┘    └──────────────┘  │
│                             │                                │
│                             ▼                                │
│                      ┌──────────────┐                        │
│                      │              │                        │
│                      │    Kafka     │                        │
│                      │  Publisher   │                        │
│                      │              │                        │
│                      └──────────────┘                        │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

## Data Model

### Product Entity

```java
{
  "id": "string",
  "sku": "string (unique)",
  "name": "string",
  "slug": "string",
  "description": "string",
  "categoryId": "string",
  "categoryName": "string",
  "price": "decimal",
  "compareAtPrice": "decimal",
  "currency": "string",
  "stockQuantity": "integer",
  "lowStockThreshold": "integer",
  "brand": "string",
  "tags": ["string"],
  "imageUrls": ["string"],
  "primaryImageUrl": "string",
  "variants": [
    {
      "variantId": "string",
      "sku": "string",
      "name": "string",
      "attributes": {"key": "value"},
      "price": "decimal",
      "stockQuantity": "integer",
      "available": "boolean"
    }
  ],
  "specifications": {"key": "value"},
  "averageRating": "decimal",
  "reviewCount": "integer",
  "featured": "boolean",
  "active": "boolean",
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

### Category Entity

```java
{
  "id": "string",
  "name": "string",
  "slug": "string",
  "description": "string",
  "parentId": "string",
  "imageUrl": "string",
  "active": "boolean",
  "displayOrder": "integer",
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

## API Endpoints

### Product Endpoints

#### Get All Products
```http
GET /api/products?page=0&size=20&sortBy=createdAt&sortDirection=DESC
```

**Query Parameters:**
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)
- `sortBy` (optional): Sort field (default: createdAt)
- `sortDirection` (optional): ASC or DESC (default: DESC)

**Response:**
```json
{
  "content": [...],
  "pageable": {...},
  "totalElements": 100,
  "totalPages": 5,
  "last": false,
  "first": true,
  "numberOfElements": 20
}
```

#### Get Product by ID
```http
GET /api/products/{id}
```

**Response:**
```json
{
  "id": "673e8f9a1234567890abcdef",
  "sku": "LAPTOP-001",
  "name": "Gaming Laptop",
  "description": "High-performance gaming laptop",
  "price": 1299.99,
  "stockQuantity": 50,
  ...
}
```

#### Get Product by SKU
```http
GET /api/products/sku/{sku}
```

#### Get Products by Category
```http
GET /api/products/category/{categoryId}?page=0&size=20
```

#### Search Products
```http
GET /api/products/search?query=laptop&page=0&size=20
```

**Query Parameters:**
- `query` (required): Search term
- `page` (optional): Page number
- `size` (optional): Page size

#### Get Featured Products
```http
GET /api/products/featured?page=0&size=10
```

#### Create Product
```http
POST /api/products
Content-Type: application/json

{
  "sku": "LAPTOP-001",
  "name": "Gaming Laptop",
  "description": "High-performance gaming laptop",
  "categoryId": "electronics",
  "categoryName": "Electronics",
  "price": 1299.99,
  "compareAtPrice": 1499.99,
  "currency": "USD",
  "stockQuantity": 50,
  "lowStockThreshold": 10,
  "brand": "TechBrand",
  "tags": ["laptop", "gaming", "electronics"],
  "imageUrls": [
    "https://example.com/laptop1.jpg",
    "https://example.com/laptop2.jpg"
  ],
  "primaryImageUrl": "https://example.com/laptop1.jpg",
  "variants": [
    {
      "variantId": "v1",
      "sku": "LAPTOP-001-16GB",
      "name": "16GB RAM",
      "attributes": {
        "ram": "16GB",
        "storage": "512GB SSD"
      },
      "price": 1299.99,
      "stockQuantity": 30,
      "available": true
    }
  ],
  "specifications": {
    "processor": "Intel Core i7",
    "graphics": "NVIDIA RTX 3060",
    "screen": "15.6 inch Full HD"
  },
  "featured": true
}
```

#### Update Product
```http
PUT /api/products/{id}
Content-Type: application/json

{
  "name": "Updated Gaming Laptop",
  "price": 1199.99,
  ...
}
```

#### Delete Product
```http
DELETE /api/products/{id}
```

#### Update Stock
```http
PATCH /api/products/{id}/stock?quantity=100
```

**Query Parameters:**
- `quantity` (required): New stock quantity

## Kafka Events

The service publishes events to the `product-events` topic:

### Event Types

1. **PRODUCT_CREATED**
```json
{
  "eventId": "uuid",
  "eventType": "PRODUCT_CREATED",
  "productId": "string",
  "sku": "string",
  "timestamp": "2025-11-16T10:30:00Z",
  "data": {...product details...}
}
```

2. **PRODUCT_UPDATED**
```json
{
  "eventId": "uuid",
  "eventType": "PRODUCT_UPDATED",
  "productId": "string",
  "sku": "string",
  "timestamp": "2025-11-16T10:30:00Z",
  "data": {...updated product details...}
}
```

3. **PRODUCT_DELETED**
```json
{
  "eventId": "uuid",
  "eventType": "PRODUCT_DELETED",
  "productId": "string",
  "sku": "string",
  "timestamp": "2025-11-16T10:30:00Z",
  "data": null
}
```

4. **PRODUCT_STOCK_UPDATED**
```json
{
  "eventId": "uuid",
  "eventType": "PRODUCT_STOCK_UPDATED",
  "productId": "string",
  "sku": "string",
  "timestamp": "2025-11-16T10:30:00Z",
  "data": {
    "oldStock": 50,
    "newStock": 45,
    "change": -5
  }
}
```

## Redis Caching

### Cache Strategy

- **Cache Key Pattern**: `products::{productId}`
- **TTL**: 10 minutes
- **Eviction Policy**: LRU (Least Recently Used)

### Cached Operations

- ✅ `findById()` - Cached with TTL
- ✅ `findBySku()` - Cached with TTL
- ❌ `findAll()` - Not cached (too large)
- ❌ `search()` - Not cached (dynamic)

### Cache Invalidation

Cache is automatically invalidated on:
- Product update
- Product deletion
- Stock update

## Configuration

### application.yml

```yaml
server:
  port: 8082

spring:
  application:
    name: product-service
  
  data:
    mongodb:
      host: localhost
      port: 27017
      database: product-db
      auto-index-creation: true
  
  cache:
    type: redis
    redis:
      time-to-live: 600000  # 10 minutes
  
  data:
    redis:
      host: localhost
      port: 6379
      timeout: 2000ms
  
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    template:
      default-topic: product-events
```

### Environment Variables

You can override configuration using environment variables:

```bash
# Server
SERVER_PORT=8082

# MongoDB
MONGODB_HOST=localhost
MONGODB_PORT=27017
MONGODB_DATABASE=product-db

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Eureka
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/

# Config Server
SPRING_CLOUD_CONFIG_URI=http://localhost:8888
SPRING_CLOUD_CONFIG_USERNAME=admin
SPRING_CLOUD_CONFIG_PASSWORD=admin123
```

## Getting Started

### Prerequisites

1. **Java 17+**
```bash
java -version
```

2. **Maven 3.9.9+**
```bash
mvn -version
```

3. **Docker & Docker Compose** (for infrastructure)
```bash
docker --version
docker-compose --version
```

4. **Running Infrastructure Services**:
   - MongoDB (port 27017)
   - Redis (port 6379)
   - Apache Kafka (port 9092)
   - Eureka Server (port 8761)
   - Config Server (port 8888)

### Start Infrastructure

From the project root:
```bash
docker-compose up -d
```

This starts:
- MongoDB
- Redis
- Kafka + Zookeeper
- PostgreSQL
- Elasticsearch
- Prometheus
- Grafana
- Jaeger

### Build the Service

```bash
# Using Maven wrapper (recommended)
./mvnw clean install

# Or using system Maven
mvn clean install

# Skip tests (faster)
./mvnw clean install -DskipTests
```

### Run the Service

```bash
# Using Maven
./mvnw spring-boot:run

# Or using Java
java -jar target/product-service-1.0.0.jar

# With custom port
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8090
```

### Run with Docker

```bash
# Build Docker image
docker build -t product-service:1.0.0 .

# Run container
docker run -d \
  --name product-service \
  -p 8082:8082 \
  -e MONGODB_HOST=host.docker.internal \
  -e REDIS_HOST=host.docker.internal \
  -e KAFKA_BOOTSTRAP_SERVERS=host.docker.internal:9092 \
  product-service:1.0.0
```

## Testing

### Unit Tests

```bash
./mvnw test
```

### Integration Tests

```bash
./mvnw verify
```

### Test with cURL

#### Create a Product
```bash
curl -X POST http://localhost:8082/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "TEST-001",
    "name": "Test Product",
    "description": "A test product",
    "categoryId": "test-cat",
    "categoryName": "Test Category",
    "price": 99.99,
    "currency": "USD",
    "stockQuantity": 100,
    "brand": "TestBrand",
    "tags": ["test"]
  }'
```

#### Get All Products
```bash
curl http://localhost:8082/api/products
```

#### Search Products
```bash
curl "http://localhost:8082/api/products/search?query=test"
```

#### Get Product by ID
```bash
curl http://localhost:8082/api/products/{product-id}
```

#### Update Stock
```bash
curl -X PATCH "http://localhost:8082/api/products/{product-id}/stock?quantity=50"
```

#### Delete Product
```bash
curl -X DELETE http://localhost:8082/api/products/{product-id}
```

## Monitoring

### Health Check
```bash
curl http://localhost:8082/actuator/health
```

**Response:**
```json
{
  "status": "UP",
  "components": {
    "mongo": {"status": "UP"},
    "redis": {"status": "UP"},
    "kafka": {"status": "UP"}
  }
}
```

### Metrics
```bash
curl http://localhost:8082/actuator/metrics
```

### Prometheus Endpoint
```bash
curl http://localhost:8082/actuator/prometheus
```

### Info
```bash
curl http://localhost:8082/actuator/info
```

## API Documentation

### Swagger UI
Access the interactive API documentation at:
```
http://localhost:8082/swagger-ui.html
```

### OpenAPI JSON
```
http://localhost:8082/v3/api-docs
```

## Service Discovery

The service automatically registers with Eureka:
```
http://localhost:8761
```

Look for `PRODUCT-SERVICE` in the instances list.

## Troubleshooting

### Common Issues

#### 1. Cannot connect to MongoDB
**Error:** `MongoSocketOpenException`

**Solution:**
```bash
# Check if MongoDB is running
docker ps | grep mongo

# Start MongoDB
docker-compose up -d mongodb
```

#### 2. Cannot connect to Redis
**Error:** `RedisConnectionFailureException`

**Solution:**
```bash
# Check if Redis is running
docker ps | grep redis

# Start Redis
docker-compose up -d redis
```

#### 3. Kafka connection issues
**Error:** `TimeoutException: Timeout expired while fetching topic metadata`

**Solution:**
```bash
# Check Kafka
docker ps | grep kafka

# Restart Kafka
docker-compose restart kafka
```

#### 4. Service not registering with Eureka
**Solution:**
```bash
# Verify Eureka is running
curl http://localhost:8761/eureka/apps

# Check application.yml eureka configuration
```

#### 5. Cache not working
**Solution:**
```bash
# Verify Redis connection
redis-cli ping

# Check cache logs
grep "cache" application.log
```

## Performance Tips

1. **Enable Connection Pooling**
   - MongoDB: Already configured with default pool
   - Redis: Lettuce client with connection pooling

2. **Optimize Queries**
   - Use indexes on frequently queried fields
   - Limit result sets with pagination

3. **Cache Wisely**
   - Cache read-heavy data
   - Set appropriate TTL values
   - Monitor cache hit ratio

4. **Async Processing**
   - Kafka events are published asynchronously
   - Consider @Async for heavy operations

## Development Guidelines

### Code Style
- Follow Java naming conventions
- Use Lombok to reduce boilerplate
- Add JavaDoc for public methods
- Write meaningful commit messages

### Testing
- Write unit tests for services
- Integration tests for repositories
- API tests for controllers
- Aim for >80% code coverage

### Error Handling
- Use custom exceptions
- Return meaningful error messages
- Log errors appropriately
- Handle edge cases

## Security Considerations

### Current Status
⚠️ **Authentication/Authorization not yet implemented**

### Planned Security Features
- JWT token validation
- Role-based access control (RBAC)
- API rate limiting
- Input validation and sanitization
- SQL injection prevention (N/A for MongoDB)
- XSS protection

## Future Enhancements

- [ ] Product reviews and ratings management
- [ ] Product recommendations
- [ ] Bulk import/export
- [ ] Product bundles and packages
- [ ] Advanced filtering (price range, multi-attribute)
- [ ] Product comparison
- [ ] Wishlist integration
- [ ] Product availability notifications
- [ ] Multi-language support
- [ ] Image upload service integration
- [ ] SEO optimization (meta tags, structured data)

## Related Services

- **Search Service**: Consumes product events for Elasticsearch indexing
- **Order Service**: Validates products during order creation
- **Inventory Service**: Synchronizes stock levels
- **Recommendation Service**: Analyzes product data for recommendations
- **Notification Service**: Sends alerts for low stock

## Contributing

Please read [CONTRIBUTING.md](../../CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

## License

This project is licensed under the MIT License - see the [LICENSE](../../LICENSE) file for details.

## Support

For issues and questions:
- Create an issue in the GitHub repository
- Check the troubleshooting section above
- Review the API documentation in Swagger UI

---

**Service Version**: 1.0.0  
**Last Updated**: November 16, 2025  
**Status**: In Development ⚙️
