# Decision Log

## D-012: Rich Movie Metadata Storage in Room
**Decision:** Store complex lists (Cast, Crew, Similar Movies, Watch Providers) as JSON strings within `MovieDetailEntity` instead of separate relational tables.
**Alternatives:** Full relational schema with foreign keys.
**Rationale:** The details page is a single-read site where data is fetched and displayed as a cohesive unit. JSON serialization provides the simplest offline-first implementation without the overhead of complex migrations or multiple DAO queries for a single screen.
**Reversibility:** Easy; can be migrated to relational tables later if deep querying of cached cast members becomes a requirement.

## D-011: Appended Network Requests for Movie Details
**Decision:** Use TMDB's `append_to_response` parameter to fetch credits, release dates, similar movies, and watch providers in a single HTTP call.
**Alternatives:** Sequential or parallel independent network calls.
**Rationale:** Reduces network overhead and improves UI responsiveness by delivering all necessary data for the rich details page in one round trip.
**Reversibility:** Easy; Retrofit interfaces can be split if needed.

... (existing entries)
