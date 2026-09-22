# AGENTS.md — Bismarck

Guidance for coding agents working in this repo. Read this before trusting the README or grep results.

## What this is

Bismarck is a caching/syncing library for Kotlin Multiplatform. A `Bismarck<T>` is a single-value cache cell combining a `Fetcher<T>` (suspend producer), a `Storage<T>` (persistence), and a `Freshness` policy (TTL/invalidation). It exposes `values`/`states`/`errors` as coroutine `StateFlow`s and dedupes concurrent fetches. Published to Maven Central as `net.sarazan:bismarck` and `net.sarazan:bismarck-serializer-kotlinx`, currently version 0.6.2 (pre-1.0; API may still break).

Targets: JVM, JS (Node only, IR), iOS (arm64, x64, simulatorArm64). No Android target, no macOS/watchOS/tvOS.

## Ground truth vs. stale docs

**The README is stale.** Its code sample documents an API removed in March 2024 (`millis =`, `eachValue {}`, `eachState {}`, `cache.value`), a dead Bintray repo, and version 0.1.0. For the real API, read:

- `bismarck/src/commonMain/kotlin/net/sarazan/bismarck/Bismarck.kt` — the entire public interface (~50 lines: `insert`, `invalidate`, `check`, `clear` + three `StateFlow`s + a public `coroutineScope` + `Config`)
- `bismarck/src/commonMain/kotlin/net/sarazan/bismarck/extensions/ConfigExtensions.kt` — the `fileStorage {}` / `freshness {}` DSL (note: `duration: Duration`, not `millis`)
- `bismarck/src/commonMain/kotlin/net/sarazan/bismarck/DefaultBismarck.kt` — the only implementation

## Module map — and the `bismarck-classic` trap

Only two modules are in the build (`settings.gradle.kts`):

- `:bismarck` — core KMP library
- `:bismarck-kotlinx-json` — kotlinx.serialization JSON binding

**`bismarck-classic/` is NOT in the build.** It is the 2019–2021 RxJava/Android predecessor, checked in deliberately as reference material. It defines a second, conflicting `net.sarazan.bismarck.Bismarck`, `Persister`, `Serializer`, and `Fetcher`. Grep hits for core type names are ~50% noise from this module. Never edit it, never cite it as the current API, and exclude it from searches (`--glob '!bismarck-classic/**'`).

## Build & verify

```
./gradlew jvmTest          # THE verification gate — the only tests that actually assert
./gradlew build            # full multiplatform build (slower; needs Xcode for iOS targets)
./gradlew koverHtmlReport  # coverage
./gradlew publishToMavenLocal
```

**Only `jvmTest` is a meaningful gate — and it is currently RED on `main`.** As of 2026-08-09, 2 of 8 CommonTests fail (`testDedupe`, `testStateChannel`, both `expected:<Stale> but was:<Fetching>`), caused by landmine 2 below (observing a flow fires a hidden `check()` that races the assertions — tracked in issue #33). If you see those two failures, they are pre-existing, not your regression; any *other* failure is yours.

 The JS and iOS test harnesses are broken by construction: their `runBlockingTest` actuals (`bismarck/src/jsTest/.../test/Platform.kt`, `bismarck/src/iosTest/.../test/Platform.kt`) do `scope.launch { testBody() }` and return without awaiting, so `jsNodeTest` / `iosSimulatorArm64Test` pass vacuously. Only the JVM actual uses `runBlocking`. Don't interpret green JS/iOS test runs as evidence of anything until those actuals are fixed.

Tests in `commonTest/CommonTests.kt` are wall-clock-timing-based (`delay(50)` etc.) and inherently flaky. Prefer adding tests with `kotlinx-coroutines-test` virtual time; do not add more `delay()`-calibrated assertions.

There is **no CI**. Nothing verifies work after merge unless you run it yourself.

## Architecture contracts that matter

- `Storage<T>` (`storage/Storage.kt`) is a two-method, whole-value, non-suspending contract: `fun get(): T?` / `fun put(data: T?)`. It has exactly two implementations (`MemoryStorage`, `FileStorage`) and one call site (`DefaultBismarck` — property init and `insert`). Any streaming/cursor/database backend requires changing this contract (see "streaming backends" below).
- `Serializer<T>` (`serialization/Serializer.kt`) is `ByteArray`-in/out, not stream-based.
- Platform file IO goes through `expect class File` (`platform/File.kt`) — whole-file `readBytes`/`writeBytes` only.

### Known landmines (verified against source, 2026-08)

1. **`DefaultBismarck.coroutineScope` is a `get()` that constructs a fresh `CoroutineScope` on every access** (`DefaultBismarck.kt:26-27`). Every internal `launch` runs in its own unstructured, never-cancelled scope; there is no cancellation path and the `SupervisorJob` is inert.
2. **Reading `values`/`states`/`errors` fires a `check()` side effect** (`DefaultBismarck.kt:29-44`) — merely observing the cache can trigger a network fetch. This is what breaks the two currently-red tests.
3. **iOS `ByteArray.toNSData()` corrupts binary data** (`iosMain/.../platform/NSDataConversions.kt`): it round-trips through `decodeToString()` + UTF-8 encoding. Any non-UTF-8 payload (protobuf, images, JVM serialization) is silently mangled on iOS. JS `File.writeBytes` has the same UTF-8-only assumption. Only the JVM path is binary-safe.
4. JS `File` resolves relative paths against `__dirname` and uses `js("require(\"fs\")")` — bundler-dependent. (JS `readBytes` is also UTF-8-lossy, same as landmine 3.)
5. `gradle.properties` has a vestigial `xcodeproj=iosApp/iosApp.xcodeproj`; no such directory exists. No sample apps exist for any platform, and no `binaries.framework {}`/CocoaPods/SPM export is configured — there is currently no way for Swift code to consume the iOS artifacts.
6. `fetchJob`/`freshnessJob` in `DefaultBismarck` are unsynchronized `var`s mutated from `Dispatchers.IO` threads (the default dispatcher on JVM and iOS) — the read-then-write dedupe gate in `fetch()` can race and fire duplicate fetches.
7. Publishing is currently broken: `SONATYPE_HOST=DEFAULT` targets Sonatype's OSSRH endpoint (sunset mid-2025) and the vanniktech plugin 0.28.0 predates Central Portal support. Do not attempt to publish until issue #38's migration lands.

### Streaming/cursor backends (design constraint, not a bug)

The whole design models a cache cell as one atomic value: `Storage` is whole-value, `Serializer` is whole-blob, and `values: StateFlow<T?>` emits one `T` at a time. Backing a Bismarck with a database cursor or paged query is not possible without breaking changes at all three layers (make `Storage` suspend + `Flow`-returning, stream-based serialization, and a multi-row observation shape). The blast radius is small (one call site) but it is an intentional v-next design conversation, not a patch.

## Conventions

- Kotlin official style; `.editorconfig` permits wildcard imports. No ktlint/detekt/spotless is wired in — match surrounding style by hand.
- Versioning: bump `VERSION_NAME` in the module's `gradle.properties`. Releases are not consistently git-tagged.
- Primarily single-author (asarazan; contributions from vaudevillain in 2020 and Phil Oliver in April 2024); history has two eras — 2019–2021 (classic) and a March 2024 modernization burst. `git log` before 2024 describes the classic architecture, not the current one.
