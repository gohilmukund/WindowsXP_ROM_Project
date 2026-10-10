package com.windows.xp.launcher.theme;

import android.content.Context;
import android.graphics.Color;
import com.windows.xp.launcher.R;

import java.util.ArrayList;
import java.util.List;

public class ThemeManager {

    public enum Theme {
        LUNA_BLUE("Windows XP (Default Blue)"),
        LUNA_OLIVE("Windows XP (Olive Green)"),
        LUNA_SILVER("Windows XP (Silver)"),
        CLASSIC("Windows Classic"),
        VISTA("Windows Vista"),
        WIN7_AERO("Windows 7 Aero Glass");

        private final String displayName;

        Theme(String name) {
            this.displayName = name;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public interface OnThemeChangeListener {
        void onThemeChanged(Theme newTheme);
    }

    private static ThemeManager instance;
    private Theme currentTheme = Theme.LUNA_BLUE;
    private final List<OnThemeChangeListener> listeners = new ArrayList<OnThemeChangeListener>();

    public static synchronized ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    private ThemeManager() {}

    public Theme getCurrentTheme() {
        return currentTheme;
    }

    public void setTheme(Theme theme) {
        if (this.currentTheme != theme) {
            this.currentTheme = theme;
            notifyListeners();
        }
    }

    public void addListener(OnThemeChangeListener l) {
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    public void removeListener(OnThemeChangeListener l) {
        listeners.remove(l);
    }

    private void notifyListeners() {
        for (OnThemeChangeListener l : listeners) {
            l.onThemeChanged(currentTheme);
        }
    }

    // --- Theme Color & Asset Accessors ---

    public int getTitlebarActiveStart() {
        switch (currentTheme) {
            case LUNA_OLIVE: return 0xFF759954;
            case LUNA_SILVER: return 0xFFE2E4EB;
            case CLASSIC: return 0xFF0A246A;
            case VISTA: return 0xDD1B222A;
            case WIN7_AERO: return 0xCC1A385C;
            case LUNA_BLUE:
            default:
                return 0xFF0055EA;
        }
    }

    public int getTitlebarActiveEnd() {
        switch (currentTheme) {
            case LUNA_OLIVE: return 0xFF45672E;
            case LUNA_SILVER: return 0xFFA6ABB8;
            case CLASSIC: return 0xFFA6CAF0;
            case VISTA: return 0xEE0D1217;
            case WIN7_AERO: return 0xDD28588E;
            case LUNA_BLUE:
            default:
                return 0xFF00138C;
        }
    }

    public int getTitlebarInactiveStart() {
        switch (currentTheme) {
            case LUNA_OLIVE: return 0xFF8FA87B;
            case LUNA_SILVER: return 0xFFD8DAE0;
            case CLASSIC: return 0xFF808080;
            case VISTA: return 0xAA2B333E;
            case WIN7_AERO: return 0x992B4566;
            case LUNA_BLUE:
            default:
                return 0xFF7A96DF;
        }
    }

    public int getTitlebarInactiveEnd() {
        switch (currentTheme) {
            case LUNA_OLIVE: return 0xFF6C825B;
            case LUNA_SILVER: return 0xFFB0B4C0;
            case CLASSIC: return 0xFFB5B5B5;
            case VISTA: return 0xCC1A2028;
            case WIN7_AERO: return 0xAA3B5B84;
            case LUNA_BLUE:
            default:
                return 0xFF5A70B2;
        }
    }

    public int getTitlebarTextColor() {
        switch (currentTheme) {
            case LUNA_SILVER: return 0xFF000000;
            default: return 0xFFFFFFFF;
        }
    }

    public int getWindowBorderColor() {
        switch (currentTheme) {
            case LUNA_OLIVE: return 0xFF45672E;
            case LUNA_SILVER: return 0xFFA6ABB8;
            case CLASSIC: return 0xFFD4D0C8;
            case VISTA: return 0xFF2A3440;
            case WIN7_AERO: return 0x99356699;
            case LUNA_BLUE:
            default:
                return 0xFF00138C;
        }
    }

    public int getTaskbarBgRes() {
        switch (currentTheme) {
            case LUNA_OLIVE: return R.drawable.taskbar_bg_olive;
            case LUNA_SILVER: return R.drawable.taskbar_bg_silver;
            case CLASSIC: return R.drawable.taskbar_bg_classic;
            case VISTA: return R.drawable.taskbar_bg_vista;
            case WIN7_AERO: return R.drawable.taskbar_bg_win7;
            case LUNA_BLUE:
            default:
                return R.drawable.taskbar_bg;
        }
    }

    public int getStartButtonBgRes() {
        switch (currentTheme) {
            case LUNA_OLIVE: return R.drawable.start_button_olive;
            case LUNA_SILVER: return R.drawable.start_button_silver;
            case CLASSIC: return R.drawable.start_button_classic;
            case VISTA: return R.drawable.start_button_vista;
            case WIN7_AERO: return R.drawable.start_button_win7;
            case LUNA_BLUE:
            default:
                return R.drawable.start_button_bg;
        }
    }

    public int getStartIconRes() {
        switch (currentTheme) {
            case CLASSIC: return R.drawable.ic_win_flag_classic;
            case VISTA: return R.drawable.ic_orb_vista;
            case WIN7_AERO: return R.drawable.ic_orb_win7;
            case LUNA_OLIVE:
            case LUNA_SILVER:
            case LUNA_BLUE:
            default:
                return R.drawable.ic_win_flag;
        }
    }

    public String getStartButtonText() {
        switch (currentTheme) {
            case CLASSIC: return "Start";
            case VISTA:
            case WIN7_AERO:
                return ""; // Orb only
            case LUNA_OLIVE:
            case LUNA_SILVER:
            case LUNA_BLUE:
            default:
                return "start";
        }
    }

    public int getWallpaperRes() {
        switch (currentTheme) {
            case CLASSIC: return R.drawable.wallpaper_classic;
            case VISTA: return R.drawable.wallpaper_vista;
            case WIN7_AERO: return R.drawable.wallpaper_win7;
            case LUNA_OLIVE:
            case LUNA_SILVER:
            case LUNA_BLUE:
            default:
                return R.drawable.bliss_wallpaper;
        }
    }
}
