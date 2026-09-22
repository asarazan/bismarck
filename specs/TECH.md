# TECH.md — Architecture & Constraints

Companion to [PRODUCT.md](PRODUCT.md). For agent-operational guidance (build commands, grep traps, verification gates) see [AGENTS.md](../AGENTS.md). This file describes how the system works and what is allowed to change.

## Core model

A `Bismarck<T>` is a single-value cache cell. Composition, not inheritance:

```
Bismarck<T>  =  Fetcher<T>?  +  Storage<T>  +  Freshness?  +  CoroutineDispatcher
```

- `Fetcher<T>` = `suspend () -> T?` — produces a fresh value (typically a network call).
- `Storage<T>` — persists the current value. `MemoryStorage` (default) or `FileStorage` (write-through file with in-memory read cache).
- `Freshness` — decides whether a `check()` triggers a fetch. `SimpleFreshness(duration)` is TTL in memory; `PersistentFreshness` survives restarts by writing `"$lastRunNanos:$resetNanos"` to a file.
- Concurrent fetches are deduped via a `job?.join()` gate in `DefaultBismarck.fetch()` — best-effort today; the gate itself races (defect 8).

State machine: `Fresh | Stale | Fetching`, derived (not stored) from fetch-count + freshness.

Null semantics worth knowing: `Fetcher` is `suspend () -> T?`, and a fetcher returning null marks the cell *fresh with a null value*; `clear()` is `insert(null)` plus a freshness reset. "Never fetched," "cleared," and "server said null" are indistinguishable in `values` — a deliberate simplification, but one consumers should know.

### Public API (`Bismarck.kt` — settled shape; semantics under repair until 1.0)

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

"Settled shape" means signatures are stable but semantics are not yet: the fixes for defects 1, 2, and 8 all change observable behavior of this surface, and defect 2's fix may add a `Config` field. The 1.0 freeze (PRODUCT.md roadmap theme 5) is what locks signatures *and* documented semantics. Anything beyond this surface is a product decision (PRODUCT.md §The bet: minimalism as the feature), not a code decision.

## Contracts and their constraints

| Contract | Shape | Constraint |
|---|---|---|
| `Storage<T>` | `fun get(): T?` / `fun put(data: T?)` | Whole-value, synchronous, non-suspending. One call site (`DefaultBismarck`). |
| `Serializer<T>` | `ByteArray` in/out | Whole-blob; no streaming. (The pre-KMP "classic" version used `InputStream`/`OutputStream`; portability was traded for it.) |
| `expect class File` | `readBytes`/`writeBytes`/`delete`/`exists` | Whole-file; no seek/stream. Actuals: `java.io.File`, `NSFileManager`, Node `fs`. |

These three whole-value contracts are why the API stays at four methods — and exactly what a database-cursor backend would break (see §Open questions).

Open dependency question an outside KMP contributor will ask in their first hour: why hand-rolled `expect class File` and `NSData` conversions instead of okio or kotlinx-io, which are binary-safe on every target and would delete defect 3 outright? There is no recorded answer yet. Adopt-or-reject explicitly as part of #34's fix — "small dependency, deletes a whole defect class" vs. "zero-IO-dependency principle" — rather than letting the hand-rolled layer persist by inertia.

## Module & target matrix

| Module | Published as | Targets |
|---|---|---|
| `:bismarck` | `net.sarazan:bismarck` | jvm, js(IR, nodejs), iosArm64, iosX64, iosSimulatorArm64 |
| `:bismarck-kotlinx-json` | `net.sarazan:bismarck-serializer-kotlinx` | same |
| `bismarck-classic/` | — **not in build** | reference-only RxJava/Android predecessor; conflicts with current type names |

Toolchain (as of 2026-08, due for modernization): Kotlin 1.9.20, coroutines 1.8.0, kotlinx-serialization 1.6.3, kotlinx-datetime 0.5.0, stately 2.0.0, Gradle 8.6, AGP 8.3.0 (unused — no Android target). Publishing via vanniktech maven-publish 0.28.0 → Maven Central.

## Verification

- **Gate:** `./gradlew jvmTest`. This is the only suite that asserts; JS/iOS `runBlockingTest` actuals launch-and-forget (defect 4). **While #33 is open, the gate is red on `main`**: 2 of 8 CommonTests fail (`testDedupe`, `testStateChannel`) due to defect 2. Last verified 2026-09-22; #33 is the source of truth for current status.
- Target state: all three platform suites assert; tests use `kotlinx-coroutines-test` virtual time instead of wall-clock `delay()`; CI runs jvm + js + iosSimulator suites plus `koverHtmlReport` on every PR.
- Coverage tooling: Kover (no thresholds enforced yet).

## Known defects (each tracked in the issue backlog)

1. **Scope lifecycle** (#32): `DefaultBismarck.coroutineScope` is a getter minting a new `CoroutineScope(dispatcher + SupervisorJob())` per access. No cancellation path exists; every internal `launch` is unstructured. Fix direction: one owned scope per instance + a disposal mechanism that doesn't grow the API carelessly.
2. **Observation side effects** (#33): the `values`/`states`/`errors` getters each `launch { check() }` on property read — observing can trigger network IO, and this is what breaks the two red tests. Fix direction: the existing `checkOnLaunch` flag is simply bypassed by the getters — delete the getter side effects; only add a new `checkOnObserve` (default off) if a real consumer needs it.
3. **Binary-unsafe IO off-JVM** (#34): iOS `ByteArray.toNSData()` round-trips through `decodeToString()`+UTF-8 (corrupts non-text payloads); JS reads *and* writes go through UTF-8 strings, lossy in both directions. Only JVM is binary-safe.
4. **Test harness** (#31): JS/iOS `runBlockingTest` actuals are fire-and-forget; suites pass vacuously.
5. **JS platform hacks** (#37): `js("require(\"fs\")")` + `__dirname`-relative path resolution — breaks under bundlers; Node-only assumption undocumented.
6. **Docs drift** (#35): README documents the pre-2024 API, dead Bintray repo, version 0.1.0.
7. **Release hygiene + broken publishing** (#38): single git tag (0.1.0) vs published 0.6.2; no changelog. `SONATYPE_HOST=DEFAULT` targets the OSSRH endpoint sunset mid-2025 — the pinned vanniktech 0.28.0 already supports the Central Portal, so the code side is a one-property switch to `CENTRAL_PORTAL`, but the `net.sarazan` namespace also needs a manual, account-level Portal migration before any publish can succeed.
8. **Unsynchronized fetch state** (#32, state-confinement scope bullet): `fetchJob`/`freshnessJob` are plain `var`s mutated from `Dispatchers.IO` threads; the read-then-write dedupe gate in `fetch()` can race under concurrent `check()` calls and fire duplicate fetches. Only `_fetchCount` is atomic.

## Open questions (design-stage; do not implement without a spec)

### Reactive / database-backed storage

Today a cell is one atomic value; backing it with a streaming DB cursor breaks all three whole-value contracts. Candidate directions, in ascending ambition:

- **A. Blob punt** — `T = List<Row>` over SQLDelight/Room used as a dumb store. Works now; no cursor benefits; fine as a documented pattern, not a feature.
- **B. Reactive storage (favored)** — evolve `Storage` to `suspend get()` / `suspend put()` + optional `observe(): Flow<T?>`; a DB-backed storage pushes row-change invalidations into the cell. Keeps the one-value model and the 4-method consumer API, but the costs must be stated honestly: (a) it is structurally Store5's `SourceOfTruth` contract (suspend writer, Flow reader) — acceptable, because the ceremony PRODUCT.md rejects is consumer-side envelopes, not storage shape, but say so before an outside contributor does; (b) it creates two writers to one cell (fetcher vs. storage pushes) with **unresolved conflict/arbitration semantics** — an explicit sub-question for the spec; (c) `suspend get()` breaks the synchronous hydration at construction (`DefaultBismarck.kt:33`), so the first `values` emission becomes pre-hydration null — an observable behavior change to the consumer surface. Breaking `Storage` change → 0.7.x, which is why PRODUCT.md sequences this before the 1.0 freeze.
- **C. Collection cells** — a second core type for multi-row/paged data (the Store5-shaped answer). Rejected by default per PRODUCT.md unless B proves insufficient.

### Platform surface

- Android target (#40): addable with modest effort — the JVM `File` actual is Context-free (pure `java.io.File`) but `androidMain` does NOT inherit from `jvmMain` under the hierarchy template, so reuse means a custom intermediate source set or duplication. Shouldn't ship without a sample app consuming it (#39).
- macOS (#40): requires first moving the Darwin actuals from `iosMain` to `appleMain` (`macosMain` is a sibling of `iosMain` — it inherits nothing from it). The Foundation APIs used are macOS-identical, so the move is mechanical.
- Swift export: revisit when JetBrains stabilizes Swift interop. There is currently **no consumable iOS artifact at all** — no `binaries.framework {}`, no CocoaPods, no SPM export. Framework export config is a stated prerequisite inside #39.
