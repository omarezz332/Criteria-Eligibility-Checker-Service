# Criteria Eligibility Checker Service

A Spring Boot REST API for managing lottery eligibility. Applicants are profiled, scored, and checked against configurable per-lottery criteria. Eligible lotteries can then be ranked and submitted as an ordered preference list.

---

## Architecture

Hexagonal (Ports & Adapters) architecture with three layers:

```
presentation/   →  controllers, mappers, DTOs
application/    →  use cases, services, port interfaces
domain/         →  models, enums, domain exceptions, EligibilityEngine
infrastructure/ →  JPA entities, repository adapters, cache, async config
```

Domain models are plain Java records with no JPA annotations. Adapters translate between domain models and JPA entities.

---

## Tech Stack

| Concern        | Technology                          |
|----------------|-------------------------------------|
| Framework      | Spring Boot 4.0.3                   |
| Language       | Java 17                             |
| Database       | PostgreSQL                          |
| Migrations     | Flyway                              |
| Persistence    | Spring Data JPA / Hibernate         |
| Caching        | Spring Cache (ConcurrentMapCache)   |
| Build          | Maven                               |

---

## Domain Concepts

### ApplicantProfile

An applicant is registered with demographic and professional attributes. A **rank mark** is automatically calculated:

```
rankMark = (educationWeight × 40%) + (careerWeight × 60%)
```

| Education Level | Weight | Career Years | Weight |
|-----------------|--------|--------------|--------|
| NONE            | 0      | 0            | 0      |
| DIPLOMA         | 30     | 1–3          | 30     |
| BSC             | 50     | 4–6          | 50     |
| MSC             | 80     | 7–9          | 70     |
| PHD             | 100    | > 9          | 100    |

### Lottery & Criteria

Lotteries are created by admins and can have one or more eligibility criteria attached. A lottery must pass **all** of its criteria to be considered eligible for an applicant.

| CriteriaType   | Description                              |
|----------------|------------------------------------------|
| AGE_MIN        | Applicant age ≥ value                   |
| AGE_MAX        | Applicant age ≤ value                   |
| GENDER         | Exact enum match (MALE / FEMALE)         |
| COUNTRY        | Case-insensitive string match            |
| NATIONALITY    | Case-insensitive string match            |
| MARITAL_STATUS | Enum match (SINGLE / MARRIED / DIVORCED / WIDOWED) |
| HAS_DISABILITY | Boolean match (true / false)             |
| MIN_RANK_MARK  | Applicant rankMark ≥ value              |

### ApplicationPreference

After checking eligibility, an applicant submits an ordered list of preferred lotteries. Re-submission replaces the previous list atomically. Optimistic locking prevents concurrent conflicts.

---

## API Reference

### Applicants — `POST /api/applicants`

Register a new applicant.

```json
{
  "name": "Jane Doe",
  "age": 30,
  "gender": "FEMALE",
  "country": "Egypt",
  "nationality": "Egyptian",
  "maritalStatus": "SINGLE",
  "hasDisability": false,
  "educationLevel": "BSC",
  "careerYears": 5
}
```

Response `201 Created` — includes calculated `rankMark`.

---

### Eligibility — `POST /api/applications/{applicantId}/eligibility`

Returns all **active** lotteries the applicant qualifies for (evaluated in parallel across all criteria).

Response `200 OK`:
```json
{
  "rankMark": 50.0,
  "eligibleLotteries": [
    { "lotteryId": "...", "lotteryName": "Housing Lottery" }
  ]
}
```

---

### Preferences — `/api/applications/{applicantId}/preferences`

| Method | Description                           |
|--------|---------------------------------------|
| POST   | Submit (or replace) preference list   |
| GET    | Retrieve current preference list      |

Submit body:
```json
{
  "preferences": [
    { "lotteryId": "...", "preferenceOrderNum": 1 },
    { "lotteryId": "...", "preferenceOrderNum": 2 }
  ]
}
```

Eligibility is re-validated server-side. Duplicate lottery IDs or order numbers are rejected (`409`). Submitting a non-eligible lottery returns `422`.

---

### Admin Lotteries — `/api/admin/lotteries`

| Method | Path                                  | Description                       |
|--------|---------------------------------------|-----------------------------------|
| POST   | `/api/admin/lotteries`                | Create a lottery (starts ACTIVE)  |
| PUT    | `/api/admin/lotteries/{id}/criteria`  | Replace full criteria set         |
| PATCH  | `/api/admin/lotteries/{id}/status`    | Set status (ACTIVE / NOT_ACTIVE)  |

Create body:
```json
{ "name": "Housing Lottery" }
```

Add criteria body:
```json
{
  "criteria": [
    { "criteriaType": "AGE_MIN", "criteriaValue": "18" },
    { "criteriaType": "GENDER", "criteriaValue": "FEMALE" }
  ]
}
```

Update status body:
```json
{ "status": "NOT_ACTIVE" }
```

---

## Error Responses

All errors follow a consistent envelope:

```json
{
  "errorCode": "APPLICANT_NOT_FOUND",
  "message": "Applicant with id ... not found"
}
```

| HTTP Status | Scenario                                      |
|-------------|-----------------------------------------------|
| 400         | Validation failure, invalid enum / type       |
| 404         | Applicant or lottery not found                |
| 409         | Duplicate preferences or concurrent conflict  |
| 422         | Submitted lottery not eligible for applicant  |
| 500         | Unexpected server error                       |

---

## Database Migrations

Managed by Flyway under `src/main/resources/db/migration/`:

| Version | Description                                                   |
|---------|---------------------------------------------------------------|
| V1      | Initial schema (all four core tables)                         |
| V2      | Unique constraint on `lottery.name`                           |
| V3      | Unique constraint on `(lottery_id, criteria_type)`            |
| V4      | Drop `version` column from `application_preference`           |
| V5      | Add `idx_lottery_status` index on `lottery(status)`           |

---

## Running Locally

**Prerequisites:** Java 17, PostgreSQL, Maven.

1. Create a PostgreSQL database (default name: `taskOn`).

2. Update credentials in `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/taskOn
   spring.datasource.username=<your_user>
   spring.datasource.password=<your_password>
   ```

3. Build and run:
   ```bash
   mvn spring-boot:run
   ```

Flyway will apply all migrations automatically on startup.

---

## Performance & Optimization

### Database Indexes

| Index | Table | Column(s) | Justification |
|-------|-------|-----------|---------------|
| `idx_lottery_status` | `lottery` | `status` | Added (V5) — every eligibility check filters `WHERE status = 'ACTIVE'`; no prior index existed |
| *(implicit)* | `lottery_criteria` | `lottery_id` | **Not added** — V3 `UNIQUE(lottery_id, criteria_type)` creates a composite B-tree index; `lottery_id` as its leftmost column already covers JOIN and WHERE on `lottery_id` alone |
| *(implicit)* | `application_preference` | `applicant_id` | **Not added** — `UNIQUE(applicant_id, lottery_id)` and `UNIQUE(applicant_id, preference_order_num)` both cover `WHERE applicant_id = ?` via the leftmost prefix rule |
| *(implicit)* | `application_preference` | `(applicant_id, preference_order_num ASC)` | **Not added** — `UNIQUE(applicant_id, preference_order_num)` is byte-for-byte equivalent (B-tree defaults to ASC); covers `findByApplicantIdOrderByPreferenceOrderNumAsc` with no additional sort step |

### Transaction & Concurrency Strategy

| Service | Method | Setting | Reason |
|---------|--------|---------|--------|
| `EligibilityService` | all (class-level) | `@Transactional(readOnly = true)` | Disables Hibernate dirty checking and snapshot tracking; allows connection pool to route to a read replica; critical on the hottest path |
| `PreferenceService` | `getPreferences` | `@Transactional(readOnly = true)` | Read-only session; no state to flush |
| `PreferenceService` | `submitPreferences` | `@Transactional(isolation = READ_COMMITTED)` | Prevents dirty reads during the delete+insert replace-all; allows concurrent readers; correct for this pattern (no phantom-read concern) |
| `LotteryService` | `addCriteria` | `@Transactional(isolation = READ_COMMITTED)` | Prevents seeing uncommitted criteria from a concurrent admin write during delete+insert |
| `LotteryService` | `updateStatus` | `@Transactional(isolation = READ_COMMITTED)` | Prevents dirty reads of in-flight status changes from concurrent admin operations |
| `LotteryService` | `create` | `@Transactional` | Default REQUIRED; PostgreSQL default isolation (READ_COMMITTED) is sufficient for a single INSERT |
| `ApplicantService` | `register` | `@Transactional` | Default REQUIRED; single INSERT path |

**Atomicity guarantees:**
- Preference resubmission: `DELETE` + `INSERT` are wrapped in a single `READ_COMMITTED` transaction — no partial state is ever visible to concurrent readers.
- Criteria replace-all: same delete+insert pattern in `LotteryService.addCriteria()`.
- Cache eviction (`@CacheEvict(allEntries = true)`) fires inside the same transaction commit, so the cache is never invalidated before the write is durable.

### Caching

- Active lotteries and their criteria are cached under `active-lotteries` (Spring `ConcurrentMapCache`).
- Cache is evicted on every lottery create, status update, or criteria replace — keeping reads stale-free in steady state.
- Eligibility checks hit the cache after the first load; only admin operations cause a miss.

### Query Design

- `findAllActiveWithCriteria` uses `LEFT JOIN FETCH` — loads lotteries and criteria in a single query, preventing N+1 reads.
- Criteria delete and preference delete both use `@Modifying` bulk-delete JPQL — one `DELETE` statement instead of N individual deletes.
- `spring.jpa.open-in-view` is disabled — no unintended lazy loads outside the service transaction boundary.
