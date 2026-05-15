# AGENTS.md

**Whenever implementing a new class**, such as a service, controller, or interface, add a simple JavaDoc comment at the beginning of the file explaining its purpose and responsibility.

## Project structure hints

- Controllers: same packages as their feature; suffix `*Controller`
- Services: same packages as their feature; suffix `*Service`
- DTOs/records: co-located with feature, in `/dto` subpackages; suffix `*DTO`

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
- Configuration via `application.properties` plus environment variables such as `OPENAI_API_KEY`

## Testing guidance
- Never mock, spy, fake, or partially replace the behavior that the test is supposed to verify.
  - If the test verifies repository queries or persistence behavior, use the real repository and database.
  - If the test verifies service orchestration, instantiate/use the real service and mock only external collaborators.
- Use builders and existing test helpers instead of creating ad-hoc fixture setup.
- Prefer `@Nested` and `@ParameterizedTest` when they make scenarios clearer.
- Follow the common naming style: `methodName__should_behavior`.
- Do not spend time on controller tests, trivial Spring Data JPA derived-query tests, or framework-behavior tests unless the task specifically needs them.

### Coverage guidance
- Do not aim to test every method, getter, setter, DTO mapping, trivial pass-through code, or Spring Data JPA generated/derived query when it only follows framework conventions or contains no additional logic.

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

## Branch and Commit Pattern

- Branches must follow the format: `feat-xxx`
  - Example: `feat-social-login`
  - Use lowercase letters and hyphens to separate words.

- Commits must follow the format: `feat: description`
  - Example: `feat: add social login`
  - The description should be short, written in English, and clearly describe the main change.

- Before committing, validate that:
  - The branch starts with `feat-`
  - The commit starts with `feat:`
  - The commit description is concise and has no ending period