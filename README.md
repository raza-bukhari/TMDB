# TMDB Android

TMDB Android is a Kotlin/Jetpack Compose app for browsing movies and TV series from [The Movie Database](https://www.themoviedb.org/). It is designed as a cinematic, local-first discovery experience with dark glass surfaces, poster-focused layouts, inline search, Discover filters, details, watchlist activity, profile stats, and person pages.

## Features

- Home feed with TMDB lists, personalized local sections, and poster-first cards.
- Inline search on Home and Discover without launching a separate activity.
- Discover for movies and TV series with advanced filters for rating, year, genre, language, runtime, provider, region, availability, and sort.
- Movie and TV detail screens with cast, similar content, trailers, watch providers, seasons, episodes, last/next episode, external ratings, and local activity.
- Person profile pages with biography and combined movie/series credits.
- Local watchlist with Movies, Series, Favorites, Watching, Completed, and Plan to watch categories.
- Local user metadata: status, favorite flag, user rating, watched date support in the model, and notes.
- Profile dashboard with local watchlist counts, average rating, favorite activity, recent notes, and theme switching.
- Stitch-aligned visual system: deep navy base, translucent glass panels, cyan/green accents, pill filters, and rating circles.

## Tech Stack

- Kotlin, Jetpack Compose, Material 3
- Modular Android architecture with `:app`, `:domain`, `:data`, `:core:*`, and `:feature:*`
- MVVM + Clean Architecture
- Koin for dependency injection
- Retrofit, OkHttp, and kotlinx.serialization for network calls
- Room for local persistence and cache-backed flows
- Paging 3 for TMDB list/search/discover results
- Coil 3 for image loading
- JUnit, Turbine, MockWebServer, Robolectric, and Compose UI tests

## Project Structure

```text
app/                  App shell, navigation host, DI aggregation
domain/               Pure Kotlin models, repository contracts, use cases
data/                 Repository implementations, paging sources, mappers
core/common/          Dispatchers and common utilities
core/database/        Room database, DAOs, entities
core/designsystem/    Theme, glass components, cards, badges, shared UI
core/navigation/      Navigation contracts
core/network/         TMDB/OMDb Retrofit APIs and DTOs
core/testing/         Fakes and test utilities
feature/movies/       Home, Discover, Watchlist, Profile
feature/detail/       Movie and TV detail screen
feature/person/       Person profile screen
feature/search/       Legacy/search module surface
```

## Requirements

- Android Studio with the bundled JDK
- Android SDK for minSdk 26 / targetSdk 36
- TMDB v4 read access token
- Optional: OMDb API key for IMDb/Rotten Tomatoes/Metacritic ratings

## Setup

Create `local.properties` in the project root. This file is ignored by Git and must not be committed.

```properties
TMDB_API_TOKEN=your_tmdb_v4_read_access_token
OMDB_API_KEY=your_optional_omdb_key
```

The app uses TMDB v4 bearer authentication through an OkHttp interceptor. Builds fail fast when `TMDB_API_TOKEN` is missing, unless you intentionally pass `-PskipTokenCheck=true` for CI or compile-only checks.

## Build and Test

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :feature:movies:testDebugUnitTest :feature:detail:testDebugUnitTest
```

For checks without local API tokens:

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest -PskipTokenCheck=true
```

Before committing:

```bash
git diff --check
```

## API Notes

- TMDB base URL: `https://api.themoviedb.org/3/`
- Image base URL: `https://image.tmdb.org/t/p/{size}{path}`
- Search uses TMDB multi-search so both movies and TV series can appear.
- Discover uses `/discover/movie` for movies and `/discover/tv` for series.
- Detail uses movie/TV-specific endpoints and explicit videos/watch-provider calls.

## Secret Handling

Do not commit `local.properties`, API tokens, generated credentials, or private config. The repository is configured to keep `local.properties` ignored.

## Current Status

The app is published at:

https://github.com/raza-bukhari/TMDB

The current implementation focuses on a local-first TMDB browsing experience with movie and TV support, watchlist activity, provider/trailer support, and Stitch-inspired cinematic UI polish.
