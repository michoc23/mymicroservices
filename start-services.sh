#!/bin/bash

# Script to start Docker and run microservices

echo "🚀 Starting Docker and Microservices..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker daemon is not running."
    echo "📝 Please run this command in your terminal:"
    echo "   sudo systemctl start docker"
    echo ""
    echo "Or to enable Docker to start on boot:"
    echo "   sudo systemctl enable --now docker"
    exit 1
fi

echo "✅ Docker is running"
echo ""

# Navigate to project directory
cd "$(dirname "$0")"

echo "🔨 Building and starting all services..."
docker compose up -d --build

echo ""
echo "⏳ Waiting for services to be healthy..."
sleep 10

echo ""
echo "📊 Service Status:"
docker compose ps

echo ""
echo "✅ Services are starting!"
echo ""
echo "📍 Service URLs:"
echo "   - User Service:        http://localhost:8081/api/v1"
echo "   - User Service Docs:   http://localhost:8081/api/v1/swagger-ui.html"
echo "   - API Gateway:         http://localhost:8082"
echo "   - Ticket Service:      http://localhost:8083"
echo "   - Ticket Service Docs: http://localhost:8083/swagger-ui.html"
echo "   - Subscription Service: http://localhost:8084"
echo "   - Frontend:            http://localhost:3000"
echo "   - pgAdmin:             http://localhost:5051"
echo ""
echo "📋 To view logs:"
echo "   docker compose logs -f"
echo ""
echo "📋 To stop services:"
echo "   docker compose down"
echo ""

