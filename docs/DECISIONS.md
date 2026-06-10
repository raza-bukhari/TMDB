# Decision Log

Append-only, newest first. Format: decision · alternatives · rationale (≤2 sentences) · reversibility.

---

## 2026-06-11 — Kickoff decisions

**D-009: Stay on compileSdk 36.1; pin core-ktx to 1.17.0** — alt: bump compileSdk to 37 (requires downloading platform android-37; only 36.1 installed). Smallest change that keeps the toolchain matching the installed SDK; revisit when targeting API 37. Reversibility: easy.

**D-008: kotlinx.serialization over Moshi** — alt: Moshi+KSP, Gson. Kotlin-first, compile-time, no reflection; pairs with Retrofit via official converter. Reversibility: easy.

**D-007: Coil 3 for image loading** — alt: Glide. Compose-native API, coroutines-based, active Compose support. Reversibility: easy.

**D-006: Search is network-only (no Room cache)** — alt: cache search results. Search results are ephemeral and query-dependent; caching adds invalidation complexity with no offline value. Reversibility: easy.

**D-005: Navigation Compose with type-safe (serializable) routes** — alt: Navigation3, manual back-stack. Stable, official, type-safe since 2.8; Navigation3 reevaluated if multi-pane is needed. Reversibility: hard-ish (route contracts spread across features) — mitigated by confining contracts to :core:navigation.

**D-004: Missing TMDB token fails at configuration time with actionable message; env var TMDB_API_TOKEN as fallback** — alt: runtime failure, empty default. Fail-fast beats a confusing 401 at runtime; env fallback keeps CI working. Reversibility: easy.

**D-003: Konsist architecture tests live in :app unit test source set** — alt: dedicated :tests:konsist module. Konsist scans the repo from disk so placement is arbitrary; :app avoids an extra module until there's more meta-testing. Reversibility: easy.

**D-002: Manual page-increment pagination, not Paging 3** — alt: Paging 3 + RemoteMediator. YAGNI — TMDB pages are simple `page`/`total_pages`; Paging 3 adds API surface through every layer. Revisit if jank or placeholders become requirements. Reversibility: moderate.

**D-001: Robolectric for Room/DAO tests** — alt: instrumented androidTest on emulator. JVM-speed feedback in the standard test task and CI without device provisioning; critical flows still get real instrumented UI tests. Reversibility: easy.
