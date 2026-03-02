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

| Version | Description                                      |
|---------|--------------------------------------------------|
| V1      | Initial schema (all four core tables)            |
| V2      | Unique constraint on `lottery.name`              |
| V3      | Unique constraint on `(lottery_id, criteria_type)` |

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

## Performance Notes

- Active lotteries with criteria are cached (`active-lotteries` cache); evicted on any lottery create/update.
- Eligibility evaluation uses a parallel stream for large lottery sets.
- Eligibility reads use read-only transactions to avoid dirty-checking overhead.
- `spring.jpa.open-in-view` is disabled.
