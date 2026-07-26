# StandardWeather

A weather app I built for Android using Jetpack Compose. It shows current conditions, hourly and weekly forecasts, and works offline by caching everything locally in Room.

---

## What it does

- Shows current weather — temperature, feels-like, humidity, wind speed, UV index, visibility
- Hourly forecast for the next 24 hours with rain probability
- 7-day forecast with high/low temps
- Temperature trend drawn as a smooth curve on a Canvas
- Works without internet — cached data loads instantly, with a banner showing when it was last synced
- Refreshes in the background every 30 minutes using WorkManager (only when network is available)
- Sends a local notification if conditions are extreme — high wind, heatwave, or freezing temps
- Background gradient changes based on the current weather condition
- City search using the WeatherAPI.com Search endpoint, with recent searches saved locally

---

## How it's structured

I went with Clean Architecture and MVVM. The idea was to keep the data, business logic, and UI completely separate so each part is easy to test and change independently.

- `data/` — network calls, local DB, and the repository that ties them together
- `domain/` — plain Kotlin models and the repository interface
- `di/` — Hilt modules
- `ui/` — Compose screens, ViewModels, and theming
- `work/` — background sync worker

Data always flows one way: `API / Room → Repository → ViewModel → Compose UI`

The repository decides whether to hit the network or serve from cache based on a 30-minute TTL.

---

## Tech stack

| What | Why |
|---|---|
| Kotlin + Jetpack Compose | Main language and UI |
| Hilt | Dependency injection |
| Room | Local cache / offline storage |
| Retrofit + OkHttp | Network calls to WeatherAPI.com |
| Kotlin Coroutines + Flow | Async work and reactive state |
| WorkManager | Background refresh |
| Coil | Loading weather icons |
| MockK + Turbine | Unit testing |

---

## Getting it running

You'll need an API key from [weatherapi.com](https://www.weatherapi.com) — sign up for a free account (the free tier covers all endpoints this app uses).

Add it to `local.properties` in the project root:

```
WEATHER_API_KEY=your_key_here
```






