# Kufi — Barber & Salon Discovery and Appointment API

**Academic project:** Altınbaş University · Software Engineering · 2026–2027

**Week 6 focus:** Testing & Validation (Development Stage 4)

**Author:** Ulaş Demir · Student ID 210513252

**Original repository:** https://github.com/Ulasdmr/kufi

> This directory is a locally prepared Week 6 code-and-test baseline based on prior submitted Kufi documents. It has **not** been verified as the current state of the remote GitHub repository, and this package was **not** pushed to GitHub. The included JUnit tests require a successful Maven run before their outcomes can be reported as PASS.

## Project overview
Kufi connects customers with local barber shops and salons, shows service prices/durations and supports appointment booking without overlapping reservations within a shop. The backend is a modular Spring Boot REST API using PostgreSQL for development and H2 for automated tests.

## Scope and implemented baseline
- Register and log in as `CUSTOMER` or `BARBER`, with BCrypt password hashes and bearer JWT.
- BARBER accounts can create shop profiles and service menus.
- Public visitors can browse shops and filter by district, view offered services.
- CUSTOMER accounts can create appointments; the backend computes the end time from service duration.
- Requests outside opening hours or overlapping an active appointment return HTTP 400/409 respectively.
- Customers and shop owners can cancel their appointments; cancelled slots become available again.
- **Week 6 testing:** pure-Java booking time smoke checks, JUnit domain tests, Spring MockMvc API tests, and an H2-backed concurrent booking test.

## Current limitations
No browser/mobile UI, staff-level scheduling, automatic public slot listing, user reviews, notifications, online payments, production database migrations or production deployment. A single shop is modelled as one booking resource. H2 cannot fully substitute for PostgreSQL concurrency testing. Test classes are supplied but have **not been run with Maven in the authoring environment**.

## Prerequisites
- JDK **17 or later** (`java -version`)
- Maven **3.9+** (`mvn -version`) for the Spring project and JUnit tests
- Optional for running the service: Docker Engine + Docker Compose with PostgreSQL 16, or local PostgreSQL 16

## Run tests first (PostgreSQL NOT required)
```bash
mvn --batch-mode --no-transfer-progress clean test
```
JUnit test evidence: `target/surefire-reports/` (created after execution). Upload **sanitized** logs or screenshots to `evidence/week-06/`.

The pure Java smoke check, runnable without Maven, is:
```bash
bash week6-pure-java/run-smoke.sh
```

## Launch PostgreSQL and the API
```bash
cp .env.example .env
# Edit .env and replace DB_PASSWORD and JWT_SECRET with secure local values.
# JWT_SECRET must contain at least 32 UTF-8 bytes; never commit your real secret.
docker compose up -d
set -a; source .env; set +a
mvn spring-boot:run
```
API default: `http://localhost:8080`. If Docker is not installed, create a local PostgreSQL 16 database named `kufi_db` for user `kufi`, update `.env`, then run Maven. Stop PostgreSQL with `docker compose down` (do not use `down -v` if you want to retain data).

## Core API endpoints
| Method | Path | Access |
|---|---|---|
| POST | `/api/v1/auth/register` | Public |
| POST | `/api/v1/auth/login` | Public |
| GET | `/api/v1/users/me` | JWT |
| POST | `/api/v1/shops` | BARBER |
| GET | `/api/v1/shops?district=Gungoren` | Public |
| POST | `/api/v1/shops/{id}/services` | BARBER owning shop |
| GET | `/api/v1/shops/{id}/services` | Public |
| POST | `/api/v1/appointments` | CUSTOMER |
| PATCH | `/api/v1/appointments/{id}/cancel` | Customer/owner |
| GET | `/api/v1/appointments/my` | JWT |

### Example booking body
After registering, creating a shop and adding a service, use a **future** time and actual database IDs:
```json
{"shopId":1,"serviceId":1,"startTime":"2030-05-10T14:00:00"}
```
Expected successful response status: 201. Rebooking the same slot should yield 409. Use a later future date if 2030 is no longer in the future.

## Week 6 evidence and reports
- `docs/week-06/Kufi_Week_06_Testing_Validation_Report.docx` / `.pdf`: academic weekly report, Development Stage 4.
- `docs/week-06/TEST_CASES.csv`: test IDs, expected/actual result status.
- `docs/week-06/GITHUB_ADIM_ADIM_TR.md`: Turkish GitHub merge/push instructions.
- `postman/Kufi_Week6.postman_collection.json`: API scenario requests (for manual Postman execution).
- `evidence/week-06/pure-java-smoke-2026-11-25.log`: one **actually executed** Java-only log.
- `.github/workflows/week6-tests.yml`: run JUnit test suite in GitHub Actions after pushing.

## Design & concurrency note
The Week 5 design described a JPQL interval-overlap query inside `@Transactional`. That check alone may be vulnerable to concurrent requests because both can see an empty slot. This testing baseline adds a `PESSIMISTIC_WRITE` lock to the shop row before checking overlap, serializing requests within that shop. The automated concurrency test is **pending a Maven run**, and production PostgreSQL confirmation remains necessary.

## Original requirements vs this runnable baseline
The Week 2 requirement IDs FR-01...FR-06 are authoritative, but Week 5 used different labels for some behaviors. Detailed mapping and incomplete items are listed in the Week 6 report; especially **FR-04 is partial** because a public available-slot listing does not yet exist, and **FR-06** reviews are intentionally deferred.

## References
- Spring Boot: https://docs.spring.io/spring-boot/reference/
- Spring Security: https://docs.spring.io/spring-security/reference/
- JUnit 5: https://junit.org/junit5/docs/current/user-guide/
- PostgreSQL: https://www.postgresql.org/docs/16/
- Maven Surefire: https://maven.apache.org/surefire/maven-surefire-plugin/
