# SADHAN

**An all-in-one daily toolbox for Android.** साधन — *instrument, tool.*

Every small task you reach for a different app to do, in one place — built the way a fine tool is built: nothing there for show, nothing watching you.

> **Status:** Design complete, implementation in progress. The v1.0.0 APK will be published on [Releases](https://github.com/Unigalactix/SADHAN/releases) when ready. The full build specification lives in [SPEC.md](SPEC.md).

## Download

1. Download the latest **SADHAN APK** from [Releases](https://github.com/Unigalactix/SADHAN/releases/latest).
2. Open it on your phone. Android asks once to allow installs from your browser or file manager.
3. Open SADHAN and use any tool. No sign-up. Ever.

Needs Android 10 or later. Every release is signed with the same key, so new versions install over old ones — and the app itself tells you when an update is out (see *Updates* below).

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

Tool inputs — your numbers, texts, photos, QR codes — never leave the phone.

## Building

Native Kotlin + Jetpack Compose, single module. JDK 17, Android SDK 35.

```sh
./gradlew assembleRelease
```

Release signing uses a `keystore.properties` kept **outside** the repo (see SPEC.md); without one the APK builds unsigned. The full specification — screens, updater design, permissions, design language — is in [SPEC.md](SPEC.md).

## Principles

Small tasks, done properly, with no AI and no agenda. Quality is measured, never claimed. If a tool can't do something, it says so.

## Licence

To be decided with the first release. The toolbox concept and this design are © 2026 Rajesh Kodaganti.
