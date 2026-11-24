# Week 6 - Development Stage 4: observed defect and correction

**Initial Mac run (25 Nov 2026):** 13 JUnit tests, 0 failures, 7 errors; BUILD FAILURE. `BookingTimeRulesTest`: 5 pass; `ConcurrentBookingTest`: 1 pass; `KufiApiIntegrationTest`: 7 setup errors. In the uploaded console log the root cause was `NullPointerException` at `ShopController.addService(ShopController.java:40)`: direct access `shop.owner.email` returned null. The shared `@BeforeEach` setup failed before seven test bodies could execute. Java-only S01-S06 passed separately.

**Diagnosis:** ORM-managed nested relations were read through public fields instead of entity accessors. The patch introduces Java getters and uses them to traverse associations, avoiding the observed invalid direct access.

**Exact patch:** `docs/week-06/week6-hibernate-lazy-fix.patch`:

- `UserAccount.java`: `getId()`, `getEmail()`.
- `BarberShop.java`: `getId()`, `getName()`, `getOwner()`.
- `ServiceOffering.java`: `getId()`, `getName()`, `getShop()`.
- `ShopController.java`: `shop.owner.email` -> `shop.getOwner().getEmail()` and safe response getters.
- `AppointmentService.java`: replace direct association fields in ownership check, cancellation, and response mapping.

**Before:** `if (!shop.owner.email.equalsIgnoreCase(auth.getName())) ...`

**After:** `if (!shop.getOwner().getEmail().equalsIgnoreCase(auth.getName())) ...`

**Retest:** Student reports applying this patch and observing **13/13 JUnit PASS**. We do not have their final console log, Surefire files, screenshot or GitHub Actions URL in this package. Record this as **student-confirmed**, not an independently inspected execution artifact. Save real proof with `mvn clean test` and `bash scripts/collect_week6_evidence.sh`.

**Separate engineering improvement (do not conflate with NPE):** `BarberShopRepository.findByIdForUpdate()` uses `@Lock(PESSIMISTIC_WRITE)` to protect shop-level booking. The H2 concurrency test was among the six initial passing tests. PostgreSQL confirmation remains outstanding.

**GitHub:** Record actual SHA/PR links after pushing; none invented here.
