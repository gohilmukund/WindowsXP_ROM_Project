# Design Specification: Windows Themes Expansion, Dynamic Tray, and Automated Testing Suite

**Date:** 2026-10-11  
**Project:** Windows XP ROM Project (LunaLauncher)  
**Target Device:** Samsung Galaxy Tab 3 7.0 (SM-T210), Android 4.4.2 KitKat (API 19)  
**Status:** Approved by User  

---

## 1. Executive Summary

This specification outlines the architecture, visual design, and testing infrastructure for extending the theme system and window management in `LunaLauncher`:
1. **Olive Green Theme Correction:** Replace the jagged start button with the authentic smooth olive pill button and fix the start menu right column to authentic light olive/beige (`#E6EAD5`).
2. **Windows 7 Aero Glass Theme Correction:** Fix the start button to the authentic glowing circular Aero orb and update the start menu to authentic Aero glass (white left pane, dark translucent slate right pane with white text, and rounded shut down button).
3. **Royale Theme Addition:** Add the authentic Windows XP Media Center Edition / Energy Blue theme with vibrant green pill start button, royal glassy taskbar, periwinkle start menu right pane, and Energy Blue titlebars.
4. **Windows 11 Fluent Theme Addition:** Add a modern Windows 11 theme with the 4-square blue Windows logo, light frosted acrylic taskbar, and rounded 2-column Fluent start menu.
5. **Dynamic System Tray & Clock Panel:** Replace the hardcoded blue tray background with dynamic theme-aware styling, ensuring the notification area and clock text dynamically adapt to every theme.
6. **Window Management & Restore/Immersive Experience:** Eliminate the black-screen letterboxing caused by display overscan, ensuring external apps run cleanly in authentic Windows windowed mode with titlebar and taskbar, and toggle cleanly to immersive fullscreen.
7. **Automated Regression Test Suite:** Implement a fast, headless automated test runner (`tools/run_tests.sh`) to verify theme contracts, drawables, color invariants, and build integrity before every release.

---

## 2. Visual Theme Specification

### 2.1 Olive Green Theme (Ref Image 1)
* **Start Button:**
  * Authentic smooth Olive Green Luna pill (`106dp × 30dp`).
  * Crisp Windows XP 4-color flag and white italic `"start"` text.
  * Drawable: `res/drawable/start_button_luna_olive.png`.
* **Taskbar & System Tray:**
  * Background: Olive gradient (`#759954` to `#45672E`).
  * Dynamic Tray: Inset olive tray (`#3D5C26` background, `#5A803B` top/left highlight border).
  * Tray Text: Crisp white (`#FFFFFF`) clock and `#CBE4B5` battery text.
* **Start Menu:**
  * Header/Bottom: Olive gradient (`#759954` to `#45672E`).
  * Left Pane: Clean white (`#FFFFFF`).
  * Right Pane: Light olive/beige tint (`#E6EAD5`).
  * Right Pane Text: Dark olive green (`#354B1A`).
  * Right Pane Divider: `#CBD4B6`.

### 2.2 Windows 7 Aero Glass Theme (Ref Image 2)
* **Start Button:**
  * Circular glowing Windows 7 Aero orb (`48dp × 48dp`), centered vertically on the taskbar.
  * Translucent glass sphere with vibrant 4-colored Windows flag.
  * Drawable: `res/drawable/start_button_win7.png`.
* **Taskbar & System Tray:**
  * Background: Translucent dark Aero glass (`#CC1A2F45`).
  * Dynamic Tray: Integrated glass tray with subtle white top highlight (`#33FFFFFF`).
  * Tray Text: Crisp white (`#FFFFFF`) clock and battery text.
* **Start Menu:**
  * Header: Hidden/integrated with modern rounded top.
  * Left Pane: Crisp white (`#FFFFFF`) with search bar field at bottom (`#FFFFFF` box with `#8E8E8E` border).
  * Right Pane: Dark translucent slate/navy glass (`#EE182C44`).
  * Right Pane Text: Crisp white (`#FFFFFF`) with subtle text shadow.
  * Right Pane Divider: `#3A5370`.
  * Shut Down Button: Classic Windows 7 rounded glass button at bottom-right (`#3D5A7D`).

### 2.3 Royale / Media Center Theme (Ref Image 3)
* **Start Button:**
  * Vibrant emerald green pill button (`106dp × 30dp`) with white italic `"start"` text.
  * Drawable: `res/drawable/start_button_royale.png`.
* **Taskbar & System Tray:**
  * Background: Royal glassy blue gradient (`#0A5BC4` to `#003B99`).
  * Dynamic Tray: Darker royal blue inset tray (`#002B7A` with `#236ED6` border).
  * Tray Text: Crisp white (`#FFFFFF`) clock and `#B8D3FF` battery text.
* **Start Menu:**
  * Header/Bottom: Royal blue gradient (`#1858D6` to `#0A3DA8`).
  * Left Pane: Clean white (`#FFFFFF`).
  * Right Pane: Pale periwinkle blue (`#D2DBEE`).
  * Right Pane Text: Deep navy (`#00156E`).
  * Right Pane Divider: `#AEC5E8`.
* **Window Titlebars:**
  * Active Gradient: Energy Blue (`#1073E6` to `#0048B3`).
  * Border: `#0048B3`.
* **Default Wallpaper:** Windows XP Media Center Edition Energy Blue wallpaper.

### 2.4 Windows 11 Fluent Theme (Ref Image 4)
* **Start Button:**
  * Modern clean Windows 11 four-square blue logo (`42dp × 32dp`).
  * Drawable: `res/drawable/start_button_win11.png`.
* **Taskbar & System Tray:**
  * Background: Light frosted acrylic bar (`#F3F3F3` with `#E0E0E0` 1dp top divider).
  * Dynamic Tray: Seamless transparent/light tray.
  * Tray Text: Dark charcoal (`#1C1C1C`) clock and battery text.
* **Start Menu:**
  * Container: Off-white acrylic (`#FDFDFD`), 12dp rounded corners, 1dp subtle border (`#E5E5E5`).
  * Left Pane: `#FFFFFF`.
  * Right Pane: Light neutral gray (`#F5F6F8`).
  * Text: Modern charcoal (`#1A1A1A`).
  * Dividers: `#EAEAEA`.
* **Default Wallpaper:** Windows 11 Bloom wallpaper.

---

## 3. Dynamic Clock Panel & System Tray Architecture

1. **`ThemeManager` Accessors:**
   * `getTaskbarTrayBgRes()`: Returns the theme-specific XML drawable shape (or color gradient) for the system tray.
   * `getTaskbarTrayTextColor()`: Returns `#FFFFFF` for dark taskbars and `#1C1C1C` for light taskbars.
   * `getTaskbarTrayBatteryColor()`: Returns theme-accented battery text colors.
2. **`LauncherActivity` Implementation:**
   * Inside `onThemeChanged()`, look up `findViewById(R.id.system_tray)` and apply `tm.getTaskbarTrayBgRes()`.
   * Update `tray_clock` and `tray_battery` text colors dynamically.
3. **`FloatingWindowOverlayService` Implementation:**
   * Apply dynamic tray styling to `tvTrayClock` and tray container in the floating taskbar overlay.

---

## 4. Window Management & Fullscreen/Windowed Toggle

1. **Elimination of Black Screen:**
   * Remove `IWindowManager.setOverscan` calls from the window toggle flow.
   * By keeping overscan at `(0, 0, 0, 0)`, the tablet display controller never crops pixels, eliminating black screen borders.
2. **Authentic Windows Windowed Mode:**
   * External Android applications run with the authentic XP/Aero/Fluent top titlebar (icon, title, minimize, maximize/restore, close) and bottom taskbar.
   * Tapping **Minimize** hides the overlays and reveals the full desktop wallpaper and desktop icons.
   * Tapping **Maximize/Restore** toggles cleanly between:
     * **Windowed Mode:** Full display framed by the titlebar and taskbar.
     * **Immersive Fullscreen:** Titlebar and taskbar hidden, with the sleek top exit pill for 1-tap restoration.

---

## 5. Automated Test Suite Architecture

### 5.1 Test Framework Layout
```
WindowsXP_ROM_Project/
├── tools/
│   ├── run_tests.sh               # Main test runner script
│   └── tests/
│       ├── ThemeManagerTest.java  # Headless unit test assertions
│       └── TestRunner.java        # Lightweight test execution engine
```

### 5.2 Test Invariants & Assertions
1. **Theme Enum Coverage:** Verifies all 8 themes are populated and return non-null, valid resources.
2. **Resource Validity:** Verifies all drawable resource IDs resolve to valid assets.
3. **Color & Contrast Verification:**
   * Verifies start menu header/bottom gradient arrays have length 2 and non-zero values.
   * Verifies right-pane background and text colors maintain minimum contrast.
   * Verifies clock text color matches theme luminosity (dark text on light taskbar, light text on dark taskbar).
4. **Start Button Dimensions:** Verifies button width and padding parameters match theme requirements (48dp for Win7/Vista, 42dp for Win11, 65dp for Classic, 106dp for Luna).
5. **Compilation Gate:** `run_tests.sh` executes the test runner prior to `build_apk.sh`. If any assertion fails, build halts immediately.

---

## 6. Implementation Plan & Deliverables

1. **Assets & Drawables:**
   * Add `start_button_luna_olive.png` (clean, uncorrupted).
   * Add `start_button_win7.png` (high-res Aero orb).
   * Add `start_button_royale.png` (emerald green pill).
   * Add `start_button_win11.png` (modern 4-square blue logo).
   * Add theme-specific tray backgrounds (`tray_bg_olive`, `tray_bg_silver`, `tray_bg_royale`, `tray_bg_win7`, `tray_bg_win11`).
   * Add wallpapers for Royale and Windows 11.
2. **Theme Engine Extension (`ThemeManager.java`):**
   * Add `ROYALE` and `WIN11` enum values.
   * Add all color, drawable, and text token accessors for the new themes.
   * Add dynamic tray background and text color accessors.
3. **UI Integration (`LauncherActivity.java` & `FloatingWindowOverlayService.java`):**
   * Apply dynamic tray styling to `system_tray`, `tray_clock`, and `tray_battery`.
   * Update start menu layout to support theme-driven right pane background, text, and dividers.
   * Simplify overlay toggle to clean Windowed / Immersive mode without black-screen overscan.
4. **Automated Test Suite:**
   * Implement `tools/tests/ThemeManagerTest.java` and `tools/run_tests.sh`.
   * Run and verify all assertions pass.
5. **Physical Hardware Verification:**
   * Deploy to Samsung Galaxy Tab 3 SM-T210 and capture verification screenshots for all themes and modes.
