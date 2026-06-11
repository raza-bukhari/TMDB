# Decision Log

Append-only, newest first. Format: decision · alternatives · rationale (≤2 sentences) · reversibility.

---

## 2026-06-12 — Milestones 4–5

**D-014: Kover applied per-module, not aggregated at root** — alt: root `kover(project(...))` aggregation. Cross-project Kover can't resolve Android library variants without per-module variant config; per-module `koverVerify` is robust and single-Android-module Kover needs no variant wiring. Each measured module (`:domain` + 3 features) carries its own 80% rule. Reversibility: easy.

**D-013: Category list pagination is append-by-page in the Room cache, orderIndex = (page-1)*20 + index** — alt: store a page column / Paging 3. TMDB returns 20/page; a monotonic orderIndex keeps the cache ranked across appended pages with no extra schema. Reversibility: easy (revisits D-002 if jank appears).

## 2026-06-11 — Kickoff decisions

**D-011: Coil pinned to 3.4.0** — alt: upgrade project Kotlin past 2.2.10 to read Coil 3.5.0's Kotlin 2.4.0 metadata. Kotlin version is coupled to AGP 9.2.1 built-in Kotlin; bumping it for an image library is disproportionate. Reversibility: easy.

**D-010 (amends D-004): token check fails only the :app assemble path, not configuration** — alt: configuration-time failure everywhere. Library compilation and all JVM tests must stay runnable without a secret; `:app:checkTmdbToken` (wired into preBuild) fails with an actionable message, `-PskipTokenCheck=true` overrides for CI. Reversibility: easy.

**D-009: Stay on compileSdk 36.1; pin core-ktx to 1.17.0** — alt: bump compileSdk to 37 (requires downloading platform android-37; only 36.1 installed). Smallest change that keeps the toolchain matching the installed SDK; revisit when targeting API 37. Reversibility: easy.

**D-008: kotlinx.serialization over Moshi** — alt: Moshi+KSP, Gson. Kotlin-first, compile-time, no reflection; pairs with Retrofit via official converter. Reversibility: easy.

**D-007: Coil 3 for image loading** — alt: Glide. Compose-native API, coroutines-based, active Compose support. Reversibility: easy.

**D-006: Search is network-only (no Room cache)** — alt: cache search results. Search results are ephemeral and query-dependent; caching adds invalidation complexity with no offline value. Reversibility: easy.

**D-005: Navigation Compose with type-safe (serializable) routes** — alt: Navigation3, manual back-stack. Stable, official, type-safe since 2.8; Navigation3 reevaluated if multi-pane is needed. Reversibility: hard-ish (route contracts spread across features) — mitigated by confining contracts to :core:navigation.

**D-004: Missing TMDB token fails at configuration time with actionable message; env var TMDB_API_TOKEN as fallback** — alt: runtime failure, empty default. Fail-fast beats a confusing 401 at runtime; env fallback keeps CI working. Reversibility: easy.

**D-003: Konsist architecture tests live in :app unit test source set** — alt: dedicated :tests:konsist module. Konsist scans the repo from disk so placement is arbitrary; :app avoids an extra module until there's more meta-testing. Reversibility: easy.

**D-002: Manual page-increment pagination, not Paging 3** — alt: Paging 3 + RemoteMediator. YAGNI — TMDB pages are simple `page`/`total_pages`; Paging 3 adds API surface through every layer. Revisit if jank or placeholders become requirements. Reversibility: moderate.

**D-001: Robolectric for Room/DAO tests** — alt: instrumented androidTest on emulator. JVM-speed feedback in the standard test task and CI without device provisioning; critical flows still get real instrumented UI tests. Reversibility: easy.
