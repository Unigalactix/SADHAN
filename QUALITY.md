# SADHAN quality release — v1.2.0-rc.1

This is the implementation and evidence ledger for the professionalization
plan. It is not a claim that emulator tests replace real-device or human QA.
The stable release remains v1.1.0 until the outstanding approval gates below
are completed. No new tools, app permissions, telemetry, database, saved
history, background media service or signing identity changes are included.

## Delivered technical scope

- Tools / Media / Games / Settings destinations, shared search aliases,
  compact workspace overview and expanded-screen tab strip.
- A RAM-only workspace controller with six unique sessions, explicit
  destructive-close confirmation, immediate erasure after confirmation,
  deterministic composition cleanup and no close-tab undo retention.
- Playback intent consistent in fullscreen and normal modes; progress
  observation limited to visible, foreground, playing sessions.
- Progressive, cancellable media/document index batches. Completed indexes
  are reused within their open tool session; provider notifications, changed
  permission scope or explicit Rescan invalidate them. No index is persisted.
- Library search/sort runs away from the UI thread; partial/error and revoked
  access states are distinct. Music separates Library, Now Playing and Queue.
- Explicit browser renderer-stopped state, disabled old-view commands,
  replacement-tab recovery and renderer termination regression coverage.
  The browser remains BETA, not an anonymous or forensic-safe browser.
- Locale-aware strict numeric input, complete-token validation, Unicode
  digits, Western/Indian grouping, exact integer validation and localized
  calculator decimal keys/results. Network/JSON numbers remain locale-neutral.
- Pane-constrained game layouts with persistent primary actions, side-by-side
  short-landscape boards/controls, scrollable card boards and scalable chess
  glyphs. Rhythm Tiles has semantic cells, alternate column buttons,
  keyboard 1–4 input and an untimed practice mode.
- Procedural game audio requests transient ducking focus and releases it.
- Shared layout tokens, tabular numerical readouts and accessible disabled,
  empty, progress, error and recovery presentation.
- Checked-in unsigned build/lint/unit/device CI with downloadable evidence.
  Signing stays on the trusted local machine, outside this repository.
- Manual GitHub issue reporting with a privacy checklist; nothing is attached
  automatically.

## Complete screen/state audit and verification map

Every entry requires portrait, short-landscape, Light/Dark, keyboard where
applicable, large-text review, tab switch/close, and resize/fold continuity.
“Route” means startup/navigation coverage, not certification of every
device-specific capability. Results are recorded separately below.

| Entry | Main task | Required exceptional states and regression scope |
|---|---|---|
| Calculator | Evaluate expressions and copy results | Incomplete/invalid input, zero division, locale grouping, narrow pinned display/action, RAM lifetime |
| Tip & Split | Bill/tip/person totals | Empty/negative/non-finite values, zero/non-integral people, localized input |
| EMI | Loan totals | Zero interest, invalid principal/rate/months, bounds, overflow, localized input |
| Age & Dates | Exact age/calendar difference | Leap day, reversed dates, date-picker cancel/resize |
| Unit Converter | Six categories | Offsets, absolute zero, large values, swapping, comma-decimal input |
| Percentage | Shares/change | Zero denominator, signed changes, malformed localized numbers |
| Discount & Tax | Discount then tax | Zero/100% discount, negative/invalid rates, non-finite result |
| QR Maker | Encode/export | Unicode, oversized data, save/share denial/failure, decode saved output |
| QR Scanner | Decode camera frames | Denied/revoked permission, no camera, no result, result URL validation, hidden-tab release |
| Text Tools | Count/transform | Empty and Unicode text, whitespace, duplicate lines, copy |
| Scratchpad | Temporary writing | Long note, keyboard, switch retention, cancelled close, confirmed erasure |
| Image Compressor | Decode/resize/export | Picker cancel, unsupported/huge/rotated/transparent photo, unchanged/larger output, export failure |
| Random Picker | List/number/dice | Empty list, reversed/full integer range, Unicode/localized integers |
| Password Generator | Secure random secret | No classes, selected-class guarantees, length, sensitive clipboard, close warning |
| Stopwatch | Elapsed/laps | Start/pause/reset, 100-lap limit, hidden progress, close warning |
| Timer | Foreground countdown | Dial/presets, pause/reset/finish, muted alarm, app stop/resume, close warning |
| World Clock | Ten live zones | DST/date boundaries, narrow pane, scalable readouts |
| Weather | City/current/five days | Disabled/offline/timeout/malformed response/no cities, retry, source attribution |
| Currency | Reference conversion | Disabled/offline/invalid amount/missing rate, dated values, explicit AED peg |
| YouTube BETA | Official search/select/embed | Disabled/offline/restricted content, unsafe links, official result interception, hidden pause |
| Private Browser BETA | HTTPS private sessions | Unsafe schemes/certificates, blocked permissions/downloads, renderer death, replacement, tab cap, close cleanup |
| Flashlight | Rear torch | Denied/in-use/missing flash, hidden/app-stop off |
| Compass | Magnetic heading | Missing sensors, calibration/accuracy limits, hidden observer removal |
| Bubble Level | Tilt/bubble | Missing sensor, device accuracy, hidden observer removal |
| Music Player | Library/queue/playback | No access/partial index, changes/revocation, no rescan on unchanged resume, pause/intent/repeat/shuffle/speed, audio focus |
| Video Player | Tracks/subtitles/fullscreen | Same library states, codec errors, pending playback, external captions, ratio/volume/fullscreen, hidden pause |
| PDF Reader | Read selected document | Invalid/password-protected/non-seekable/empty PDF, page bounds, zoom, folder grant revocation |
| Snake | Eat/grow/steer | Reverse/self/wall collision, tail vacancy, pause, controls beside board |
| Solitaire | Draw-one Klondike | Deal uniqueness, legal sequences/foundations, reveal/recycle, undo/new-game |
| Chess | Local two-player | Legal moves, check/mate/stalemate, castling safety, en passant, promotion and draw rules |
| Pixel Runner | Original platformer | Jump/landing/gaps/lives/goal, held input, pause and pane-constrained controls |
| Lane Racer | Traffic dodge | Lane bounds, collision, speed, pause, visible controls in short landscape |
| Rhythm Tiles | Timed/untimed taps | Correct/wrong tile, timeout, semantic/keyboard input, practice, focus-safe audio |
| Block Drop | Falling blocks | Walls/rotation/drop, row compaction/score/level, seven-piece bag, spawn game-over |

Settings, About, Developer, offline licenses, workspace overview, confirmation
dialogs and updater are shared journeys and must be included in device runs.
Settings changes must survive an upgrade; tool contents must not survive
closing their session or process death.

## Six master-screen hand-off

Use actual emulator captures, not the supplied concept poster, as the
implementation references for:

1. Tools catalog (Light and Dark).
2. Workspace overview and destructive confirmation.
3. Calculator (compact, short landscape, companion pane).
4. Music (library, Now Playing, queue).
5. Video (normal/fullscreen/options/subtitle selection).
6. Game (portrait/landscape, controls beside board, accessible practice).

The APK is the interactive prototype. The owner-provided README overview
remains labelled concept artwork. Visual approval by a human is outstanding;
do not label these designs “approved” merely because screenshots exist.

Actual API 35 phone captures (540 x 1200, density 210, font scale 1.0):

| Master view | Light | Dark |
|---|---|---|
| Tools | [Capture](docs/assets/screenshots/home-light.png) | [Capture](docs/assets/screenshots/home-dark.png) |
| Workspace | [Capture](docs/assets/screenshots/workspace-light.png) | [Capture](docs/assets/screenshots/workspace-dark.png) |
| Calculator | [Capture](docs/assets/screenshots/calculator-light.png) | [Capture](docs/assets/screenshots/calculator-dark.png) |
| Music | [Capture](docs/assets/screenshots/music-light.png) | [Capture](docs/assets/screenshots/music-dark.png) |
| Video | [Capture](docs/assets/screenshots/video-light.png) | [Capture](docs/assets/screenshots/video-dark.png) |
| Game | [Capture](docs/assets/screenshots/game-light.png) | [Capture](docs/assets/screenshots/game-dark.png) |

These contain synthetic local media and made-up inputs only. Capture waits
for an active app window, idle accessibility events and prepared video; a
system dialog covering the app fails the capture rather than being hidden.

## Device and state matrix

| Tier | Planned evidence | Status |
|---|---|---|
| Android 15/API 35 phone emulator | Navigation, all 34 routes, themes, tab lifetime, permissions, media/PDF, browser recovery | Passed 18 cases |
| Android 15/API 35 foldable emulator | Adaptive journeys and actual closed/half-open/open continuity | Passed 17 cases, including all 3 postures |
| Large text / short landscape | 200% font, all 34 routes/Internet gates and pinned landscape game actions | Passed 2 cases; scrolling where needed |
| Minimum-supported Android 10 device | Permissions, file access, keyboard, WebView and media compatibility | Requires physical device |
| Real low-/mid-range phone | Cold/warm startup, jank, battery, thermal behavior, long library session | Requires physical device |
| Real foldable | Hinge/posture behavior, display switching, TalkBack/focus and media surfaces | Requires physical device |
| Human usability | Critical-task completion without verbal guidance | Requires participants |

## Evidence rules

Record command, source revision, APK version/signature, device/profile,
dimensions, font scale and result for every automated run. Emulator timing is
development evidence only, not certification of real-device performance.
No telemetry is collected from app users.

Proposed real-device gates: p95 cold launch below two seconds on an agreed
mid-range device; under 2% janky frames in defined flows; stable retained
memory across repeated workspace cycles; normally 48dp touch targets;
4.5:1 normal-text contrast; usable critical flows at 200% text size.

### Executed technical checks

- `testDebugUnitTest`: 31 tests, zero failures, including locale parsing,
  cancelled/stale index generations and workspace lifetime/close decisions.
- `lintRelease`: passed without a new baseline or disabled detectors.
  Existing dependency-update, legacy resource/backup-configuration and
  supplied launcher-art warnings remain (23 warnings, one information item);
  this does not claim a warning-free lint report.
- `assembleDebug assembleDebugAndroidTest`: passed.
- Phone runner: `am instrument -w -e notClass app.sadhan.FoldPostureTest
  app.sadhan.test/androidx.test.runner.AndroidJUnitRunner` returned
  `OK (18 tests)`. Profile: SadhanApi35, Android 15/API 35, x86_64,
  540 x 1200 at density 210, font 1.0.
- At font scale 2.0, `everyToolOpensAndInternetGatesWork` and
  `shortLandscapeGamesKeepBoardAndPrimaryControlsInThePane` returned
  `OK (2 tests)`. Short viewport: 1200 x 540 at density 210.
  The checked-in viewport tests now express their targets as 800 x 700dp
  (expanded) and 900 x 400dp (short landscape), and restore the original
  companion state, rather than assuming every device has the phone's density.
- Foldable runner: 17 cases returned `OK (17 tests)` on SadhanFold35,
  the official 7.6-inch foldable profile, Android 15/API 35, x86_64,
  1768 x 2208 at density 420, font 1.0. The activity and calculation survived
  CLOSED / HALF_OPENED / OPENED state changes. The two phone-specific viewport
  tests were excluded because their fixed pixels/restoration assumptions do
  not describe the foldable's density and base companion layout.
  [Actual unfolded workspace](docs/assets/screenshots/fold-workspace-dark.png).
- Browser gallery validation: 1440/768/390/320 CSS-pixel viewports, all six
  gallery images loaded with alt text, no horizontal overflow, 34/7 catalog
  filtering and all three primary download links still targeting stable.
- [Hosted quality run 44194057120](https://github.com/Unigalactix/SADHAN/actions/runs/44194057120)
  passed both jobs: unit/lint/unsigned release build and API 35 device checks.
  Its JUnit report records 19 cases, zero failures/errors and one expected
  phone-only fold-posture skip (18 passed). The downloaded device artifact
  contains all twelve required Light/Dark master-view PNGs. Missing captures
  now fail CI; AGP retains the tested app only in the disposable emulator until
  collection finishes. The hosted runner's action-runtime deprecation
  annotations are maintenance warnings, not hidden test failures.

On the shared Windows host, initial Pixel Launcher/System UI boot ANRs were
identified explicitly; covered screenshots were discarded and recaptured.
Later repeat debug-instrumentation startup attempts also hit real process
startup ANRs during a roughly 250-second emulator boot, with about 350MB of
free host RAM and failing emulator radio/Bluetooth services. Those failed
attempts are not counted as passes or an ANR-free certification. No app
timeout was relaxed to hide them. Keyboard tests put the native window into
keyboard mode before requesting control focus, rather than relying on a
previous test's input mode.

### Signed installation and development diagnostics

- `assembleRelease` succeeded. `apksigner verify --print-certs` matched the
  permanent SADHAN certificate:
  `d4c42eb688ed3de7f10aaf71226db155b34950c3762b7375dbbfbd13376ccf9e`.
- Verified package `app.sadhan`, version `1.2.0-rc.1`, code 3, minSdk 29,
  targetSdk 35 and the unchanged permission allowlist.
- Installed the public v1.1.0 signed APK, selected Dark and disabled master
  Internet, then installed the candidate in place. Both Settings survived.
- Uninstalled and freshly installed the signed candidate. About's manual
  online check displayed `You're up to date (v1.2.0-rc.1)`, without offering
  the older stable release.
- Final signed-install checks used the isolated API 35 phone with a host GPU,
  540 x 1200 physical framebuffer and density 210; optional emulator-only
  Wellbeing/System Intelligence packages were disabled. This is a different
  diagnostic fixture from the software-rendered UI acceptance runs.
- Three process-cold `am start -W` development samples were 2526, 2377 and
  2383ms. One idle snapshot reported 30,797KB PSS and 157,912KB RSS.
  This tiny, resource-constrained emulator sample does **not** establish p95,
  a physical-device launch target, jank, battery, thermal or memory-leak
  acceptance. Those gates remain open.

The final release's APK checksum is recorded in its GitHub release notes,
not embedded in this source ledger (packaged source-control metadata changes
the APK checksum when the release commit changes).

## Remaining external approvals — not completed by automation

- Physical camera optics, sensor accuracy, audio routes, codec/storage-device
  compatibility, battery and thermal measurements.
- TalkBack/Switch Access usability assessment by users, beyond semantics and
  keyboard automation.
- Moderated critical-task testing and owner sign-off of the six master views.
- Independent browser/session-isolation review before any stable-browser claim.
- Clean vector brand master review if vector source art is supplied.
- Secure signing-key backup/restore procedure on owner-approved storage.

No background service/notification permission, persisted tool history, new
minimum SDK or changed package/signing key is authorized by this release.
The UI remains English; number entry/formatting is locale-aware, not a claim
of translated app-language coverage.

## Release controls

- PR/push CI builds **unsigned** artifacts and runs tests/lint.
- Treat CI artifacts as test builds, never as the permanent signed release.
- Build/sign locally with the existing external keystore; verify the SADHAN
  certificate, versionCode and APK checksum.
- Publish a clearly identified release candidate, not latest stable, while
  external gates remain outstanding.
- The candidate uses versionCode 3. Android will not install the older stable
  v1.1.0 (code 2) over it. Returning to that older build requires uninstalling
  first, which removes Settings; future releases must use a higher code.
- Verify public download, fresh install/current-version check and in-place
  upgrade preserving Settings. Never mark “APK ready” before the file and
  certificate verification actually succeed.
