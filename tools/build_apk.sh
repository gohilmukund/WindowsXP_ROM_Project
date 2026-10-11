#!/usr/bin/env bash
set -e

# Universal Android APK Build Script for Windows XP Suite
# Usage: ./build_apk.sh <app_dir> <output_apk>

APP_DIR="$1"
OUTPUT_APK="$(readlink -f "$2" 2>/dev/null || realpath "$2" 2>/dev/null || echo "$PWD/$2")"

if [ -z "$APP_DIR" ] || [ -z "$OUTPUT_APK" ]; then
    echo "Usage: $0 <app_dir> <output_apk>"
    exit 1
fi

ANDROID_JAR="/usr/local/lib/android.jar"
R8_JAR="/usr/local/lib/r8.jar"
KEYSTORE="/usr/local/lib/debug.keystore"

TOOLS_DIR="$(cd "$(dirname "$0")" && pwd)"
if [ -f "$TOOLS_DIR/run_tests.sh" ] && [[ "$APP_DIR" == *"LunaLauncher"* ]]; then
    echo "[0/5] Running automated regression tests..."
    "$TOOLS_DIR/run_tests.sh"
fi

cd "$APP_DIR"

rm -rf build
mkdir -p build/gen build/bin/classes build/dist

echo "[1/5] Compiling resources with aapt..."
if [ -d "res" ]; then
    aapt package -f -m -J build/gen -M AndroidManifest.xml -S res -I "$ANDROID_JAR"
else
    aapt package -f -m -J build/gen -M AndroidManifest.xml -I "$ANDROID_JAR"
fi

echo "[2/5] Compiling Java sources with javac..."
JAVA_FILES=$(find build/gen src -name "*.java" 2>/dev/null)
javac -source 1.8 -target 1.8 -bootclasspath "$ANDROID_JAR" -cp "$ANDROID_JAR" -d build/bin/classes $JAVA_FILES

echo "[3/5] Dexing bytecodes with D8..."
java -cp "$R8_JAR" com.android.tools.r8.D8 --min-api 19 --lib "$ANDROID_JAR" --output build/bin $(find build/bin/classes -name "*.class")

echo "[4/5] Packaging APK with aapt..."
if [ -d "lib" ]; then
    cp -r lib build/bin/
fi
cd build/bin
if [ -d "../../res" ]; then
    aapt package -f -M ../../AndroidManifest.xml -S ../../res -I "$ANDROID_JAR" -F raw.apk .
else
    aapt package -f -M ../../AndroidManifest.xml -I "$ANDROID_JAR" -F raw.apk .
fi

if [ -d "../../assets" ]; then
    aapt add raw.apk assets/* 2>/dev/null || true
fi
cd ../..

echo "[5/5] Aligning and signing APK..."
zipalign -f -p 4 build/bin/raw.apk build/dist/aligned.apk
cp build/dist/aligned.apk "$OUTPUT_APK"
jarsigner -sigalg SHA1withRSA -digestalg SHA1 -keystore "$KEYSTORE" -storepass android -keypass android "$OUTPUT_APK" androiddebugkey > /dev/null

echo "Build successful: $OUTPUT_APK"
