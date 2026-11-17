# SADHAN — v1 Build Specification

The complete blueprint for the first build. Written 2026-11-16. When the code lands, this file stays as the design record.

## 1. Identity

- **Name:** SADHAN (साधन — instrument/tool)
- **Package:** `app.sadhan`
- **Version:** 1.0.0 (versionCode 1)
- **Platform:** Android 10+ (minSdk 29), targetSdk 35, compileSdk 35, portrait
- **Stack:** Kotlin 2.0.21, AGP 8.7.3, Jetpack Compose (BOM 2024.12.01), navigation-compose, material-icons-extended, ZXing core 3.5.3, CameraX 1.4.1. **No other third-party SDKs** — networking via `HttpURLConnection`, JSON via `org.json` (built into Android).
- **Distribution:** signed APK on GitHub Releases of this repo. Same signing key for every release so updates install in place.
- **License:** MIT (owner-approved during implementation).
- **Approved v1 additions:** local music player, local video player, PDF reader,
  and an About developer page with portfolio, LinkedIn and GitHub links.

## 2. Non-negotiables

1. No ads, no analytics/telemetry/crash SDKs, no accounts, no login.
2. **No history:** tool state lives in memory only and is destroyed when the user leaves the tool. Only Settings (SharedPreferences) persist.
3. Internet features are individually toggleable in Settings; a master Internet switch overrides all.
4. Features that are unstable ship with a visible **BETA** tag (card chip + in-screen banner).
5. Permissions are minimal: `INTERNET`, `CAMERA`, `REQUEST_INSTALL_PACKAGES`. Camera feature declared not required.

AndroidX Core also declares the app-local signature permission
`app.sadhan.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` to protect its internal
broadcast receivers on older Android versions. This grants no additional
device capability and is not a user/runtime permission.

## 3. Design language

Pure black background (#000000), white text, one amber accent (#FFB300), dark translucent cards, 20dp corners. Home = sectioned grid of tool cards: **Everyday / Pocket / Time / Internet / Sensors / Media**. A reusable `BetaChip` composable (amber outline, "BETA") marks beta tools on their card and as a banner inside the screen.

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
| 11 | Timer | Circular drag-to-set dial + presets (1/5/10/15/30/60); animated ring; foreground alarm tone + permission-free haptic feedback on finish (subject to system sound/haptic settings). No background alarm or VIBRATE permission. |
| 12 | World Clock | Kolkata, Dubai, London, New York, Los Angeles, Singapore, Tokyo, Sydney, Berlin, Toronto (`java.time`) |
| 13 | Weather | Open-Meteo geocoding + forecast (no key): current temp, condition from weather code, humidity/wind, 5-day min/max |
| 14 | Currency | `api.frankfurter.app/latest` (no key); USD, EUR, GBP, INR, AED, JPY, AUD, CAD, SGD, CHF, CNY, NZD, ZAR, BRL, KRW; rates in memory only. Frankfurter does not supply AED: AED conversions are labelled indicative estimates at the fixed 3.6725 AED/USD peg. Rates include their reference date. |
| 15 | YouTube **BETA** | BetaChip + banner "Beta — playback can be unstable. Uses YouTube's official embed." Paste link/ID → `youtube-nocookie.com/embed/<id>` in a WebView; search box → `m.youtube.com/results` in the same WebView; JS enabled on that WebView only |
| 16 | Flashlight | `CameraManager` torch on the back camera with flash |
| 17 | Compass | Rotation-vector sensor (fallback accel+magnetometer) → heading + rotating dial |
| 18 | Level | Accelerometer → bubble offset + X/Y tilt degrees |
| 19 | Image Compressor | Photo picker → original size, quality slider 10–100, JPEG compress, result size, Save to Pictures/SADHAN or Share. Orientation is decoded by the platform; large images fit within 2048 × 2048, metadata is removed, transparency becomes black. |
| 20 | Settings | Master Internet toggle; Weather / Currency / YouTube (Beta) / Update-check toggles (SharedPreferences) |
| 21 | About | Version, "No ads. No tracking. No accounts. Nothing you type in a tool is saved.", Check-for-updates button, link to this repo |
| 22 | Music Player | Android document picker + MediaPlayer, audio focus, play/pause/restart/seek. Local formats supported by the device; pauses in background, releases on leaving. No scanned library, playlists or history. |
| 23 | Video Player | Android document picker + VideoView with native playback/seek controls. Pauses in background, releases on leaving; codec support depends on device. |
| 24 | PDF Reader | Android document picker + PdfRenderer, page navigation and pinch zoom/pan. Local, seekable, non-password-protected PDFs; no copies, bookmarks or recents. |
| 25 | About developer | Rajesh Kodaganti (Rugved), public GitHub bio, portfolio, LinkedIn and GitHub. Reachable from Home and About. |

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

Single Gradle module (`app`), internally modular by responsibility:
`MainActivity` + NavHost; one file per tool under `ui/tools/`; shared UI and
screen metadata under `ui/`; theme under `ui/theme/`; pure calculations,
conversion, image export and HTTP under `core/`; preferences and network
repositories under `data/`; local document handling under `media/`; version
comparison, checks, download verification and install under `update/`.
No tool state is shared across screen visits.

In addition to the specified dependencies, AndroidX Core, Activity Compose
and Lifecycle Runtime Compose provide FileProvider, file/permission activity
contracts and lifecycle-safe cleanup. These are Android framework integration
libraries, not telemetry SDKs. JUnit and AndroidX Compose test libraries are
test-only. No additional media, PDF or networking SDKs are used.

`gradle.properties`: `-Xmx2g`, workers.max=2. Gradle wrapper 8.10.2 ships with
the repo. `.gitignore`: `build/`, `.gradle/`, `local.properties`, `*.jks`,
`keystore.properties`. Exported images and downloaded APKs are explicit
user actions; temporary sharing/update files use app cache, not tool history.

## 8. Release checklist (v1.0.0)

1. `./gradlew assembleRelease` succeeds.
2. `apksigner verify --print-certs` shows the SADHAN certificate.
3. Create GitHub Release `v1.0.0`, attach the APK (asset name `SADHAN-v1.0.0.apk`), release notes list the toolbox and mark YouTube **BETA**.
4. Confirm the in-app updater sees v1.0.0 as current (no update card on a fresh install of the same build).

## 9. Out of scope for v1

Widgets, notifications/reminders, cloud anything, accounts, saved notes/history of any kind, AI features, iOS/desktop versions.

## 10. v1.1.0 — owner-requested workspace and premium upgrade

This section supersedes conflicting v1.0 UI/state details above. Identity stays
`app.sadhan`, minSdk 29, target/compileSdk 35; versionCode is 2. Sign with the
same permanent SADHAN certificate and publish `SADHAN-v1.1.0.apk`.

### Appearance and adaptive workspace

- System is the default theme; Light and Dark are saved Settings.
- Follow the supplied visual references: frosted light/navy glass surfaces,
  orange primary actions, cyan readouts, readable contrast and restrained
  transitions. No external fonts/images/telemetry dependencies.
- Remove the portrait lock. Handle window configuration changes in the
  Compose activity so visible tool state stays in RAM during resizing,
  rotation and fold/unfold. WindowManager reports separating hinges; controls
  stay in a usable pane. Larger screens offer a companion tool.
- Up to six unique tool/game tabs remain composed in memory. Switching tabs
  keeps work; closing a tab destroys it. No workspace restore, saved queues,
  scores, notes, browser history or document index. Hidden camera/sensor/video
  sessions stop; games pause. Music may continue between tabs only while the
  app remains foregrounded. No background-service permissions were approved.

### Local media and documents

- Media3 ExoPlayer + PlayerView replace the basic platform wrappers for queue
  controls, previous/next, shuffle, repeat off/all/one, speed, seeking,
  embedded audio/subtitle tracks, external subtitles, ratio and fullscreen.
  SRT/VTT/SSA/ASS/TTML support and codec support depend on the file/decoder.
  This does not claim complete VLC feature/codec parity.
- The owner explicitly requested storage scans. Add runtime
  `READ_MEDIA_AUDIO`, `READ_MEDIA_VIDEO`, `READ_MEDIA_VISUAL_USER_SELECTED`
  and legacy `READ_EXTERNAL_STORAGE` with maxSdk 32. File pickers still work
  without these. Do not request MANAGE_EXTERNAL_STORAGE.
- PDF/document scans use an explicitly chosen Storage Access Framework tree.
  Persist its read grant and URI as a Settings choice only. Rescan metadata
  into RAM. Other document formats are opened by a chosen installed viewer.
- Media3 1.8.0 is a justified AndroidX dependency for robust local playback and
  track selection; it compiles against SDK 35. WindowManager 1.3.0 provides
  hinge awareness. No Media3 service/session/network or telemetry SDK is added.
  Remove the library's optional ACCESS_NETWORK_STATE permission; playback
  accepts only local content/file URIs.

### YouTube and private browser

- YouTube keeps a BETA chip/banner, separate Search/Paste controls, official
  mobile-site results and in-app result selection into the official embed.
- The owner requested an incognito-default browser. Its BETA implementation
  supports up to eight in-memory HTTPS tabs, back/forward/reload, disabled
  cookies/DOM/form storage, disabled local-file/content access, blocked
  website device permissions and downloads, and cleanup when its tool tab
  closes. No persisted tabs/history.
- State clearly that websites can see IP/fingerprint information and that
  WebView/Android operational data may remain. Do not promise anonymity,
  forensic erasure or full Chrome incognito isolation. Browser has its own
  Internet toggle beneath the master switch.

### Additional utilities and games

There are now 27 tools and seven original games. Add Percentage, Discount &
Tax, Random Picker/Dice and Secure Password Generator (SecureRandom, 12–64
characters, at least one per chosen class, sensitive clipboard flag).

Add Snake, draw-one Klondike Solitaire, local two-player legal Chess, Pixel
Runner, Lane Racer, Rhythm Tiles and Block Drop. Use only original procedural
visuals/audio and generic mechanics: no ROMs or copied Mario/Tetris branding,
characters, artwork or music. Scores/progress live only in their open tab.
Game logic belongs in pure Kotlin `games/`; native screens in `ui/games/`.
