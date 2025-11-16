#!/bin/bash

# Run script for all microservices
# Usage: ./run-all.sh

set -e

echo "========================================"
echo "Starting Enterprise E-commerce Platform"
echo "========================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}Docker is not running. Please start Docker first.${NC}"
    exit 1
fi

# Start infrastructure services
echo ""
echo -e "${BLUE}Starting infrastructure services...${NC}"
docker-compose up -d

echo ""
echo -e "${YELLOW}Waiting for services to be ready...${NC}"
sleep 10

# Function to run a service
run_service() {
    local service_name=$1
    local service_path=$2
    local port=$3
    
    echo ""
    echo -e "${YELLOW}Starting ${service_name} on port ${port}...${NC}"
    
    if [ -d "$service_path" ]; then
        cd "$service_path"
        
        if [ -f "pom.xml" ]; then
            nohup ./mvnw spring-boot:run > "../../logs/${service_name}.log" 2>&1 &
        elif [ -f "build.gradle" ]; then
            nohup ./gradlew bootRun > "../../logs/${service_name}.log" 2>&1 &
        else
            echo -e "${RED}No build file found for ${service_name}${NC}"
            cd - > /dev/null
            return 1
        fi
        
        echo -e "${GREEN}✓ ${service_name} started${NC}"
        cd - > /dev/null
    else
        echo -e "${YELLOW}⊘ ${service_name} not found, skipping...${NC}"
    fi
}

# Create logs directory
mkdir -p logs

# Start services in order
echo ""
echo "========================================"
echo "Starting Services"
echo "========================================"

# Infrastructure services first
run_service "Service Discovery" "services/service-discovery" "8761"
sleep 5

run_service "Config Service" "services/config-service" "8888"
sleep 5

run_service "API Gateway" "services/api-gateway" "8080"
sleep 5

# Business services
run_service "User Service" "services/user-service" "8081"
run_service "Product Service" "services/product-service" "8082"
run_service "Search Service" "services/search-service" "8083"
run_service "Order Service" "services/order-service" "8084"
run_service "Inventory Service" "services/inventory-service" "8085"
run_service "Payment Service" "services/payment-service" "8086"
run_service "Notification Service" "services/notification-service" "8087"
run_service "Review Service" "services/review-service" "8088"
run_service "Analytics Service" "services/analytics-service" "8089"

echo ""
echo "========================================"
echo -e "${GREEN}All services started!${NC}"
echo "========================================"
echo ""
echo "Service URLs:"
echo "  • API Gateway:        http://localhost:8080"
echo "  • Service Discovery:  http://localhost:8761"
echo "  • Config Service:     http://localhost:8888"
echo ""
echo "Infrastructure Services:"
echo "  • Postgres:           localhost:5432"
echo "  • MongoDB:            localhost:27017"
echo "  • Redis:              localhost:6379"
echo "  • Elasticsearch:      http://localhost:9200"
echo "  • Kafka:              localhost:9092"
echo "  • Prometheus:         http://localhost:9090"
echo "  • Grafana:            http://localhost:3000"
echo "  • Jaeger:             http://localhost:16686"
echo "  • MinIO Console:      http://localhost:9001"
echo ""
echo "Logs are available in ./logs/ directory"
echo ""
echo "To stop all services, run: ./stop-all.sh"
echo ""
