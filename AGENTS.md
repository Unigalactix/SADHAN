# AGENTS.md — SADHAN

Instructions for any coding agent (or human) continuing this project. Read this file first, then `README.md` (the product) and `SPEC.md` (the complete v1 blueprint — it is the source of truth for what to build).

## Project status

- **Design: complete.** README + SPEC were finalized 2026-11-16.
- **Code: implemented.** v1.1.0 includes 27 utilities, seven original retro games,
  RAM-only workspace tabs, media/document libraries, a developer page, MIT
  licensing and a GitHub Pages landing page. Read `SPEC.md` for current details.
- **Repo:** `github.com/Unigalactix/SADHAN` (public). Owner: Rajesh Kodaganti (Unigalactix).
- **Distribution:** signed APK attached to GitHub Releases. No Play Store.

## What SADHAN is

An all-in-one daily toolbox for Android (Kotlin + Jetpack Compose): 19 small tools (calculator, tip/split, EMI, age/dates, unit converter, QR maker + scanner, text tools, self-erasing scratchpad, stopwatch, dial timer, world clock, weather, currency, flashlight, compass, level, image compressor), plus Settings and About. Built in the style of the AETHER music app: native, one design language, distributed as a signed APK via GitHub Releases with in-app update checks.

The initial v1.0 owner-approved additions brought the total to 22 tools: local music player,
video player and PDF reader. There is also an About developer screen with
portfolio/LinkedIn/GitHub links. The four supplied brand variants are used for
the adaptive launcher, splash screen, themed icon and static website in `docs/`.
GitHub Pages publishes `main` / `docs`; the code is MIT-licensed.

## Golden rules — do not break these

1. **No ads, no analytics/telemetry/crash SDKs, no accounts, no login.** Ever. If a library phones home, it does not go in.
2. **No saved history.** Owner-approved v1.1 workspace tabs keep state only in
   RAM until the tab closes; nothing is restored after process death. The
   ONLY persisted data is Settings (SharedPreferences), including the explicitly
   chosen document-library folder. No databases, saved inputs, indexes or scores.
3. **Minimal permissions:** `INTERNET`, `CAMERA`, `REQUEST_INSTALL_PACKAGES`,
   plus owner-approved opt-in media-library reads (`READ_MEDIA_AUDIO`,
   `READ_MEDIA_VIDEO`, `READ_MEDIA_VISUAL_USER_SELECTED`, and
   `READ_EXTERNAL_STORAGE` maxSdk 32). No unrestricted all-files access,
   background service, notification or other permission without approval.
4. **Dependency discipline:** Kotlin 2.0.21, AGP 8.7.3, Compose BOM 2024.12.01, navigation-compose, material-icons-extended, ZXing core 3.5.3, CameraX 1.4.1. Networking via `HttpURLConnection`, JSON via `org.json`. Anything beyond this list needs a strong reason stated in the PR/commit.
5. **Beta means tagged.** Unstable features ship with a visible `BETA` chip on the home card and a beta banner in the screen. In v1, that is the YouTube tool (official `youtube-nocookie` embed + `m.youtube.com` search in a WebView).
6. **Identity is frozen:** package `app.sadhan`, minSdk 29, targetSdk/compileSdk 35. Renaming the package or changing the signing key breaks updates for every installed user.

v1.1 additions: 27 utilities + seven original offline games, six in-memory
workspace tabs, Light/Dark/System themes, foldable companion panes, Media3
local playback, opt-in libraries and a BETA private-session browser. The
owner's newer navy/frosted-light, orange/cyan glass references supersede the
original black/amber-only appearance. See SPEC §10 for exact limits.

## Environment setup

- JDK 17, Android SDK with platform 35 + build-tools 35.0.0.
- `local.properties` → `sdk.dir=<your Android SDK path>` (never commit this file).
- Build: `./gradlew assembleRelease` (Gradle wrapper 8.10.2 — generate it with `gradle wrapper --gradle-version 8.10.2` if missing).

## Signing — read carefully

- Release builds are signed with a keystore kept **outside this repo** at `../SADHAN-signing/` (`sadhan-release.jks` + `keystore.properties` with `storeFile/storePassword/keyAlias/keyPassword`).
- `app/build.gradle.kts` must read that properties file when it exists and sign release builds with it; without it, release builds unsigned (fine for development).
- **If no keystore exists yet, generate it once:**

  ```sh
  keytool -genkeypair -v -keystore ../SADHAN-signing/sadhan-release.jks \
    -alias sadhan -keyalg RSA -keysize 2048 -validity 10000 \
    -dname "CN=SADHAN, O=Unigalactix"
  ```

- ⚠️ **The first key that ships is permanent.** Every future release must use the same key or existing users cannot update in place. Back the keystore + password up somewhere safe. Never commit the keystore or its password to this repo, and never print the password in logs, issues, or chat.

## The updater contract (do not change casually)

The app self-updates AETHER-style, and it depends on exact conventions:

- On start (if enabled in Settings) the app GETs `https://api.github.com/repos/Unigalactix/SADHAN/releases/latest`, reads `tag_name`, strips the leading `v`, and semver-compares with `BuildConfig.VERSION_NAME`.
- **Release tags must be `v<semver>`** (e.g. `v1.0.0`, `v1.1.0`).
- **The APK asset on each release must be named `SADHAN-v<semver>.apk`** and be the release-signed build. The updater picks the `.apk` asset's `browser_download_url`, downloads it to `cacheDir/updates/`, and installs via a FileProvider `ACTION_VIEW` intent (mime `application/vnd.android.package-archive`). If `canRequestPackageInstalls()` is false, send the user to the unknown-sources settings screen first.
- `versionCode` must increase with every release; `versionName` must match the tag.

## Code conventions

- Single module (`app`), package `app.sadhan`: `MainActivity` + NavHost; one file
  per tool under `ui/tools/`, games under `ui/games/`, pure game rules under
  `games/`; updater under `update/`; settings under `data/`; themes under
  `ui/theme/` (frosted light/navy glass, orange/cyan accents, 20–28dp corners).
- State: composable/tab-scoped state lives **only until that tab closes**.
  Switching tabs must preserve it in RAM. No `savedStateHandle` persistence
  for tool inputs, no DataStore/Room for tool data.
- Internet tools (Weather via Open-Meteo, Currency via Frankfurter — both keyless) must check the Settings toggles first and show "Turned off in Settings" when disabled.
- Every screen from SPEC §4 must exist and work; if you must simplify one, say so in the commit message and in the release notes.

## Definition of done — v1.0.0 (SPEC §8)

1. `./gradlew assembleRelease` succeeds.
2. `apksigner verify --print-certs` on the APK shows the SADHAN certificate; note the SHA-256 fingerprint in the release notes (the fingerprint is public; the password is not).
3. GitHub Release `v1.0.0` created with `SADHAN-v1.0.0.apk` attached; notes list the toolbox and mark YouTube **BETA**.
4. Fresh install of that APK shows no "update available" card (updater sees itself as current).

## Out of scope for v1

Widgets, notifications/reminders, cloud sync, accounts, saved notes/history of any kind, AI features, other platforms. See SPEC §9.

## Working agreements

- Commit messages: imperative, prefixed by area when useful (e.g. `tools: add QR scanner`, `updater: handle blocked installs`).
- Keep `README.md` and `SPEC.md` truthful: if implementation diverges from SPEC, update SPEC in the same change and flag it.
- When in doubt about scope, permissions, or the golden rules: stop and ask the owner rather than guessing.


## Alawyas Commit and push to Unigalactix/SADHAN as Unigalactix github account.