# KediBiloTV

A Netflix-style, cat-themed Android IPTV application supporting Android TV, phones, and tablets.

**Built by Hüseyin Karacif**

---

## Features

- **Live TV, Movies & Series** via Xtream Codes API
- **Netflix-style Home** — auto-rotating hero banner, content type tabs with icons
- **Continue Watching** — resume with progress bar indicator
- **Favorites** — save any content for quick access
- **Category Search** — search/filter categories by name in real time
- **Dual Platform** — single APK, runtime platform detection (TV / mobile)
- **Neon Gatos Cinema** design — bold, cinematic, cat-energy UI
- **Low resource usage** — runs smoothly on 2GB RAM devices

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI — Mobile | Jetpack Compose + Material 3 |
| UI — TV | Compose TV (androidx.tv) |
| Navigation | Compose Navigation |
| Video Player | Media3 / ExoPlayer |
| Database | Room |
| Networking | Ktor Client (OkHttp engine) |
| Dependency Injection | Hilt |
| Image Loading | Coil |
| Async | Coroutines + Flow |
| Architecture | Clean Architecture — UseCase → Repository → ViewModel → Screen |

---

## Project Structure

```
app/src/main/java/com/kedibilotv/
├── data/
│   ├── api/          # Xtream Codes API service and DTOs
│   ├── db/           # Room database, entities, DAOs
│   └── repository/   # Repository implementations
├── domain/
│   ├── model/        # Domain models
│   ├── repository/   # Repository interfaces
│   └── usecase/      # Business logic use cases
├── ui/
│   ├── common/       # Shared UI components
│   ├── theme/        # KediBilo theme, colors, typography
│   ├── navigation/   # NavHost and route definitions
│   ├── login/        # Login screen
│   ├── home/         # Home screen (TV + Mobile)
│   ├── category/     # Category list
│   ├── content/      # Content list + search
│   ├── detail/       # Movie / Series detail screen
│   ├── player/       # Video player screen
│   └── settings/     # Settings screen
├── player/           # Media3 player wrapper
└── di/               # Hilt DI modules
```

---

## Screens

| Screen | Description |
|---|---|
| Login | Server connection credentials input |
| Home | Netflix-style hero banner, content type tabs (Live/Movies/Series with icons), Continue Watching with progress bars, Favorites |
| Category | Sub-category grid + live search filter (search icon in top bar) |
| Content List | Poster cards + live search filter |
| Detail | Movie/Series info, season/episode selector, favorite toggle |
| Player | Fullscreen Media3 player, D-pad + gesture controls |
| Settings | Buffer size, sign out |

---

## Requirements

- Android Studio Hedgehog or later
- JDK 17
- Android SDK 34
- minSdk 24

---

## Getting Started

```bash
git clone https://github.com/huseyinkaracif/KediBiloTV.git
cd KediBiloTV
./gradlew assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

---

## Usage

1. Open the app
2. Enter your Xtream Codes server credentials:
   - **Server URL** — e.g. `http://server.com:8080`
   - **Username**
   - **Password**
3. Tap **Connect**
4. Browse Live TV, Movies, or Series


## License

MIT © Hüseyin Karacif
