#!/bin/bash

# Stop script for all microservices
# Usage: ./stop-all.sh

echo "========================================"
echo "Stopping Enterprise E-commerce Platform"
echo "========================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Stop all Spring Boot applications
echo ""
echo -e "${YELLOW}Stopping Spring Boot services...${NC}"
pkill -f "spring-boot" || true

# Stop infrastructure services
echo ""
echo -e "${YELLOW}Stopping infrastructure services...${NC}"
docker-compose down

echo ""
echo "========================================"
echo -e "${GREEN}All services stopped!${NC}"
echo "========================================"
echo ""
