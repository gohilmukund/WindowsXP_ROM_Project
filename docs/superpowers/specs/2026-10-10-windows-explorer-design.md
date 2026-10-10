# Windows XP Explorer (My Computer & File Manager) Design Spec
**Target Device**: Samsung Galaxy Tab 3 7.0 Wi-Fi (SM-T210 / SM-T210R)  
**OS**: Android 4.4.2 KitKat (API 19)  
**Package**: `com.windows.xp.explorer`  
**Date**: 2026-10-10  

---

## 1. Overview & Architectural Goals
Windows Explorer (`WindowsExplorer.apk`) provides the authentic Windows XP file browsing and management experience on the Galaxy Tab 3 7.0. It integrates seamlessly into the Luna shell ecosystem alongside `LunaLauncher`, `InternetExplorer`, `WindowsMediaPlayer`, `MSPaint`, and `Notepad`.

### Core Goals:
1. **Authentic Luna Aesthetic**: Classic two-tone royal blue gradient title bar, classic menu bar (`File`, `Edit`, `View`, `Favorites`, `Tools`, `Help`), standard buttons toolbar (Back, Forward, Up, Search, Folders, Views, Delete), and address bar with drive dropdown and green "Go" button.
2. **Iconic Left Task Pane**: Two-pane layout featuring the classic Windows XP collapsible blue task pane on the left with accordion groups:
   - **System Tasks**: View system info, Add or remove programs, Search files.
   - **Other Places**: Quick navigation pills for *My Computer*, *My Documents*, *My Pictures*, *My Music*, and *Downloads*.
   - **Details**: Live properties pane showing the selected item's name, type, size, thumbnail/icon, and modification timestamp.
3. **Storage Topology (Windows Drive Mapping)**:
   - **My Computer** (Virtual Root): Displays system drives with capacity meters.
   - **Local Disk (C:)**: `/sdcard` (`/storage/emulated/0`), ~5.6 GB internal storage.
   - **Removable Disk (D:)**: `/storage/extSdCard` (MicroSD slot), dynamically mounted when card is inserted.
   - **System Partition (S:)**: `/system` (KitKat OS partition, read-only).
4. **File Associations & Native Retro App Launching**:
   - Text files (`.txt`, `.log`, `.cfg`, `.ini`) -> Launch `Notepad.apk` (`com.windows.xp.notepad`)
   - Images (`.png`, `.jpg`, `.jpeg`, `.bmp`, `.gif`) -> Launch `MSPaint.apk` (`com.windows.xp.mspaint`) or `Fotoo`
   - Music & Audio (`.mp3`, `.wav`, `.ogg`, `.m4a`) -> Launch `WindowsMediaPlayer.apk` (`com.windows.xp.wmp`)
   - Web pages (`.html`, `.htm`) -> Launch `InternetExplorer.apk` (`com.windows.xp.internetexplorer`)
   - Applications (`.apk`) -> Android Package Installer
5. **Hardware Keys & Desktop Integration**:
   - Right Back button navigates Up one directory level (or minimizes to taskbar if at root).
   - "My Computer" desktop icon on `LunaLauncher` opens `WindowsExplorer`.
   - Start Menu shortcuts ("My Documents", "My Pictures", "My Music", "My Computer") route directly to corresponding Explorer locations.

---

## 2. User Interface Layout & Component Hierarchy

The 1024x600 tablet display is organized as follows:

```
+------------------------------------------------------------------------------------------+
| [XP Icon] My Computer / Local Disk (C:)                        [_] [口] [X] (Luna Title) |
+------------------------------------------------------------------------------------------+
|  File   Edit   View   Favorites   Tools   Help                             (Classic Menu) |
+------------------------------------------------------------------------------------------+
| [< Back] [> Fwd] [^ Up] | [Search] [Folders] [Views v] [Delete]             (XP Toolbar) |
+------------------------------------------------------------------------------------------+
| Address: [ C:\Documents and Settings\Owner\My Documents            v] [-> Go]  (Address) |
+-------------------------------+----------------------------------------------------------+
| LEFT TASK PANE (240px wide)   | RIGHT EXPLORER PANE (Flex)                               |
|                               |                                                          |
| v System Tasks                |   [C:] Local Disk (C:)       [D:] Removable Disk (D:)    |
|   * View system info          |   5.6 GB Free / 5.6 GB       Insert disk...              |
|   * Search for files          |                                                          |
|                               |   Files Stored on This Computer:                         |
| v Other Places                |   [Folder] My Documents      [Folder] My Pictures        |
|   * My Computer               |   [Folder] My Music          [Folder] Downloads          |
|   * My Documents              |                                                          |
|   * My Pictures               |   [Doc] Welcome to XP.txt    [Audio] Beethoven.mp3       |
|   * Control Panel             |                                                          |
|                               |                                                          |
| v Details                     |                                                          |
|   My Documents                |                                                          |
|   File Folder                 |                                                          |
|   Date: 10/10/2026            |                                                          |
+-------------------------------+----------------------------------------------------------+
| 6 objects (Disk free space: 5.6 GB)                                         (Status Bar) |
+------------------------------------------------------------------------------------------+
```

### Color Palette (Windows XP Luna Classic):
- Window Background: `#ECE9D8` (Luna beige/gray)
- Title Bar Gradient: `#0055EA` (start) to `#3A87F9` (end)
- Task Pane Left Background: `#7BA2E7`
- Task Header Normal: `#215DC6` with white text
- Task Card Body: `#D6DFF7`
- Selection Blue: `#316AC5` with white text

---

## 3. Component Architecture & Data Flow

### 1. `ExplorerActivity.java`
- Manages current navigation state (history stack for Back/Forward).
- Handles path resolving: converts virtual routes (`computer://`, `shell:MyDocuments`, `C:\`, `D:\`) to concrete filesystem paths.
- Listens for hardware keys (`KEYCODE_BACK` navigates back through folder stack; if stack empty, sends `com.windows.xp.ACTION_MINIMIZE`).

### 2. `TaskPaneView.java`
- Custom accordion view rendering collapsible cards.
- Supports expanding/collapsing sections with animated headers.
- Emits navigation events to `ExplorerActivity`.

### 3. `FileGridView.java` & `FileAdapter.java`
- Renders drive items (with storage progress meter) or directory items (folders & files).
- Supports 3 view modes: **Icons** (48x48 icon + label below), **List** (16x16 icon + label beside), and **Details** (columns: Name, Size, Type, Date Modified).
- Dynamic file type resolution:
  - Directory -> Folder icon
  - `.txt` -> Notepad icon
  - `.png` / `.jpg` -> Picture icon
  - `.mp3` / `.wav` -> Media Player icon
  - `.apk` -> Package icon

### 4. `FileOperationsManager.java`
- Asynchronous filesystem operations (New Folder, Rename, Delete, Copy, Paste).
- File selection details provider for the left "Details" accordion.

---

## 4. Integration with LunaLauncher & Custom ROM

1. **`LunaLauncher` Hook**:
   - `desktop_my_computer` click listener -> Launches `com.windows.xp.explorer` targeting `computer://`.
   - Start Menu `My Documents` -> Launches `com.windows.xp.explorer` with extra `path=/sdcard/Documents`.
   - Start Menu `My Pictures` -> Launches `com.windows.xp.explorer` with extra `path=/sdcard/Pictures`.
   - Start Menu `My Music` -> Launches `com.windows.xp.explorer` with extra `path=/sdcard/Music`.
2. **ROM Baking Pipeline**:
   - `WindowsExplorer.apk` baked into `/system/priv-app/WindowsExplorer.apk` (mode 644).
   - Added to debloat script exceptions, `cook_windows_xp_rom.sh`, and release packages.

---

## 5. Verification & Testing Strategy
1. **Compilation**: Built with Android SDK 19 (`android.jar` + `D8` + `aapt` + `zipalign`).
2. **Device Deployment**: Install live via ADB on Samsung Galaxy Tab 3 7.0 (`41007d269000b000`).
3. **Functional Verification**:
   - Navigation between Drives (C:), Folders, Subfolders, and Other Places.
   - File launching into Notepad, Paint, and WMP.
   - Folder creation, rename, and deletion.
   - Back button minimization and Home button Start Menu integration.
4. **ROM Cooking**: Package into `WindowsXP_Tab3_Luna_v1.1_Odin.tar.md5` and `WindowsXP_Tab3_Luna_v1.1_TWRP.zip`.
