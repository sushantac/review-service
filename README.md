# review-service

Reviews & Ratings microservice for the e-commerce platform, written in **Kotlin** with **Spring Boot 3.4** (Java 21, Maven).

## Business purpose

Customer reviews drive social proof on product pages. This service lets authenticated
customers submit 1–5 star reviews, automatically marks reviews **Verified Purchase**
by consuming the `order.placed` Kafka event, aggregates per-product rating summaries,
and gives admins a moderation queue (approve / reject). Approvals publish a
`review.created` event for downstream consumers.

## API (via API Gateway :8080, service port :8086)

| Method | Path | Auth |
|--------|------|------|
| GET | `/api/v1/products/{productId}/reviews` | public (approved only) |
| GET | `/api/v1/products/{productId}/reviews/summary` | public |
| POST | `/api/v1/reviews` | JWT (one per user × product) |
| GET | `/api/v1/reviews/my` | JWT |
| GET | `/api/v1/reviews/{id}` | public |
| PUT / DELETE | `/api/v1/reviews/{id}` | JWT (owner) |
| GET | `/api/v1/admin/reviews?status=` | JWT + ADMIN |
| POST | `/api/v1/admin/reviews/{id}/approve` | JWT + ADMIN |
| POST | `/api/v1/admin/reviews/{id}/reject` | JWT + ADMIN |

Full contract: [`review-service.yaml`](../sdlc/docs/07-api-specs/review-service.yaml) and
[`13-service-communication.md`](../sdlc/docs/13-service-communication.md).

## Kafka

- Consumes `order.placed` (group `review-service`) → `verified_purchases` table (idempotent via `eventId`)
- Produces `review.created` on approval

## Database

PostgreSQL schema `review` (Liquibase-managed, `ddl-auto: validate`):
`reviews` (UNIQUE product_id + user_id, rating CHECK 1–5, status PENDING/APPROVED/REJECTED),
`verified_purchases`, `idempotency_keys`.

## Build & test

Requires Java 21.

```bash
./mvnw clean package     # build
./mvnw test              # unit tests (14)
./mvnw spotless:check    # Kotlin lint (ktlint)
```

## Run locally

```bash
# from the sdlc orchestrator repo
docker compose --profile backend up --build review-service
```

Health: `http://localhost:8086/actuator/health`
