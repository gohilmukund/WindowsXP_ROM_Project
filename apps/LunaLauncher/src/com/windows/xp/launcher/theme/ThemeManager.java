package com.windows.xp.launcher.theme;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import com.windows.xp.launcher.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ThemeManager {

    public static class WallpaperItem {
        private final String name;
        private final int resId;
        private final String filePath;

        public WallpaperItem(String name, int resId) {
            this.name = name;
            this.resId = resId;
            this.filePath = null;
        }

        public WallpaperItem(String name, String filePath) {
            this.name = name;
            this.resId = -1;
            this.filePath = filePath;
        }

        public String getName() { return name; }
        public int getResId() { return resId; }
        public String getFilePath() { return filePath; }
        public boolean isCustom() { return filePath != null; }

        @Override
        public String toString() {
            return name;
        }
    }

    public enum Theme {
        LUNA_BLUE("Windows XP (Default Blue)"),
        LUNA_OLIVE("Windows XP (Olive Green)"),
        LUNA_SILVER("Windows XP (Silver)"),
        ROYALE("Windows XP (Royale / Media Center)"),
        CLASSIC("Windows Classic"),
        VISTA("Windows Vista"),
        WIN7_AERO("Windows 7 Aero Glass"),
        WIN11("Windows 11 (Fluent)");

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

    private static final String PREF_NAME = "xp_theme_prefs";
    private static final String KEY_THEME = "current_theme";
    private static final String KEY_WALLPAPER_RES = "wallpaper_res";
    private static final String KEY_WALLPAPER_PATH = "wallpaper_path";

    private static ThemeManager instance;
    private Theme currentTheme = Theme.LUNA_BLUE;
    private int userWallpaperRes = -1;
    private String userWallpaperPath = null;
    private Context appContext;
    private final List<OnThemeChangeListener> listeners = new ArrayList<OnThemeChangeListener>();

    public static synchronized ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    private ThemeManager() {}

    public void init(Context context) {
        if (context == null) return;
        if (this.appContext == null) {
            this.appContext = context.getApplicationContext();
            loadPreferences();
        }
    }

    private void loadPreferences() {
        if (appContext == null) return;
        try {
            SharedPreferences sp = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String themeStr = sp.getString(KEY_THEME, null);
            if (themeStr != null) {
                try {
                    this.currentTheme = Theme.valueOf(themeStr);
                } catch (Exception ignored) {}
            }
            this.userWallpaperRes = sp.getInt(KEY_WALLPAPER_RES, -1);
            this.userWallpaperPath = sp.getString(KEY_WALLPAPER_PATH, null);
        } catch (Exception ignored) {}
    }

    private void savePreferences() {
        if (appContext == null) return;
        try {
            SharedPreferences sp = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(KEY_THEME, currentTheme.name());
            editor.putInt(KEY_WALLPAPER_RES, userWallpaperRes);
            editor.putString(KEY_WALLPAPER_PATH, userWallpaperPath);
            editor.apply();
        } catch (Exception ignored) {}
    }

    public Theme getCurrentTheme() {
        return currentTheme;
    }

    public void setTheme(Theme theme) {
        if (theme == null) return;
        if (this.currentTheme != theme) {
            this.currentTheme = theme;
            this.userWallpaperRes = -1;
            this.userWallpaperPath = null;
            savePreferences();
            notifyListeners();
        }
    }

    public void setUserWallpaperRes(int resId) {
        this.userWallpaperRes = resId;
        this.userWallpaperPath = null;
        savePreferences();
        notifyListeners();
    }

    public void setUserWallpaperPath(String path) {
        this.userWallpaperPath = path;
        this.userWallpaperRes = -1;
        savePreferences();
        notifyListeners();
    }

    public int getUserWallpaperRes() {
        return userWallpaperRes;
    }

    public String getUserWallpaperPath() {
        return userWallpaperPath;
    }

    public boolean hasCustomWallpaper() {
        return (userWallpaperPath != null && new File(userWallpaperPath).exists()) || userWallpaperRes > 0;
    }

    public void clearCustomWallpaper() {
        this.userWallpaperRes = -1;
        this.userWallpaperPath = null;
        savePreferences();
        notifyListeners();
    }

    public List<WallpaperItem> getBuiltInWallpapers() {
        List<WallpaperItem> list = new ArrayList<WallpaperItem>();
        list.add(new WallpaperItem("Bliss (Windows XP Default)", R.drawable.bliss_wallpaper));
        list.add(new WallpaperItem("Autumn", R.drawable.wallpaper_autumn));
        list.add(new WallpaperItem("Azul", R.drawable.wallpaper_azul));
        list.add(new WallpaperItem("Red moon desert", R.drawable.wallpaper_desert));
        list.add(new WallpaperItem("Stonehenge", R.drawable.wallpaper_stonehenge));
        list.add(new WallpaperItem("Tulips", R.drawable.wallpaper_tulips));
        list.add(new WallpaperItem("Wind", R.drawable.wallpaper_wind));
        list.add(new WallpaperItem("Silver", R.drawable.wallpaper_silver));
        list.add(new WallpaperItem("Olive Green", R.drawable.wallpaper_olive));
        list.add(new WallpaperItem("Royale Energy Blue", R.drawable.wallpaper_royale));
        list.add(new WallpaperItem("Windows Classic Teal", R.drawable.wallpaper_classic));
        list.add(new WallpaperItem("Windows Vista Aurora", R.drawable.wallpaper_vista));
        list.add(new WallpaperItem("Windows 7 Harmony", R.drawable.wallpaper_win7));
        list.add(new WallpaperItem("Windows 11 Bloom", R.drawable.wallpaper_win11));
        return list;
    }

    public void applyWallpaperToView(View desktop) {
        if (desktop == null) return;
        Context ctx = desktop.getContext();
        if (userWallpaperPath != null) {
            File f = new File(userWallpaperPath);
            if (f.exists()) {
                Bitmap bmp = decodeSampledBitmapFromFile(userWallpaperPath, 1024, 600);
                if (bmp != null) {
                    desktop.setBackgroundDrawable(new BitmapDrawable(ctx.getResources(), bmp));
                    return;
                }
            }
        }
        if (userWallpaperRes > 0) {
            try {
                desktop.setBackgroundResource(userWallpaperRes);
                return;
            } catch (Throwable ignored) {}
        }
        desktop.setBackgroundResource(getWallpaperRes());
    }

    public static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);

            if (options.outWidth <= 0 || options.outHeight <= 0) {
                return null;
            }

            int inSampleSize = 1;
            if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
                final int halfHeight = options.outHeight / 2;
                final int halfWidth = options.outWidth / 2;
                while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2;
                }
            }

            options.inSampleSize = inSampleSize;
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            return BitmapFactory.decodeFile(path, options);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    public void addListener(OnThemeChangeListener l) {
        if (l == null) return;
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    public void removeListener(OnThemeChangeListener l) {
        listeners.remove(l);
    }

    private void notifyListeners() {
        for (OnThemeChangeListener l : new ArrayList<OnThemeChangeListener>(listeners)) {
            l.onThemeChanged(currentTheme);
        }
    }

    // --- Theme Color & Asset Accessors ---

    public int getTitlebarActiveStart() {
        switch (currentTheme) {
            case LUNA_OLIVE: return 0xFF759954;
            case LUNA_SILVER: return 0xFFE2E4EB;
            case ROYALE: return 0xFF1073E6;
            case CLASSIC: return 0xFF0A246A;
            case VISTA: return 0xDD1B222A;
            case WIN7_AERO: return 0xCC1A385C;
            case WIN11: return 0xFFE8E8E8;
            case LUNA_BLUE:
            default:
                return 0xFF0055EA;
        }
    }

    public int getTitlebarActiveEnd() {
        switch (currentTheme) {
            case LUNA_OLIVE: return 0xFF45672E;
            case LUNA_SILVER: return 0xFFA6ABB8;
            case ROYALE: return 0xFF0048B3;
            case CLASSIC: return 0xFFA6CAF0;
            case VISTA: return 0xEE0D1217;
            case WIN7_AERO: return 0xDD28588E;
            case WIN11: return 0xFFE8E8E8;
            case LUNA_BLUE:
            default:
                return 0xFF00138C;
        }
    }

    public int getTitlebarInactiveStart() {
        switch (currentTheme) {
            case LUNA_OLIVE: return 0xFF8FA87B;
            case LUNA_SILVER: return 0xFFD8DAE0;
            case ROYALE: return 0xFF7A96DF;
            case CLASSIC: return 0xFF808080;
            case VISTA: return 0xAA2B333E;
            case WIN7_AERO: return 0x992B4566;
            case WIN11: return 0xFFF3F3F3;
            case LUNA_BLUE:
            default:
                return 0xFF7A96DF;
        }
    }

    public int getTitlebarInactiveEnd() {
        switch (currentTheme) {
            case LUNA_OLIVE: return 0xFF6C825B;
            case LUNA_SILVER: return 0xFFB0B4C0;
            case ROYALE: return 0xFF5A70B2;
            case CLASSIC: return 0xFFB5B5B5;
            case VISTA: return 0xCC1A2028;
            case WIN7_AERO: return 0xAA3B5B84;
            case WIN11: return 0xFFF3F3F3;
            case LUNA_BLUE:
            default:
                return 0xFF5A70B2;
        }
    }

    public int getTitlebarTextColor() {
        switch (currentTheme) {
            case LUNA_SILVER:
            case WIN11:
                return 0xFF1C1C1C;
            default:
                return 0xFFFFFFFF;
        }
    }

    public int getWindowBorderColor() {
        switch (currentTheme) {
            case LUNA_OLIVE: return 0xFF45672E;
            case LUNA_SILVER: return 0xFFA6ABB8;
            case ROYALE: return 0xFF0048B3;
            case CLASSIC: return 0xFFD4D0C8;
            case VISTA: return 0xFF2A3440;
            case WIN7_AERO: return 0x99356699;
            case WIN11: return 0xFFD0D0D0;
            case LUNA_BLUE:
            default:
                return 0xFF00138C;
        }
    }

    public int getTaskbarBgRes() {
        switch (currentTheme) {
            case LUNA_OLIVE: return R.drawable.taskbar_bg_olive;
            case LUNA_SILVER: return R.drawable.taskbar_bg_silver;
            case ROYALE: return R.drawable.taskbar_bg_royale;
            case CLASSIC: return R.drawable.taskbar_bg_classic;
            case VISTA: return R.drawable.taskbar_bg_vista;
            case WIN7_AERO: return R.drawable.taskbar_bg_win7;
            case WIN11: return R.drawable.taskbar_bg_win11;
            case LUNA_BLUE:
            default:
                return R.drawable.taskbar_bg;
        }
    }

    public int getTaskbarTabBgRes() {
        switch (currentTheme) {
            case LUNA_OLIVE: return R.drawable.taskbar_tab_bg_olive;
            case LUNA_SILVER: return R.drawable.taskbar_tab_bg_silver;
            case ROYALE: return R.drawable.taskbar_tab_bg_royale;
            case CLASSIC: return R.drawable.taskbar_tab_bg_classic;
            case VISTA: return R.drawable.taskbar_tab_bg_vista;
            case WIN7_AERO: return R.drawable.taskbar_tab_bg_win7;
            case WIN11: return R.drawable.taskbar_tab_bg_win11;
            case LUNA_BLUE:
            default:
                return R.drawable.taskbar_tab_bg;
        }
    }

    public int getTaskbarTabActiveRes() {
        switch (currentTheme) {
            case LUNA_OLIVE: return R.drawable.taskbar_tab_active_olive;
            case LUNA_SILVER: return R.drawable.taskbar_tab_active_silver;
            case ROYALE: return R.drawable.taskbar_tab_active_royale;
            case CLASSIC: return R.drawable.taskbar_tab_active_classic;
            case VISTA: return R.drawable.taskbar_tab_active_vista;
            case WIN7_AERO: return R.drawable.taskbar_tab_active_win7;
            case WIN11: return R.drawable.taskbar_tab_active_win11;
            case LUNA_BLUE:
            default:
                return R.drawable.taskbar_tab_active;
        }
    }

    public int getTaskbarTabTextColor() {
        switch (currentTheme) {
            case LUNA_SILVER:
            case CLASSIC:
            case WIN11:
                return 0xFF1C1C1C;
            default:
                return 0xFFFFFFFF;
        }
    }

    public int getTaskbarTrayBgRes() {
        switch (currentTheme) {
            case LUNA_OLIVE: return R.drawable.tray_bg_olive;
            case LUNA_SILVER: return R.drawable.tray_bg_silver;
            case ROYALE: return R.drawable.tray_bg_royale;
            case WIN7_AERO: return R.drawable.tray_bg_win7;
            case WIN11: return R.drawable.tray_bg_win11;
            case CLASSIC: return R.drawable.taskbar_bg_classic;
            case VISTA: return R.drawable.tray_bg_vista;
            case LUNA_BLUE:
            default:
                return R.drawable.tray_bg;
        }
    }

    public int getTaskbarTrayTextColor() {
        switch (currentTheme) {
            case CLASSIC:
            case LUNA_SILVER:
            case WIN11:
                return 0xFF1C1C1C;
            default:
                return 0xFFFFFFFF;
        }
    }

    public int getTaskbarTrayBatteryColor() {
        switch (currentTheme) {
            case LUNA_OLIVE: return 0xFFCBE4B5;
            case LUNA_SILVER: return 0xFF4A4E58;
            case ROYALE: return 0xFFB8D3FF;
            case WIN11: return 0xFF404040;
            case CLASSIC: return 0xFF000000;
            case VISTA: return 0xFFD6D6D6;
            case WIN7_AERO: return 0xFFD0E4FA;
            case LUNA_BLUE:
            default:
                return 0xFFC5D9F8;
        }
    }

    public int getStartMenuRightPaneBgColor() {
        switch (currentTheme) {
            case LUNA_OLIVE: return 0xFFE6EAD5;
            case LUNA_SILVER: return 0xFFEAECF0;
            case ROYALE: return 0xFFD2DBEE;
            case WIN7_AERO: return 0xEE182C44;
            case WIN11: return 0xFFF5F6F8;
            case CLASSIC: return 0xFFD4D0C8;
            case VISTA: return 0xEE1E242B;
            case LUNA_BLUE:
            default:
                return 0xFFD3E5FA;
        }
    }

    public int getStartMenuRightPaneTextColor() {
        switch (currentTheme) {
            case LUNA_OLIVE: return 0xFF354B1A;
            case ROYALE: return 0xFF00156E;
            case VISTA:
            case WIN7_AERO: return 0xFFFFFFFF;
            case WIN11: return 0xFF1A1A1A;
            case CLASSIC:
            case LUNA_SILVER:
                return 0xFF000000;
            case LUNA_BLUE:
            default:
                return 0xFF00156E;
        }
    }

    public int getStartMenuDividerColor() {
        switch (currentTheme) {
            case LUNA_OLIVE: return 0xFFCBD4B6;
            case LUNA_SILVER: return 0xFFCCD0DA;
            case ROYALE: return 0xFFAEC5E8;
            case VISTA: return 0xFF3A4B5E;
            case WIN7_AERO: return 0xFF3A5370;
            case WIN11: return 0xFFEAEAEA;
            case CLASSIC: return 0xFF808080;
            case LUNA_BLUE:
            default:
                return 0xFFB5CFF5;
        }
    }

    public int getStartButtonWidthDp() {
        switch (currentTheme) {
            case VISTA:
            case WIN7_AERO:
                return 48;
            case WIN11:
                return 42;
            case CLASSIC:
                return 65;
            case LUNA_BLUE:
            case LUNA_OLIVE:
            case LUNA_SILVER:
            case ROYALE:
            default:
                return 106;
        }
    }

    public int getStartButtonBgRes() {
        return getAuthenticStartButtonRes();
    }

    public int getAuthenticStartButtonRes() {
        switch (currentTheme) {
            case LUNA_OLIVE: return R.drawable.start_button_luna_olive;
            case LUNA_SILVER: return R.drawable.start_button_luna_silver;
            case ROYALE: return R.drawable.start_button_royale;
            case WIN11: return R.drawable.start_button_win11;
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
            case WIN11:
                return ""; // Orb only
            case LUNA_OLIVE:
            case LUNA_SILVER:
            case ROYALE:
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
            case ROYALE: return R.drawable.wallpaper_royale;
            case WIN11: return R.drawable.wallpaper_win11;
            case LUNA_BLUE:
            default:
                return R.drawable.bliss_wallpaper;
        }
    }

    public int[] getStartMenuHeaderColors() {
        switch (currentTheme) {
            case LUNA_OLIVE: return new int[]{0xFF759954, 0xFF45672E};
            case LUNA_SILVER: return new int[]{0xFFD8DAE0, 0xFFA6ABB8};
            case ROYALE: return new int[]{0xFF1858D6, 0xFF0A3DA8};
            case CLASSIC: return new int[]{0xFF0A246A, 0xFFA6CAF0};
            case VISTA: return new int[]{0xFF283444, 0xFF0B1017};
            case WIN7_AERO: return new int[]{0xFF224874, 0xFF112338};
            case WIN11: return new int[]{0xFFFFFFFF, 0xFFFFFFFF};
            case LUNA_BLUE:
            default:
                return new int[]{0xFF245EDC, 0xFF153B8A};
        }
    }

    public int[] getStartMenuBottomColors() {
        switch (currentTheme) {
            case LUNA_OLIVE: return new int[]{0xFF658A44, 0xFF3D5C26};
            case LUNA_SILVER: return new int[]{0xFFC8CBD2, 0xFF959AA6};
            case ROYALE: return new int[]{0xFF1858D6, 0xFF0A3DA8};
            case CLASSIC: return new int[]{0xFF0A246A, 0xFF7090C0};
            case VISTA: return new int[]{0xFF1B222A, 0xFF080C12};
            case WIN7_AERO: return new int[]{0xFF18385A, 0xFF0D1E30};
            case WIN11: return new int[]{0xFFF5F6F8, 0xFFF5F6F8};
            case LUNA_BLUE:
            default:
                return new int[]{0xFF2D66E0, 0xFF183E9E};
        }
    }
}
