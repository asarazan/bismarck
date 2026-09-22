# PRODUCT.md — What Bismarck Is

## One-liner

Bismarck is a caching/syncing library for Kotlin Multiplatform with a deliberately small consumer API: a cache cell is a fetcher, a storage, and a freshness policy. The consumer-facing surface is four suspend methods, three flows, and an exposed coroutine scope.

## The problem

Every mobile/multiplatform app rebuilds the same machinery: fetch remote data, persist it locally, know when it's stale, dedupe concurrent refreshes, and expose loading/error state to the UI. Doing this correctly with coroutines, per-platform persistence, and TTL logic is boilerplate-heavy and easy to get subtly wrong (races between cached value and live value, duplicate in-flight fetches, freshness state lost across restarts). Candor requires saying that Bismarck itself currently has open defects on the first two (TECH.md defects 2 and 8) — roadmap theme 1 exists to close them; the differentiation below is earned only once it ships.

## The bet: minimalism as the feature

The established library in this space is [Store5](https://github.com/MobileNativeFoundation/Store) (Mobile Native Foundation): capable, actively maintained, and deliberately explicit — builders, `StoreReadRequest`/`StoreReadResponse` envelopes, converters, source-of-truth adapters. That is real power, and it costs API surface. Bismarck takes the opposite trade:

- **The consumer-facing API fits on one screen** (`Bismarck.kt`, ~50 lines): `insert`, `invalidate`, `check`, `clear`, plus `values`/`states`/`errors` StateFlows and an exposed `coroutineScope` (whose lifecycle semantics are under repair — TECH.md Known defect 1).
- **One concept**: a cell. No request/response envelopes, no read-vs-write policies, no converter chains.
- **Config is small**: `fetcher = { ... }` assignment plus `fileStorage {}` / `freshness {}` DSL blocks. Memory-only by default, file-backed in two lines.
- **Freshness survives restarts** when given a path (`PersistentFreshness`) — a detail that is easy to get wrong by hand. (Currently verified on JVM only; the JS/iOS test harnesses are vacuous until #31.)

The API was deliberately shrunk in the March 2024 modernization (sync accessors and iterator-style observers were removed rather than patched when they raced with the flows). Preserving that minimalism is a product constraint, scoped precisely: *ceremony* means consumer-visible envelope/request types and second core concepts, and those are rejected by default. The implementer SPI (`Storage`, `Serializer`, `Freshness`) may evolve; the consumer call-site may not.

## Target users

1. KMP app developers (iOS/JVM today; Android pending the target addition, #40) who want file- or memory-backed caching of API responses without adopting a data-layer framework.
2. Kotlin/JS (Node) services with the same shape of need — memory-backed today; file persistence on JS is UTF-8-lossy until #34/#37 land.
3. (Aspirational) Swift-first iOS developers, once Kotlin→Swift export stabilizes and a consumable framework export exists (#39).

## Current status (as of 2026-09)

Version 0.6.2 on Maven Central, pre-1.0, dormant since May 2024, primarily single-author (vaudevillain contributed in 2020; Phil Oliver's 2024 test-harness work sits unmerged on `po/*` branches). The test suite and defect status live in [TECH.md §Verification](TECH.md) and the issue tracker — the short version: JVM works with two known-red tests (#33), iOS/JS compile but carry correctness gaps (#34, #31), there is no CI yet (#30), the README is stale (#35), and publishing needs the Central Portal migration (#38). Development is agent-assisted; GitHub issues are the source of truth for the work backlog (see [AGENTS.md](../AGENTS.md)).

## Roadmap themes

Themes, not a strict sequence — in particular, green iOS CI may require the minimum Kotlin/Xcode-compatibility bump from theme 3 before theme 1 can finish.

1. **Trustworthy** — CI, real tests on all targets, the known correctness bugs fixed.
2. **Honest docs** — README that matches the shipped API; specs kept current.
3. **Modern toolchain** — current Kotlin 2.x, current coroutines/serialization; Android + macOS targets; sample app.
4. **Storage contract resolution** — answer the reactive/database-backed storage question (#41, TECH.md §Open questions) *before* freezing anything; the favored option is a breaking `Storage` change and must land pre-1.0.
5. **1.0** — freeze the consumer surface (signatures + documented semantics); adjudicate the legacy API questions first (#6, #7, #8, #13, #14); tagged, changelogged releases.

## Non-goals

- Not an ORM, not a database, not a normalized entity cache (no graph relationships between cells).
- Not a Store5 clone; if a feature needs envelope types or a second core concept, the answer is no or a redesign.
- No reflection, no annotation processing, no code generation.
