# Quick Reference & Troubleshooting Guide

## 📚 Glossary of Terms

### Cloud & Architecture Terms

**Microservices**
- Multiple small, independent services instead of one big application
- Each service does one thing well
- Services communicate with each other
- Easier to scale and maintain

**Event-Driven Architecture**
- Services communicate by publishing and consuming events
- Asynchronous processing (don't wait for response)
- Enables loose coupling between services
- Example: Order Service → publishes "ORDER_CREATED" → Inventory & Notification services listen

**REST API (Representational State Transfer)**
- Standard way for applications to communicate over HTTP
- Uses URLs and HTTP verbs (GET, POST, PUT, DELETE)
- Request-Response model (synchronous)
- Example: `POST /orders` - create new order

**Message Queue / Message Broker**
- System that receives messages from one service and delivers to others
- Temporarily stores messages
- Ensures no messages are lost
- RabbitMQ is the message broker in this project

**Asynchronous Processing**
- Fire and forget
- Don't wait for result
- Services process at their own pace
- Example: Publish order event, continue without waiting

**Synchronous Processing**
- Wait for response
- Blocking operation
- Classic request-response
- Example: API call that returns data immediately

---

### Database Terms

**PostgreSQL**
- Relational database
- Stores data in tables with rows and columns
- ACID compliant (reliable transactions)
- Supports complex queries

**CRUD Operations**
- **Create**: Add new record
- **Read**: Retrieve data
- **Update**: Modify existing record
- **Delete**: Remove record
- Fundamental database operations

**Transaction**
- A series of database operations that must all succeed or all fail
- Ensures data consistency
- Example: Insert order and order items must both succeed or both fail

**Table**
- Collection of related data organized in rows and columns
- Like a spreadsheet
- Has columns (fields) and rows (records)

**Foreign Key**
- A field that references another table
- Creates relationships between tables
- Example: order.user_id references users.id

---

### Web & Security Terms

**JWT (JSON Web Token)**
- Token that proves user identity
- Contains user info, expiration, digital signature
- Cannot be forged (signed with secret key)
- Stateless (no server-side storage needed)

**Authentication**
- Verifying who you are
- "Are you really John@example.com?"
- Done with username/password or token

**Authorization**
- Determining what you can do
- "Can you access this resource?"
- Based on roles/permissions

**HTTPS**
- Secure version of HTTP
- Encrypts communication (padlock in browser)
- Prevents eavesdropping
- Required for sending passwords/tokens

**CORS (Cross-Origin Resource Sharing)**
- Rules allowing websites to communicate across different domains
- Prevents malicious websites from stealing data
- Example: Angular app from localhost:4200 can call API at localhost:8080

**API Endpoint**
- A specific URL that performs an action
- Example: `POST /orders` can create an order
- `GET /orders/123` can retrieve order 123

---

### Frontend Terms

**Component**
- Reusable piece of UI (button, form, card, etc.)
- Has HTML template, CSS styling, TypeScript logic
- Angular application is made of multiple components

**Service**
- Reusable business logic (not tied to UI)
- Makes API calls
- Manages data
- Shared across components

**RxJS Observable**
- Represents a stream of data over time
- Like a subscription (get updates automatically)
- Used for: API responses, form changes, timers

**Two-Way Binding**
- Angular automatically syncs UI with component data
- When user types in input field, component variable updates
- When component variable changes, UI updates automatically

**Directive**
- Marker on a DOM element that tells Angular to do something
- Examples: *ngIf (show/hide), *ngFor (loop), [ngClass] (conditional styling)

**Dependency Injection**
- Framework automatically provides dependencies to components
- Don't manually create services
- Framework manages the injection

---

### Backend Terms

**Spring Boot**
- Java framework that makes building web apps easy
- Auto-configures common settings
- Creates standalone executable JAR files
- Production-ready out of the box

**Annotation**
- Metadata marker with `@` symbol
- Tells framework/compiler to do something
- Examples: @RestController, @Service, @Repository, @Transactional

**Controller**
- The "handler" of HTTP requests
- Receives request → processes → returns response
- Maps URLs to methods

**Service**
- Contains business logic
- Called by controllers
- Doesn't handle HTTP directly
- Reusable across controllers

**Repository**
- Data access layer
- Handles database operations (CRUD)
- Separates data logic from business logic

**DTO (Data Transfer Object)**
- Simple object for transferring data
- Doesn't have business logic
- Used for request/response bodies
- Decouples API from internal data structures

**Entity**
- Represents database table in Java
- Uses annotations to map to database
- Example: `@Entity class Order { ... }` maps to orders table

**Spring Dependency Injection**
- Framework automatically creates and injects objects
- Reduces coupling between components
- Makes testing easier
- Manages object lifecycle

---

### DevOps Terms

**Docker Container**
- Lightweight, isolated environment for running applications
- Package includes app + dependencies + OS
- "Build once, run anywhere"
- Like a virtual machine but lighter and faster

**Docker Image**
- Blueprint for creating containers
- Read-only template
- Contains OS, libraries, application code

**Docker Compose**
- Tool to run multiple containers together
- Defines entire application stack in one file
- One command to start everything

**Volume (Docker)**
- Persistent storage for containers
- Data survives container restart
- Can be shared between containers
- Example: PostgreSQL volume stores database files

**Network (Docker)**
- Enables communication between containers
- Containers can reference each other by service name
- Example: `postgres:5432` hostname works within Docker network

**Port Mapping**
- Maps container port to host port
- Container: 5432 → Host: 5432
- Format: `host:container`

---

### RabbitMQ Terms

**Producer / Publisher**
- Service that sends messages (Order Service sends events)

**Consumer / Subscriber**
- Service that receives messages (Inventory & Notification listen)

**Queue**
- Buffer that stores messages
- FIFO (First In, First Out)
- Messages wait here until consumer processes them

**Exchange**
- Receives messages from producer
- Forwards to queues based on routing rules
- Like a switchboard

**Routing Key**
- Identifies which queue receives the message
- Example: messages with key "order.created" go to certain queue

**Binding**
- Connection between exchange and queue
- Defines routing rules
- Example: "When routing key matches 'order.*', send to inventory_queue"

**Dead Letter Queue (DLQ)**
- Holds messages that failed to process
- After max retries, messages go here
- Admin can investigate failures

**Message Persistence**
- Messages written to disk
- Survives RabbitMQ restart
- Ensures no message loss

---

## ⚙️ Common Tasks

### Starting the Project

**Full Startup:**
```bash
# Terminal 1 - Start Docker services
cd docker
docker-compose up -d

# Wait 10 seconds for services to start

# Terminal 2 - Order Service
cd backend/order-service
mvn spring-boot:run

# Terminal 3 - Inventory Service
cd backend/inventory-service
mvn spring-boot:run

# Terminal 4 - Notification Service
cd backend/notification-service
mvn spring-boot:run

# Terminal 5 - Frontend
cd frontend
npm start
```

**Access Points:**
- Frontend: http://localhost:4200
- Order Service: http://localhost:8080
- Inventory Service: http://localhost:8081
- Notification Service: http://localhost:8082
- RabbitMQ Dashboard: http://localhost:15672 (guest/guest)
- PostgreSQL: localhost:5432 (postgres/password)

### Stopping the Project

```bash
# Stop Angular (Terminal 5): Ctrl+C
# Stop services (Terminal 2-4): Ctrl+C each
# Stop Docker containers:
cd docker
docker-compose down
```

### Making Code Changes

**Backend Changes (Order Service example):**
```bash
cd backend/order-service
# Make changes to files
# Stop: Ctrl+C
mvn spring-boot:run  # Automatically recompiles and restarts
```

**Frontend Changes:**
```bash
cd frontend
# Make changes to TypeScript/HTML/CSS
# Angular automatically recompiles and refreshes browser
```

### Database Operations

**Check PostgreSQL is running:**
```bash
# Should see postgres container running
docker-compose ps
```

**Connect to PostgreSQL:**
```bash
docker-compose exec postgres psql -U postgres -d orderdb

# Inside psql:
\dt                    # Show all tables
SELECT * FROM users;   # View users table
SELECT * FROM orders;  # View orders table
\q                     # Exit psql
```

**Reset Database:**
```bash
# WARNING: Deletes all data!
docker-compose down -v  # -v removes volumes (delete data)
docker-compose up -d    # Creates fresh database
```

### Testing API Endpoints

**Using cURL:**
```bash
# Get all orders
curl -X GET http://localhost:8080/orders \
  -H "Authorization: Bearer <jwt_token>"

# Create order
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <jwt_token>" \
  -d '{"userId": 1, "items": [{"productId": 1, "quantity": 2}]}'
```

**Using Postman:**
1. Open Postman
2. Create new request
3. Set method (GET, POST, etc.)
4. Set URL (http://localhost:8080/orders)
5. Add Authorization header with Bearer token
6. Add request body if needed
7. Click Send

---

## 🐛 Troubleshooting

### Problem: "Port already in use"

**Error message:**
```
Port 8080 is already in use
```

**Solution:**
```bash
# Find what's using the port (macOS/Linux)
lsof -i :8080

# Kill the process
kill -9 <PID>

# Or use a different port
# Edit application.properties and change server.port=8080 to 8081
```

### Problem: "Cannot connect to RabbitMQ"

**Error message:**
```
Connection refused. Trying next server.
```

**Solution:**
```bash
# Check if containers are running
docker-compose ps

# If not running:
cd docker
docker-compose up -d

# Check RabbitMQ logs
docker-compose logs rabbitmq

# Restart RabbitMQ
docker-compose restart rabbitmq
```

### Problem: "Database connection refused"

**Error message:**
```
java.io.IOException: Connection to localhost:5432 refused
```

**Solution:**
```bash
# Check if PostgreSQL is running
docker-compose ps

# Check PostgreSQL logs
docker-compose logs postgres

# Restart PostgreSQL
docker-compose restart postgres

# If still fails, reset database
docker-compose down -v
docker-compose up -d
```

### Problem: "JWT token expired"

**Error message:**
```
401 Unauthorized: Token has expired
```

**Solution:**
- Get a new token by logging in again
- Or check token expiration time in code (usually 24 hours)
- Token should be automatically included in frontend

### Problem: "Orders not being created"

**Debug steps:**
1. Check Order Service logs (look for errors)
2. Verify PostgreSQL is running: `docker-compose ps`
3. Verify RabbitMQ is running: `docker-compose ps`
4. Check frontend console for errors (F12 → Console tab)
5. Verify JWT token is valid
6. Check request payload format

### Problem: "Inventory not updating after order"

**Debug steps:**
1. Check RabbitMQ is running
2. Check Inventory Service logs
3. Verify queues exist in RabbitMQ dashboard: http://localhost:15672
4. Check if message reached queue (check Dead Letter Queue)
5. Restart Inventory Service: `Ctrl+C` then `mvn spring-boot:run`

### Problem: "Frontend cannot connect to backend"

**Error message:**
```
CORS error in browser console
Access to XMLHttpRequest blocked by CORS policy
```

**Solution:**
1. Verify backend service is running on correct port
2. Check API endpoint URL matches (http://localhost:8080)
3. Backend should have CORS configuration (already included)
4. Verify JWT token is being sent in headers

### Problem: "npm start doesn't work"

**Error message:**
```
ng: command not found
```

**Solution:**
```bash
cd frontend
npm install               # Install Angular CLI locally
npm start                 # Now should work
# Or use: npx ng serve
```

### Problem: "Maven build fails"

**Error message:**
```
[ERROR] COMPILATION ERROR
```

**Solution:**
```bash
# Clear Maven cache
mvn clean

# Try building again
mvn install

# If still fails, check Java version
java -version   # Should be 17 or higher

# Check pom.xml for dependency issues
```

---

## 🔍 Monitoring & Debugging

### View Logs

**Order Service:**
```bash
# Already running in terminal, see output
# Or save to file:
mvn spring-boot:run > order-service.log 2>&1
```

**Docker containers:**
```bash
# View specific service logs
docker-compose logs postgres
docker-compose logs rabbitmq

# Follow logs (live updates)
docker-compose logs -f postgres

# Show last 100 lines
docker-compose logs --tail=100 postgres
```

### RabbitMQ Management Dashboard

1. Open http://localhost:15672
2. Login: guest / guest
3. Tabs:
   - **Connections**: Active connections
   - **Channels**: Message channels
   - **Queues**: Message queues (see pending messages)
   - **Exchanges**: Message exchanges
   - **Admin**: Users, vhosts

### Check Database State

```bash
# Connect to database
docker-compose exec postgres psql -U postgres -d orderdb

# View recent orders
SELECT id, user_id, status, created_at FROM orders ORDER BY created_at DESC LIMIT 10;

# View inventory changes
SELECT product_id, previous_quantity, new_quantity, timestamp FROM inventory_logs ORDER BY timestamp DESC LIMIT 10;

# View notifications
SELECT id, user_id, status, sent_at FROM notifications ORDER BY sent_at DESC LIMIT 10;
```

### Health Checks

**Check all services are responsive:**
```bash
# Order Service health
curl http://localhost:8080/actuator/health

# Inventory Service health
curl http://localhost:8081/actuator/health

# Should return: {"status":"UP"}
```

---

## 📝 API Quick Reference

### Authentication

**Login:**
```
POST /auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "password"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "username": "admin"
}
```

### Orders

**Create Order:**
```
POST /orders
Authorization: Bearer <token>
Content-Type: application/json

{
  "userId": 1,
  "items": [
    {"productId": 1, "quantity": 2},
    {"productId": 3, "quantity": 1}
  ]
}

Response: 201 Created
{
  "id": 5001,
  "status": "CREATED",
  "totalAmount": 299.99,
  "items": [...]
}
```

**Get All Orders:**
```
GET /orders
Authorization: Bearer <token>

Response: 200 OK
[
  {"id": 5001, "status": "CREATED", ...},
  {"id": 5002, "status": "CREATED", ...}
]
```

**Get Order by ID:**
```
GET /orders/5001
Authorization: Bearer <token>

Response: 200 OK
{
  "id": 5001,
  "status": "CREATED",
  "items": [...]
}
```

**Cancel Order:**
```
DELETE /orders/5001
Authorization: Bearer <token>

Response: 204 No Content
```

### Products

**Get All Products:**
```
GET /products
Authorization: Bearer <token>

Response: 200 OK
[
  {"id": 1, "name": "Laptop", "price": 999.99, "stock": 50, ...},
  {"id": 2, "name": "Mouse", "price": 29.99, "stock": 200, ...}
]
```

---

## 📚 Learning Resources

### For Understanding Microservices
- [Microservices.io by Chris Richardson](https://microservices.io)
- YouTube: "Microservices Architecture" by various creators

### For Angular
- [Angular Official Documentation](https://angular.io/docs)
- [RxJS Documentation](https://rxjs.dev)

### For Spring Boot
- [Spring Boot Official Docs](https://spring.io/projects/spring-boot)
- [Spring Data JPA Guide](https://spring.io/projects/spring-data-jpa)

### For RabbitMQ
- [RabbitMQ Tutorials](https://www.rabbitmq.com/getstarted.html)
- [RabbitMQ Management UI Guide](https://www.rabbitmq.com/management.html)

### For Docker
- [Docker Official Documentation](https://docs.docker.com)
- [Docker Compose Documentation](https://docs.docker.com/compose/)

### For PostgreSQL
- [PostgreSQL Official Documentation](https://www.postgresql.org/docs/)

---

## ✅ System Health Checklist

Before reporting an issue, verify:

- [ ] Docker containers running: `docker-compose ps`
- [ ] All 3 backend services running
- [ ] Frontend running on port 4200
- [ ] Can access http://localhost:4200
- [ ] Can login with admin/password
- [ ] RabbitMQ dashboard accessible: http://localhost:15672
- [ ] Can create order in frontend
- [ ] Order appears in Order Service logs
- [ ] Order appears in database: `SELECT * FROM orders;`
- [ ] Inventory updates after order
- [ ] No ERROR lines in service logs

If all check, system is healthy! ✅

---

## 🚀 Performance Tips

### Frontend Optimization
- Enable production build: `npm run build`
- Use lazy loading for routes
- Implement virtual scrolling for long lists
- Minimize API calls using caching

### Backend Optimization
- Add database indexes on frequently queried columns
- Implement caching (Redis)
- Use connection pooling (HikariCP)
- Monitor slow queries

### Message Queue Optimization
- Increase consumer instances if messages pile up
- Adjust batch processing sizes
- Monitor dead letter queue for issues
- Increase RabbitMQ memory if needed

### Database Optimization
- Regular backups
- Monitor query performance
- Archive old data
- Use replication for scaling reads

