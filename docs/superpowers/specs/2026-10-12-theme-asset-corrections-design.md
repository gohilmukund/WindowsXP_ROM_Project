# Design Specification: Theme Asset Corrections (Win7, Win11, Royale)

**Date**: 2026-10-12  
**Branch**: `feat/fix-theme-assets-win7-royale-win11`  
**Status**: In Review  

---

## 1. Overview & Problem Statement

In the previous iteration of the theme engine, placeholder/procedural assets were generated for several themes:
1. **Windows 7 Start Button**: A procedurally rendered 4-square icon was used rather than the authentic 3D Aero Glass Start Orb. Additionally, because the taskbar height is 32dp and button width is 48dp, aspect ratio distortion must be prevented.
2. **Windows 11 Default Wallpaper**: The generated `wallpaper_win11.png` was cropped from a screenshot that contained a static Start Menu and desktop icons baked into the background. It must be replaced with the clean, unblemished Microsoft 4K Bloom master wallpaper (`img0.jpg`) scaled cleanly to 1024x600.
3. **Windows 7 Taskbar Color**: The taskbar gradient was too dark (`#0B1E32` charcoal/black). Real Windows 7 Aero Glass uses a translucent cyan-blue gradient (`#3B74A8` to `#1C4166` with a top specular highlight edge).
4. **Royale (Windows XP Media Center Edition / Energy Blue)**:
   - Start button used Luna Blue colors instead of the official glossy emerald green pill with Franklin Gothic Medium Italic typography.
   - Taskbar was generic navy instead of official Energy Blue (`#5896DF` -> `#397DD0` -> `#254394` with a top specular highlight `#99C6F1`).
   - Window borders and titlebars need exact Microsoft `royale.msstyles` color matching (`#004BAA` border, `#2E62D4` -> `#6894EE` active titlebar gradient).

---

## 2. Detailed Technical Specifications

### 2.1 Asset Pipeline & Authentic Sources

| Asset Target | Source / Extraction Method | Dimensions / Format |
|---|---|---|
| `start_button_win7.png` | Scaled from authentic vector SVG in `TheRealBYK/win7-start-orbs` rendered with `resvg-js-cli` with 32-bit alpha transparency. Centered in a 48x32dp canvas (144x96px at 3x density) with 31dp orb diameter to maintain exact 1:1 circular proportions without stretching. | 144x96 RGBA PNG (48x32dp) |
| `wallpaper_win11.png` | Downsampled directly from Microsoft's clean 4K master (`C:\Windows\Web\Wallpaper\Windows\img0.jpg`, 3840x2400) cleanly cropped/scaled using LANCZOS to 1024x600 without any desktop UI elements. | 1024x600 RGB PNG |
| `start_button_royale.png` | Official Microsoft `royale.msstyles` emerald pill texture extracted from RetroBar (`startxproyalebutton.png`), composited with the official Windows XP waving flag and authentic `framdit.ttf` (Franklin Gothic Medium Italic) typography with drop shadow. | 106x34 RGBA PNG |
| `taskbar_bg_win7.xml` | Authentic Aero Glass cyan-blue gradient with top specular highlight edge: `#76AEE0` top stroke, gradient from `#3B74A8` (top) to `#2B5885` (mid) to `#1C4166` (bottom). | Android XML `<shape>` |
| `tray_bg_win7.xml` | Aero Glass system tray inset with semi-transparent frosted highlight: `#33FFFFFF` solid with `#44FFFFFF` border. | Android XML `<shape>` |
| `taskbar_bg_royale.xml` | Energy Blue multi-stop layer-list: top 1dp specular line `#99C6F1`, gradient from `#5896DF` to `#397DD0` to `#254394`. | Android XML `<layer-list>` |
| `tray_bg_royale.xml` | Royale system tray inset: dark royal blue gradient `#204A9C` to `#102C66` with `#456DAC` highlight stroke. | Android XML `<shape>` |

### 2.2 ThemeManager & Color Tokens

In `apps/LunaLauncher/src/com/windows/xp/launcher/theme/ThemeManager.java`:
- **Royale Active Titlebar**:
  - `getTitlebarActiveStart(ROYALE)`: `0xFF2E62D4`
  - `getTitlebarActiveEnd(ROYALE)`: `0xFF6894EE`
- **Royale Inactive Titlebar**:
  - `getTitlebarInactiveStart(ROYALE)`: `0xFF7A96DF`
  - `getTitlebarInactiveEnd(ROYALE)`: `0xFFA8BCF0`
- **Royale Window Border**:
  - `getWindowBorderColor(ROYALE)`: `0xFF004BAA`
- **DisplayProperties CRT Preview**:
  - Synchronize `CrtPreviewView` colors for Royale and Win7 with the updated tokens.

---

## 3. Automated Testing & Verification Gate

1. **Unit / Headless Regression Tests**:
   - Update `tools/tests/ThemeManagerTest.java` to test the updated Royale titlebar and border color tokens.
   - Run `./tools/run_tests.sh` in WSL. 5/5 tests must pass before compiling the APK.
2. **APK Compilation**:
   - Execute `./tools/build_apk.sh apps/LunaLauncher LunaLauncher.apk`. Must compile with exit code 0.
3. **Hardware Device Verification (Samsung Galaxy Tab 3 SM-T210)**:
   - Install APK via ADB.
   - Verify Windows 7 theme: capture screenshot showing circular Aero Start Orb and authentic Aero Glass taskbar.
   - Verify Windows 11 theme: capture screenshot showing clean Bloom wallpaper without desktop icons.
   - Verify Royale theme: capture screenshot showing authentic green Start button, Energy Blue taskbar, and royal blue window borders.

---

## 4. Work Decomposition

- **Task 1: Generate & Replace High-Resolution Assets**:
  - Update `tools/generate_theme_assets.py` to produce `start_button_win7.png`, `wallpaper_win11.png`, `start_button_royale.png`.
  - Update `taskbar_bg_win7.xml`, `tray_bg_win7.xml`, `taskbar_bg_royale.xml`, and `tray_bg_royale.xml`.
- **Task 2: ThemeManager & DisplayProperties Color Alignments**:
  - Update `ThemeManager.java` for Royale titlebar and border colors.
  - Update `DisplayPropertiesWindowView.java` preview colors.
  - Update and verify `ThemeManagerTest.java` assertions.
- **Task 3: Automated Regression Tests & APK Build**:
  - Run `./tools/run_tests.sh`.
  - Run `./tools/build_apk.sh apps/LunaLauncher LunaLauncher.apk`.
- **Task 4: Hardware Deployment & Visual Verification**:
  - Push APK to Samsung Galaxy Tab 3.
  - Switch to Windows 7, Windows 11, and Royale themes; take screencaps.
- **Task 5: Branch Review & Merge**:
  - Run subagent code review.
  - Merge `feat/fix-theme-assets-win7-royale-win11` into `master`.
