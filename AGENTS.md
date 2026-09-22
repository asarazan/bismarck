# AGENTS.md — Bismarck

Guidance for coding agents working in this repo. Read this before trusting the README or grep results.

## What this is

Bismarck is a caching/syncing library for Kotlin Multiplatform. A `Bismarck<T>` is a single-value cache cell combining a `Fetcher<T>` (suspend producer), a `Storage<T>` (persistence), and a `Freshness` policy (TTL/invalidation). It exposes `values`/`states`/`errors` as coroutine `StateFlow`s and dedupes concurrent fetches (best-effort — see landmine 6). Published to Maven Central as `net.sarazan:bismarck` and `net.sarazan:bismarck-serializer-kotlinx`, version 0.6.2 as of 2026-09 (pre-1.0; API may still break).

Product intent and constraints: [specs/PRODUCT.md](specs/PRODUCT.md). Architecture contracts and the full defect list: [specs/TECH.md](specs/TECH.md).

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

**Only `jvmTest` is a meaningful gate — and while issue #33 is open, it is RED on `main`.** Last verified 2026-09-22: 2 of 8 CommonTests fail (`testDedupe`, `testStateChannel`, both `expected:<Stale> but was:<Fetching>`), caused by landmine 2 below (observing a flow fires a hidden `check()` that races the assertions). Issue #33 is the source of truth for current status: re-run `jvmTest` on a clean checkout of `main` to establish your baseline before changing anything. While #33 is open, those two failures are pre-existing; any *other* failure is yours — and once #33 is closed, ALL failures are yours.

The JS and iOS test harnesses are broken by construction: their `runBlockingTest` actuals (`bismarck/src/jsTest/.../test/Platform.kt`, `bismarck/src/iosTest/.../test/Platform.kt`) do `scope.launch { testBody() }` and return without awaiting, so `jsNodeTest` / `iosSimulatorArm64Test` pass vacuously. Only the JVM actual uses `runBlocking`. Don't interpret green JS/iOS test runs as evidence of anything until those actuals are fixed.

Tests in `commonTest/CommonTests.kt` are wall-clock-timing-based (`delay(50)` etc.) and inherently flaky. Prefer adding tests with `kotlinx-coroutines-test` virtual time; do not add more `delay()`-calibrated assertions.

There is **no CI**. Nothing verifies work after merge unless you run it yourself.

## Architecture contracts that matter

- `Storage<T>` (`storage/Storage.kt`) is a two-method, whole-value, non-suspending contract: `fun get(): T?` / `fun put(data: T?)`. It has exactly two implementations (`MemoryStorage`, `FileStorage`) and one call site (`DefaultBismarck` — property init and `insert`). Any streaming/cursor/database backend requires changing this contract (see "streaming backends" below).
- `Serializer<T>` (`serialization/Serializer.kt`) is `ByteArray`-in/out, not stream-based.
- Platform file IO goes through `expect class File` (`platform/File.kt`) — whole-file `readBytes`/`writeBytes` only.

### Known landmines (verified against source, 2026-08)

1. **`DefaultBismarck.coroutineScope` is a `get()` that constructs a fresh `CoroutineScope` on every access** (`DefaultBismarck.kt:26-27`). Every internal `launch` runs in its own unstructured, never-cancelled scope; there is no cancellation path and the `SupervisorJob` is inert. Tracked in #32.
2. **Reading `values`/`states`/`errors` fires a `check()` side effect** (`DefaultBismarck.kt:29-44`) — merely observing the cache can trigger a network fetch. This is what breaks the two red tests. Tracked in #33.
3. **iOS `ByteArray.toNSData()` corrupts binary data** (`iosMain/.../platform/NSDataConversions.kt`): it round-trips through `decodeToString()` + UTF-8 encoding. Any non-UTF-8 payload (protobuf, images, JVM serialization) is silently mangled on iOS. JS is lossy in both directions (`writeBytes` and `readBytes` go through UTF-8 strings). Only the JVM path is binary-safe. Tracked in #34.
4. JS `File` resolves relative paths against `__dirname` and uses `js("require(\"fs\")")` — bundler-dependent. Tracked in #37.
5. No sample apps exist for any platform, and no `binaries.framework {}`/CocoaPods/SPM export is configured — there is currently no way for Swift code to consume the iOS artifacts. Tracked in #39 (framework export is a stated prerequisite there).
6. `fetchJob`/`freshnessJob` in `DefaultBismarck` are unsynchronized `var`s mutated from `Dispatchers.IO` threads (the default dispatcher on JVM and iOS) — the read-then-write dedupe gate in `fetch()` can race and fire duplicate fetches. Tracked in #32 (scope bullet on state confinement).
7. Publishing is broken by configuration: `SONATYPE_HOST=DEFAULT` targets Sonatype's OSSRH endpoint, sunset mid-2025. The pinned vanniktech plugin (0.28.0) already supports the Central Portal — the code fix is switching that property to `CENTRAL_PORTAL` — but the `net.sarazan` namespace also needs a manual, account-level Portal migration. Both tracked in #38; don't attempt to publish before it lands.

### Streaming/cursor backends (design constraint, not a bug)

The whole design models a cache cell as one atomic value: `Storage` is whole-value, `Serializer` is whole-blob, and `values: StateFlow<T?>` emits one `T` at a time. Backing a Bismarck with a database cursor or paged query is not possible without breaking changes at all three layers (make `Storage` suspend + `Flow`-returning, stream-based serialization, and a multi-row observation shape). The blast radius is small (one call site) but it is an intentional v-next design conversation, not a patch.

## Conventions

- Kotlin official style; `.editorconfig` permits wildcard imports. No ktlint/detekt/spotless is wired in — match surrounding style by hand.
- Versioning: bump `VERSION_NAME` in the **root** `gradle.properties` (modules inherit it; their own `gradle.properties` carry only `POM_*` keys). Releases are not consistently git-tagged.
- Primarily single-author (asarazan; vaudevillain contributed 11 commits in 2020). Phil Oliver's April 2024 test-harness work was never merged — it lives on the unmerged `origin/po/testing` branch and is prior art for #31. History has two eras — 2019–2021 (classic) and a March 2024 modernization burst. `git log` before 2024 describes the classic architecture, not the current one.
