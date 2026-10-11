#!/usr/bin/env bash
set -e
PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$PROJECT_ROOT"

ANDROID_JAR="${ANDROID_JAR:-/usr/local/lib/android.jar}"
if [ ! -f "$ANDROID_JAR" ]; then
  echo "Error: android.jar not found at $ANDROID_JAR" >&2
  exit 1
fi

rm -rf build/test_classes && mkdir -p build/test_classes

# Generate R.java unconditionally for LunaLauncher
mkdir -p apps/LunaLauncher/build/gen
aapt package -f -m -J apps/LunaLauncher/build/gen -M apps/LunaLauncher/AndroidManifest.xml -S apps/LunaLauncher/res -I "$ANDROID_JAR"

# Compile source classes needed for test
javac -source 1.8 -target 1.8 -Xlint:-options -cp "$ANDROID_JAR:build/test_classes" -d build/test_classes \
  apps/LunaLauncher/build/gen/com/windows/xp/launcher/R.java \
  apps/LunaLauncher/src/com/windows/xp/launcher/theme/ThemeManager.java \
  tools/tests/*.java

java -cp "build/test_classes:$ANDROID_JAR" tools.tests.TestRunner
