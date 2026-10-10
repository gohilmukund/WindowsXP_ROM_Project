# System Design Specification: Windows XP Luna Custom ROM for Samsung Galaxy Tab 3 7.0 (SM-T210)

**Date:** 2026-10-10  
**Target Hardware:** Samsung Galaxy Tab 3 7.0 Wi-Fi (`SM-T210` / `SM-T210R`, codename `lt02wifi`)  
**Base Operating System:** Cleaned Android 4.4.2 KitKat (Stock build `T210RUES0CQA1`, kernel 3.4.5, Vivante GC1000 GPU)  
**Host Environment:** Windows 11 + WSL2 Ubuntu  

---

## 1. Project Overview & Vision
The goal of this project is to convert the Samsung Galaxy Tab 3 7.0 into a dedicated, authentic **Windows XP Luna** machine. Rather than using an emulation layer or third-party launcher theme, the system will be modified from the ground up:
* The stock TouchWiz interface, Knox, and Google Play Services will be completely stripped to recover memory and eliminate failed sign-ins.
* A custom native Windows XP shell (`LunaLauncher`), Internet Explorer browser, Windows Media Player, MS Paint, Notepad, and retro gaming suite will run directly on the hardware with full Vivante GPU acceleration.
* Physical hardware buttons will act as desktop keys: Home opens the Start Menu, Menu triggers the XP Alt+Tab task switcher, and Back minimizes the active window to the taskbar.

---

## 2. Hardware Profile & System Constraints

| Component | Hardware Specification | Architectural Consideration |
| :--- | :--- | :--- |
| **SoC / CPU** | Marvell Armada PXA986 (Dual-core Cortex-A9 @ 1.2 GHz) | Low CPU overhead required; Dalvik JIT optimization enabled. |
| **GPU** | Vivante GC1000 (Hardware GLES 2.0) | Proprietary drivers locked to KitKat (`Gralloc 0.3`). Hardware compositing preserved. |
| **RAM** | 1.0 GB LPDDR2 (approx. 810 MB usable) | Stock OS consumes ~500 MB; debloating targets < 200 MB idle, leaving > 600 MB free for apps. |
| **Display** | 7.0" TFT LCD, 1024 x 600 pixels (170 ppi) | Ideal 16:9.4 aspect ratio for 1024x600 uncompressed Bliss bitmap and XP taskbar layout. |
| **Keys** | Physical Home Key + Capacitive Menu & Back keys | Intercepted at shell level for native Windows desktop ergonomics. |

---

## 3. System Debloating & Optimization (Phase A)

### 3.1. Applications & Services Removed
To guarantee optimal battery life and maximize free RAM, the following packages are purged from `/system/app/` and `/system/priv-app/`:
1. **Samsung Bloatware:** `SecLauncher3` (TouchWiz), `SamsungApps`, `ChatON`, `SecEmail`, `S-Suggest`, `MusicPlayer`, `VideoPlayer`, `SamsungHub`, `SamsungWidget_*`, `KNOXAgent`, `ContainerAgent`.
2. **Google Services (Non-functioning):** `GmsCore` (Google Play Services), `GoogleLoginService`, `GoogleServicesFramework`, `Phonesky` (Play Store), `GoogleFeedback`.
3. **Unused Daemons:** All carrier-specific setup wizards, diagnostic reporters, and telemetry.

### 3.2. System Configuration Tweaks (`build.prop`)
* `qemu.hw.mainkeys=1`: Completely disables the Android on-screen navigation bar. The screen area extends 100% to all 600 vertical pixels so the Windows XP taskbar sits flush against the bottom edge.
* `persist.sys.theme=luna`: Flags LunaLauncher as the primary desktop environment.
* `dalvik.vm.heapgrowthlimit=128m` / `dalvik.vm.heapsize=256m`: Optimizes heap allocations for multi-window performance on 1GB RAM.

### 3.3. Audio & Visual Boot Experience
* **Boot Animation:** Replace `/system/media/bootanimation.zip` with authentic Windows XP 1024x600 boot animation: black background, Windows XP flag logo, and the animated 3-segment blue marquee loading bar.
* **Startup Sound:** Standard `/system/media/audio/ui/PowerOn.ogg` replaced with authentic uncompressed `Windows XP Startup.wav` audio trigger.
* **Shutdown Sound:** Injected to trigger on system shutdown intent.
* **Default Wallpaper:** Pre-configured to uncompressed 1024x600 `Bliss.bmp`.

---

## 4. Hardware Button Integration

```
[ Menu / Recent Apps ]       [ Physical Home Button ]        [ Back Key ]
         │                              │                         │
         ▼                              ▼                         ▼
  Windows XP Alt+Tab             Windows XP Start            Minimizes Active
  Task Switcher Overlay           Menu (Open/Close)         Window to Taskbar
```

1. **Physical Home Button (`KeyEvent.KEYCODE_HOME`):**
   * Intercepted by `LunaLauncher`.
   * Toggles the Windows XP Start Menu open or closed with audio feedback (`start_menu_click.wav`).
2. **Capacitive Menu Key (`KeyEvent.KEYCODE_MENU`):**
   * Toggles the **Windows XP Alt+Tab Task Switcher**.
   * Displays a classic centered dark-blue translucent dialog showing icons of all active/running applications. Tapping or pressing cycles through running applications.
3. **Capacitive Back Key (`KeyEvent.KEYCODE_BACK`):**
   * In foreground applications, Back sends an explicit minimize intent: the app window animates down to its corresponding tab on the taskbar, returning focus to the desktop without destroying the process.

---

## 5. Native Windows XP Application Suite

### 5.1. `LunaLauncher.apk` (Desktop, Taskbar & Start Menu)
* **Desktop:**
  * Displays uncompressed Bliss background.
  * Desktop icons: *My Computer*, *My Documents*, *Internet Explorer*, *Windows Media Player*, *Fotoo*, *MS Paint*, *Notepad*, *Games*, *Recycle Bin*.
  * Single-tap / double-tap customizable action. Blue bounding highlight box on tap.
* **Taskbar:**
  * **Start Button:** Classic rounded green XP button with embossed Windows flag.
  * **Window Tabs:** Dynamically generated 3D-styled tabs for open apps. Clicking a tab alternates between minimize and restore.
  * **System Tray:** 
    * Network status: Real-time Wi-Fi signal icon.
    * Power status: Battery percentage gauge with AC charging indicator.
    * Digital Clock: Real-time 12-hour/24-hour time with tooltips.
* **Start Menu:**
  * Two-column XP hierarchy:
    * Header with user profile avatar.
    * Left panel (White): Frequent programs (Internet Explorer, Outlook Express / Mail, Media Player, Notepad, Paint).
    * Right panel (Blue): My Documents, My Pictures (Fotoo), My Music, My Computer, Control Panel (Android Settings).
    * Bottom bar: "Log Off" and "Turn Off Computer" (dialog offering Standby, Restart, Turn Off).

### 5.2. `InternetExplorer.apk` (The Modern Web Browser)
* **User Interface:**
  * Classic Windows XP IE6 / IE7 Luna chrome: blue title bar, minimize/maximize/close buttons.
  * Toolbar: Back, Forward, Stop, Refresh, Home, Favorites, History.
  * Address bar with "Go" button and spinning Windows/e globe animated during network activity.
  * Status bar at bottom showing page load progress, security lock icon for HTTPS, and status text.
* **Rendering Core:**
  * Built using a custom modern web engine wrapping updated Gecko/Chromium with complete TLS 1.3, modern cipher suites, and modern HTML5/JS support.
  * Full compatibility with Instagram, YouTube, search engines, and modern portals.

### 5.3. `WindowsMediaPlayer.apk` (Music & Media Player)
* **Visuals:** Iconic Windows Media Player 9/10 brushed silver-blue metallic interface.
* **Engine & Controls:**
  * Media scanner for internal storage and MicroSD card (`/sdcard/Music`).
  * Play, Pause, Stop, Previous, Next, Seek slider, and Volume control.
  * Visualizer canvas with retro oscilloscope and frequency spectrum bars.
  * Playlist editor and album art rendering.

### 5.4. `MSPaint.apk` (Classic MS Paint)
* **Interface:**
  * Top menu: File (New, Open, Save, Exit), Edit, View, Image, Colors, Help.
  * Left Tool Palette: Pencil, Paint Brush, Eraser, Paint Bucket (Fill), Line, Rectangle, Rounded Rectangle, Ellipse, Color Picker, Zoom.
  * Bottom Color Palette: 28 classic Windows XP color squares with primary and secondary color indicators.
  * Interactive canvas with smooth multi-touch drawing. Saves drawings directly to `/sdcard/Pictures/` as PNG/BMP.

### 5.5. `Notepad.apk` (Classic Notepad)
* **Interface:**
  * Menu bar: File, Edit, Format, View, Help.
  * Clean monospace text editor using classic typography (Courier New / Consolas style).
  * Feature set: Word wrap toggle, Find/Replace, Insert Time/Date (`F5` equivalent), character and line counting.
  * Loads and saves plain text files directly in `/sdcard/Documents/`.

### 5.6. Retro Gaming & DOS Suite (`Games`)
* **Classic Windows XP Games:**
  * **Minesweeper (`winmine`):** Classic smiley face reset button, flag and bomb counter, beginner/intermediate/expert grid sizes, classic sound effects.
  * **Solitaire (`sol`):** Classic Klondike card game with traditional XP card backs and victory card bounce animation.
  * **3D Pinball for Windows - Space Cadet:** Native ARM port running at full 60 FPS with authentic music and sounds.
* **DOS Emulation Core:**
  * Embedded optimized DOSBox engine pre-configured for retro DOS titles with on-screen virtual keyboard/gamepad overlay.

### 5.7. `Fotoo.apk` (Digital Photo Frame & Slideshow)
* Pre-configured and mapped to "My Pictures" for automated photo slideshows, Google Drive/SMB streaming, and digital photo frame display.

---

## 6. ROM Building & WSL Repackaging Workflow (Phase B)

1. **Extraction (WSL):**
   * Mount and unpack stock `AP_T210RUES0CQA1...tar.md5` to extract `system.img.ext4`.
   * Unpack ext4 filesystem into a clean working tree in WSL Ubuntu (`/mnt/c/.../WindowsXP_ROM_Project/rom_base/system`).
2. **Modification:**
   * Remove TouchWiz, Knox, and GApps APKs.
   * Inject built APKs (`LunaLauncher.apk`, `InternetExplorer.apk`, `WindowsMediaPlayer.apk`, `MSPaint.apk`, `Notepad.apk`, `SpaceCadetPinball.apk`, `Fotoo.apk`) into `/system/priv-app/` with permissions `0644`.
   * Inject `bootanimation.zip` into `/system/media/` and audio files into `/system/media/audio/ui/`.
   * Update `build.prop` with `qemu.hw.mainkeys=1` and memory performance parameters.
3. **Repackaging:**
   * Build clean flashable ZIP with standard `updater-script` targeting `mmcblk0p14` (system) and `mmcblk0p15` (cache).
   * Sign package with test keys.
4. **Flashing & Deployment:**
   * Push directly to `/sdcard/` and flash via TWRP recovery.

---

## 7. Success Criteria & Verification Matrix

* [ ] System boots cleanly with Windows XP boot animation and startup sound.
* [ ] Device boots directly to Windows XP Bliss desktop with zero TouchWiz or Google setup wizards.
* [ ] Hardware Home key toggles the Start Menu.
* [ ] Hardware Menu key opens the XP Alt+Tab task switcher.
* [ ] Hardware Back key minimizes the active window to the taskbar.
* [ ] Internet Explorer loads modern websites (e.g., instagram.com, youtube.com) without SSL errors.
* [ ] Windows Media Player scans and plays local MP3 tracks with audio visualizer.
* [ ] MS Paint and Notepad create and save files to Documents and Pictures.
* [ ] 3D Pinball and classic games run smoothly with audio.
* [ ] Free RAM exceeds 550 MB on idle.
