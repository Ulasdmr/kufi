# Kufi — Week 7 integrated architecture

This describes the locally prepared code; it is not proof of a fresh clone or a PostgreSQL run. Update if your actual branch differs.

```mermaid
flowchart TB
  Client[Customer or Barber REST client] --> JWT[Spring Security and JWT filter]
  JWT --> Auth[Auth / User controller]
  JWT --> Shop[Shop and Service catalog controller]
  JWT --> Booking[Appointment controller and service]
  Booking --> Rules[BookingTimeRules]
  Booking --> Lock[Shop row lock PESSIMISTIC_WRITE]
  Auth --> JPA[Spring Data JPA and Hibernate]
  Shop --> JPA
  Lock --> JPA
  JPA --> Pg[(PostgreSQL 16 runtime)]
  JPA -.-> H2[(H2 tests)]
```

**Flow:** Client → signed JWT → appointment POST → service load → locked shop row → duration and hours checks → JPQL overlap check → insert or HTTP 409. The lock is per shop rather than per individual barber.

**Week 3 comparison:** Conceptual separate UI/security/controller/service/storage layers were consolidated into a modular Spring Boot monolith and one PostgreSQL datastore. The bundled source currently has a REST API but no implemented frontend, so do not claim a mobile interface or UI screenshot.
