# Payment Service

Enterprise-grade payment processing service supporting multiple payment gateways (Stripe and PayPal) with comprehensive transaction management, webhooks, and real-time event streaming.

## Features

### Core Payment Features
- ✅ **Multi-Gateway Support**: Stripe and PayPal integration
- ✅ **Payment Intent Management**: Create, confirm, cancel payment intents
- ✅ **Transaction Tracking**: Complete payment lifecycle tracking
- ✅ **Refund Processing**: Full and partial refunds
- ✅ **Webhook Handling**: Real-time payment status updates
- ✅ **Event Streaming**: Kafka-based event publishing

### Technical Features
- ✅ **Database**: PostgreSQL for reliable transaction storage
- ✅ **Caching**: Redis for payment data caching
- ✅ **Service Discovery**: Eureka client registration
- ✅ **Configuration**: Spring Cloud Config integration
- ✅ **API Documentation**: Swagger/OpenAPI 3.0
- ✅ **Monitoring**: Actuator health checks and metrics
- ✅ **Security**: Input validation and exception handling

## Architecture

### Payment Flow

```
Client → Payment Service → Payment Gateway (Stripe/PayPal)
                ↓
          Database (PostgreSQL)
                ↓
          Event Bus (Kafka)
                ↓
          Cache (Redis)
```

### Supported Payment Methods

1. **Stripe**
   - Credit/Debit Cards
   - Payment Intents API
   - Webhook verification
   - Automatic payment methods

2. **PayPal**
   - PayPal account payments
   - Order creation and capture
   - Webhook notifications
   - Sandbox and Live modes

## Tech Stack

- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: PostgreSQL
- **Cache**: Redis
- **Message Broker**: Apache Kafka
- **Service Discovery**: Netflix Eureka
- **Payment Gateways**: 
  - Stripe SDK 24.2.0
  - PayPal SDK 2.36.0
- **API Documentation**: SpringDoc OpenAPI
- **Build Tool**: Maven

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- PostgreSQL 15+
- Redis 7+
- Apache Kafka 3.5+
- Eureka Server running on port 8761
- Stripe API keys (for production)
- PayPal API credentials (for production)

## Configuration

### Environment Variables

```bash
# Stripe Configuration
export STRIPE_API_KEY=sk_test_your_stripe_secret_key
export STRIPE_WEBHOOK_SECRET=whsec_your_webhook_secret

# PayPal Configuration
export PAYPAL_CLIENT_ID=your_paypal_client_id
export PAYPAL_CLIENT_SECRET=your_paypal_client_secret
export PAYPAL_MODE=sandbox # or 'live' for production

# Database Configuration
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=payment_db
export DB_USER=postgres
export DB_PASSWORD=postgres

# Redis Configuration
export REDIS_HOST=localhost
export REDIS_PORT=6379

# Kafka Configuration
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

### Application Configuration

See `src/main/resources/application.yml` for detailed configuration options.

## Getting Started

### 1. Setup Database

```sql
CREATE DATABASE payment_db;
```

The application will automatically create tables on startup using JPA.

### 2. Install Dependencies

```bash
./mvnw clean install
```

### 3. Run the Service

```bash
./mvnw spring-boot:run
```

The service will start on port **8086**.

### 4. Verify Service is Running

```bash
curl http://localhost:8086/api/payments/health
```

## API Endpoints

### Payment Operations

#### Create Payment
```http
POST /api/payments
Content-Type: application/json

{
  "orderId": "ORD-12345",
  "userId": "user-123",
  "amount": 99.99,
  "currency": "USD",
  "paymentMethod": "STRIPE",
  "description": "Order payment",
  "customerName": "John Doe",
  "customerEmail": "john@example.com",
  "customerPhone": "+1234567890",
  "billingAddress": "123 Main St, New York, NY 10001"
}
```

**Response:**
```json
{
  "id": 1,
  "orderId": "ORD-12345",
  "status": "PENDING",
  "paymentUrl": "pi_xxx_secret_xxx",
  "transactionId": "pi_xxx",
  "amount": 99.99,
  "currency": "USD",
  "createdAt": "2024-01-15T10:30:00"
}
```

#### Confirm Payment
```http
POST /api/payments/{id}/confirm
```

#### Cancel Payment
```http
POST /api/payments/{id}/cancel
```

#### Refund Payment
```http
POST /api/payments/{id}/refund?amount=99.99&reason=Customer request
```

#### Get Payment by ID
```http
GET /api/payments/{id}
```

#### Get Payment by Order ID
```http
GET /api/payments/order/{orderId}
```

#### Get User Payments
```http
GET /api/payments/user/{userId}
```

#### Get Payments by Status
```http
GET /api/payments/status/{status}
```

### Payment Statuses

- `PENDING` - Payment initiated, awaiting customer action
- `PROCESSING` - Payment being processed
- `COMPLETED` - Payment successfully completed
- `FAILED` - Payment failed
- `CANCELLED` - Payment cancelled
- `REFUNDED` - Full refund processed
- `PARTIALLY_REFUNDED` - Partial refund processed

### Webhook Endpoints

#### Stripe Webhook
```http
POST /api/webhooks/stripe
Header: Stripe-Signature: xxx
```

#### PayPal Webhook
```http
POST /api/webhooks/paypal
```

## Swagger API Documentation

Access interactive API documentation at:
```
http://localhost:8086/swagger-ui.html
```

OpenAPI spec available at:
```
http://localhost:8086/api-docs
```

## Payment Gateway Integration

### Stripe Integration

#### Setup
1. Create Stripe account at https://stripe.com
2. Get API keys from Dashboard → Developers → API Keys
3. Set up webhook endpoint in Dashboard → Developers → Webhooks
4. Add webhook secret to configuration

#### Supported Events
- `payment_intent.succeeded`
- `payment_intent.payment_failed`
- `payment_intent.canceled`
- `charge.refunded`

### PayPal Integration

#### Setup
1. Create PayPal developer account at https://developer.paypal.com
2. Create REST API app in Dashboard
3. Get Client ID and Secret
4. Configure webhook notifications
5. Set mode to 'sandbox' for testing or 'live' for production

#### Supported Events
- `PAYMENT.CAPTURE.COMPLETED`
- `PAYMENT.CAPTURE.DENIED`
- `PAYMENT.CAPTURE.REFUNDED`
- `CHECKOUT.ORDER.APPROVED`

## Kafka Events

The service publishes the following events to the `payment-events` topic:

### Event Types

1. **payment.created**
   - Published when a payment is initiated
   - Contains payment details

2. **payment.completed**
   - Published when payment is successfully processed
   - Triggers order fulfillment

3. **payment.failed**
   - Published when payment fails
   - Contains failure reason

4. **payment.cancelled**
   - Published when payment is cancelled
   - Triggers order cancellation

5. **payment.refunded**
   - Published when refund is processed
   - Contains refund details

### Event Payload Example
```json
{
  "id": 1,
  "orderId": "ORD-12345",
  "userId": "user-123",
  "amount": 99.99,
  "status": "COMPLETED",
  "timestamp": "2024-01-15T10:30:00"
}
```

## Monitoring

### Health Check
```bash
curl http://localhost:8086/actuator/health
```

### Metrics
```bash
curl http://localhost:8086/actuator/metrics
```

### Prometheus Metrics
```bash
curl http://localhost:8086/actuator/prometheus
```

## Docker Deployment

### Build Image
```bash
docker build -t payment-service:latest .
```

### Run Container
```bash
docker run -d \
  --name payment-service \
  -p 8086:8086 \
  -e STRIPE_API_KEY=sk_test_xxx \
  -e PAYPAL_CLIENT_ID=xxx \
  -e DB_HOST=postgres \
  payment-service:latest
```

### Docker Compose
See root `docker-compose.yml` for complete infrastructure setup.

## Testing

### Unit Tests
```bash
./mvnw test
```

### Integration Tests
```bash
./mvnw verify
```

### Test Payment with Stripe Test Cards

- **Success**: 4242 4242 4242 4242
- **Decline**: 4000 0000 0000 0002
- **Requires Auth**: 4000 0025 0000 3155

See: https://stripe.com/docs/testing

### Test Payment with PayPal Sandbox

Use PayPal sandbox accounts:
- Buyer: sb-buyer@personal.example.com
- Merchant: sb-merchant@business.example.com

## Security Considerations

1. **API Keys**: Never commit API keys to version control
2. **Webhook Signatures**: Always verify webhook signatures
3. **HTTPS**: Use HTTPS in production
4. **Input Validation**: All inputs are validated
5. **Rate Limiting**: Implement rate limiting for production
6. **PCI Compliance**: Never store card details directly

## Performance Optimization

- **Connection Pooling**: HikariCP for database connections
- **Redis Caching**: Payment data cached for 10 minutes
- **Async Processing**: Webhook processing is asynchronous
- **Database Indexes**: Optimized queries with indexes

## Error Handling

The service provides comprehensive error responses:

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Payment Failed",
  "message": "Insufficient funds",
  "path": "/api/payments"
}
```

## Troubleshooting

### Common Issues

1. **Payment Creation Fails**
   - Check Stripe/PayPal API keys
   - Verify network connectivity
   - Check logs for detailed error messages

2. **Webhook Not Received**
   - Verify webhook URL is publicly accessible
   - Check webhook secret configuration
   - Review webhook signature verification

3. **Database Connection Issues**
   - Verify PostgreSQL is running
   - Check connection credentials
   - Ensure database exists

4. **Redis Connection Issues**
   - Verify Redis is running
   - Check Redis host and port
   - Review connection timeout settings

## Development

### Code Structure
```
payment-service/
├── src/main/java/com/ecommerce/payment/
│   ├── controller/         # REST controllers & webhooks
│   ├── service/           # Business logic & gateway integration
│   ├── model/             # JPA entities
│   ├── dto/               # Data transfer objects
│   ├── repository/        # Data access layer
│   └── exception/         # Exception handling
├── src/main/resources/
│   └── application.yml    # Configuration
└── Dockerfile             # Container image
```

### Adding New Payment Gateway

1. Create gateway service class (e.g., `SquarePaymentService`)
2. Implement payment operations
3. Update `PaymentService` to route to new gateway
4. Add webhook controller
5. Update configuration
6. Add tests

## Contributing

1. Fork the repository
2. Create feature branch
3. Commit changes
4. Push to branch
5. Create Pull Request

## License

Copyright © 2024 E-Commerce Platform. All rights reserved.

## Support

For issues and questions:
- GitHub Issues: [Create an issue]
- Email: support@ecommerce-platform.com
- Documentation: [API Docs](http://localhost:8086/swagger-ui.html)

## Related Services

- **Product Service**: Product catalog management
- **Order Service**: Order processing and management
- **Notification Service**: Payment confirmation notifications
- **Analytics Service**: Payment analytics and reporting

## Changelog

### Version 1.0.0 (2024-01-15)
- Initial release
- Stripe integration
- PayPal integration
- Webhook handling
- Event streaming
- Redis caching
- API documentation
