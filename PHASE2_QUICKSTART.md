# Phase 2: Quick Start Guide

## 🚀 Start Everything in 5 Minutes

### Step 1: Start Infrastructure (30 seconds)
```bash
cd "/Users/issa.ar4/Desktop/Documents/PersonalProjects/Enterprise-Level E-commerce Platform"
docker-compose up -d
```

**Wait for services to start** (check with `docker ps`):
- ✅ MongoDB on port 27017
- ✅ Redis on port 6379  
- ✅ Kafka on port 9092
- ✅ Eureka on port 8761
- ✅ Config Server on port 8888

### Step 2: Build Product Service (2 minutes)
```bash
cd services/product-service
./mvnw clean package -DskipTests
```

### Step 3: Run Product Service (5 seconds)
```bash
./mvnw spring-boot:run
```

### Step 4: Test It Works (30 seconds)

**Health Check**:
```bash
curl http://localhost:8082/actuator/health
```

**Create a Product**:
```bash
curl -X POST http://localhost:8082/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "TEST-001",
    "name": "Test Product",
    "description": "A test product",
    "categoryId": "test",
    "categoryName": "Test Category",
    "price": 99.99,
    "currency": "USD",
    "stockQuantity": 100,
    "brand": "TestBrand",
    "tags": ["test"]
  }'
```

**Get All Products**:
```bash
curl http://localhost:8082/api/products
```

**Access Swagger UI**:
Open http://localhost:8082/swagger-ui.html in your browser

---

## 📝 Quick API Reference

### Product Endpoints

**Create Product**:
```bash
POST /api/products
Content-Type: application/json

{
  "sku": "string",
  "name": "string",
  "description": "string",
  "categoryId": "string",
  "categoryName": "string",
  "price": 99.99,
  "currency": "USD",
  "stockQuantity": 100,
  "brand": "string",
  "tags": ["tag1", "tag2"]
}
```

**Get Product by ID**:
```bash
GET /api/products/{id}
```

**Get All Products (Paginated)**:
```bash
GET /api/products?page=0&size=20&sortBy=createdAt&sortDirection=DESC
```

**Search Products**:
```bash
GET /api/products/search?query=laptop&page=0&size=20
```

**Update Product**:
```bash
PUT /api/products/{id}
Content-Type: application/json

{
  "name": "Updated Product Name",
  "price": 149.99
}
```

**Delete Product**:
```bash
DELETE /api/products/{id}
```

**Update Stock**:
```bash
PATCH /api/products/{id}/stock?quantity=200
```

---

## 🔧 Troubleshooting

### Service Won't Start?

**Check Infrastructure**:
```bash
docker ps
```

**Check Logs**:
```bash
docker-compose logs mongodb
docker-compose logs redis
docker-compose logs kafka
```

### Can't Connect to MongoDB?

```bash
# Restart MongoDB
docker-compose restart mongodb

# Check MongoDB logs
docker-compose logs -f mongodb
```

### Can't Connect to Redis?

```bash
# Test Redis
docker exec -it redis redis-cli ping

# Should return: PONG
```

### Can't Connect to Kafka?

```bash
# Check Kafka
docker-compose logs -f kafka

# Restart Kafka
docker-compose restart kafka zookeeper
```

### Build Failures?

```bash
# Clean build
./mvnw clean install -DskipTests

# If Lombok issues
./mvnw clean compile -DskipTests
```

---

## 📊 Monitoring

**Service Health**:
```bash
curl http://localhost:8082/actuator/health
```

**Metrics**:
```bash
curl http://localhost:8082/actuator/metrics
```

**Prometheus Endpoint**:
```bash
curl http://localhost:8082/actuator/prometheus
```

**Check Eureka**:
Open http://localhost:8761

**Check Product Service Registration**:
Look for `PRODUCT-SERVICE` in Eureka dashboard

---

## 🎯 What to Do Next

### Test the Service:
1. Create multiple products
2. Search for products
3. Update product stock
4. Delete products
5. Check caching (create, get, update, get again)
6. Monitor Kafka events

### Development:
1. Add more product fields
2. Implement Category controller
3. Add product reviews
4. Implement product recommendations
5. Add image upload functionality

### Documentation:
- Read `services/product-service/README.md` for detailed API docs
- Review `PHASE2_PROGRESS.md` for next steps
- Check `ARCHITECTURE.md` for system design

---

## 🏆 Success Checklist

- ✅ Infrastructure running (MongoDB, Redis, Kafka, Eureka)
- ✅ Product Service built and running
- ✅ Can create products via API
- ✅ Can retrieve products via API
- ✅ Health check returns UP
- ✅ Swagger UI accessible
- ✅ Service registered with Eureka

---

**You're all set! Start building! 🚀**

For detailed documentation, see:
- `services/product-service/README.md`
- `PHASE2_PROGRESS.md`
- `PHASE2_IMPLEMENTATION_SUMMARY.md`
