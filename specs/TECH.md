# TECH.md — Architecture & Technical Ground Truth

Companion to [PRODUCT.md](PRODUCT.md). For agent-operational guidance (build commands, grep traps, verification gates) see [AGENTS.md](../AGENTS.md). This file describes how the system works and what is allowed to change.

## Core model

A `Bismarck<T>` is a single-value cache cell. Composition, not inheritance:

```
Bismarck<T>  =  Fetcher<T>?  +  Storage<T>  +  Freshness?  +  CoroutineDispatcher
```

- `Fetcher<T>` = `suspend () -> T?` — produces a fresh value (typically a network call).
- `Storage<T>` — persists the current value. `MemoryStorage` (default) or `FileStorage` (write-through file with in-memory read cache).
- `Freshness` — decides whether a `check()` triggers a fetch. `SimpleFreshness(duration)` is TTL in memory; `PersistentFreshness` survives restarts by writing `"$lastRunNanos:$resetNanos"` to a file.
- Concurrent fetches are deduped via a `job?.join()` gate in `DefaultBismarck.fetch()`.

State machine: `Fresh | Stale | Fetching`, derived (not stored) from fetch-count + freshness.

### Public API (frozen surface, `Bismarck.kt`)

```kotlin
val values: StateFlow<T?>
val states: StateFlow<State?>
val errors: StateFlow<Throwable?>
val coroutineScope: CoroutineScope  // public — exposed deliberately (commit 787b3c3) for non-suspend callers like postInvalidate(); its per-access-fresh-instance behavior is Known defect 1
suspend fun insert(value: T?)   // manual write-through
suspend fun invalidate()        // reset freshness + check
suspend fun check()             // fetch iff stale
suspend fun clear()             // insert(null) + reset
```

Anything beyond this surface is a product decision (PRODUCT.md §The bet), not a code decision. Note `coroutineScope` is genuinely part of the surface — fixes to defect 1 change the observable behavior of an existing public member and must say so.

## Contracts and their constraints

| Contract | Shape | Constraint |
|---|---|---|
| `Storage<T>` | `fun get(): T?` / `fun put(data: T?)` | Whole-value, synchronous, non-suspending. One call site (`DefaultBismarck`). |
| `Serializer<T>` | `ByteArray` in/out | Whole-blob; no streaming. (The pre-KMP "classic" version used `InputStream`/`OutputStream`; portability was traded for it.) |
| `expect class File` | `readBytes`/`writeBytes`/`delete`/`exists` | Whole-file; no seek/stream. Actuals: `java.io.File`, `NSFileManager`, Node `fs`. |

These three whole-value contracts are load-bearing: they are why the API stays four methods, and they are exactly what a database-cursor backend would need to break (see §Open questions).

## Module & target matrix

| Module | Published as | Targets |
|---|---|---|
| `:bismarck` | `net.sarazan:bismarck` | jvm, js(IR, nodejs), iosArm64, iosX64, iosSimulatorArm64 |
| `:bismarck-kotlinx-json` | `net.sarazan:bismarck-serializer-kotlinx` | same |
| `bismarck-classic/` | — **not in build** | reference-only RxJava/Android predecessor; conflicts with current type names |

Toolchain (as of 2026-08, due for modernization): Kotlin 1.9.20, coroutines 1.8.0, kotlinx-serialization 1.6.3, kotlinx-datetime 0.5.0, stately 2.0.0, Gradle 8.6, AGP 8.3.0 (unused — no Android target). Publishing via vanniktech maven-publish 0.28.0 → Maven Central.

## Verification

- **Gate:** `./gradlew jvmTest`. This is currently the only suite that asserts; JS/iOS `runBlockingTest` actuals launch-and-forget (known defect). **The gate is currently red on `main`**: 2 of 8 CommonTests fail (`testDedupe`, `testStateChannel`) due to Known defect 2 — verified 2026-08-09.
- Target state: all three platform suites assert; tests use `kotlinx-coroutines-test` virtual time instead of wall-clock `delay()`; CI runs jvm + js + iosSimulator suites plus `koverHtmlReport` on every PR.
- Coverage tooling: Kover (no thresholds enforced yet).

## Known defects (ground truth for the issue tracker)

1. **Scope lifecycle**: `DefaultBismarck.coroutineScope` is a getter minting a new `CoroutineScope(dispatcher + SupervisorJob())` per access. No cancellation path exists; every internal `launch` is unstructured. Fix direction: one owned scope per instance + a `close()`/scope-parameter story that doesn't grow the API carelessly.
2. **Observation side effects**: the `values`/`states`/`errors` getters each `launch { check() }` on property read — observing can trigger network IO, and this is what breaks the two currently-red tests. Fix direction: the existing `checkOnLaunch` flag is simply bypassed by the getters — delete the getter side effects; only add a new `checkOnObserve` (default off) if a real consumer needs it.
3. **Binary-unsafe IO off-JVM**: iOS `ByteArray.toNSData()` round-trips through `decodeToString()`+UTF-8 (corrupts non-text payloads); JS `File.writeBytes` writes UTF-8 strings. Only JVM is binary-safe.
4. **Test harness**: JS/iOS `runBlockingTest` actuals are fire-and-forget; suites pass vacuously.
5. **JS platform hacks**: `js("require(\"fs\")")` + `__dirname`-relative path resolution — breaks under bundlers; Node-only assumption undocumented.
6. **Docs drift**: README documents the pre-2024 API, dead Bintray repo, version 0.1.0.
7. **Release hygiene + broken publishing**: single git tag (0.1.0) vs published 0.6.2; no changelog. Worse: `SONATYPE_HOST=DEFAULT` targets the OSSRH endpoint sunset mid-2025, and vanniktech publish 0.28.0 predates Central Portal support — publishing is impossible until migrated (issue #38, includes a manual account-level namespace migration).
8. **Unsynchronized fetch state**: `fetchJob`/`freshnessJob` are plain `var`s mutated from `Dispatchers.IO` threads; the read-then-write dedupe gate in `fetch()` can race under concurrent `check()` calls and fire duplicate fetches. Only `_fetchCount` is atomic.

## Open questions (design-stage; do not implement without a spec)

### Reactive / database-backed storage

Today a cell is one atomic value; backing it with a streaming DB cursor breaks all three whole-value contracts. Candidate directions, in ascending ambition:

- **A. Blob punt** — `T = List<Row>` over SQLDelight/Room used as a dumb store. Works now; no cursor benefits; fine as a documented pattern, not a feature.
- **B. Reactive storage (favored)** — evolve `Storage` to `suspend get()` / `suspend put()` + optional `observe(): Flow<T?>`; a DB-backed storage pushes row-change invalidations into the cell. Keeps the one-value model and the 4-method API; requires `DefaultBismarck` init rework (no more `storage.get()` in a property initializer) and a breaking `Storage` change → 0.7.x.
- **C. Collection cells** — a second core type for multi-row/paged data (the Store5-shaped answer). Presumptively rejected by PRODUCT.md unless B proves insufficient.

### Platform surface

- Android target: addable with modest effort — the JVM `File` actual is Context-free (pure `java.io.File`) but `androidMain` does NOT inherit from `jvmMain` under the hierarchy template, so reuse means a custom intermediate source set or duplication. Needs a consumer story (sample app) to be honest.
- macOS: requires first moving the Darwin actuals from `iosMain` to `appleMain` (`macosMain` is a sibling of `iosMain` — it inherits nothing from it). The Foundation APIs used are macOS-identical, so the move is mechanical.
- Swift export: revisit when JetBrains stabilizes Swift interop. Note there is currently **no consumable iOS artifact at all** — no `binaries.framework {}`, no CocoaPods, no SPM export; any Swift-facing story starts by adding framework export config.
