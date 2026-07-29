# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Study cafe seat reservation service. Server-rendered (SSR) with Thymeleaf, session-based authentication via Spring Security. Stack: Spring Boot 4.1.0, Java 21, Gradle, MySQL (JPA/Hibernate).

## Commands

- `./gradlew compileJava` — fast compile check, use this after every code change
- `./gradlew build` — full build (compile + test)
- `./gradlew test` — run all tests
- `./gradlew test --tests "com.example.studyreservation.SomeTest"` — run a single test class
- `./gradlew test --tests "com.example.studyreservation.SomeTest.someMethod"` — run a single test method
- `./gradlew bootRun` — run the app locally

Note: DB credentials live in `src/main/resources/application-local.yaml` (git-ignored) — copy `application-local.yaml.example` to `application-local.yaml` and fill in real values before running `bootRun` on a new machine. The committed `application.yaml` activates `local,dev` by default: `local` supplies the datasource credentials above, `dev` supplies dev-mode JPA behavior (`application-dev.yaml`: `ddl-auto: update`, `show-sql: true`). A parallel `application-prod.yaml` (`ddl-auto: validate`, `show-sql: false`) exists as a structural exercise in separating dev/prod config — swap `SPRING_PROFILES_ACTIVE` to use it; it has no real deployment target yet.

## Architecture

### Package-by-feature layout

Code is organized by domain feature under `com.example.studyreservation`, not by technical layer: `room`, `seat`, `user`, `reservation`, `security`, `config`, `common`. Each feature package holds its own entity, repository, and service (e.g. `seat/Seat.java`, `seat/SeatRepository.java`, `seat/SeatService.java`). Controllers/DTOs/Thymeleaf views have not been added yet — only the domain layer (entity → repository → service) and cross-cutting concerns (security, exception handling) exist so far. When adding those, follow the same per-feature package convention.

### Domain model

- `Room` — has `capacity`; `Seat` belongs to a `Room` (`@ManyToOne`), unique on `(room_id, seat_number)`.
- `User` — `email` (unique, login id), `password` (BCrypt-encoded), `role` (`Role.USER`/`Role.ADMIN`).
- `Reservation` — belongs to a `User` and a `Seat`. Time is modeled as **fixed 1-hour slots**: each row stores one `(seat, reservationDate, startTime)` slot rather than a start/end range. Unique constraint on `(seat_id, reservation_date, start_time)` is the DB-level, concurrency-safe defense against double-booking (checked first in `ReservationService` via `existsBy...`, but the constraint is what actually protects against race conditions between concurrent requests).
- Multi-hour bookings (e.g. 09:00–11:00) are stored as **multiple `Reservation` rows sharing one `reservationGroupId` (UUID)** — one row per hour slot. `ReservationService.reserve()` splits a start/end range into slots and validates every slot is free before inserting; `cancelReservation()` looks up and deletes all rows for a `reservationGroupId` together, after verifying the caller owns them (`Reservation.isOwnedBy`).
- Cancellation is a hard delete (no soft-delete/status field) — a cancelled slot immediately becomes bookable again, which is why the unique constraint approach works cleanly.
- All entities extend `common.BaseTimeEntity` (`createdAt`/`updatedAt` via JPA auditing — `@EnableJpaAuditing` is on `StudyReservationApplication`). Entities use `@NoArgsConstructor(access = PROTECTED)` + Lombok `@Builder` on an all-args constructor; avoid adding public no-arg constructors or setters.

### Authentication & authorization

Session-based auth (not JWT/stateless) via Spring Security, matching the SSR/Thymeleaf model. `security/CustomUserDetails` wraps `User` and maps `role` to a `ROLE_*` authority; `security/CustomUserDetailsService` loads users by email. `config/SecurityConfig` defines the filter chain: `/admin/**` requires `ROLE_ADMIN`, `/`, `/signup`, `/login` are public, everything else requires authentication, form login at `/login`.

Known gap: URL-level authorization failures (e.g. non-admin hitting `/admin/**`) are intercepted by Spring Security's `ExceptionTranslationFilter` before reaching `GlobalExceptionHandler` — a custom `AccessDeniedHandler` still needs to be wired into `SecurityConfig` to render the same error page for those.

### Error handling

`common/exception/GlobalExceptionHandler` (`@ControllerAdvice`) maps domain exceptions to HTTP status codes and renders `templates/error.html` with the correct status set via `ModelAndView(viewName, HttpStatus)` (HTML error pages were chosen over JSON error responses, matching the SSR approach). Convention: one exception class per failure case in `common/exception/`, mapped explicitly by type in the handler — 404 for not-found, 409 for conflicts (duplicate email/seat number, double-booked slot, or a `DataIntegrityViolationException` racing the unique constraint), 403 for ownership violations, 400 for invalid input (e.g. reservation times not aligned to the hour). New domain exceptions should be added to this handler rather than left to fall through to the generic 500 handler.
