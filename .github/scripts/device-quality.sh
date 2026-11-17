#!/usr/bin/env bash
set -euo pipefail

result=0
adb shell wm density 210
adb shell svc power stayon true
./gradlew connectedDebugAndroidTest -Pandroid.injected.androidTest.leaveApksInstalledAfterRun=true --no-daemon --console=plain || result=$?
mkdir -p app/build/outputs/quality-evidence
if ! adb pull /sdcard/Android/data/app.sadhan/files/quality-evidence/. app/build/outputs/quality-evidence; then
  echo "::error::Master-view screenshots could not be collected."
  result=1
fi
for theme in light dark; do
  for view in home workspace calculator music video game; do
    if [[ ! -s "app/build/outputs/quality-evidence/$view-$theme.png" ]]; then
      echo "::error::Missing master-view evidence: $view-$theme.png"
      result=1
    fi
  done
done
exit "$result"
