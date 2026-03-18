# Technical Architecture Document

## System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              CLIENT LAYER                                   │
│                         (Web Browser on Customer's Device)                  │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │                     Angular Frontend Application                      │  │
│  │  • Product browsing interface                                         │  │
│  │  • Shopping cart functionality                                        │  │
│  │  • Order placement form                                               │  │
│  │  • Real-time notifications using RxJS                                │  │
│  │  • JWT token management for authentication                           │  │
│  └───────────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
                              HTTPS / REST API
                                    ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│                         API LAYER (Entry Point)                             │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │                    API Gateway (Spring Boot)                          │  │
│  │  • Single entry point for all client requests                         │  │
│  │  • Request routing logic                                              │  │
│  │  • JWT token validation                                               │  │
│  │  • CORS handling for cross-origin requests                           │  │
│  │  • Logs all incoming requests                                         │  │
│  └───────────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
           HTTP Requests routed to appropriate microservice
         ┌──────────────────┬──────────────────┬──────────────────┐
         ↓                  ↓                  ↓                  ↓
┌────────────────┐  ┌──────────────┐  ┌──────────────────┐  ┌───────────┐
│ ORDER SERVICE  │  │ INVENTORY    │  │ NOTIFICATION     │  │ (Future   │
│ (Port 8080)    │  │ SERVICE      │  │ SERVICE          │  │ Services) │
│                │  │ (Port 8081)  │  │ (Port 8082)      │  │           │
│ Responsibilities:
│                │  │
│ • Creates orders
│ • Stores order data
│ • Publishes events
│ • Retrieves orders
│ • Cancels orders
│
│ REST Endpoints:
│ POST   /orders
│ GET    /orders
│ GET    /orders/{id}
│ DELETE /orders/{id}
│                │  │ Responsibilities:
│                │  │                │  │
│                │  │ • Listens for  │  │ • Listens for
│                │  │ order events   │  │ order events
│                │  │ • Updates prod.│  │ • Sends emails
│                │  │ stock levels   │  │ • Sends SMS
│                │  │ • Prevents     │  │ • Logs events
│                │  │ overselling    │  │
│                │  │                │  │
│                │  │ Database:      │  │ Service:
│                │  │ • Product      │  │ • Email provider
│                │  │ • Quantity     │  │ • SMS provider
│                │  │ • LastUpdated  │  │
└────────────────┘  └──────────────┘  └──────────────────┘
         ↑                  ↑                  ↑
         └──────────────────┼──────────────────┘
                            ↓
                  MESSAGE BROKER LAYER
         ┌──────────────────────────────────────┐
         │      RabbitMQ (Event Bus)            │
         │  • Queue: ORDER_CREATED              │
         │  • Exchange: orders                  │
         │  • Routing Keys: order.*             │
         │  • Persistent message storage        │
         │  • Retry mechanism: 3 attempts       │
         │  • Management UI: :15672             │
         └──────────────────────────────────────┘
                            ↓
                  EVENT FLOW (Asynchronous)
              Order Publ. → RabbitMQ → Multiple Consumers
                            ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│                         DATA LAYER (PostgreSQL)                             │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │                    PostgreSQL Database (Port 5432)                    │  │
│  │                                                                        │  │
│  │  Tables:                                                               │  │
│  │  ├─ users (id, username, password_hash, email, created_at)           │  │
│  │  ├─ orders (id, user_id, order_date, status, total_amount)           │  │
│  │  ├─ order_items (id, order_id, product_id, quantity, price)          │  │
│  │  ├─ products (id, name, description, price, stock_quantity)          │  │
│  │  ├─ inventory_logs (id, product_id, old_qty, new_qty, timestamp)     │  │
│  │  └─ notifications (id, order_id, user_id, message, sent_at, status)  │  │
│  │                                                                        │  │
│  │  Data Persistence:                                                     │  │
│  │  • All data survives service restarts                                 │  │
│  │  • Volume mounted for data durability                                 │  │
│  │  • ACID compliance (Atomicity, Consistency, Isolation, Durability)    │  │
│  └───────────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Sequence Diagram: Complete Order Flow

```
Participant: Customer, Frontend, Gateway, Order Service, RabbitMQ, Inventory, Notification, Database

Customer->>Frontend: Fills order form & clicks "Place Order"
Frontend->>Frontend: Validates form data
Frontend->>Frontend: Retrieves JWT token
Frontend->>Gateway: POST /orders with JWT token and order data
Gateway->>Gateway: Validates JWT token
Gateway->>OrderService: Routes to Order Service
OrderService->>Database: Begins transaction
OrderService->>Database: Validates user exists
OrderService->>Database: Validates products exist
OrderService->>Database: Inserts order record
OrderService->>Database: Inserts order_items records
OrderService->>Database: Commits transaction
OrderService->>RabbitMQ: Publishes ORDER_CREATED event
OrderService->>Gateway: Returns order confirmation (201 Created)
Gateway->>Frontend: Returns success response
Frontend->>Customer: Shows "Order placed successfully!"

[In Background - Asynchronously]
RabbitMQ->>RabbitMQ: Stores event in queue
RabbitMQ->>InventoryService: Delivers ORDER_CREATED event
RabbitMQ->>NotificationService: Delivers ORDER_CREATED event

InventoryService->>Database: Retrieves product quantities
InventoryService->>Database: Calculates: current_qty - ordered_qty
InventoryService->>Database: Updates product stock
InventoryService->>Database: Inserts inventory_log record
InventoryService->>Database: Commits

NotificationService->>Database: Retrieves customer email
NotificationService->>EmailProvider: Sends confirmation email
EmailProvider->>EmailProvider: Delivers to customer inbox
NotificationService->>Database: Inserts notification record
NotificationService->>Database: Sets status = "SENT"
```

## Data Flow for Each Microservice

### Order Service Data Flow
```
Input (from Frontend):
  {
    userId: 123,
    items: [
      { productId: 1, quantity: 2 },
      { productId: 5, quantity: 1 }
    ]
  }
          ↓
  [Validation Layer]
  - User exists?
  - Products exist?
  - Quantities available?
  - User authenticated?
          ↓
  [Business Logic Layer]
  - Calculate total amount
  - Create Order object
  - Create OrderItems objects
          ↓
  [Database Layer]
  - Save to users_orders table
  - Save to order_items table
  - Commit transaction
          ↓
  [Message Layer]
  - Create ORDER_CREATED event
  - Convert to JSON
  - Publish to RabbitMQ
          ↓
Output (to Frontend):
  {
    id: 5001,
    userId: 123,
    status: "CREATED",
    items: [...],
    createdAt: "2024-03-18T10:30:00Z"
  }
```

### Inventory Service Data Flow
```
Input (from RabbitMQ):
  OrderCreatedEvent {
    orderId: 5001,
    orderItems: [
      { productId: 1, quantity: 2 },
      { productId: 5, quantity: 1 }
    ]
  }
          ↓
  [Message Handler]
  - Deserialize JSON to Java object
  - Extract order items
          ↓
  [Business Logic]
  For each item in order:
    1. Query: SELECT quantity FROM products WHERE id = productId
    2. Calculate: newQty = currentQty - orderedQty
    3. Validate: newQty >= 0 (prevent overselling)
    4. Update: UPDATE products SET quantity = newQty
    5. Log: INSERT INTO inventory_logs (...)
          ↓
  [Error Handling]
  If insufficient stock:
    - Log error
    - Send alert to admin
    - Fail gracefully
    - RabbitMQ will retry the message
          ↓
Output (stored in Database):
  inventory_logs table:
  {
    id: 12345,
    productId: 1,
    previousQuantity: 50,
    newQuantity: 48,
    orderId: 5001,
    timestamp: "2024-03-18T10:30:02Z"
  }
```

### Notification Service Data Flow
```
Input (from RabbitMQ):
  OrderCreatedEvent {
    orderId: 5001,
    userId: 123,
    customerEmail: "john@example.com",
    items: [...]
  }
          ↓
  [Message Handler]
  - Extract customer info
  - Extract order details
          ↓
  [Email Template Builder]
  - Build HTML email
  - Include order number, items, total
  - Add order tracking link
          ↓
  [Notification Logic]
  - Validate email format
  - Prepare email payload
          ↓
  [External Service Call]
  - Send via Email Provider (SMTP/SendGrid/AWS SES)
          ↓
  [Error Handling]
  If email fails:
    - RabbitMQ retries (exponential backoff)
    - After 3 retries, moves to dead letter queue
    - Admin alerted for manual intervention
          ↓
  [Database Update]
  - Log: INSERT INTO notifications (...)
  - Set status: "SENT" or "FAILED"
  - Record timestamp
          ↓
Result:
  Customer receives email confirmation with order details
```

## Technology Stack - Detailed Components

### Frontend Stack
```
Angular Framework
├── Core (@angular/core)
│   ├── Components (UI building blocks)
│   ├── Services (business logic reuse)
│   ├── Dependency Injection (IoC container)
│   └── Change Detection (React to data changes)
│
├── Common (@angular/common)
│   ├── NgIf, NgFor (structural directives)
│   ├── Pipes (format data)
│   └── HTTP utilities
│
├── Forms (@angular/forms)
│   ├── Reactive Forms (form validation)
│   ├── Form Controls (field management)
│   └── Validators (email, required, etc.)
│
├── Router (@angular/router)
│   ├── Routing rules (URL → Component)
│   ├── Lazy loading (load modules on-demand)
│   ├── Guards (protect routes)
│   └── Navigation
│
└── Platform Browser (@angular/platform-browser)
    ├── DOM rendering
    ├── Event handling
    └── Browser APIs

RxJS - Reactive Programming Library
├── Observables (data streams)
├── Operators (map, filter, switchMap, etc.)
├── Subjects (multicast observables)
└── Used for:
    ├── HTTP request handling
    ├── Form value changes
    ├── Real-time updates
    └── Error handling

TypeScript
├── Type Safety (prevent runtime errors)
├── Interfaces (define data contracts)
├── Classes (OOP)
├── Enums (named constants)
└── Benefits:
    ├── Catch errors at compile-time
    ├── Better IDE autocomplete
    └── Self-documenting code
```

### Backend Stack

#### Spring Boot Ecosystem
```
Spring Framework (Core IoC Container)
├── Application Context (manages beans)
├── Dependency Injection (loose coupling)
├── Configuration Management
└── Event Publishing

Spring Web MVC
├── DispatcherServlet (central request handler)
├── Controllers (handle HTTP requests)
├── Request Mapping (@PostMapping, @GetMapping)
├── Response handling (JSON serialization)
└── Exception handling

Spring Data JPA
├── Repository Pattern (CRUD operations)
├── Query Methods (auto-generated queries)
│   └── findById, findAll, save, delete
├── JPQL (Java Persistence Query Language)
├── Native SQL support
└── Transaction Management (@Transactional)

Spring Security
├── Authentication (who are you?)
│   ├── UsernamePasswordAuthenticationFilter
│   ├── AuthenticationManager
│   └── PasswordEncoder
├── Authorization (what can you do?)
│   ├── Role-based access control
│   ├── Method-level security (@Secured)
│   └── Endpoint protection
├── JWT Integration
│   ├── Token generation
│   ├── Token validation
│   └── Token refresh
└── CORS (Cross-Origin Resource Sharing)

Spring AMQP
├── RabbitTemplate (send messages)
├── @RabbitListener (receive messages)
├── Message Converters (JSON serialization)
├── Retry Configuration
│   ├── Max attempts
│   ├── Backoff policy
│   ├── Dead letter queue
│   └── Exception handling
└── Connection Factory (manage connections)
```

#### Java 17 Features Used
```
Modern Java Syntax:
├── Records (immutable data classes)
├── Text Blocks (multi-line strings)
├── Switch Expressions (pattern matching ready)
├── Sealed Classes (restrict inheritance)
└── Stream API (functional programming)

Benefits:
├── Cleaner code
├── Better performance
├── Long-term support (LTS release)
└── Security improvements
```

### Messaging Infrastructure

#### RabbitMQ Architecture
```
RabbitMQ Server (Broker)
│
├── Exchanges (publish point)
│   ├── Type: Direct
│   ├── Type: Fanout (broadcast to all queues)
│   ├── Type: Topic (pattern-based routing)
│   └── Type: Headers (header-based routing)
│
├── Queues (storage point)
│   ├── Durable (survives restarts)
│   ├── Queue Length (max messages)
│   ├── TTL (time to live)
│   └── Dead Letter Exchange (failed messages)
│
├── Bindings (Exchange → Queue mapping)
│   └── Routing Keys (defining patterns)
│
└── Connections & Channels
    ├── Connection pooling
    ├── Multiplexing (multiple channels per connection)
    └── Flow control (backpressure handling)

Message Flow:
Producer → Exchange → Routing Key → Queue → Consumer
```

#### Event Schema
```
ORDER_CREATED Event:
{
  "eventId": "evt_20240318_001",
  "eventType": "ORDER_CREATED",
  "timestamp": "2024-03-18T10:30:00Z",
  "aggregateId": {
    "orderId": 5001
  },
  "data": {
    "userId": 123,
    "totalAmount": 299.99,
    "orderItems": [
      {
        "productId": 1,
        "productName": "Laptop",
        "quantity": 2,
        "unitPrice": 149.99
      }
    ],
    "shippingAddress": {
      "street": "123 Main St",
      "city": "Springfield",
      "country": "USA"
    }
  },
  "metadata": {
    "correlationId": "corr_12345",
    "source": "order-service",
    "version": "1.0"
  }
}
```

### Database Schema

#### PostgreSQL Tables

**users Table:**
```sql
CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(50) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  email VARCHAR(100) UNIQUE NOT NULL,
  first_name VARCHAR(50),
  last_name VARCHAR(50),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  is_active BOOLEAN DEFAULT true
);
```

**products Table:**
```sql
CREATE TABLE products (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  price DECIMAL(10, 2) NOT NULL,
  stock_quantity INT DEFAULT 0,
  sku VARCHAR(100) UNIQUE NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  is_available BOOLEAN DEFAULT true
);
```

**orders Table:**
```sql
CREATE TABLE orders (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id),
  order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  status VARCHAR(50) DEFAULT 'CREATED', -- CREATED, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
  total_amount DECIMAL(10, 2) NOT NULL,
  shipping_address VARCHAR(255),
  notes TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**order_items Table:**
```sql
CREATE TABLE order_items (
  id BIGSERIAL PRIMARY KEY,
  order_id BIGINT NOT NULL REFERENCES orders(id),
  product_id BIGINT NOT NULL REFERENCES products(id),
  quantity INT NOT NULL,
  unit_price DECIMAL(10, 2) NOT NULL,
  subtotal DECIMAL(10, 2) NOT NULL,
  CONSTRAINT check_quantity CHECK (quantity > 0)
);
```

**inventory_logs Table:**
```sql
CREATE TABLE inventory_logs (
  id BIGSERIAL PRIMARY KEY,
  product_id BIGINT NOT NULL REFERENCES products(id),
  order_id BIGINT REFERENCES orders(id),
  previous_quantity INT NOT NULL,
  new_quantity INT NOT NULL,
  change_reason VARCHAR(100), -- 'ORDER_PLACED', 'RESTOCK', 'ADJUSTMENT'
  timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**notifications Table:**
```sql
CREATE TABLE notifications (
  id BIGSERIAL PRIMARY KEY,
  order_id BIGINT NOT NULL REFERENCES orders(id),
  user_id BIGINT NOT NULL REFERENCES users(id),
  notification_type VARCHAR(50), -- 'EMAIL', 'SMS', 'PUSH'
  recipient VARCHAR(255),
  message TEXT,
  status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, SENT, FAILED
  sent_at TIMESTAMP,
  failed_reason TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Deployment Architecture

### Development Environment (Local)
```
Docker Desktop
├── Container 1: PostgreSQL 15
│   ├── Port: 5432
│   ├── Volume: postgres_data (persistent)
│   └── Network: bridge (docker network)
├── Container 2: RabbitMQ 3
│   ├── AMQP Port: 5672
│   ├── Management UI Port: 15672
│   └── Network: bridge
└── Host Machine
    ├── Order Service (8080) - Spring Boot
    ├── Inventory Service (8081) - Spring Boot
    ├── Notification Service (8082) - Spring Boot
    └── Frontend (4200) - Angular Dev Server
```

### Production Architecture (Recommended)
```
Kubernetes Cluster
├── Frontend Pod Replicas (3+)
│   └── Angular + Nginx (load balanced)
├── API Gateway Pod Replicas (3+)
│   └── Spring Boot
├── Order Service Pod Replicas (5+)
│   └── Spring Boot (higher load)
├── Inventory Service Pod Replicas (5+)
│   └── Spring Boot (higher load)
├── Notification Service Pod Replicas (3+)
│   └── Spring Boot (lower priority)
├── PostgreSQL StatefulSet (with backup)
│   └── Primary + Standby replicas
├── RabbitMQ Cluster (3 nodes)
│   ├── Node 1 (elected as leader)
│   ├── Node 2 (mirror queue)
│   └── Node 3 (mirror queue)
└── Monitoring
    ├── Prometheus (metrics)
    ├── Grafana (dashboards)
    ├── LogStash (log aggregation)
    └── Alerts (PagerDuty/OpsGenie)
```

## Performance Considerations

### Scalability Points

**Database:**
- Connection pooling (HikariCP)
- Query optimization with indices
- Replication for read scaling
- Sharding for write scaling

**Message Queue:**
- Consumer group scaling
- Partition replication
- Memory optimization
- Persistent storage

**Services:**
- Horizontal scaling (add more instances)
- Load balancing (round-robin, least connections)
- Circuit breaker pattern (prevent cascading failures)
- Caching (Redis for frequently accessed data)

## Security Architecture

### Authentication & Authorization Flow

```
1. User Login
   POST /auth/login
   Body: { username, password }
              ↓
2. Server Validates
   - Bcrypt comparison with stored hash
   - Check if user is active
              ↓
3. Token Generation
   - Create JWT payload: { userId, roles, exp: now + 24h}
   - Sign with secret key
   - Return token to client
              ↓
4. Client Stores Token
   - Stores in HttpOnly cookie OR memory
              ↓
5. Subsequent Requests
   Header: Authorization: Bearer <token>
              ↓
6. Server Validates Token
   - Verify signature (not tampered)
   - Check expiration (not expired)
   - Extract user info
              ↓
7. Allow/Deny Access
   - ✅ Valid: Process request
   - ❌ Invalid/Expired: Return 401 Unauthorized
              ↓
8. Token Refresh
   - Client can request new token before expiration
   - Secure refresh token rotation
```

### Data Security

- **Encryption at Rest**: Database encryption (TDE)
- **Encryption in Transit**: HTTPS/TLS
- **Password Hashing**: Bcrypt (adaptive hash)
- **Rate Limiting**: Prevent brute force attacks
- **Input Validation**: Prevent SQL injection
- **CORS Configuration**: Only trusted origins

---

## Summary of Tech Stack Benefits

| Technology | Benefit |
|-----------|---------|
| **Angular** | Fast, interactive UI; two-way binding; built-in testing |
| **TypeScript** | Type safety; catch errors early; better IDE support |
| **Java 17** | Stable LTS; great performance; large ecosystem |
| **Spring Boot** | Convention over configuration; auto-configuration; production-ready |
| **Spring Data JPA** | Less boilerplate; easier database operations; caching support |
| **RabbitMQ** | Reliable delivery; multiple consumer support; message persistence |
| **PostgreSQL** | ACID compliance; JSON support; advanced querying; scaling |
| **Docker** | Consistency across environments; easy deployment; lightweight |
| **JWT** | Stateless; scalable; RESTful; works across domains |

This combination creates a **enterprise-grade, highly scalable, fault-tolerant system** that can serve thousands of concurrent users while maintaining data integrity and system reliability.
