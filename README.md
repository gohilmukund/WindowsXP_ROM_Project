# 🖥️ Windows XP Professional - Luna Edition (Custom ROM)
### Target Device: Samsung Galaxy Tab 3 7.0 (SM-T210 / SM-T210R | `lt02wifi`)
### Base System: Android 4.4.2 KitKat (Stock `T210RUES0CQA1`)

[![Device](https://img.shields.io/badge/Device-Samsung%20Galaxy%20Tab%203%207.0-blue.svg)](https://developer.samsung.com)
[![Platform](https://img.shields.io/badge/Platform-Marvell%20PXA986-orange.svg)]()
[![Base OS](https://img.shields.io/badge/Base-Android%204.4.2%20KitKat-brightgreen.svg)]()
[![Theme](https://img.shields.io/badge/Theme-Windows%20XP%20Luna-0055EA.svg)]()
[![Release](https://img.shields.io/badge/Version-v1.2%20Final-blueviolet.svg)]()

---

## 📌 Overview

**Windows XP Professional Luna Edition** is a custom, retro-themed Android 4.4.2 KitKat ROM engineered specifically for the **Samsung Galaxy Tab 3 7.0 Wi-Fi (SM-T210 / SM-T210R)**. 

It completely transforms the tablet experience into an authentic **Windows XP Luna** desktop interface while maintaining full Android performance, touch controls, hardware acceleration, and app compatibility.

---

## ✨ Key Features & System Architecture

### 1. 🪟 Windows XP Luna Application Suite
Preinstalled directly into `/system/priv-app/` with root privileges and system integrated branding:
* **Luna Launcher**: Authentic Windows XP Taskbar, Start Menu, Desktop shortcuts, and Bliss wallpaper.
* **Windows Explorer**: Windows XP file manager style navigation for internal and external storage.
* **Internet Explorer**: Custom retro-themed web browser.
* **Windows Media Player**: Classic WMP 9/10 UI media player.
* **MS Paint**: Classic touch-enabled digital painting app.
* **Notepad**: Desktop-style text editor.
* **Minesweeper & Solitaire**: Classic Windows XP desktop games.
* **Fotoo**: Windows XP picture viewer & digital frame display.

### 2. 🎮 Retro Gaming & Legacy Media Apps
Preinstalled in `/system/app/`:
* **Space Cadet Pinball**: 3D Pinball for Windows - Space Cadet (`v2.5`).
* **Firefox Legacy**: Mozilla Firefox `68.11.0` optimized for ARMv7 devices.
* **NewPipe Legacy**: Lightweight YouTube client (`v0.20.8`) with background video playback.

### 3. 🔊 Nostalgic Audio & Visual Immersion
* **Boot Animation**: High-resolution Windows XP Professional boot screen replacing stock TouchWiz animations (`/system/media/bootanimation.zip`).
* **System Audio**: Authentic Windows XP Startup sound played on boot (`/system/media/audio/ui/PowerOn.ogg`).
* **System Tweaks & Branding**: Custom `build.prop` entries reporting `Windows XP Professional Luna Edition (v1.2)` under device settings.

### 4. 🧹 Deep Debloating & Performance Optimizations
* **TouchWiz Stripped**: Removed bloated Samsung services (`sCloud`, `SecLauncher2`, `ChatON`, `AllShare`, `SamsungApps`, `Yahoo News/Stock`, `PolarisViewer`).
* **Defunct GApps Purged**: Removed non-functional Google Play Services (`GmsCore`, `Phonesky`, `Velvet`, `GoogleLoginService`, legacy Play Store apps) to free up RAM and storage.

---

## 📁 Repository Structure

```text
WindowsXP_ROM_Project/
├── apps/                        # Native Windows XP Luna App Suite
│   ├── Fotoo/
│   ├── InternetExplorer/
│   ├── LunaLauncher/
│   ├── Minesweeper/
│   ├── MSPaint/
│   ├── Notepad/
│   ├── Solitaire/
│   ├── WindowsExplorer/
│   └── WindowsMediaPlayer/
├── assets/                      # System Multimedia & Customization Assets
│   ├── audio/                   # Windows XP PowerOn.ogg sound
│   ├── bootanimation/           # bootanimation.zip
│   └── tweaks/                  # build.prop.append parameters
├── dist/                        # Ready-to-Flash Release Builds
│   ├── WindowsXP_Tab3_Luna_v1.2_TWRP.zip      # TWRP Flashable ZIP
│   └── WindowsXP_Tab3_Luna_v1.2_Odin.tar.md5  # Odin Flashable Package
├── tools/                       # Build Scripts & Icon Generators
│   ├── build_apk.sh             # Batch APK compilation script
│   ├── cook_windows_xp_rom.sh   # Master ROM cooking & packaging script
│   ├── debloat.ps1              # ADB-based debloating script for Windows
│   └── generate_*_icons.py      # Icon extraction & generation tools
├── .gitignore
└── README.md
```

---

## 🚀 Flashing Instructions

> ⚠️ **Warning**: Flashing custom firmware carries inherent risks. Always back up your existing partitions (EFS, System, Data) via TWRP before proceeding.

### Option 1: Flashing via TWRP Recovery (Recommended)

1. **Prerequisites**:
   * Samsung Galaxy Tab 3 7.0 (SM-T210 / T210R) running TWRP Recovery (`twrp-3.6.2_9-0-lt02.img.tar` or similar).
2. **Steps**:
   1. Transfer `WindowsXP_Tab3_Luna_v1.2_TWRP.zip` (from [`dist/`](file:///c:/Users/gohil/Downloads/Samsung_Tab3_SM-T210_Tools/WindowsXP_ROM_Project/dist)) to an SD Card or internal storage.
   2. Boot tablet into TWRP Recovery (`Volume Up + Home + Power`).
   3. Perform a **Wipe** -> **Factory Reset** (Wipe `Data`, `Cache`, and `Dalvik-Cache`).
   4. Select **Install** -> Choose `WindowsXP_Tab3_Luna_v1.2_TWRP.zip`.
   5. Swipe to confirm Flash.
   6. Reboot System and enjoy Windows XP!

---

### Option 2: Flashing via Samsung Odin3

1. **Prerequisites**:
   * Windows PC with [SAMSUNG USB Drivers](file:///c:/Users/gohil/Downloads/Samsung_Tab3_SM-T210_Tools/SAMSUNG_USB_Driver_for_Mobile_Phones.exe) installed.
   * [Odin3 v3.09](file:///c:/Users/gohil/Downloads/Samsung_Tab3_SM-T210_Tools/Odin3_v3.09) utility.
2. **Steps**:
   1. Boot tablet into **Download Mode** (`Volume Down + Home + Power`, then press `Volume Up` to continue).
   2. Connect tablet to PC via USB cable.
   3. Open `Odin3_v3.09.exe`. Ensure `COM` port turns blue/yellow.
   4. Click **AP** (or **PDA**) and select `WindowsXP_Tab3_Luna_v1.2_Odin.tar.md5` (from [`dist/`](file:///c:/Users/gohil/Downloads/Samsung_Tab3_SM-T210_Tools/WindowsXP_ROM_Project/dist)).
   5. Click **Start** and wait for the `PASS!` message.
   6. The tablet will automatically reboot into the Windows XP Luna ROM.

---

## 🛠️ Building & Cooking from Source

The master cooking script [`tools/cook_windows_xp_rom.sh`](file:///c:/Users/gohil/Downloads/Samsung_Tab3_SM-T210_Tools/WindowsXP_ROM_Project/tools/cook_windows_xp_rom.sh) mounts the base raw system ext4 image, applies debloating, injects applications and media assets, applies `build.prop` optimizations, and builds both TWRP ZIP and Odin TAR.MD5 packages.

### Build Requirements
* Linux environment or WSL2 (Ubuntu / Debian).
* `e2fsprogs`, `mount`, `zip`, `img2simg`, `tar`, `md5sum`.

### Running the Chef Script
```bash
chmod +x tools/cook_windows_xp_rom.sh
sudo ./tools/cook_windows_xp_rom.sh
```

---

## 📄 License & Credits

* **Device Kernel & Base Firmware**: Samsung Electronics Co., Ltd.
* **Windows XP Luna Design & Icons**: Microsoft Corporation.
* **Project Maintainer**: Windows XP ROM Project Team.
