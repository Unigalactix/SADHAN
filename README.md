# SADHAN

**An all-in-one daily toolbox for Android.** साधन — *instrument, tool.*

Every small task you reach for a different app to do, in one place — built the way a fine tool is built: nothing there for show, nothing watching you.

> **v1.0.0:** 22 tools, a native Android app, and a signed APK distributed on
> [Releases](https://github.com/Unigalactix/SADHAN/releases/latest).
> The build specification and implementation notes live in [SPEC.md](SPEC.md).

## Download

1. Download the latest **SADHAN APK** from [Releases](https://github.com/Unigalactix/SADHAN/releases/latest).
2. Open it on your phone. Android asks once to allow installs from your browser or file manager.
3. Open SADHAN and use any tool. No sign-up. Ever.

Needs Android 10 or later. Every release is signed with the same key, so new versions install over old ones — and the app itself tells you when an update is out (see *Updates* below).

**Website:** [unigalactix.github.io/SADHAN](https://unigalactix.github.io/SADHAN/)
— responsive landing page, tool catalog, developer links, and latest-release
download buttons.

## Zero nonsense

- **No ads.** Not one ad SDK.
- **No tracking, no analytics.** No telemetry, no crash-reporting phoning home.
- **No account and no server of ours.** There is nothing to sign up for.
- **No history.** Every tool forgets everything the moment you leave it. The only thing the app remembers is your Settings choices.
- **No paywalls.** Everything is there from the first launch.

## The toolbox (v1)

**Everyday**
- Calculator — full expression evaluator, session-only history
- Tip & Split — bill, tip %, per-person totals
- EMI Calculator — monthly EMI, total interest, total payable
- Age & Dates — exact age, days between dates
- Unit Converter — length, weight, temperature, data, area, speed

**Pocket**
- QR Maker — text to QR, share or save
- QR Scanner — camera decoding, copy or open the result
- Text Tools — word/char counts, case changer, duplicate-line remover
- Scratchpad — a note that erases itself when you leave
- Image Compressor — shrink a photo, see before/after sizes

**Time**
- Stopwatch — with laps
- Timer — a needle dial you drag, not a number pad
- World Clock — ten cities, live

**Internet** *(each can be switched off in Settings)*
- Weather — Open-Meteo: no key, no account
- Currency — Frankfurter rates for 15 major currencies
- YouTube — **BETA** *(see below)*

**Sensors**
- Flashlight, Compass, Bubble Level

**Media**
- Music Player — choose local audio, play/pause and seek
- Video Player — local video with Android playback controls
- PDF Reader — page navigation, pinch zoom and pan

**About developer** — Rajesh Kodaganti (Rugved), with links to
[portfolio](https://rajeshkodaganti.com),
[LinkedIn](https://www.linkedin.com/in/rajesh-kodaganti-323118215/) and
[GitHub](https://github.com/Unigalactix). Available from Home and About.

Local media uses the system file picker without storage permissions. Playback
pauses in the background and stops when you leave the tool. There are no
playlists, recent files or saved positions. PDF support is for local, seekable,
non-password-protected files. Media codecs depend on Android/device support.

The timer is foreground-only, with an alarm tone and system haptic feedback;
it does not schedule background alarms or notifications. Currency values are
dated reference rates, not live trading quotes. AED uses an explicitly labelled
USD-peg estimate because Frankfurter does not supply an AED rate.

## Beta features

Features tagged **BETA** in the app are experimental and may be unstable — the tag means exactly that, so you always know which parts are still settling.

- **YouTube (Beta)** — paste a link or search, and watch in a clean in-app player using YouTube's official embed. Playback can break when YouTube changes things; that's why it's beta.

## Updates — the AETHER method

SADHAN checks this repo's Releases when it starts. If a newer version exists, it shows an **Update available** card; one tap downloads the new APK and hands it to Android's installer. No Play Store, no server, no account — GitHub Releases is the whole update system. You can switch update checks off in Settings.

## Privacy

Apart from the features you explicitly use, SADHAN makes only these requests:

| For | Asks | Sends |
|---|---|---|
| Weather | Open-Meteo | The city name you typed |
| Currency rates | Frankfurter | Nothing about you |
| YouTube (Beta) | YouTube's official embed | Whatever YouTube's own player sends |
| Update check | GitHub's release list for this repo | Nothing: it reads the newest release's number |

Your calculations, notes, photos and QR content are processed on the phone.
City searches go to Open-Meteo; YouTube searches and video links go to YouTube.
Sharing or saving an image happens only when you choose it.

## Building

Native Kotlin + Jetpack Compose, single module. JDK 17, Android SDK 35.

```sh
./gradlew assembleRelease
```

Release signing uses a `keystore.properties` kept **outside** the repo (see SPEC.md); without one the APK builds unsigned. The full specification — screens, updater design, permissions, design language — is in [SPEC.md](SPEC.md).

On Windows, run `.\gradlew.bat assembleRelease`. Create an ignored
`local.properties` with `sdk.dir=<Android SDK path>` first.

Validation:

```sh
./gradlew testDebugUnitTest lintRelease
./gradlew connectedDebugAndroidTest
```

The device tests need a connected Android 10+ device or emulator. Source is
organized into per-tool screens with shared `core`, `data`, `media`, `ui/theme`
and `update` packages; no database or tool persistence is used.

The initial release was validated with 10 JVM tests, release lint, and eight
device tests on Android 15/API 35. Device checks cover all tool routes, Internet
gates, state erasure, Settings persistence, developer links and beta labels,
QR export/decoding, PDF rendering, and local audio/video playback.
Physical sensors, camera optics, and all device-specific codecs still need
on-device evaluation; YouTube remains beta.

See [third-party notices](THIRD_PARTY_NOTICES.md) for libraries and service
attributions.

### Landing page

The static site is in [docs/](docs/), with no framework, build step, external
fonts, analytics, or package dependencies. GitHub Pages publishes from the
`main` branch, `/docs` folder. Its download buttons look up the latest release;
if GitHub's API is unavailable, they still link to the Releases page.

Brand assets use the four owner-supplied variants: light launcher, dark web
icon, monochrome splash mark, and full website wordmark.

## Principles

Small tasks, done properly, with no AI and no agenda. Quality is measured, never claimed. If a tool can't do something, it says so.

## Licence

[MIT](LICENSE). Copyright © 2026 Rajesh Kodaganti.
