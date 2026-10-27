# Architecture Decision Records (ADR)

## ADR-01: Backend Framework and Authentication
- **Issue:** Managing role-based authorization securely for cross-platform (web/mobile) consumption.
- **Selected Option:** Java 21 & Spring Boot 3 REST API + Stateless JWT Authentication.
- **Alternative:** Server-side HttpSession (Cookies) or Node.js / Express.js.
- **Reason / Trade-off:** Provides strong typing, native @Transactional integrity, and stateless scalability. Eliminates server memory overhead compared to sessions, though it requires strict token expiration policies.
- **Reference URL:** https://docs.spring.io/spring-boot/index.html , RFC 7519 (JWT Standard)

## ADR-02: Database Management System (DBMS)
- **Issue:** Preventing race conditions and double-bookings during concurrent appointment requests.
- **Selected Option:** PostgreSQL 16 (RDBMS).
- **Alternative:** MongoDB (NoSQL) or SQLite.
- **Reason / Trade-off:** Appointment systems are highly relational. PostgreSQL guarantees strict ACID compliance and allows the implementation of Partial Unique Indexes to prevent conflicting records at the database level.
- **Reference URL:** https://www.postgresql.org/docs/16/mvcc.html
