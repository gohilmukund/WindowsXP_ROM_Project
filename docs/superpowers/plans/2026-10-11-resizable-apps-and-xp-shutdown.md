# Resizable Android Apps & Authentic Windows XP Shutdown Implementation Plan

**Goal:** Provide full multi-window resizability and desktop integration for all Android apps, integrate authentic downloaded XP/Classic/Vista/Win7 theme icons and Start buttons, and implement the pixel-accurate working Windows XP "Turn off computer" dialog.

**Architecture:**
- **Authentic Asset Package**: Download and integrate original, pixel-perfect bitmap icons for Start buttons, Explorer toolbar (Back, Forward, Up, Search, Folders), Drives, and Shutdown buttons across Luna Blue, Olive, Silver, Classic, Vista, and Win7.
- **Pixel-Accurate Shutdown Dialog**: A customized overlay dialog with desktop dimming, Luna header, circular action buttons (Stand By, Turn Off, Restart) executing actual system sleep, shutdown, and reboot cascades.
- **Desktop Core Suite**: Native `WindowFrameView` wrappers for Internet Explorer (`IEWindowView` with modern WebView engine), Windows Media Player (`WMPWindowView` with audio/video engine), and Windows Picture Viewer (`PictureViewerWindowView` with zoom/rotate/slideshow).
- **Floating App Frame Service**: A system overlay (`FloatingWindowService`) providing Windows XP window frame, draggable titlebar, Min/Max/Restore/Immersive/Close controls for any external installed Android application.

## Architecture Diagram

```mermaid
graph TD
    subgraph Desktop Environment (LunaLauncher)
        Desktop[Desktop & Taskbar]
        StartMenu[Start Menu]
        AllPrograms[All Programs Menu]
        
        ShutdownDialog[Windows XP Shutdown Dialog] -->|Stand By| SleepExec[input keyevent 26 / Screen Off]
        ShutdownDialog -->|Turn Off| PowerExec[IPowerManager / Intent / reboot -p]
        ShutdownDialog -->|Restart| RebootExec[IPowerManager / ACTION_REBOOT / reboot]
        
        DesktopWindowMgr[WindowFrameView Manager]
        IE[Internet Explorer - IEWindowView] --> DesktopWindowMgr
        WMP[Windows Media Player - WMPWindowView] --> DesktopWindowMgr
        PicViewer[Picture Viewer - PictureViewerWindowView] --> DesktopWindowMgr
        Explorer[Windows Explorer] --> DesktopWindowMgr
        Paint[MS Paint] --> DesktopWindowMgr
        Notepad[Notepad] --> DesktopWindowMgr
        Cmd[Command Prompt] --> DesktopWindowMgr
    end
    
    subgraph External Installed Apps
        ExtApps[Firefox / Fotoo / Pinball / Android Apps]
        FloatService[Floating XP Frame Overlay Service]
        ExtApps --- FloatService
        FloatService -->|Minimize / Restore / Immersive| Desktop
    end
    
    subgraph Theme Engine
        ThemeMgr[ThemeManager]
        ThemeMgr -->|Luna Blue / Olive / Silver / Classic / Vista / Win7| Desktop
        ThemeMgr -->|Authentic Start Buttons & Icons| DesktopWindowMgr
    end
```

## Global Constraints
- Target device: Samsung Galaxy Tab 3 7.0 Wi-Fi (`SM-T210`, Android 4.4.2 KitKat, API 19).
- Hardware resolution: 1024x600 landscape.
- Do NOT cook or flash the ROM yet — build and verify everything within the working environment.
- Preserve all existing functionality (Explorer C: browsing, theme engine, hardware keys, Control Panel, etc.).
- Evidence before claims: Test and capture screenshots on physical hardware via ADB for every feature.
