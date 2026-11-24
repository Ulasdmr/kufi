# Kufi — Week 7 clean install and reproduction guide

## Prerequisites
- JDK >=17 (source target 17), Maven >=3.9, Git, Python 3 for optional HTTP smoke, and PostgreSQL 16 or Docker Compose.
- Observed in user's initial test log: Java 24.0.2, Maven 3.9.11, Spring Boot 3.5.5, H2 2.3.232. These are not proof of a tested clean-room Week 7 setup.

## 1. Clone (only after Week7 branch is actually pushed)
```bash
git clone --branch week7-integration https://github.com/Ulasdmr/kufi kufi-clean-week7
cd kufi-clean-week7
git rev-parse HEAD
java -version
mvn -version
```

## 2. Run local JUnit (no PostgreSQL needed)
```bash
bash week6-pure-java/run-smoke.sh
mvn --batch-mode --no-transfer-progress clean verify
bash scripts/collect_week6_evidence.sh
```
Expected **13/13** JUnit if all matching source/tests present; record actual results. The 6 Java-only smoke checks are separate.

## 3. Configure and start PostgreSQL
```bash
cp .env.example .env
# Edit .env with unique LOCAL DB_PASSWORD and JWT_SECRET at least 32 bytes.
# Do not commit or share secrets.
docker compose --env-file .env up -d
docker compose --env-file .env ps
```
If you cannot use Docker, create a local PostgreSQL 16 `kufi_db` database and `kufi` user, then configure `.env` accordingly.

## 4. Start the API in a SECOND terminal
```bash
set -a
source .env
set +a
mvn spring-boot:run
```
Expected base URL is `http://localhost:8080`. Change `PORT` and smoke URL if port occupied.

## 5. Test integration in a THIRD terminal
```bash
python3 scripts/week7_integration_smoke.py --base-url http://localhost:8080
```
The script uses fresh, random test accounts without exposing passwords or tokens. Tests shop/service creation, search, booking, conflict, unauthorized cancel, successful cancel, rebook, and customer schedule. **Expected** is 14/14; not yet executed on the user's runtime. Save a real sanitized log.

## 6. Complete proof
- Fill actual values (not guesses) in `REPRODUCIBILITY_RESULTS.csv` for R01–R07.
- Record tester, date, OS, dependency versions, Git hash and error/correction/retest where relevant.
- Have a peer follow instructions, or state self-tested if no peer is available.
- For real PostgreSQL concurrency, create two truly simultaneous requests separately; H2 test is not proof.

## Troubleshooting
| Error | Likely check |
|---|---|
| no pom.xml | Wrong directory; cd into repo root |
| connection refused 8080 | API server not running |
| 5432 port in use | Another Postgres container/installation |
| failed DB password | Recheck .env, Compose and roles |
| JWT secret missing | Source .env before Maven start |
| 401/403 on protected requests | Check JWT and user role |
| JUnit passes; PostgreSQL fails | H2 and PostgreSQL are different environments |

## 7. Extra Week 7 PostgreSQL concurrency check (R07)

Only after confirming the server is connected to **PostgreSQL 16**, run:

```bash
python3 scripts/week7_postgres_concurrency.py --base-url http://localhost:8080
```

The script creates a synthetic shop and two customers, launches two simultaneous HTTP POST bookings for the same slot, then checks both customers' active appointments. **Expected**: HTTP 201 and 409, and exactly one active booking. The result is **not yet observed** in this artifact; save your real PASS/FAIL output and update R07. This is a minimal two-thread check, not a high-load benchmark.
