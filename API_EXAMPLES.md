# API Examples — Criteria Eligibility Checker Service

Full end-to-end flow using curl. Run steps in order.

---

## Step 1 — Create a Lottery (Admin)

```bash
curl -X POST http://localhost:8080/api/admin/lotteries \
  -H "Content-Type: application/json" \
  -d '{"name": "Housing Lottery 2024"}'
```

**Response `201 Created`:**
```json
{
  "id": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "name": "Housing Lottery 2024",
  "status": "ACTIVE",
  "createdDate": "2024-01-01T10:00:00"
}
```

---

## Step 2 — Add Criteria to the Lottery (Admin)

Replace `{lotteryId}` with the ID from Step 1.

```bash
curl -X POST http://localhost:8080/api/admin/lotteries/{lotteryId}/criteria \
  -H "Content-Type: application/json" \
  -d '{
    "criteria": [
      { "criteriaType": "AGE_MIN",        "criteriaValue": "18" },
      { "criteriaType": "AGE_MAX",        "criteriaValue": "45" },
      { "criteriaType": "GENDER",         "criteriaValue": "MALE" },
      { "criteriaType": "COUNTRY",        "criteriaValue": "Egypt" },
      { "criteriaType": "NATIONALITY",    "criteriaValue": "Egyptian" },
      { "criteriaType": "MARITAL_STATUS", "criteriaValue": "SINGLE" },
      { "criteriaType": "HAS_DISABILITY", "criteriaValue": "false" },
      { "criteriaType": "MIN_RANK_MARK",  "criteriaValue": "30.0" }
    ]
  }'
```

**Response `201 Created`:**
```json
[
  { "id": "...", "criteriaType": "AGE_MIN",        "criteriaValue": "18" },
  { "id": "...", "criteriaType": "AGE_MAX",        "criteriaValue": "45" },
  { "id": "...", "criteriaType": "GENDER",         "criteriaValue": "MALE" },
  { "id": "...", "criteriaType": "COUNTRY",        "criteriaValue": "Egypt" },
  { "id": "...", "criteriaType": "NATIONALITY",    "criteriaValue": "Egyptian" },
  { "id": "...", "criteriaType": "MARITAL_STATUS", "criteriaValue": "SINGLE" },
  { "id": "...", "criteriaType": "HAS_DISABILITY", "criteriaValue": "false" },
  { "id": "...", "criteriaType": "MIN_RANK_MARK",  "criteriaValue": "30.0" }
]
```

---

## Step 3 — Update Lottery Status (Admin)

```bash
curl -X PATCH http://localhost:8080/api/admin/lotteries/{lotteryId}/status \
  -H "Content-Type: application/json" \
  -d '{"status": "ACTIVE"}'
```

**Response `200 OK`:**
```json
{
  "id": "...",
  "name": "Housing Lottery 2024",
  "status": "ACTIVE",
  "createdDate": "2024-01-01T10:00:00"
}
```

---

## Step 4 — Register an Applicant

```bash
curl -X POST http://localhost:8080/api/applicants \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Omar Ahmed",
    "age": 28,
    "gender": "MALE",
    "country": "Egypt",
    "nationality": "Egyptian",
    "maritalStatus": "SINGLE",
    "hasDisability": false,
    "disabilityName": null,
    "educationLevel": "BSC",
    "careerYears": 3
  }'
```

> Rank Mark = (BSC=50 × 40%) + (3yrs=30 × 60%) = **38.0**

**Response `201 Created`:**
```json
{
  "id": "yyyyyyyy-yyyy-yyyy-yyyy-yyyyyyyyyyyy",
  "name": "Omar Ahmed",
  "age": 28,
  "gender": "MALE",
  "country": "Egypt",
  "nationality": "Egyptian",
  "maritalStatus": "SINGLE",
  "hasDisability": false,
  "disabilityName": null,
  "educationLevel": "BSC",
  "careerYears": 3,
  "rankMark": 38.0,
  "createdDate": "2024-01-01T10:05:00"
}
```

---

## Step 5 — Check Eligibility

Replace `{applicantId}` with the ID from Step 4.

```bash
curl -X POST http://localhost:8080/api/applications/{applicantId}/eligibility
```

**Response `200 OK`:**
```json
{
  "applicantId": "yyyyyyyy-yyyy-yyyy-yyyy-yyyyyyyyyyyy",
  "rankMark": 38.0,
  "eligibleLotteries": [
    {
      "id": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
      "name": "Housing Lottery 2024",
      "status": "ACTIVE",
      "createdDate": "2024-01-01T10:00:00"
    }
  ],
  "message": "You are eligible for 1 lottery(s)."
}
```

---

## Step 6 — Submit Preference List

```bash
curl -X POST http://localhost:8080/api/applications/{applicantId}/preferences \
  -H "Content-Type: application/json" \
  -d '{
    "preferences": [
      { "lotteryId": "{lotteryId}", "preferenceOrderNum": 1 }
    ]
  }'
```

**Response `201 Created`:**
```json
{
  "applicantId": "yyyyyyyy-yyyy-yyyy-yyyy-yyyyyyyyyyyy",
  "preferences": [
    {
      "id": "...",
      "lotteryId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
      "lotteryRankMark": 38.0,
      "preferenceOrderNum": 1,
      "createdDate": "2024-01-01T10:10:00"
    }
  ],
  "message": "Preferences submitted successfully."
}
```

---

## Step 7 — Get Preferences

```bash
curl -X GET http://localhost:8080/api/applications/{applicantId}/preferences
```

---

## Error Cases

### 400 — Invalid lottery status
```bash
curl -X PATCH http://localhost:8080/api/admin/lotteries/{lotteryId}/status \
  -H "Content-Type: application/json" \
  -d '{"status": "WRONG"}'
```

### 404 — Applicant not found
```bash
curl -X POST http://localhost:8080/api/applications/00000000-0000-0000-0000-000000000000/eligibility
```

### 409 — Concurrent preference update conflict
```bash
# Submit preferences from two clients simultaneously for the same applicantId
# → 409 Conflict: retry the request
```

### 422 — Ineligible lottery submitted in preferences
```bash
curl -X POST http://localhost:8080/api/applications/{applicantId}/preferences \
  -H "Content-Type: application/json" \
  -d '{
    "preferences": [
      { "lotteryId": "{lotteryIdApplicantDoesNotQualifyFor}", "preferenceOrderNum": 1 }
    ]
  }'
```

---

## Supported Enum Values

| Field            | Accepted Values                              |
|------------------|----------------------------------------------|
| `gender`         | `MALE`, `FEMALE`                             |
| `maritalStatus`  | `SINGLE`, `MARRIED`, `DIVORCED`, `WIDOWED`   |
| `educationLevel` | `NONE`, `DIPLOMA`, `BSC`, `MSC`, `PHD`       |
| `status`         | `ACTIVE`, `NOT_ACTIVE`                       |
| `criteriaType`   | `AGE_MIN`, `AGE_MAX`, `GENDER`, `COUNTRY`, `NATIONALITY`, `MARITAL_STATUS`, `HAS_DISABILITY`, `MIN_RANK_MARK` |
