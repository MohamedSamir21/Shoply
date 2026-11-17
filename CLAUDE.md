# Shoply

Android e-commerce app. Kotlin, Jetpack Compose, MVI, Hilt, Firebase Auth + Firestore, Coil 3.

## Current feature

Current feature: edit-profile — spec at `specs/edit-profile/spec.md`.

## Architecture

- Each feature lives in `com.shopping.shoply.<feature>` and splits into `data`, `domain`, `presentation` (plus `di` when it provides bindings).
- Domain does not depend on data, presentation, or the Android framework.
- Data does not depend on presentation. Presentation does not depend on data directly.
- These layer rules are enforced by `CleanArchitectureKonsistTest`; keep it green.
- Dependency injection uses Hilt. Do not introduce Koin.
- `FirebaseAuth` and `FirebaseFirestore` are provided once, in `common/di/FirebaseModule.kt`.

## Presentation (MVI)

- Each screen has a `State` data class, an `Intent` sealed interface for user actions, and an `Event` sealed interface for one-off effects (snackbars, navigation).
- The ViewModel exposes `state` as a `StateFlow`, `events` as a `Flow`, and receives input only through `onIntent(intent)`.
- User-visible text lives in `res/values/strings.xml`. Errors reach the UI as `UiText`.
- Every composable has light and dark previews of each of its states.

## Data

- Repository functions that can fail return `Result<T, DataError>` and never throw to callers.
- Firestore Boolean fields must not start with `is` (Firestore strips the prefix and stores two diverging fields).
- Never log passwords, tokens, email addresses, phone numbers, or photo data.

## Testing

- Write unit tests for core logic only: validation, mapping, ViewModel state transitions, and repository write rules. Exhaustive coverage is not a goal.

## Repository

- Build with JDK 21 (Gradle 8.7 fails on JDK 25).
- One branch per feature. Run `/verify --base=<base branch>` before opening a pull request.
- `docs/` is local-only and is never committed.
- Commits and pull requests carry no AI attribution.
