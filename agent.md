# PharmaSync rules
Full requirements: project-details.md. Offline-first pharmacy inventory + POS.
Two Spring Boot apps (head-office, branch-app) + shared domain module.

## Stack
- Java 21, Spring Boot 4.x, Maven multi-module (domain, branch-app, head-office)
- Branch: H2 + Spring Data JPA + Thymeleaf. Head office: PostgreSQL.
- Whole system starts with `docker compose up`. No paid services, no secrets in repo.
- springdoc OpenAPI, Spring Security, Bean Validation, RFC 9457 Problem Details, pagination
- Actuator, Micrometer, JSON logs
- JUnit 5, Testcontainers, ArchUnit, k6 scripts
- Use the Maven wrapper (mvnw) so Maven does not need to be installed.

## Core rules
- Branch works fully offline. Changes saved as OPERATIONS (append-only log + outbox), never overwrite rows.
- Sync is idempotent (operation UUID), resumable, ordered by a logical (Lamport) clock.
- Sell first-expiry-first-out. Never sell expired or recalled batches.
- Prescription-only sale needs prescriber reg no, patient name, prescription date.
- Patterns allowed: Command, Strategy, State, Composite, Observer, Adapter. Each must serve a requirement.
- Keep code MINIMAL and WORKING. Small commits.

## Every session
1. Read memory.md, Plan.md, errors.md.
2. Do only the requested tasks. Build and test until green.
3. Tick Plan.md, update memory.md, git commit.
4. Log any mistake you made in errors.md.
