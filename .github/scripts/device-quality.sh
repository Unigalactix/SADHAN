#!/usr/bin/env bash
set -euo pipefail

result=0
adb shell wm density 210
adb shell svc power stayon true
./gradlew connectedDebugAndroidTest --no-daemon --console=plain || result=$?
mkdir -p app/build/outputs/quality-evidence
if ! adb pull /sdcard/Android/data/app.sadhan/files/quality-evidence app/build/outputs/quality-evidence; then
  echo "::warning::Master-view screenshots were not available; inspect the device test report."
fi
exit "$result"
