#!/bin/bash

# CrediYa Authentication Service - Build Script for Podman
# This script builds the JAR file and Docker image for the authentication service

set -e

echo "Building CrediYa Authentication Service..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
SERVICE_NAME="authentication-service"
IMAGE_NAME="crediya/authentication-service"
IMAGE_TAG="latest"
JAR_NAME="authentication-service.jar"

echo -e "${BLUE}Step 1: Building JAR with Gradle...${NC}"
./gradlew clean build -x test
if [ $? -eq 0 ]; then
    echo -e "${GREEN}JAR build successful${NC}"
else
    echo -e "${RED}JAR build failed${NC}"
    exit 1
fi

echo -e "${BLUE}Step 2: Copying JAR to deployment directory...${NC}"
# Find the built JAR file
JAR_FILE=$(find applications/app-service/build/libs -name "*.jar" | head -1)
if [ -z "$JAR_FILE" ]; then
    echo -e "${RED}JAR file not found in applications/app-service/build/libs${NC}"
    exit 1
fi

# Copy JAR to deployment directory
cp "$JAR_FILE" "deployment/$JAR_NAME"
echo -e "${GREEN}JAR copied to deployment/$JAR_NAME${NC}"

echo -e "${BLUE}Step 3: Building Docker image with Podman...${NC}"
podman build -t "$IMAGE_NAME:$IMAGE_TAG" -f deployment/Dockerfile deployment/
if [ $? -eq 0 ]; then
    echo -e "${GREEN}Docker image built successfully${NC}"
else
    echo -e "${RED}Docker image build failed${NC}"
    exit 1
fi

echo -e "${BLUE}Step 4: Image information...${NC}"
podman images | grep "$IMAGE_NAME"

echo -e "${GREEN}Build completed successfully!${NC}"
echo -e "${YELLOW}Next steps:${NC}"
echo -e "   • Run: ${BLUE}podman-compose up -d${NC} to start services"
echo -e "   • Check: ${BLUE}podman-compose logs -f${NC} to view logs"
echo -e "   • Test: ${BLUE}curl http://localhost:8080/actuator/health${NC}"
