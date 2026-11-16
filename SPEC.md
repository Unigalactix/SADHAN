# SADHAN — v1 Build Specification

The complete blueprint for the first build. Written 2026-11-16. When the code lands, this file stays as the design record.

## 1. Identity

- **Name:** SADHAN (साधन — instrument/tool)
- **Package:** `app.sadhan`
- **Version:** 1.0.0 (versionCode 1)
- **Platform:** Android 10+ (minSdk 29), targetSdk 35, compileSdk 35, portrait
- **Stack:** Kotlin 2.0.21, AGP 8.7.3, Jetpack Compose (BOM 2024.12.01), navigation-compose, material-icons-extended, ZXing core 3.5.3, CameraX 1.4.1. **No other third-party SDKs** — networking via `HttpURLConnection`, JSON via `org.json` (built into Android).
- **Distribution:** signed APK on GitHub Releases of this repo. Same signing key for every release so updates install in place.

## 2. Non-negotiables

1. No ads, no analytics/telemetry/crash SDKs, no accounts, no login.
2. **No history:** tool state lives in memory only and is destroyed when the user leaves the tool. Only Settings (SharedPreferences) persist.
3. Internet features are individually toggleable in Settings; a master Internet switch overrides all.
4. Features that are unstable ship with a visible **BETA** tag (card chip + in-screen banner).
5. Permissions are minimal: `INTERNET`, `CAMERA`, `REQUEST_INSTALL_PACKAGES`. Camera feature declared not required.

## 3. Design language

Pure black background (#000000), white text, one amber accent (#FFB300), dark translucent cards, 20dp corners. Home = sectioned grid of tool cards: **Everyday / Pocket / Time / Internet / Sensors**. A reusable `BetaChip` composable (amber outline, "BETA") marks beta tools on their card and as a banner inside the screen.

## 4. Screens

| # | Tool | Notes |
|---|---|---|
| 1 | Calculator | Hand-written expression evaluator (+ − × ÷ % parentheses, decimals); session-only history line |
| 2 | Tip & Split | Bill, tip %, people → per-person + totals |
| 3 | EMI Calculator | Principal, annual rate, months → EMI, total interest, total payable |
| 4 | Age & Dates | Material3 date pickers; age in y/m/d; days-between mode |
| 5 | Unit Converter | Length, Weight, Temperature, Data, Area, Speed; factor tables |
| 6 | QR Maker | ZXing bitmap; Share (FileProvider cache file) + Save to Pictures (MediaStore) |
| 7 | QR Scanner | CameraX preview + ZXing `MultiFormatReader` on YUV_888 frames; result with Copy / Open URL; runtime camera permission |
| 8 | Text Tools | Live word/char/line counts; UPPERCASE, lowercase, Title Case, dedupe lines, trim |
| 9 | Scratchpad | State held only in the composable — leaving destroys it; labelled "erases itself when you leave" |
| 10 | Stopwatch | Start/pause/reset + in-session laps |
| 11 | Timer | Circular drag-to-set dial + presets (1/5/10/15/30/60); animated ring; alarm tone + vibration on finish |
| 12 | World Clock | Kolkata, Dubai, London, New York, Los Angeles, Singapore, Tokyo, Sydney, Berlin, Toronto (`java.time`) |
| 13 | Weather | Open-Meteo geocoding + forecast (no key): current temp, condition from weather code, humidity/wind, 5-day min/max |
| 14 | Currency | `api.frankfurter.app/latest` (no key); USD, EUR, GBP, INR, AED, JPY, AUD, CAD, SGD, CHF, CNY, NZD, ZAR, BRL, KRW; rates in memory only |
| 15 | YouTube **BETA** | BetaChip + banner "Beta — playback can be unstable. Uses YouTube's official embed." Paste link/ID → `youtube-nocookie.com/embed/<id>` in a WebView; search box → `m.youtube.com/results` in the same WebView; JS enabled on that WebView only |
| 16 | Flashlight | `CameraManager` torch on the back camera with flash |
| 17 | Compass | Rotation-vector sensor (fallback accel+magnetometer) → heading + rotating dial |
| 18 | Level | Accelerometer → bubble offset + X/Y tilt degrees |
| 19 | Image Compressor | Photo picker → original size, quality slider 10–100, JPEG compress, result size, Save to Pictures/SADHAN or Share |
| 20 | Settings | Master Internet toggle; Weather / Currency / YouTube (Beta) / Update-check toggles (SharedPreferences) |
| 21 | About | Version, "No ads. No tracking. No accounts. Nothing you type in a tool is saved.", Check-for-updates button, link to this repo |

When a feature is off in Settings, its screen shows "Turned off in Settings" instead of loading.

## 5. Updater (AETHER's method)

- `UpdateChecker` fetches `https://api.github.com/repos/Unigalactix/SADHAN/releases/latest` (User-Agent "SADHAN"), parses `tag_name` (strip `v`), semver-compares against `BuildConfig.VERSION_NAME`, returns latest version + release URL + the `.apk` asset's `browser_download_url` when newer.
- Silent check on app start (if enabled); an **Update available: vX.Y.Z** card appears on Home and in About; manual check in About.
- Install: download the asset to `cacheDir/updates/`, then `ACTION_VIEW` with a FileProvider URI (`application/vnd.android.package-archive`, grant-read + new-task flags). FileProvider via `androidx.core` + `res/xml/file_paths` covering cache-path. If `canRequestPackageInstalls()` is false, route the user to `Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES` first.

## 6. Signing

- Keystore generated once with keytool (alias `sadhan`, RSA 2048, validity 10000 days, `CN=SADHAN, O=Unigalactix`) and stored **outside the repo** with its `keystore.properties` (`storeFile/storePassword/keyAlias/keyPassword`).
- `app/build.gradle.kts` reads `../SADHAN-signing/keystore.properties` when present and signs release with it; otherwise release builds unsigned.
- Keystore and passwords never enter the repo, chat, or this document. The certificate SHA-256 fingerprint (public) will be recorded in the release notes of v1.0.0.

## 7. Project layout

Single module (`app`), package `app.sadhan`: `MainActivity` + NavHost; one file per tool screen under `ui/tools/`; `UpdateChecker` under `update/`; `SettingsStore` under `data/`; theme under `ui/theme/`. `gradle.properties`: `-Xmx2g`, workers.max=2. Gradle wrapper 8.10.2 ships with the repo. `.gitignore`: `build/`, `.gradle/`, `local.properties`, `*.jks`, `keystore.properties`.

## 8. Release checklist (v1.0.0)

1. `./gradlew assembleRelease` succeeds.
2. `apksigner verify --print-certs` shows the SADHAN certificate.
3. Create GitHub Release `v1.0.0`, attach the APK (asset name `SADHAN-v1.0.0.apk`), release notes list the toolbox and mark YouTube **BETA**.
4. Confirm the in-app updater sees v1.0.0 as current (no update card on a fresh install of the same build).

## 9. Out of scope for v1

Widgets, notifications/reminders, cloud anything, accounts, saved notes/history of any kind, AI features, iOS/desktop versions.
