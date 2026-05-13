# AGENTS.md


Actionable guidance for AI coding agents working in `clss-search-evaluator`.
Prefer small, repo-grounded changes over generic cleanups.

## Project structure hints

- Controllers: `br.com.caelum.gnarus.*.*Controller` (feature packages under `gnarus`)
- Services: same packages as their feature; suffix `*Service`
- Repositories: `*Repository`; extend `JpaRepository` or custom interfaces
- DTOs/records: co-located with feature or in `dto`/`Dto` subpackages
- Views: `src/main/webapp/WEB-INF/views/*.jsp`
- Custom tags: `src/main/webapp/WEB-INF/tags/`
- Migrations: `src/main/resources/db/migration/V*.sql`

## Boundaries

- **Always do:** Run tests before finishing (scoped or full as appropriate), follow existing patterns in the touched area, keep diffs small and focused.
- **Ask first:** Adding new production dependencies, database schema changes, changes to `gnarus-cli` or root-level config, broad refactors.
- **Never do:** Commit secrets or API keys; add `@Cacheable`/`@CachePut`/`@CacheEvict` (use Gnarus wrappers); introduce Lombok; expose entities to views, JSON, or cache. For non-controller code, do not assume `@Supports` gates automatically—check `FeatureResolver` explicitly.

## Current stack

- Java 21
- Spring Boot 3.5.13
- Batch-style Spring Boot app executed on startup
- Maven wrapper (`./mvnw`)
- `spring-boot-starter`
- `spring-boot-starter-web`
- `spring-boot-starter-validation`
- `spring-boot-configuration-processor`
- `com.openai:openai-java` 4.35.0
- `spring-boot-starter-test`
- `com.squareup.okhttp3:mockwebserver` 4.12.0
- JSON dataset input and JSON file output
- Configuration via `application.yml` plus environment variables such as `OPENAI_API_KEY`


## Working style for this repo

- Match the style of the touched area first. This repo has legacy and modern patterns side by side.
- For new or substantially changed code, prefer the guidance below even when nearby legacy code is older.
- Do not refactor unrelated legacy code only to enforce style.
- Keep packaging by feature and keep helpers close to the feature that owns them.
- Keep business logic out of JSPs.
- Do not introduce Lombok.

## Preferred Java and Spring patterns

- Controllers should stay thin: bind input, validate, call a focused use case or service, map output.
- Services should contain the use-case logic and stay intentionally small.
- Prefer constructor injection in new or touched classes.
- Legacy `@Autowired` field injection exists; do not rewrite it unless the task already requires touching that class deeply.
- Prefer DTOs or projections for JSON, views, and cache payloads. Do not expose entities directly.
- Prefer `Optional` and explicit return types over `null`-driven APIs.
- Prefer `java.time` for new code.
- Use `record` for simple DTO-like types when it fits the local style.
- Follow the logger API already used in the touched class or package. This repo uses both SLF4J and Log4j.
- Comments should explain why, not restate obvious code.

## Code style examples

- **Thin controller with @Valid + service call:** `ClassSummaryWithAIEvaluationController` — constructor injection, `@Valid` on request body, delegates to service, returns DTOs.
- **Service with transactional boundaries:** `ClassSessionService` — constructor injection, focused `@Transactional` methods, uses DTOs like `ClassSessionDTO`.
- **DTOs as records:** `ClassSessionDTO`, `SearchSuggestionRequest`, `SubCategoryInformationDTO` — use `record` for request/response and view models.
- **Repository tests:** `ClassSessionAttendanceRepositoryTest` — extends `DatabaseTest`, uses `@DataJpaTest` + `@DbTest`, naming `methodName__should_behavior`.
- **Avoid:** Legacy patterns (e.g. `@Autowired` field injection, entities in response) when adding new code; match the touched area when editing legacy.

## Validation and security

- Controllers commonly use `@Valid` with `BindingResult`.
- Reuse `@InitBinder` and dedicated validators when the area already follows that pattern.
- Reuse sanitization patterns already present in the repo for user-provided text, including `NoHtmlValidator`, `SafeHtmlValidator`, and AntiSamy-backed flows when applicable.
- Never commit secrets.
- Do not send entities directly to views, JSON responses, or cache.


## Testing guidance

- Use JUnit 5 + AssertJ by default.
- Never mock, spy, fake, or partially replace the behavior that the test is supposed to verify.
  - If the test verifies repository queries or persistence behavior, use the real repository and database.
  - If the test verifies service orchestration, instantiate/use the real service and mock only external collaborators.
- Prefer `DatabaseTest` for repository tests and database-heavy integration tests. It already provides a real `EntityManager`, fixtures, and common helpers.
- Do not create, drop, alter, or patch database schema inside tests.
  - Do not use `CREATE TABLE`, `DROP TABLE`, `ALTER TABLE`, or schema-fixing native SQL in test setup.
  - Tests should run against the schema produced by the normal test configuration/migrations, not a schema improvised in test setup.
- Avoid native SQL for test data setup.
- Direct `@DataJpaTest` also exists in the repo. Match the local package style when that area already uses it.
- Use builders and existing test helpers instead of creating ad-hoc fixture setup.
- Prefer `@Nested` and `@ParameterizedTest` when they make scenarios clearer.
- Follow the common naming style: `methodName__should_behavior`.
- Do not spend time on controller tests, trivial Spring Data JPA derived-query tests, or framework-behavior tests unless the task specifically needs them.

### Coverage guidance

- Do not aim to test every method, getter, setter, DTO mapping, trivial pass-through code, or Spring Data JPA generated/derived query when it only follows framework conventions or contains no additional logic.
  - Test getters, setters, DTO mappings, and derived values only when they contain meaningful logic, such as validation, normalization, conditional mapping, fallback/default values, formatting, filtering, computed fields, or business rules.
  - Do not write repository tests just to prove that Spring Data JPA generated queries work. Test repository methods only when they contain meaningful query logic, such as custom `@Query`, joins, projections, filters with business rules, sorting/pagination rules, `EntityGraph`, native queries, custom repository implementations, or database-specific behavior.

- Focus tests on behavior with meaningful logic:
  - `if` / `else`;
  - `switch`;
  - early returns;
  - validations;
  - permission/access checks;
  - feature flags;
  - limits and thresholds;
  - state transitions;
  - persistence changes;
  - side effects such as events, queues, files, notifications, emails, or external calls;
  - exception handling and rollback behavior.

- For every new or changed meaningful branch, cover both sides when observable:
  - one test where the condition is true;
  - one test where the condition is false;
  - for `switch`/enum logic, cover each meaningful case and the default/error case when it exists.

- Each test should assert observable behavior, persisted state, returned value, thrown exception, or side effect.
  - Avoid tests that only execute code without meaningful assertions.

- Prefer scenario coverage over class coverage.
  - A class with heavy business logic may need many tests.
  - A class with only wiring/delegation may need no direct tests.

## Before finishing

- Tests pass: `./mvnw test` (or scoped) is green.
- Diff is small and focused; no unrelated refactors.
- No Lombok, no direct Spring cache annotations, no entity exposure.
- If you created a table needed in local dev, update `../jobs/dumpAnonimizado/config.yaml` so the anonymized dump still includes it safely.
- Do not run the asset build; CI handles it.

## When unsure

- Ask a clarifying question or propose a short plan before large changes.
- Do not push broad refactors or speculative rewrites without confirmation.
- Prefer small, targeted edits over repo-wide cleanup.

## Quick reminders

- Prefer code that is easy to review over clever abstractions.
- Preserve established behavior unless the task explicitly changes it.
- When legacy code conflicts with the preferred pattern, improve the touched area without turning the task into a broad rewrite.
- Git: keep commits scoped to the task; avoid unrelated refactors in the same diff.
