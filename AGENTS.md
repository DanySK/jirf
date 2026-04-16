# JIRF

## Purpose
- JIRF is a small Java library that reflectively instantiates objects and applies implicit conversion chains to satisfy constructor arguments.
- The public entry points are `Factory` and `FactoryBuilder`; the main implementation is in `src/main/java/org/danilopianini/jirf/FactoryImpl.java`.

## Working Rules
- Before changing code, read `README.md`, `build.gradle.kts`, `settings.gradle.kts`, and the relevant source or test files.
- Confirm changes with `./gradlew build`. Treat this as the default validation command for the repository.
- Use Conventional Commits for any commit message. The local `commit-msg` hook enforces `type(scope): subject` with the standard types listed in `.git/hooks/commit-msg`.

## Codebase Notes
- This is a Gradle `java-library` project.
- Compilation targets Java 11 via the multi-JVM plugin.
- Tests use JUnit 5 and currently live in `src/test/java/org/danilopianini/jirf/test/TestFactory.java`.
- `FactoryBuilder` wires built-in conversions such as boxing, primitive widening or narrowing, array conversions, and `Object -> String`.
- `FactoryImpl` stores singleton bindings, models implicit conversions as a directed graph, uses shortest paths to resolve conversion chains, and benchmarks constructors before instantiation.

## Change Guidance
- Preserve the existing public API style: small interfaces, fluent builder methods, and immutable-style result objects.
- When editing constructor selection or conversion logic, check singleton injection, varargs handling, and conversion-path selection because those behaviors are heavily coupled.
- Add or update focused tests when behavior changes, especially around nulls, overload resolution, arrays, and implicit conversion chains.

## Practical Defaults
- Prefer minimal, targeted edits over broad refactors.
- Keep source compatible with the repository's Java baseline.
- Do not assume a change is correct until `./gradlew build` passes.
