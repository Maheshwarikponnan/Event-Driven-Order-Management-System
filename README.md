# Event-Driven Order Management System

## Overview

This is a full-stack event-driven order management system built with Angular frontend and Java Spring Boot microservices backend. The system demonstrates microservices architecture, event-driven design, messaging with RabbitMQ, and scalable backend engineering.

## Architecture

The system consists of:
- **Order Service**: Handles order creation and management
- **Inventory Service**: Manages product stock levels
- **Notification Service**: Sends order confirmations
- **API Gateway**: Routes requests to services
- **Angular Frontend**: User interface for order management

## Event Flow

1. User places order via Angular frontend
2. Order Service saves order and publishes ORDER_CREATED event
3. Inventory Service consumes event and updates stock
4. Notification Service sends confirmation

## Tech Stack

- **Frontend**: Angular, Angular Material, RxJS
- **Backend**: Java Spring Boot, Spring Web, Spring Data JPA, Spring Security
- **Messaging**: RabbitMQ
- **Database**: PostgreSQL
- **Containerization**: Docker

## Setup Instructions

### Prerequisites
- Java 17
- Node.js and Angular CLI
- Docker and Docker Compose
- Maven

### Running the Services

1. Start PostgreSQL and RabbitMQ via Docker:
   ```bash
   docker-compose up -d
   ```

2. Run each microservice:
   ```bash
   cd backend/order-service && mvn spring-boot:run
   cd backend/inventory-service && mvn spring-boot:run
   cd backend/notification-service && mvn spring-boot:run
   ```

3. Run Angular frontend:
   ```bash
   cd frontend && ng serve
   ```

## Authentication

The system includes JWT-based authentication:
- Login endpoint: POST /auth/login with username/password
- Protected endpoints require Bearer token in Authorization header
- Frontend includes login component and auth guard

Default credentials: admin/password

## Event Retry Mechanisms

RabbitMQ consumers include retry policies:
- Max 3 attempts for failed message processing
- Exponential backoff (1s, 2s, 4s)
- Failed messages are rejected and not requeued

## Future Improvements

- Role-based access control
- Order status tracking
- Centralized logging