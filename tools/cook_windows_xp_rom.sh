#!/usr/bin/env bash
# ==============================================================================
# Windows XP Luna Custom ROM - Master Chef & Packaging Script
# Target: Samsung Galaxy Tab 3 7.0 Wi-Fi (SM-T210 / SM-T210R, lt02wifi)
# OS Base: Android 4.4.2 KitKat (Stock T210RUES0CQA1)
# ==============================================================================
set -euo pipefail

echo "=========================================================="
echo " Starting Windows XP Luna Master ROM Cooking & Packaging  "
echo "=========================================================="

WORK_DIR="/home/xp_builder"
MNT="$WORK_DIR/mnt_system"
SRC_DIR="/mnt/c/Users/gohil/Downloads/Samsung_Tab3_SM-T210_Tools/WindowsXP_ROM_Project"
TOOLS_DIR="/mnt/c/Users/gohil/Downloads/Samsung_Tab3_SM-T210_Tools"
ROMS_DIR="$TOOLS_DIR/ROMS"
OUT_DIR="$WORK_DIR/dist"
WINDOWS_DIST="$SRC_DIR/dist"

mkdir -p "$MNT" "$OUT_DIR" "$WINDOWS_DIST"

cleanup() {
    if mountpoint -q "$MNT"; then
        echo "[!] Cleanup: unmounting $MNT..."
        umount "$MNT" || true
    fi
}
trap cleanup EXIT

# 1. Mount system.raw.img
echo "[1/8] Mounting raw system ext4 image..."
if mountpoint -q "$MNT"; then
    umount "$MNT"
fi
mount -o loop "$WORK_DIR/system.raw.img" "$MNT"
echo "  Initial mounted system disk usage:"
df -h "$MNT"

# 2. Debloat - Purge Defunct GApps & TouchWiz Bloat
echo "[2/8] Debloating defunct Google Apps and TouchWiz bloatware..."
GAPPS_PRIV=(
    GmsCore GoogleLoginService GoogleServicesFramework Phonesky Velvet
    GoogleBackupTransport GoogleFeedback GooglePartnerSetup
)
for app in "${GAPPS_PRIV[@]}"; do
    rm -f "$MNT/priv-app/${app}.apk" "$MNT/priv-app/${app}.odex"
done

GAPPS_APP=(
    Books Drive Gmail2 Hangouts Magazines Maps Music2
    PlayGames PlusOne Street Videos YouTube
)
for app in "${GAPPS_APP[@]}"; do
    rm -f "$MNT/app/${app}.apk" "$MNT/app/${app}.odex"
done

TOUCHWIZ_PRIV=(
    SecLauncher2 sCloudBackupApp sCloudDataRelay sCloudDataSync sCloudQuotas
    sCloudSyncCalendar sCloudSyncContacts sCloudSyncSNote
    SamsungApps SamsungAppsUNA3 SamsungLink S-Voice KLMSAgent
    SyncmlDM SPenSdkPlatform
)
for app in "${TOUCHWIZ_PRIV[@]}"; do
    rm -f "$MNT/priv-app/${app}.apk" "$MNT/priv-app/${app}.odex"
done

TOUCHWIZ_APP=(
    SecBrowser ChatON AllshareFileShare AllshareFileShareClient
    AllshareMediaServer AllshareMediaShare YahooNews YahooStock
    PolarisViewer TripAdvisor Dropbox Flipboard
)
for app in "${TOUCHWIZ_APP[@]}"; do
    rm -f "$MNT/app/${app}.apk" "$MNT/app/${app}.odex"
done

echo "  Debloating complete. Cleaned packages."

# 3. Inject Windows XP Luna Suite
echo "[3/8] Injecting Windows XP Luna suite into /system/priv-app..."
APPS_DIR="$SRC_DIR/apps"
declare -A PRIV_APPS=(
    ["LunaLauncher.apk"]="$APPS_DIR/LunaLauncher/LunaLauncher.apk"
    ["InternetExplorer.apk"]="$APPS_DIR/InternetExplorer/InternetExplorer.apk"
    ["WindowsMediaPlayer.apk"]="$APPS_DIR/WindowsMediaPlayer/WindowsMediaPlayer.apk"
    ["MSPaint.apk"]="$APPS_DIR/MSPaint/MSPaint.apk"
    ["Notepad.apk"]="$APPS_DIR/Notepad/Notepad.apk"
    ["Minesweeper.apk"]="$APPS_DIR/Minesweeper/Minesweeper.apk"
    ["Solitaire.apk"]="$APPS_DIR/Solitaire/Solitaire.apk"
    ["Fotoo.apk"]="$APPS_DIR/Fotoo/Fotoo.apk"
    ["WindowsExplorer.apk"]="$APPS_DIR/WindowsExplorer/WindowsExplorer.apk"
)

for target in "${!PRIV_APPS[@]}"; do
    src="${PRIV_APPS[$target]}"
    if [ -f "$src" ]; then
        cp -f "$src" "$MNT/priv-app/$target"
        chmod 644 "$MNT/priv-app/$target"
        chown root:root "$MNT/priv-app/$target"
        echo "  Injected priv-app: $target"
    else
        echo "  [ERROR] Source APK not found: $src" >&2
        exit 1
    fi
done

echo "[4/8] Injecting Retro Games & Essential Apps into /system/app..."
declare -A SYSTEM_APPS=(
    ["SpaceCadetPinball.apk"]="$TOOLS_DIR/pinball-v2.5.apk"
    ["Firefox.apk"]="$TOOLS_DIR/Firefox-68.11.0-arm.apk"
    ["NewPipeLegacy.apk"]="$TOOLS_DIR/NewPipeLegacy-v0.20.8.apk"
)

for target in "${!SYSTEM_APPS[@]}"; do
    src="${SYSTEM_APPS[$target]}"
    if [ -f "$src" ]; then
        cp -f "$src" "$MNT/app/$target"
        chmod 644 "$MNT/app/$target"
        chown root:root "$MNT/app/$target"
        echo "  Injected app: $target"
    else
        echo "  [ERROR] Source APK not found: $src" >&2
        exit 1
    fi
done

# 4. Inject Boot Animation & Audio
echo "[5/8] Injecting Windows XP Boot Animation and PowerOn audio..."
rm -f "$MNT/media/bootsamsung.qmg" "$MNT/media/bootsamsungloop.qmg" "$MNT/media/shutdown.qmg"
cp -f "$SRC_DIR/assets/bootanimation/bootanimation.zip" "$MNT/media/bootanimation.zip"
chmod 644 "$MNT/media/bootanimation.zip"
chown root:root "$MNT/media/bootanimation.zip"

mkdir -p "$MNT/media/audio/ui"
cp -f "$SRC_DIR/assets/audio/PowerOn.ogg" "$MNT/media/audio/ui/PowerOn.ogg"
chmod 644 "$MNT/media/audio/ui/PowerOn.ogg"
chown root:root "$MNT/media/audio/ui/PowerOn.ogg"

# 5. Apply build.prop Tweaks
echo "[6/8] Applying build.prop optimizations and branding..."
sed -i '/qemu.hw.mainkeys/d' "$MNT/build.prop"
sed -i '/persist.sys.theme/d' "$MNT/build.prop"
sed -i '/ro.build.display.id=/c\ro.build.display.id=Windows XP Professional Luna Edition (v1.0)' "$MNT/build.prop"
sed -i '/ro.product.model=/c\ro.product.model=Galaxy Tab 3 - Windows XP Edition' "$MNT/build.prop"

cat "$SRC_DIR/assets/tweaks/build.prop.append" >> "$MNT/build.prop"
chmod 644 "$MNT/build.prop"
chown root:root "$MNT/build.prop"

echo "  Updated system disk usage:"
df -h "$MNT"
sync

# 6. Build TWRP Flashable ZIP Package
echo "[7/8] Staging and creating TWRP Flashable ZIP..."
TWRP_STAGE="$WORK_DIR/twrp_staging"
rm -rf "$TWRP_STAGE"
mkdir -p "$TWRP_STAGE/system" "$TWRP_STAGE/META-INF/com/google/android"

echo "  Extracting reference recovery binary..."
unzip -q -o "$ROMS_DIR/Stock_210_kernel.zip" "META-INF/com/google/android/update-binary" -d "$TWRP_STAGE"

echo "  Copying boot kernel..."
cp -f "$WORK_DIR/boot.img" "$TWRP_STAGE/boot.img"

echo "  Copying complete cooked system tree..."
cp -a "$MNT/." "$TWRP_STAGE/system/"

cat << 'EOF' > "$TWRP_STAGE/META-INF/com/google/android/updater-script"
ui_print(" ");
ui_print("============================================");
ui_print(" Windows XP Professional Luna Edition v1.1  ");
ui_print("      Samsung Galaxy Tab 3 7.0 SM-T210      ");
ui_print("============================================");
ui_print(" ");
show_progress(0.200000, 10);
ui_print("Formatting /system partition...");
format("ext4", "EMMC", "/dev/block/mmcblk0p14", "0", "/system");
ui_print("Mounting /system...");
run_program("/sbin/busybox", "mount", "/system");
show_progress(0.700000, 60);
ui_print("Installing Windows XP Luna System Files...");
package_extract_dir("system", "/system");
set_perm_recursive(0, 0, 0755, 0644, "/system");
set_perm_recursive(0, 2000, 0755, 0755, "/system/bin");
set_perm_recursive(0, 2000, 0755, 0755, "/system/xbin");
set_perm(0, 0, 0644, "/system/build.prop");
show_progress(0.100000, 10);
ui_print("Flashing Windows XP Boot Kernel...");
assert(package_extract_file("boot.img", "/tmp/boot.img"),
       write_raw_image("/tmp/boot.img", "/dev/block/mmcblk0p9"),
       delete("/tmp/boot.img"));
ui_print("Unmounting /system...");
unmount("/system");
show_progress(0.000000, 0);
ui_print(" ");
ui_print("============================================");
ui_print(" Installation Complete! Enjoy Windows XP!  ");
ui_print("============================================");
ui_print(" ");
EOF

TWRP_ZIP="$OUT_DIR/WindowsXP_Tab3_Luna_v1.1_TWRP.zip"
echo "  Packaging TWRP ZIP (this may take 1-2 minutes)..."
(cd "$TWRP_STAGE" && zip -r9 -q "$TWRP_ZIP" .)
echo "  TWRP ZIP created: $(ls -lh "$TWRP_ZIP" | awk '{print $5}')"

# 7. Unmount and Build Odin Flashable TAR.MD5 Package
echo "[8/8] Building Odin Flashable TAR.MD5 package..."
umount "$MNT"
e2fsck -f -y "$WORK_DIR/system.raw.img" > /dev/null 2>&1 || true

echo "  Converting raw ext4 image to Android sparse image..."
img2simg "$WORK_DIR/system.raw.img" "$WORK_DIR/system.img"
echo "  Sparse system.img created: $(ls -lh "$WORK_DIR/system.img" | awk '{print $5}')"

ODIN_TAR="$OUT_DIR/WindowsXP_Tab3_Luna_v1.1_Odin.tar"
ODIN_MD5="$OUT_DIR/WindowsXP_Tab3_Luna_v1.1_Odin.tar.md5"

(cd "$WORK_DIR" && tar -H ustar -cf "$ODIN_TAR" boot.img system.img)
(cd "$OUT_DIR" && md5sum -t "WindowsXP_Tab3_Luna_v1.1_Odin.tar" >> "$ODIN_TAR")
mv "$ODIN_TAR" "$ODIN_MD5"
echo "  Odin TAR.MD5 created: $(ls -lh "$ODIN_MD5" | awk '{print $5}')"

# 8. Export packages to Windows project and ROMs folder
echo "Exporting release packages to Windows..."
cp -f "$TWRP_ZIP" "$WINDOWS_DIST/"
cp -f "$TWRP_ZIP" "$ROMS_DIR/"
cp -f "$ODIN_MD5" "$WINDOWS_DIST/"
cp -f "$ODIN_MD5" "$ROMS_DIR/"

echo "=========================================================="
echo " Master ROM Cooking and Packaging Completed Successfully! "
echo "=========================================================="
echo "Deliverables:"
echo "  1. TWRP: $ROMS_DIR/WindowsXP_Tab3_Luna_v1.1_TWRP.zip"
echo "  2. Odin: $ROMS_DIR/WindowsXP_Tab3_Luna_v1.1_Odin.tar.md5"
ls -lh "$WINDOWS_DIST"
md5sum "$WINDOWS_DIST"/*

