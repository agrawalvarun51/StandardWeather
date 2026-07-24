# StandardWeather ☀️⛅🌧️

An **offline-first** Android weather forecasting app built with modern Jetpack stack.

---

## Features

| Capability | Details |
|---|---|
| Current Weather | Temperature, feels-like, humidity, wind speed, UV index, visibility |
| Hourly Forecast | Next 24 hours with precipitation probability |
| Weekly Forecast | 7-day view with high/low temps and weather icons |
| Temperature Graph | Canvas-drawn smooth curve for 24-hour temperature trend |
| Offline-first | Room DB is source of truth; cached data shown immediately |
| Smart TTL refresh | Network call only when cache is older than 30 minutes |
| Background Sync | WorkManager periodic job every 30 minutes (network-required) |
| Extreme Weather Alerts | Notification for high wind (≥20 m/s), heat (≥40°C), cold (≤−10°C), and API alerts |
| Dynamic Theming | Gradient background changes per weather condition (sunny/rainy/stormy/snowy/…) |
| City Search | Geocoding API with debounced input; recent searches persisted in Room |

---

## Architecture

```
app/
├── data/
│   ├── local/          # Room DB, DAOs, Entities
│   ├── remote/         # Retrofit API service + DTOs
│   ├── mapper/         # DTO ↔ Entity ↔ Domain mappers
│   └── repository/     # WeatherRepositoryImpl (offline-first logic)
├── domain/
│   ├── model/          # Pure Kotlin domain models
│   └── repository/     # WeatherRepository interface
├── di/                 # Hilt modules (Network, Database, Repository)
├── ui/
│   ├── screen/         # WeatherScreen, SearchScreen (Jetpack Compose)
│   ├── viewmodel/      # WeatherViewModel, SearchViewModel
│   ├── state/          # WeatherUiState, SearchUiState
│   ├── navigation/     # NavHost + Screen sealed class
│   └── theme/          # Dynamic colour palettes per weather condition
└── work/               # WeatherSyncWorker + WorkManagerScheduler
```

**Design pattern:** Clean Architecture with MVVM + Repository pattern.

**Unidirectional Data Flow:** `API / DB → Repository → ViewModel (StateFlow) → Compose UI`

---

## Tech Stack

| Library | Version | Purpose |
|---|---|---|
| Jetpack Compose | BOM 2024.10 | UI |
| Material 3 | via BOM | Design system |
| Hilt | 2.52 | Dependency injection |
| Room | 2.6.1 | Local database / cache |
| Retrofit 2 | 2.11.0 | Network (OWM API) |
| OkHttp | 4.12.0 | HTTP client + logging |
| Gson | 2.11.0 | JSON serialisation |
| Kotlin Coroutines | 1.9.0 | Async operations |
| Flow | via Coroutines | Reactive streams |
| WorkManager | 2.9.1 | Background sync |
| Coil | 2.7.0 | Image loading |
| Navigation Compose | 2.8.3 | In-app navigation |
| MockK | 1.13.12 | Unit testing mocks |
| Turbine | 1.1.0 | Flow testing |

---

## Setup

### 1. Get an API key

Register at [openweathermap.org](https://openweathermap.org) and subscribe to the **One Call API 3.0** (free tier available).

### 2. Add the key to `local.properties`

```properties
WEATHER_API_KEY=your_key_here
```

### 3. Build & run

```bash
./gradlew assembleDebug
# or open in Android Studio and press Run
```

---

## Running Tests

```bash
./gradlew test
```

Covers:
- `WeatherRepositoryImplTest` – offline-first logic, TTL cache invalidation, search
- `WeatherViewModelTest` – UI state transitions (Loading → Success / Error)
- `WeatherMapperTest` – DTO-to-entity and entity-to-domain mappings

---

## Offline Behaviour

1. On first launch, enter a city name to search and tap a result.
2. The app fetches live weather and caches it in Room.
3. With no internet, the cached data is served immediately with an "Offline" banner and last-sync timestamp.
4. Background WorkManager job refreshes all cached cities every 30 minutes when network is available.

---

## Assumptions

- OpenWeatherMap One Call 3.0 is used (requires a free account subscription to the plan).
- City identification is based on lat/lon composite key (`lat_lon`) — sufficient for unique city resolution.
- Hourly forecast is capped at 24 items; daily at 7.
- Notification permission is requested at startup on Android 13+; app functions fully without it.
- Temperature unit is Celsius (metric).
