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
        return getAuthenticStartButtonRes();
    }

    public int getAuthenticStartButtonRes() {
        switch (currentTheme) {
            case LUNA_OLIVE: return R.drawable.start_button_luna_olive;
            case LUNA_SILVER: return R.drawable.start_button_luna_silver;
            case CLASSIC: return R.drawable.start_button_classic;
            case VISTA: return R.drawable.start_button_vista;
            case WIN7_AERO: return R.drawable.start_button_win7;
            case LUNA_BLUE:
            default:
                return R.drawable.start_button_luna_blue;
        }
    }

    public int getMyComputerIconRes() {
        switch (currentTheme) {
            case CLASSIC: return R.drawable.ic_classic_my_computer;
            case VISTA: return R.drawable.ic_vista_my_computer;
            case WIN7_AERO: return R.drawable.ic_win7_my_computer;
            default: return R.drawable.ic_my_computer;
        }
    }

    public int getDriveCIconRes() {
        switch (currentTheme) {
            case CLASSIC: return R.drawable.ic_classic_drive_c;
            case VISTA: return R.drawable.ic_vista_drive_c;
            case WIN7_AERO: return R.drawable.ic_win7_drive_c;
            default: return R.drawable.ic_drive_c;
        }
    }

    public int getFolderIconRes() {
        switch (currentTheme) {
            case CLASSIC: return R.drawable.ic_classic_folder;
            case VISTA: return R.drawable.ic_vista_folder;
            case WIN7_AERO: return R.drawable.ic_win7_folder;
            default: return R.drawable.ic_folder;
        }
    }

    public int getRecycleBinIconRes() {
        switch (currentTheme) {
            case CLASSIC: return R.drawable.ic_classic_recycle_bin;
            case VISTA: return R.drawable.ic_vista_recycle_bin;
            case WIN7_AERO: return R.drawable.ic_win7_recycle_bin;
            default: return R.drawable.ic_recycle_bin;
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
            case LUNA_OLIVE: return R.drawable.wallpaper_olive;
            case LUNA_SILVER: return R.drawable.wallpaper_silver;
            case LUNA_BLUE:
            default:
                return R.drawable.bliss_wallpaper;
        }
    }

    public int[] getStartMenuHeaderColors() {
        switch (currentTheme) {
            case LUNA_OLIVE: return new int[]{0xFF759954, 0xFF45672E};
            case LUNA_SILVER: return new int[]{0xFFD8DAE0, 0xFFA6ABB8};
            case CLASSIC: return new int[]{0xFF0A246A, 0xFFA6CAF0};
            case VISTA: return new int[]{0xFF283444, 0xFF0B1017};
            case WIN7_AERO: return new int[]{0xFF224874, 0xFF112338};
            case LUNA_BLUE:
            default:
                return new int[]{0xFF245EDC, 0xFF153B8A};
        }
    }

    public int[] getStartMenuBottomColors() {
        switch (currentTheme) {
            case LUNA_OLIVE: return new int[]{0xFF658A44, 0xFF3D5C26};
            case LUNA_SILVER: return new int[]{0xFFC8CBD2, 0xFF959AA6};
            case CLASSIC: return new int[]{0xFF0A246A, 0xFF7090C0};
            case VISTA: return new int[]{0xFF1B222A, 0xFF080C12};
            case WIN7_AERO: return new int[]{0xFF18385A, 0xFF0D1E30};
            case LUNA_BLUE:
            default:
                return new int[]{0xFF2D66E0, 0xFF183E9E};
        }
    }
}
