# PRODUCT.md — What Bismarck Is

## One-liner

Bismarck is a caching/syncing library for Kotlin Multiplatform with a radically minimal API: a cache cell is a fetcher, a storage, and a freshness policy — four suspend methods, three flows, and an exposed coroutine scope, nothing else.

## The problem

Every mobile/multiplatform app rebuilds the same machinery: fetch remote data, persist it locally, know when it's stale, dedupe concurrent refreshes, and expose loading/error state to the UI. Doing this correctly with coroutines, per-platform persistence, and TTL logic is boilerplate-heavy and easy to get subtly wrong (races between cached value and live value, duplicate in-flight fetches, freshness state lost across restarts).

## The bet: minimalism as the feature

The incumbent in this niche is [Store5](https://github.com/MobileNativeFoundation/Store) (Mobile Native Foundation) — capable, actively maintained, and ceremonious: builders, `StoreReadRequest`/`StoreReadResponse` envelopes, converters, source-of-truth adapters. Bismarck's differentiation is the opposite trade:

- **Whole public API fits on one screen** (`Bismarck.kt`, ~50 lines): `insert`, `invalidate`, `check`, `clear`, plus `values`/`states`/`errors` StateFlows and an exposed `coroutineScope` (whose lifecycle semantics are under repair — TECH.md Known defect 1).
- **One concept**: a cell. No request/response envelopes, no read-vs-write policies, no converter chains.
- **Config is a small DSL**: `fetcher {}`, `fileStorage {}`, `freshness {}` — memory-only by default, file-backed in two lines.
- **Freshness survives restarts** when given a path (`PersistentFreshness`), which most hand-rolled caches get wrong.

The API was deliberately shrunk in the March 2024 modernization (sync accessors and iterator-style observers were removed rather than patched when they raced with the flows). Preserving that minimalism is a product constraint: features that require envelope types or a second core concept are presumptively rejected.

## Target users

1. KMP app developers (Android/iOS/JVM) who want file- or memory-backed caching of API responses without adopting a data-layer framework.
2. Kotlin/JS (Node) services with the same shape of need.
3. (Aspirational) Swift-first iOS developers, once Kotlin→Swift export stabilizes and the iOS artifact story is real.

## Current status (2026-08)

Version 0.6.2 on Maven Central, pre-1.0, dormant since May 2024, primarily single-author (community contributions: vaudevillain in 2020, Phil Oliver in April 2024 — among the last commits before dormancy). Functional on JVM though the test suite is currently red (2 of 8 tests fail from a known observation-side-effect bug); iOS and JS targets compile but have known correctness gaps (binary-unsafe file IO on iOS/JS, no-op test harnesses). No CI, stale README, and the Maven Central publish path is broken pending the Sonatype Central Portal migration. Development is agent-assisted; GitHub issues are the source of truth for the work backlog (see [AGENTS.md](../AGENTS.md)).

## Roadmap themes (in order)

1. **Trustworthy** — CI, real tests on all targets, the known correctness bugs fixed.
2. **Honest docs** — README that matches the shipped API; specs kept current.
3. **Modern toolchain** — Kotlin 2.2.x, current coroutines/serialization; Android + macOS targets; sample app.
4. **1.0** — API freeze on the minimal surface; tagged, changelogged releases.
5. **v-next design question** — reactive/database-backed storage (see TECH.md §Open questions) without sacrificing the one-concept API.

## Non-goals

- Not an ORM, not a database, not a normalized entity cache (no graph relationships between cells).
- Not a Store5 clone; if a feature needs Store5-style ceremony, the answer is no or a redesign.
- No reflection, no annotation processing, no code generation.
