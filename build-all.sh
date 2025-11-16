#!/bin/bash

# Build script for all microservices
# Usage: ./build-all.sh

set -e

echo "========================================"
echo "Building Enterprise E-commerce Platform"
echo "========================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to build a service
build_service() {
    local service_name=$1
    local service_path=$2
    
    echo ""
    echo -e "${YELLOW}Building ${service_name}...${NC}"
    
    if [ -d "$service_path" ]; then
        cd "$service_path"
        
        if [ -f "pom.xml" ]; then
            echo "Using Maven..."
            ./mvnw clean package -DskipTests
        elif [ -f "build.gradle" ]; then
            echo "Using Gradle..."
            ./gradlew clean build -x test
        else
            echo -e "${RED}No build file found for ${service_name}${NC}"
            cd - > /dev/null
            return 1
        fi
        
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✓ ${service_name} built successfully${NC}"
        else
            echo -e "${RED}✗ ${service_name} build failed${NC}"
            cd - > /dev/null
            return 1
        fi
        
        cd - > /dev/null
    else
        echo -e "${YELLOW}⊘ ${service_name} not found, skipping...${NC}"
    fi
}

# Build shared modules first
echo ""
echo "========================================"
echo "Building Shared Modules"
echo "========================================"

build_service "Common Models" "shared/common-models"
build_service "Common Utils" "shared/common-utils"
build_service "Security Config" "shared/security-config"

# Build infrastructure services
echo ""
echo "========================================"
echo "Building Infrastructure Services"
echo "========================================"

build_service "Service Discovery" "services/service-discovery"
build_service "Config Service" "services/config-service"
build_service "API Gateway" "services/api-gateway"

# Build business services
echo ""
echo "========================================"
echo "Building Business Services"
echo "========================================"

build_service "User Service" "services/user-service"
build_service "Product Service" "services/product-service"
build_service "Search Service" "services/search-service"
build_service "Order Service" "services/order-service"
build_service "Inventory Service" "services/inventory-service"
build_service "Payment Service" "services/payment-service"
build_service "Recommendation Service" "services/recommendation-service"
build_service "Notification Service" "services/notification-service"
build_service "Review Service" "services/review-service"
build_service "Analytics Service" "services/analytics-service"

echo ""
echo "========================================"
echo -e "${GREEN}Build Complete!${NC}"
echo "========================================"
echo ""
echo "Next steps:"
echo "1. Start infrastructure: docker-compose up -d"
echo "2. Run services: ./run-all.sh"
echo ""
