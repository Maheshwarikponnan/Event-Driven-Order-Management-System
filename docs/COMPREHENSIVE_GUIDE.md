# Event-Driven Order Management System - Comprehensive Guide

## 📋 Table of Contents
1. [What Is This Project?](#what-is-this-project)
2. [How Does It Work?](#how-does-it-work)
3. [System Architecture](#system-architecture)
4. [Technology Stack](#technology-stack)
5. [Project Structure](#project-structure)
6. [Key Components Explained](#key-components-explained)
7. [How to Run It](#how-to-run-it)
8. [Features](#features)
9. [Security](#security)
10. [FAQ](#faq)

---

## What Is This Project?

### Simple Explanation
Imagine an **online store** where customers can:
- Browse products
- Log in securely
- Place orders for products
- Get confirmation that their order is being processed

Behind the scenes, the system automatically:
- Validates the order
- **Reduces the stock** when an order is placed
- **Sends a confirmation notification** to the customer

This whole system is built using **modern cloud-native architecture**, which means multiple specialized services working together rather than one giant application.

### Business Purpose
This is an **e-commerce order management system** that handles:
- **User Authentication**: Customers securely login
- **Order Management**: Creating and tracking orders
- **Inventory Management**: Automatically updating product stock levels
- **Notifications**: Sending confirmations to customers
- **API Gateway**: A "front door" that routes all requests to the right service

---

## How Does It Work?

### The Happy Path: A Customer Places an Order

```
[Customer]
    ↓ (User clicks "Place Order" on website)
    ↓
[Angular Frontend]
    ↓ (Sends order request)
    ↓
[API Gateway]
    ↓ (Routes to correct service)
    ↓
[Order Service] (1)
    ├─→ Saves order to database
    ├─→ Publishes "ORDER_CREATED" event
    ↓
[Message Queue - RabbitMQ] (2)
    ├─→ Event stored temporarily
    ├─→ Two services listening...
    ↓
    ├─ [Inventory Service] (3a)
    │   ├─→ Receives event
    │   ├─→ Reduces product stock
    │   └─→ Updates database
    │
    └─ [Notification Service] (3b)
        ├─→ Receives event
        ├─→ Sends confirmation email/SMS
        └─→ Logs notification
```

### What's Happening Step-by-Step:

1. **Customer Action**: User fills out order form and clicks "Place Order"
2. **Frontend**: Angular app collects the form data and sends it to the backend
3. **API Gateway**: Acts as a "receptionist" who directs the request to the right service
4. **Order Service**: 
   - Validates the order data
   - Saves it to the database
   - Creates an event saying "Hey everyone, a new order was created!"
   - Publishes this event to a message queue
5. **Message Queue (RabbitMQ)**: 
   - Holds the message temporarily
   - Makes sure every interested service gets it
   - Like a bulletin board where services can read announcements
6. **Inventory Service**: 
   - Reads: "A new order was created"
   - Reduces the product stock count
   - Updates the database
7. **Notification Service**:
   - Reads: "A new order was created"
   - Sends a confirmation to the customer
   - Logs the notification

### Why This Design?

**Traditional Approach** (One Big Application):
- All code in one place
- Hard to scale
- One bug can crash everything
- Difficult to update without stopping the whole system

**This Modern Approach** (Microservices):
- Each service does ONE thing well
- Easy to scale only the parts that need it
- Services can be deployed independently
- One service failing doesn't crash others
- Easy to work on different services simultaneously

---

## System Architecture

### High-Level Overview

```
┌─────────────────────────────────────────────────────────────┐
│                        Client Layer                          │
│  ┌───────────────────────────────────────────────────────┐  │
│  │     Angular Frontend (Web Application)               │  │
│  │  - Login page                                         │  │
│  │  - Product list                                       │  │
│  │  - Order form                                         │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                           ↓
                  HTTPs / RESTful API
                           ↓
┌─────────────────────────────────────────────────────────────┐
│                      Server Layer                           │
│  ┌───────────────────────────────────────────────────────┐  │
│  │          API Gateway (Request Router)                 │  │
│  │  - Routes requests to correct microservice            │  │
│  │  - Handles authentication                             │  │
│  └───────────────────────────────────────────────────────┘  │
│               ↓              ↓              ↓                 │
│  ┌────────────────┐ ┌──────────────┐ ┌──────────────────┐  │
│  │ Order Service  │ │Inventory     │ │ Notification     │  │
│  │                │ │ Service      │ │ Service          │  │
│  │ - Create order │ │              │ │                  │  │
│  │ - Get order    │ │ - Check qty  │ │ - Send email     │  │
│  │ - Cancel order │ │ - Update qty │ │ - Track notifs   │  │
│  └────────────────┘ └──────────────┘ └──────────────────┘  │
│               ↑              ↑              ↑                 │
│               └──────────────┬──────────────┘                │
│                              ↓                              │
│                      Message Queue                          │
│                    (RabbitMQ - Event Bus)                   │
│              Services communicate via events                │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│                      Data Layer                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │        PostgreSQL Database                            │  │
│  │  - Stores orders, products, users, inventory          │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### Four Layers Explained

1. **Client Layer**: What the user sees and interacts with
2. **API Gateway Layer**: The "traffic controller" directing requests
3. **Microservices Layer**: Independent services handling different business logic
4. **Data Layer**: Where all information is stored permanently

---

## Technology Stack

### Frontend Technology

#### **Angular (v21.2.0)**
- **What it is**: A web framework for building user interfaces
- **What it does**: Creates the interactive website the customer sees
- **Simple analogy**: Like building blocks for websites - modular and reusable
- **Used for**: 
  - Login page
  - Product display
  - Order form
  - Real-time updates

#### **TypeScript**
- **What it is**: A "safer" version of JavaScript with type checking
- **What it does**: Prevents common programming mistakes
- **Simple analogy**: Like having a spell-checker for code
- **Used for**: All frontend code logic

#### **RxJS**
- **What it is**: A library for handling asynchronous operations
- **What it does**: Manages data streams and real-time updates
- **Simple analogy**: Like a news subscription - automatically get updates when something happens
- **Used for**: API calls, handling server responses, managing component state

### Backend Technology

#### **Java (v17)**
- **What it is**: A programming language for server applications
- **What it does**: Executes the business logic on the server
- **Simple analogy**: Like the chef in a restaurant - takes orders and processes them
- **Used for**: All backend services

#### **Spring Boot (v3.2.0)**
- **What it is**: A framework that makes building Java applications easier
- **What it does**: Handles common tasks like database connections, web requests, security
- **Simple analogy**: Like a blueprint that speeds up construction
- **Used for**: Core framework for all microservices

#### **Spring Data JPA**
- **What it is**: A library for talking to databases
- **What it does**: Converts Java objects to database records and vice versa
- **Simple analogy**: Like a translator between Java and the database language
- **Used for**: Saving and retrieving data from PostgreSQL

#### **Spring Security**
- **What it is**: A framework for authentication and authorization
- **What it does**: Verifies who users are and what they're allowed to do
- **Simple analogy**: Like a security guard checking ID cards
- **Used for**: Login validation, protecting endpoints

#### **JWT (JSON Web Tokens)**
- **What it is**: A secure way to store user session information
- **What it does**: Creates a token that proves the user is logged in
- **Simple analogy**: Like a movie ticket - shows you have permission to access something
- **Used for**: Authentication after user logs in

### Messaging Technology

#### **RabbitMQ (v3-management)**
- **What it is**: A message broker (intermediary for messages)
- **What it does**: Receives events from one service and delivers them to others
- **Simple analogy**: Like a postal service between services - sends messages reliably
- **Used for**: 
  - Inventory Service receives order creation events
  - Notification Service receives order creation events
  - Enables services to communicate without being tightly coupled

#### **AMQP (Advanced Message Queuing Protocol)**
- **What it is**: The protocol (language) RabbitMQ uses
- **What it does**: Standardizes how messages are formatted and sent
- **Simple analogy**: Like postal service rules - defines how mail should be formatted
- **Used for**: Communication between services

### Database Technology

#### **PostgreSQL (v15)**
- **What it is**: A relational database (stores structured data)
- **What it does**: Permanently stores all application data
- **Simple analogy**: Like a digital filing cabinet that's organized and searchable
- **Used for**: 
  - User accounts
  - Orders
  - Products
  - Inventory levels
  - Notifications

### DevOps Technology

#### **Docker**
- **What it is**: A containerization platform
- **What it does**: Packages applications with all their dependencies in isolated boxes
- **Simple analogy**: Like shipping containers - standardized, predictable, and work anywhere
- **Used for**: Running PostgreSQL and RabbitMQ consistently

#### **Docker Compose**
- **What it is**: A tool for running multiple Docker containers together
- **What it does**: Orchestrates PostgreSQL and RabbitMQ as a single system
- **Simple analogy**: Like a conductor organizing multiple musicians
- **Used for**: Running entire backend infrastructure with one command

### Build Tools

#### **Maven**
- **What it is**: A build tool for Java projects
- **What it does**: Manages dependencies and builds the application
- **Simple analogy**: Like a recipe book - tells the computer how to build the application
- **Used for**: Compiling services and managing dependencies

#### **npm & NodeJS**
- **What it is**: Package manager and runtime for JavaScript/Node
- **What it does**: Manages frontend dependencies and builds the Angular application
- **Simple analogy**: Like a package manager for frontend code
- **Used for**: Running Angular development server

---

## Project Structure

```
Angular_Java/
│
├── README.md                    # Basic overview
├── COMPREHENSIVE_GUIDE.md       # This file
│
├── backend/                     # All server-side code
│   ├── api-gateway/             # Entry point for all requests
│   │   └── src/main/java/...
│   │
│   ├── order-service/           # Handles order operations
│   │   ├── pom.xml              # Dependencies
│   │   └── src/main/java/...
│   │       ├── controller/      # REST endpoints
│   │       ├── service/         # Business logic
│   │       ├── entity/          # Database models
│   │       ├── dto/             # Data transfer objects
│   │       ├── messaging/       # Event publishing
│   │       └── config/          # Configuration
│   │
│   ├── inventory-service/       # Manages product stock
│   │   ├── pom.xml
│   │   └── src/main/java/...
│   │       ├── controller/
│   │       ├── service/
│   │       ├── entity/
│   │       ├── messaging/       # Event consuming
│   │       └── config/
│   │
│   └── notification-service/    # Sends confirmations
│       ├── pom.xml
│       └── src/main/java/...
│           ├── service/
│           ├── messaging/       # Event consuming
│           └── config/
│
├── frontend/                    # Angular web application
│   ├── package.json             # Dependencies
│   ├── tsconfig.json            # TypeScript config
│   ├── angular.json             # Angular config
│   └── src/
│       ├── main.ts              # Application entry point
│       ├── index.html           # Main HTML file
│       ├── styles.scss          # Global styles
│       └── app/
│           ├── app.ts           # Root component
│           ├── app.routes.ts    # URL routing
│           ├── api.ts           # API communication
│           ├── auth.ts          # Authentication logic
│           ├── order.ts         # Order logic
│           ├── product.ts       # Product logic
│           ├── login/           # Login component
│           ├── product-list/    # Product list component
│           └── order-form/      # Order form component
│
├── docker/                      # Container configuration
│   └── docker-compose.yml       # PostgreSQL & RabbitMQ setup
│
└── docs/                        # Documentation
```

---

## Key Components Explained

### Backend Services

#### **1. Order Service** 🛒
**Purpose**: Handles all order-related operations

**What it does**:
- Users create orders through the frontend
- Service validates the order data
- Saves order to database
- **Publishes an event** saying "Hey, new order created!"
- Provides endpoints to view and cancel orders

**Key Classes**:
- `OrderController`: Receives HTTP requests about orders
- `OrderService`: Contains the business logic
- `OrderEventPublisher`: Publishes events to RabbitMQ
- `Order` & `OrderItem` entities: Database models

**Endpoints**:
```
POST /orders              Create a new order
GET /orders               Get all orders
GET /orders/{id}          Get specific order
DELETE /orders/{id}       Cancel order
```

#### **2. Inventory Service** 📦
**Purpose**: Manages product stock levels

**What it does**:
- Listens for "ORDER_CREATED" events from RabbitMQ
- Updates product stock when orders are placed
- Prevents overselling (prevents orders that exceed available stock)
- Tracks inventory levels

**Key Classes**:
- `OrderCreatedEventConsumer`: Listens for order events
- `InventoryService`: Updates stock levels
- `Product` entity: Represents products in inventory

**How it works**:
1. Order Service creates order → publishes event
2. RabbitMQ holds the event
3. Inventory Service receives event
4. Calculates: Current stock - Ordered quantity = New stock
5. Updates database

#### **3. Notification Service** 📧
**Purpose**: Sends confirmations to customers

**What it does**:
- Listens for "ORDER_CREATED" events from RabbitMQ
- Sends confirmation messages (email, SMS, push notification)
- Logs all notifications sent
- Can retry if delivery fails

**Key Classes**:
- `OrderCreatedEventConsumer`: Listens for order events
- Notification service: Sends actual messages

**How it works**:
1. Order Service creates order → publishes event
2. RabbitMQ holds the event
3. Notification Service receives event
4. Sends confirmation to customer email/phone
5. Logs that notification was sent

#### **4. API Gateway** 🚪
**Purpose**: Routes all requests to the correct service

**What it does**:
- Single entry point for all frontend requests
- Routes requests to Order Service, Inventory Service, etc.
- Handles authentication validation
- Prevents unauthorized access

**Why it exists**:
- Frontend only needs to know one URL
- Can add common logic (logging, rate limiting, etc.)
- Can change backend structure without frontend changes
- Like a receptionist directing visitors to the right department

### Frontend Components

#### **Login Component** 🔐
- User enters username and password
- Sends credentials to Order Service
- Receives JWT token if valid
- Stores token for future requests

#### **Product List Component** 📋
- Displays all available products
- Shows product name, price, description, stock level
- Allows users to select products to order

#### **Order Form Component** 📝
- Users select products and quantities
- Forms a shopping cart
- Submits order when user clicks "Place Order"
- Shows confirmation after order is placed

---

## How to Run It

### Prerequisites
Make sure you have installed:
- **Java 17** or higher
- **Node.js** 18+ and npm
- **Docker** and **Docker Compose**
- **Maven** (for building Java projects)

### Step 1: Start Infrastructure (PostgreSQL & RabbitMQ)

```bash
cd docker
docker-compose up -d
```

This command:
- Downloads PostgreSQL and RabbitMQ images (if not already downloaded)
- Starts PostgreSQL on port 5432
- Starts RabbitMQ on port 5672
- RabbitMQ management UI available at http://localhost:15672

### Step 2: Run Backend Services

Open three separate terminal windows and run:

**Terminal 1 - Order Service:**
```bash
cd backend/order-service
mvn spring-boot:run
```
Runs on: `http://localhost:8080`

**Terminal 2 - Inventory Service:**
```bash
cd backend/inventory-service
mvn spring-boot:run
```
Runs on: `http://localhost:8081`

**Terminal 3 - Notification Service:**
```bash
cd backend/notification-service
mvn spring-boot:run
```
Runs on: `http://localhost:8082`

### Step 3: Run Frontend

Open another terminal:

```bash
cd frontend
npm install          # Install dependencies (first time only)
npm start           # Start Angular development server
```

Runs on: `http://localhost:4200`

### Step 4: Access the Application

1. Open browser to `http://localhost:4200`
2. Login with default credentials:
   - Username: `admin`
   - Password: `password`
3. Browse products
4. Create an order
5. Watch the system work!

### To Stop Everything

```bash
# Stop services: Press Ctrl+C in each terminal

# Stop Docker containers:
cd docker
docker-compose down
```

---

## Features

### 1. Authentication & Security 🔒
- **JWT Tokens**: Secure, stateless authentication
- **Password Hashing**: Passwords are securely encrypted
- **Protected Endpoints**: Only logged-in users can place orders
- **Token Expiration**: Tokens expire after a time period (security measure)

### 2. Event-Driven Architecture ⚡
- **Decoupled Services**: Services don't directly call each other
- **Reliable Messaging**: RabbitMQ ensures no messages are lost
- **Asynchronous Processing**: Services process events independently
- **Scalability**: Easy to add new services that listen to same events

### 3. Microservices Benefits 🎯
- **Independent Deployment**: Can update one service without stopping others
- **Technology Flexibility**: Can use different languages/frameworks per service
- **Fault Isolated**: One service failing doesn't crash the whole system
- **Scalable**: Can run multiple instances of heavily-used services

### 4. Retry Mechanisms 🔄
- **Failed Messages**: Retried up to 3 times before giving up
- **Exponential Backoff**: 1 second → 2 seconds → 4 seconds between retries
- **Dead Letter Queue**: Failed messages stored for investigation

### 5. Data Management 📊
- **Transactions**: Ensures data consistency
- **Relationships**: Orders link to customers and products
- **Audit Trail**: Can track what happened and when

---

## Security

### Authentication Flow

```
User enters credentials
        ↓
Frontend sends to /auth/login
        ↓
Backend validates username/password
        ↓
If valid: Generate JWT token
If invalid: Return error
        ↓
Frontend stores token in memory/localStorage
        ↓
Future API calls include token in header:
Authorization: Bearer <token>
        ↓
Backend verifies token is valid
        ↓
Allows or denies access
```

### Security Features

1. **Password Security**
   - Passwords are hashed (one-way encryption)
   - Even database admins can't read passwords
   - Stored securely in database

2. **JWT Tokens**
   - Tokens are signed with a secret key
   - Can't be forged
   - Contains user info and expiration
   - Stateless (no session storage needed)

3. **HTTPS Ready**
   - All communication can be encrypted
   - Prevents eavesdropping on API calls

4. **Input Validation**
   - All user inputs are validated
   - Prevents invalid/malicious data

---

## FAQ

### Q: What if one service crashes?
**A**: The system continues working. Other services are unaffected. The crashed service can be restarted independently. Messages are queued in RabbitMQ until the service is back up.

### Q: How does RabbitMQ know where to send messages?
**A**: Each service registers itself as a "listener" to specific queue names. When Order Service publishes to "ORDER_CREATED" queue, RabbitMQ automatically delivers to all registered listeners.

### Q: What if the database goes down?
**A**: Services can't operate without it. However:
- Messages in RabbitMQ are safe
- Once database is back up, messages are processed
- No data is lost (RabbitMQ persists messages to disk)

### Q: Can I add a new microservice?
**A**: Yes! For example, to add recommendation service:
1. Create new Spring Boot service
2. Listen to "ORDER_CREATED" events
3. Generate product recommendations
4. Return recommendations to frontend
5. No changes needed to existing services!

### Q: How many users can it handle?
**A**: Depends on infrastructure:
- Single servers: 100-1000 concurrent users
- With load balancing: 10,000+ concurrent users
- Scale by adding more service instances
- RabbitMQ handles increased message volume

### Q: What's the difference between REST API and Events?
**REST API** (synchronous):
```
Client: "Give me product details"     ← Request
Server: "Here are the details"        → Response (wait)
```
Caller waits for response.

**Events** (asynchronous):
```
Service A: "Here's an order event!"   → Publishes
RabbitMQ: Holds the message
Service B: "Got it, updating stock"   (processes whenever ready)
```
Publisher doesn't wait for response.

### Q: Why not just use one big application?
**Microservices advantages**:
- ✅ Independent scaling
- ✅ Fault isolation
- ✅ Technology flexibility
- ✅ Parallel development
- ✅ Easy deployment
- ❌ But: More complex to setup and monitor

---

## Understanding the Data Flow

### Complete Example: Customer Orders a Product

```
1. FRONTEND
   └─ User fills form: "I want 2 x Laptop"
   └─ Clicks "Place Order" button
   └─ Angular sends POST request with order data

2. API GATEWAY
   └─ Receives request at /orders
   └─ Checks if user is authenticated (validates JWT token)
   └─ Routes request to Order Service

3. ORDER SERVICE
   └─ Receives order: { items: [{ productId: 1, quantity: 2 }], userId: 5 }
   └─ Validates data (items exist, quantity > 0, etc.)
   └─ Saves order to PostgreSQL database
   └─ Order saved with status: "CREATED"
   └─ Creates event object with order details
   └─ Publishes event to RabbitMQ queue: "ORDER_CREATED"
   └─ Returns order confirmation to frontend

4. FRONTEND
   └─ Receives confirmation
   └─ Shows message: "Order placed successfully!"
   └─ Redirects to order history

5. RABBITQ (In Background)
   └─ Event stored in message queue
   └─ Several services are listening...

6. INVENTORY SERVICE
   └─ Receives: "ORDER_CREATED" event
   └─ Reads: Product 1 (Laptop), Quantity: 2
   └─ Checks database:
      Current stock of Laptop: 50
   └─ Calculates: 50 - 2 = 48
   └─ Updates database: Laptop stock is now 48
   └─ Logs the update

7. NOTIFICATION SERVICE
   └─ Receives: "ORDER_CREATED" event
   └─ Reads: Recipient email: customer@email.com
   └─ Sends email: "Thank you for your order! Order ID: 12345"
   └─ Logs notification delivery

8. CUSTOMER
   └─ Sees confirmation on website
   └─ Receives order confirmation email
   └─ System automatically reduced stock
   └─ Everyone is happy! 😊
```

---

## Summary

### What was built:
A modern, scalable **e-commerce order management system** that demonstrates enterprise software architecture.

### Key technologies:
- **Frontend**: Angular with TypeScript for interactive UI
- **Backend**: Java Spring Boot microservices for business logic
- **Messaging**: RabbitMQ for service communication
- **Database**: PostgreSQL for data persistence
- **DevOps**: Docker for containerization

### Why it matters:
This architecture is used by companies like Netflix, Amazon, and Uber because it:
- Scales to millions of users
- Handles high transaction volumes
- Remains available even when individual services have issues
- Allows rapid development and deployment
- Makes it easy to fix bugs without disrupting the system

### Real-world applications:
- E-commerce platforms (Amazon, eBay, Shopify)
- Food delivery apps (Uber Eats, DoorDash)
- Ride-sharing (Uber, Lyft)
- Streaming services (Netflix)
- Banking systems

This project is a simplified version of these production systems but demonstrates all the core principles! 🚀
