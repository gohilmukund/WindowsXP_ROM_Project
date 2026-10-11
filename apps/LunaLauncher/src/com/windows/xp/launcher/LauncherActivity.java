package com.windows.xp.launcher;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.media.AudioManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import android.app.Dialog;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import com.windows.xp.launcher.service.FloatingWindowOverlayService;
import com.windows.xp.launcher.theme.ThemeManager;
import com.windows.xp.launcher.window.CmdPromptView;
import com.windows.xp.launcher.window.ControlPanelWindowView;
import com.windows.xp.launcher.window.DisplayPropertiesWindowView;
import com.windows.xp.launcher.window.ExplorerWindowView;
import com.windows.xp.launcher.window.IEWindowView;
import com.windows.xp.launcher.window.NetworkConnectionsWindowView;
import com.windows.xp.launcher.window.NotepadWindowView;
import com.windows.xp.launcher.window.PaintWindowView;
import com.windows.xp.launcher.window.PictureViewerWindowView;
import com.windows.xp.launcher.window.SystemPropertiesWindowView;
import com.windows.xp.launcher.window.WMPWindowView;
import com.windows.xp.launcher.window.WindowFrameView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LauncherActivity extends Activity implements ThemeManager.OnThemeChangeListener {

    private View startMenuContainer;
    private View allProgramsContainer;
    private LinearLayout allProgramsList;
    private LinearLayout btnStart;
    private LinearLayout taskbarTabsContainer;
    private TextView trayClock;
    private TextView trayBattery;
    private FrameLayout desktopWindowContainer;

    private LinearLayout volumePopupContainer;
    private SeekBar seekMasterVolume;
    private TextView tvVolumePercent;
    private CheckBox chkVolumeMute;
    private ImageView trayVolume;
    private final Runnable hideVolumeRunnable = new Runnable() {
        @Override
        public void run() {
            if (volumePopupContainer != null && volumePopupContainer.getVisibility() == View.VISIBLE) {
                volumePopupContainer.setVisibility(View.GONE);
            }
        }
    };

    private final Handler handler = new Handler(Looper.getMainLooper());
    private AltTabDialog altTabDialog;
    private ActivityManager activityManager;
    private PackageManager packageManager;

    private final List<WindowFrameView> openWindows = new ArrayList<WindowFrameView>();
    private WindowFrameView activeWindow = null;

    private static class AppInfoItem {
        final String title;
        final String packageName;
        final Drawable icon;

        AppInfoItem(String title, String packageName, Drawable icon) {
            this.title = title;
            this.packageName = packageName;
            this.icon = icon;
        }
    }

    private boolean isAllProgramsPopulated = false;
    private final List<AppInfoItem> cachedInstalledApps = new ArrayList<AppInfoItem>();

    private final BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL);

            if (level >= 0 && scale > 0) {
                int pct = (level * 100) / scale;
                trayBattery.setText(pct + "%" + (isCharging ? " ⚡" : ""));
            }
        }
    };

    private final BroadcastReceiver minimizeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (activeWindow != null && activeWindow.getVisibility() == View.VISIBLE) {
                activeWindow.minimize();
            }
            refreshTaskbarTabs();
        }
    };

    private final Runnable clockRunnable = new Runnable() {
        @Override
        public void run() {
            updateClock();
            refreshTaskbarTabs();
            handler.postDelayed(this, 3000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_launcher);

        activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        packageManager = getPackageManager();

        startMenuContainer = findViewById(R.id.start_menu_container);
        allProgramsContainer = findViewById(R.id.all_programs_container);
        allProgramsList = (LinearLayout) findViewById(R.id.all_programs_list);
        View btnCloseAllPrograms = findViewById(R.id.btn_close_all_programs);
        if (btnCloseAllPrograms != null) {
            btnCloseAllPrograms.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    closeAllPrograms();
                }
            });
        }

        btnStart = (LinearLayout) findViewById(R.id.btn_start);
        taskbarTabsContainer = (LinearLayout) findViewById(R.id.taskbar_tabs_container);
        trayClock = (TextView) findViewById(R.id.tray_clock);
        trayBattery = (TextView) findViewById(R.id.tray_battery);
        desktopWindowContainer = (FrameLayout) findViewById(R.id.desktop_window_container);

        setupStartButton();
        setupDesktopIcons();
        setupStartMenuItems();

        ThemeManager.getInstance().addListener(this);
        onThemeChanged(ThemeManager.getInstance().getCurrentTheme());

        // Register receivers
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        registerReceiver(minimizeReceiver, new IntentFilter("com.windows.xp.ACTION_MINIMIZE"));

        handler.post(clockRunnable);
        populateAllProgramsList();
        startPreloadingInstalledApps();
        setupVolumeControl();
        handleThemeIntent(getIntent());
        try {
            startService(new Intent(this, FloatingWindowOverlayService.class));
        } catch (Exception ignored) {}
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshTaskbarTabs();
        try {
            Intent closeOverlay = new Intent(this, FloatingWindowOverlayService.class);
            closeOverlay.setAction(FloatingWindowOverlayService.ACTION_CLOSE_OVERLAY);
            startService(closeOverlay);
        } catch (Exception ignored) {}
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent != null && Intent.ACTION_MAIN.equals(intent.getAction()) && intent.hasCategory(Intent.CATEGORY_HOME)) {
            toggleStartMenu();
        }
        handleThemeIntent(intent);
    }

    private void handleThemeIntent(Intent intent) {
        if (intent == null) return;
        if (intent.hasExtra("theme")) {
            String themeName = intent.getStringExtra("theme");
            try {
                ThemeManager.Theme t = ThemeManager.Theme.valueOf(themeName.toUpperCase());
                ThemeManager.getInstance().setTheme(t);
            } catch (Exception ignored) {}
        }
        if (intent.getBooleanExtra("toggle_start", false)) {
            toggleStartMenu();
        }
        if (intent.getBooleanExtra("toggle_all_programs", false)) {
            toggleAllPrograms();
        }
        if (intent.getBooleanExtra("close_all_windows", false)) {
            closeAllWindows();
        }
        if (intent.hasExtra("open_explorer")) {
            openExplorer(intent.getStringExtra("open_explorer"));
        }
        if (intent.getBooleanExtra("show_shutdown", false)) {
            showShutdownDialog();
        }
        if (intent.hasExtra("open_ie")) {
            openInternetExplorer(intent.getStringExtra("open_ie"));
        }
        if (intent.getBooleanExtra("open_wmp", false)) {
            openMediaPlayer(null);
        }
        if (intent.getBooleanExtra("open_picture_viewer", false)) {
            openPictureViewer(null);
        }
        if (intent.getBooleanExtra("toggle_volume", false)) {
            toggleVolumeControlPopup();
        }
        if (intent.getBooleanExtra("show_volume", false)) {
            showVolumeControlPopup(false);
        }
    }

    private void closeAllWindows() {
        List<WindowFrameView> copy = new ArrayList<WindowFrameView>(openWindows);
        for (WindowFrameView w : copy) {
            desktopWindowContainer.removeView(w);
        }
        openWindows.clear();
        activeWindow = null;
        refreshTaskbarTabs();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(clockRunnable);
        handler.removeCallbacks(hideVolumeRunnable);
        ThemeManager.getInstance().removeListener(this);
        try {
            unregisterReceiver(batteryReceiver);
            unregisterReceiver(minimizeReceiver);
        } catch (Exception ignored) {}
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            adjustVolume(AudioManager.ADJUST_RAISE);
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            adjustVolume(AudioManager.ADJUST_LOWER);
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            showAltTabSwitcher();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_HOME) {
            toggleStartMenu();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (altTabDialog != null && altTabDialog.isShowing()) {
                altTabDialog.dismiss();
                return true;
            }
            if (volumePopupContainer != null && volumePopupContainer.getVisibility() == View.VISIBLE) {
                volumePopupContainer.setVisibility(View.GONE);
                return true;
            }
            if (allProgramsContainer != null && allProgramsContainer.getVisibility() == View.VISIBLE) {
                allProgramsContainer.setVisibility(View.GONE);
                return true;
            }
            if (startMenuContainer.getVisibility() == View.VISIBLE) {
                closeStartMenu();
                return true;
            }
            // If active floating window is open, minimize it to taskbar
            if (activeWindow != null && activeWindow.getVisibility() == View.VISIBLE) {
                activeWindow.minimize();
                return true;
            }
            return true; // Ignore back on bare desktop to prevent quitting
        }
        return super.onKeyDown(keyCode, event);
    }

    public void toggleStartMenu() {
        if (startMenuContainer.getVisibility() == View.VISIBLE) {
            closeStartMenu();
        } else {
            startMenuContainer.setVisibility(View.VISIBLE);
            startMenuContainer.bringToFront();
        }
    }

    private void closeStartMenu() {
        if (startMenuContainer != null && startMenuContainer.getVisibility() == View.VISIBLE) {
            startMenuContainer.setVisibility(View.GONE);
        }
        closeAllPrograms();
    }

    private void closeAllPrograms() {
        if (allProgramsContainer != null && allProgramsContainer.getVisibility() == View.VISIBLE) {
            allProgramsContainer.setVisibility(View.GONE);
        }
    }

    private void toggleAllPrograms() {
        if (allProgramsContainer == null) return;
        if (allProgramsContainer.getVisibility() == View.VISIBLE) {
            allProgramsContainer.setVisibility(View.GONE);
        } else {
            if (!isAllProgramsPopulated) {
                populateAllProgramsList();
            }
            allProgramsContainer.setVisibility(View.VISIBLE);
            allProgramsContainer.bringToFront();
        }
    }

    private void showAltTabSwitcher() {
        if (altTabDialog != null && altTabDialog.isShowing()) {
            altTabDialog.dismiss();
        }
        altTabDialog = new AltTabDialog(this);
        altTabDialog.show();
    }

    private void setupStartButton() {
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleStartMenu();
            }
        });

        View rootDesktop = findViewById(R.id.root_desktop);
        if (rootDesktop != null) {
            rootDesktop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    closeStartMenu();
                    closeVolumePopup();
                }
            });
            rootDesktop.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    closeStartMenu();
                    closeVolumePopup();
                    openDisplayProperties();
                    return true;
                }
            });
        }
    }

    private void setupDesktopIcons() {
        findViewById(R.id.icon_my_computer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeStartMenu();
                openExplorer("computer://");
            }
        });

        findViewById(R.id.icon_my_documents).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeStartMenu();
                openExplorer("/sdcard/Documents");
            }
        });

        findViewById(R.id.icon_internet_explorer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeStartMenu();
                openInternetExplorer("https://www.google.com");
            }
        });

        findViewById(R.id.icon_wmp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeStartMenu();
                openMediaPlayer(null);
            }
        });

        findViewById(R.id.icon_fotoo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeStartMenu();
                openPictureViewer(null);
            }
        });

        findViewById(R.id.icon_paint).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeStartMenu();
                openPaint();
            }
        });

        findViewById(R.id.icon_notepad).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeStartMenu();
                openNotepad(null);
            }
        });

        findViewById(R.id.icon_cmd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeStartMenu();
                openCommandPrompt();
            }
        });

        findViewById(R.id.icon_games).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeStartMenu();
                showGamesDialog();
            }
        });

        findViewById(R.id.icon_recycle_bin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeStartMenu();
                Toast.makeText(LauncherActivity.this, "Recycle Bin is empty.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupStartMenuItems() {
        findViewById(R.id.sm_item_ie).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeStartMenu();
                openInternetExplorer("https://www.google.com");
            }
        });

        findViewById(R.id.sm_item_wmp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeStartMenu();
                openMediaPlayer(null);
            }
        });

        findViewById(R.id.sm_item_paint).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeStartMenu();
                openPaint();
            }
        });

        findViewById(R.id.sm_item_notepad).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeStartMenu();
                openNotepad(null);
            }
        });

        findViewById(R.id.sm_item_cmd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeStartMenu();
                openCommandPrompt();
            }
        });

        findViewById(R.id.sm_item_fotoo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeStartMenu();
                openPictureViewer(null);
            }
        });

        findViewById(R.id.sm_item_all_programs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleAllPrograms();
            }
        });

        findViewById(R.id.sm_item_my_docs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeStartMenu();
                openExplorer("/sdcard/Documents");
            }
        });

        findViewById(R.id.sm_item_my_pics).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeStartMenu();
                openPictureViewer(null);
            }
        });

        findViewById(R.id.sm_item_my_comp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeStartMenu();
                openExplorer("computer://");
            }
        });

        findViewById(R.id.sm_item_control_panel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeStartMenu();
                openControlPanel();
            }
        });

        findViewById(R.id.btn_sm_logoff).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeStartMenu();
                Toast.makeText(LauncherActivity.this, "Windows XP: Session Locked.", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btn_sm_shutdown).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeStartMenu();
                showShutdownDialog();
            }
        });
    }

    private void populateAllProgramsList() {
        if (allProgramsList == null) return;
        allProgramsList.removeAllViews();

        // 1. Windows Accessories
        addProgramSectionHeader(allProgramsList, "Accessories");
        addProgramItem(allProgramsList, null, R.drawable.ic_cmd, "Command Prompt", new Runnable() {
            @Override
            public void run() { openCommandPrompt(); }
        });
        addProgramItem(allProgramsList, null, R.drawable.ic_notepad, "Notepad", new Runnable() {
            @Override
            public void run() { openNotepad(null); }
        });
        addProgramItem(allProgramsList, null, R.drawable.ic_paint, "Paint", new Runnable() {
            @Override
            public void run() { openPaint(); }
        });
        addProgramItem(allProgramsList, null, R.drawable.ic_my_computer, "Windows Explorer", new Runnable() {
            @Override
            public void run() { openExplorer("computer://"); }
        });

        // 2. Games
        addProgramSectionHeader(allProgramsList, "Games");
        addProgramItem(allProgramsList, null, R.drawable.ic_games, "3D Pinball (Space Cadet)", new Runnable() {
            @Override
            public void run() {
                if (!launchApp("com.fexed.spacecadetpinball")) {
                    launchApp("com.windows.xp.pinball");
                }
            }
        });
        addProgramItem(allProgramsList, null, R.drawable.ic_games, "Minesweeper", new Runnable() {
            @Override
            public void run() { launchApp("com.windows.xp.minesweeper"); }
        });
        addProgramItem(allProgramsList, null, R.drawable.ic_games, "Solitaire", new Runnable() {
            @Override
            public void run() { launchApp("com.windows.xp.solitaire"); }
        });

        // 3. System Tools & Settings
        addProgramSectionHeader(allProgramsList, "System Tools");
        addProgramItem(allProgramsList, null, R.drawable.ic_control_panel, "Control Panel", new Runnable() {
            @Override
            public void run() { openControlPanel(); }
        });
        addProgramItem(allProgramsList, null, R.drawable.ic_system_properties, "System Properties", new Runnable() {
            @Override
            public void run() { openSystemProperties(); }
        });
        addProgramItem(allProgramsList, null, R.drawable.ic_network, "Network Connections", new Runnable() {
            @Override
            public void run() { openNetworkConnections(); }
        });
        addProgramItem(allProgramsList, null, R.drawable.ic_display, "Display Properties", new Runnable() {
            @Override
            public void run() { openDisplayProperties(); }
        });

        // 4. Internet & Media
        addProgramSectionHeader(allProgramsList, "Internet & Media");
        addProgramItem(allProgramsList, null, R.drawable.ic_internet_explorer, "Internet Explorer", new Runnable() {
            @Override
            public void run() {
                if (!launchApp("com.windows.xp.internetexplorer")) {
                    launchApp("org.mozilla.firefox");
                }
            }
        });
        addProgramItem(allProgramsList, null, R.drawable.ic_wmp, "Windows Media Player", new Runnable() {
            @Override
            public void run() {
                openMediaPlayer(null);
            }
        });
        addProgramItem(allProgramsList, null, R.drawable.ic_picture_viewer, "Windows Picture and Fax Viewer", new Runnable() {
            @Override
            public void run() {
                openPictureViewer(null);
            }
        });
        addProgramItem(allProgramsList, null, R.drawable.ic_fotoo, "Fotoo Digital Frame", new Runnable() {
            @Override
            public void run() {
                if (!launchApp("com.bo.fotoo")) {
                    launchApp("com.boombuler.fotoo");
                }
            }
        });

        // 5. Installed Android Applications (Instant from cached items)
        if (!cachedInstalledApps.isEmpty()) {
            addProgramSectionHeader(allProgramsList, "Installed Applications (" + cachedInstalledApps.size() + ")");
            for (final AppInfoItem app : cachedInstalledApps) {
                addProgramItem(allProgramsList, app.icon, R.drawable.ic_my_computer, app.title, new Runnable() {
                    @Override
                    public void run() {
                        launchApp(app.packageName);
                    }
                });
            }
        }
        isAllProgramsPopulated = true;
    }

    private void startPreloadingInstalledApps() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                    mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                    List<ResolveInfo> pkgAppsList = packageManager.queryIntentActivities(mainIntent, 0);
                    String myPkg = getPackageName();

                    final List<AppInfoItem> items = new ArrayList<AppInfoItem>();
                    for (ResolveInfo ri : pkgAppsList) {
                        if (ri.activityInfo == null) continue;
                        String pkgName = ri.activityInfo.packageName;
                        if (myPkg.equals(pkgName)) continue;

                        CharSequence label = ri.loadLabel(packageManager);
                        String title = label != null ? label.toString() : ri.activityInfo.name;
                        Drawable icon = null;
                        try {
                            icon = ri.loadIcon(packageManager);
                        } catch (Exception ignored) {}

                        items.add(new AppInfoItem(title, pkgName, icon));
                    }

                    Collections.sort(items, new Comparator<AppInfoItem>() {
                        @Override
                        public int compare(AppInfoItem a, AppInfoItem b) {
                            return a.title.compareToIgnoreCase(b.title);
                        }
                    });

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            cachedInstalledApps.clear();
                            cachedInstalledApps.addAll(items);
                            populateAllProgramsList();
                        }
                    });
                } catch (Exception ignored) {}
            }
        }).start();
    }

    private void addProgramSectionHeader(LinearLayout container, String title) {
        TextView header = new TextView(this);
        header.setText(title);
        header.setTextColor(0xFF003399);
        header.setTypeface(null, android.graphics.Typeface.BOLD);
        header.setTextSize(10);
        header.setPadding(dpToPx(6), dpToPx(6), dpToPx(6), dpToPx(2));
        container.addView(header);
    }

    private void addProgramItem(LinearLayout container, Drawable icon, int iconRes, String title, final Runnable action) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4));
        row.setBackgroundResource(R.drawable.menu_item_selector);
        row.setClickable(true);

        ImageView img = new ImageView(this);
        if (icon != null) {
            img.setImageDrawable(icon);
        } else {
            img.setImageResource(iconRes);
        }
        LinearLayout.LayoutParams imgLp = new LinearLayout.LayoutParams(dpToPx(20), dpToPx(20));
        row.addView(img, imgLp);

        TextView tv = new TextView(this);
        tv.setText(title);
        tv.setTextColor(0xFF000000);
        tv.setTextSize(11);
        tv.setMaxLines(1);
        LinearLayout.LayoutParams tvLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvLp.leftMargin = dpToPx(8);
        row.addView(tv, tvLp);

        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeAllPrograms();
                closeStartMenu();
                if (action != null) action.run();
            }
        });

        container.addView(row);
    }

    private void setDesktopLauncher(int viewId, final String pkg, final Runnable fallback) {
        View v = findViewById(viewId);
        if (v == null) return;
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeStartMenu();
                if (!launchApp(pkg) && fallback != null) {
                    fallback.run();
                }
            }
        });
    }

    // --- Multi-Window Management ---

    public void openWindow(final WindowFrameView window, final String title, final int iconRes, int widthDp, int heightDp) {
        int cascade = (openWindows.size() % 6) * 28;
        int x = dpToPx(50 + cascade);
        int y = dpToPx(30 + cascade);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(dpToPx(widthDp), dpToPx(heightDp));
        window.setLayoutParams(lp);
        window.setX(x);
        window.setY(y);
        window.setTitle(title);
        window.setIcon(iconRes);

        window.setWindowListener(new WindowFrameView.WindowListener() {
            @Override
            public void onWindowFocus(WindowFrameView w) {
                activateWindow(w);
            }

            @Override
            public void onWindowMinimize(WindowFrameView w) {
                if (activeWindow == w) {
                    activeWindow = null;
                }
                refreshTaskbarTabs();
            }

            @Override
            public void onWindowMaximize(WindowFrameView w, boolean isMaximized) {
                activateWindow(w);
            }

            @Override
            public void onWindowClose(WindowFrameView w) {
                openWindows.remove(w);
                if (activeWindow == w) {
                    activeWindow = openWindows.isEmpty() ? null : openWindows.get(openWindows.size() - 1);
                }
                refreshTaskbarTabs();
            }
        });

        desktopWindowContainer.addView(window);
        openWindows.add(window);
        activateWindow(window);
    }

    public void activateWindow(WindowFrameView window) {
        closeVolumePopup();
        for (WindowFrameView w : openWindows) {
            w.setActive(w == window);
        }
        if (window != null) {
            window.bringToFront();
        }
        activeWindow = window;
        refreshTaskbarTabs();
    }

    public void openCommandPrompt() {
        final WindowFrameView win = new WindowFrameView(this);
        CmdPromptView cmdView = new CmdPromptView(this);
        cmdView.setOnExitListener(new CmdPromptView.OnExitListener() {
            @Override
            public void onExit() {
                win.close();
            }
        });
        win.setWindowContent(cmdView);
        openWindow(win, "C:\\WINDOWS\\system32\\cmd.exe", R.drawable.ic_cmd, 560, 360);
    }

    public void openNotepad(File file) {
        WindowFrameView win = new WindowFrameView(this);
        NotepadWindowView notepadView = new NotepadWindowView(this);
        win.setWindowContent(notepadView);
        String title = file != null ? file.getName() + " - Notepad" : "Untitled - Notepad";
        openWindow(win, title, R.drawable.ic_notepad, 540, 380);
    }

    public void openPaint() {
        WindowFrameView win = new WindowFrameView(this);
        PaintWindowView paintView = new PaintWindowView(this);
        win.setWindowContent(paintView);
        openWindow(win, "untitled - Paint", R.drawable.ic_paint, 580, 420);
    }

    public void openExplorer(String path) {
        final WindowFrameView win = new WindowFrameView(this);
        ExplorerWindowView explorerView = new ExplorerWindowView(this);
        explorerView.setFileActionListener(new ExplorerWindowView.FileActionListener() {
            @Override
            public void onOpenTextFile(File file) {
                openNotepad(file);
            }

            @Override
            public void onOpenMediaFile(File file) {
                openMediaPlayer(file);
            }

            @Override
            public void onOpenImageFile(File file) {
                openPictureViewer(file);
            }
        });
        explorerView.setOnTitleChangeListener(new ExplorerWindowView.OnTitleChangeListener() {
            @Override
            public void onTitleChange(String title, int iconRes) {
                win.setTitle(title);
                win.setIcon(iconRes);
                refreshTaskbarTabs();
            }
        });
        win.setWindowContent(explorerView);
        openWindow(win, "My Computer", ThemeManager.getInstance().getMyComputerIconRes(), 620, 440);
        if (path != null) {
            explorerView.navigateTo(path, false);
        }
    }

    public void openInternetExplorer(String url) {
        final WindowFrameView win = new WindowFrameView(this);
        IEWindowView ieView = new IEWindowView(this);
        ieView.setOnTitleUpdateListener(new IEWindowView.OnTitleUpdateListener() {
            @Override
            public void onTitleUpdated(String title) {
                win.setTitle(title);
                refreshTaskbarTabs();
            }
        });
        if (url != null) {
            ieView.loadUrl(url);
        }
        win.setWindowContent(ieView);
        openWindow(win, "Microsoft Internet Explorer", R.drawable.ic_ie, 640, 440);
    }

    public void openMediaPlayer(File file) {
        final WindowFrameView win = new WindowFrameView(this);
        WMPWindowView wmpView = new WMPWindowView(this);
        if (file != null) {
            wmpView.playFile(file);
        }
        win.setWindowContent(wmpView);
        openWindow(win, "Windows Media Player", R.drawable.ic_wmp, 580, 420);
    }

    public void openPictureViewer(File file) {
        final WindowFrameView win = new WindowFrameView(this);
        PictureViewerWindowView picView = new PictureViewerWindowView(this);
        if (file != null) {
            picView.openImage(file);
        }
        win.setWindowContent(picView);
        openWindow(win, "Windows Picture and Fax Viewer", R.drawable.ic_picture_viewer, 600, 430);
    }

    public void openControlPanel() {
        final WindowFrameView win = new WindowFrameView(this);
        ControlPanelWindowView cpView = new ControlPanelWindowView(this);
        cpView.setControlPanelListener(new ControlPanelWindowView.ControlPanelListener() {
            @Override
            public void onOpenDisplayProperties() {
                openDisplayProperties();
            }

            @Override
            public void onOpenSystemProperties() {
                openSystemProperties();
            }

            @Override
            public void onOpenNetworkConnections() {
                openNetworkConnections();
            }

            @Override
            public void onOpenDateTime() {
                try {
                    startActivity(new Intent(Settings.ACTION_DATE_SETTINGS));
                } catch (Exception e) {
                    Toast.makeText(LauncherActivity.this, "Date & Time Settings", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onOpenAppsManager() {
                try {
                    startActivity(new Intent(Settings.ACTION_APPLICATION_SETTINGS));
                } catch (Exception e) {
                    Toast.makeText(LauncherActivity.this, "Add or Remove Programs", Toast.LENGTH_SHORT).show();
                }
            }
        });
        win.setWindowContent(cpView);
        openWindow(win, "Control Panel", R.drawable.ic_control_panel, 600, 420);
    }

    public void openSystemProperties() {
        final WindowFrameView win = new WindowFrameView(this);
        SystemPropertiesWindowView sysView = new SystemPropertiesWindowView(this);
        sysView.setOnDialogActionListener(new SystemPropertiesWindowView.OnDialogActionListener() {
            @Override
            public void onClose() {
                win.close();
            }
        });
        win.setWindowContent(sysView);
        openWindow(win, "System Properties", R.drawable.ic_system_properties, 500, 420);
    }

    public void openNetworkConnections() {
        final WindowFrameView win = new WindowFrameView(this);
        NetworkConnectionsWindowView netView = new NetworkConnectionsWindowView(this);
        netView.setOnDialogCloseListener(new NetworkConnectionsWindowView.OnDialogCloseListener() {
            @Override
            public void onClose() {
                win.close();
            }
        });
        win.setWindowContent(netView);
        openWindow(win, "Network Connections", R.drawable.ic_network, 520, 400);
    }

    public void openDisplayProperties() {
        final WindowFrameView win = new WindowFrameView(this);
        DisplayPropertiesWindowView dispView = new DisplayPropertiesWindowView(this);
        dispView.setOnDialogActionListener(new DisplayPropertiesWindowView.OnDialogActionListener() {
            @Override
            public void onClose() {
                win.close();
            }
        });
        win.setWindowContent(dispView);
        openWindow(win, "Display Properties", R.drawable.ic_display, 520, 430);
    }

    @Override
    public void onThemeChanged(ThemeManager.Theme newTheme) {
        ThemeManager tm = ThemeManager.getInstance();
        View desktop = findViewById(R.id.root_desktop);
        if (desktop != null) {
            desktop.setBackgroundResource(tm.getWallpaperRes());
        }
        View taskbar = findViewById(R.id.taskbar);
        if (taskbar != null) {
            taskbar.setBackgroundResource(tm.getTaskbarBgRes());
        }

        ImageView imgFlag = (ImageView) findViewById(R.id.img_start_flag);
        TextView tvStart = (TextView) findViewById(R.id.tv_start_text);

        if (btnStart != null) {
            btnStart.setBackgroundResource(tm.getAuthenticStartButtonRes());
            ViewGroup.LayoutParams lp = btnStart.getLayoutParams();

            if (newTheme == ThemeManager.Theme.VISTA || newTheme == ThemeManager.Theme.WIN7_AERO) {
                lp.width = dpToPx(48);
                btnStart.setLayoutParams(lp);
                btnStart.setGravity(Gravity.CENTER);
                btnStart.setPadding(0, 0, 0, 0);
            } else if (newTheme == ThemeManager.Theme.CLASSIC) {
                lp.width = dpToPx(65);
                btnStart.setLayoutParams(lp);
                btnStart.setGravity(Gravity.CENTER);
                btnStart.setPadding(0, 0, 0, 0);
            } else {
                // Luna Blue, Olive & Silver (106px wide)
                lp.width = dpToPx(106);
                btnStart.setLayoutParams(lp);
                btnStart.setGravity(Gravity.CENTER_VERTICAL);
                btnStart.setPadding(0, 0, 0, 0);
            }
        }

        // Hide overlay vector flag/text so authentic downloaded bitmap is shown cleanly
        if (imgFlag != null) {
            imgFlag.setVisibility(View.GONE);
        }
        if (tvStart != null) {
            tvStart.setVisibility(View.GONE);
        }

        // Update Desktop Icons to adapt to theme
        View vComp = findViewById(R.id.icon_my_computer);
        if (vComp instanceof ViewGroup && ((ViewGroup) vComp).getChildCount() > 0) {
            View child = ((ViewGroup) vComp).getChildAt(0);
            if (child instanceof ImageView) {
                ((ImageView) child).setImageResource(tm.getMyComputerIconRes());
            }
        }
        View vRecycle = findViewById(R.id.icon_recycle_bin);
        if (vRecycle instanceof ViewGroup && ((ViewGroup) vRecycle).getChildCount() > 0) {
            View child = ((ViewGroup) vRecycle).getChildAt(0);
            if (child instanceof ImageView) {
                ((ImageView) child).setImageResource(tm.getRecycleBinIconRes());
            }
        }
        View vDocs = findViewById(R.id.icon_my_documents);
        if (vDocs instanceof ViewGroup && ((ViewGroup) vDocs).getChildCount() > 0) {
            View child = ((ViewGroup) vDocs).getChildAt(0);
            if (child instanceof ImageView) {
                ((ImageView) child).setImageResource(tm.getFolderIconRes());
            }
        }

        // Apply theme colors to Start Menu header, bottom bar, and All Programs header
        View smHeader = findViewById(R.id.start_menu_header_layout);
        if (smHeader != null) {
            int[] hColors = tm.getStartMenuHeaderColors();
            GradientDrawable gdHeader = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, hColors);
            gdHeader.setCornerRadii(new float[]{dpToPx(6), dpToPx(6), dpToPx(6), dpToPx(6), 0, 0, 0, 0});
            smHeader.setBackgroundDrawable(gdHeader);
        }

        View smBottom = findViewById(R.id.start_menu_bottom_layout);
        if (smBottom != null) {
            int[] bColors = tm.getStartMenuBottomColors();
            GradientDrawable gdBottom = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, bColors);
            smBottom.setBackgroundDrawable(gdBottom);
        }

        View apHeader = findViewById(R.id.all_programs_header_layout);
        if (apHeader != null) {
            int[] hColors = tm.getStartMenuHeaderColors();
            GradientDrawable gdAp = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, hColors);
            gdAp.setCornerRadii(new float[]{dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4), 0, 0, 0, 0});
            apHeader.setBackgroundDrawable(gdAp);
        }

        refreshTaskbarTabs();
    }

    // --- App Launching & Helpers ---

    private boolean launchApp(String pkg) {
        try {
            Intent intent = packageManager.getLaunchIntentForPackage(pkg);
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                // Start Floating Window Overlay Service
                try {
                    CharSequence label = packageManager.getApplicationLabel(
                            packageManager.getApplicationInfo(pkg, 0));
                    Intent overlay = new Intent(this, FloatingWindowOverlayService.class);
                    overlay.putExtra(FloatingWindowOverlayService.EXTRA_PACKAGE_NAME, pkg);
                    overlay.putExtra(FloatingWindowOverlayService.EXTRA_APP_LABEL, label.toString());
                    startService(overlay);
                } catch (Exception ignored) {}

                return true;
            }
        } catch (Exception ignored) {}
        Toast.makeText(this, "Application [" + pkg + "] starting...", Toast.LENGTH_SHORT).show();
        return false;
    }

    private void showGamesDialog() {
        String[] games = new String[]{"3D Pinball (Space Cadet)", "Minesweeper", "Solitaire"};
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Windows XP Games");
        b.setItems(games, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        if (!launchApp("com.fexed.spacecadetpinball")) {
                            launchApp("com.windows.xp.pinball");
                        }
                        break;
                    case 1: launchApp("com.windows.xp.minesweeper"); break;
                    case 2: launchApp("com.windows.xp.solitaire"); break;
                }
            }
        });
        b.show();
    }

    private Dialog shutdownDialog;

    private void showShutdownDialog() {
        if (shutdownDialog != null && shutdownDialog.isShowing()) {
            shutdownDialog.dismiss();
        }

        final Dialog d = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        d.setContentView(R.layout.dialog_turn_off_computer);
        d.setCanceledOnTouchOutside(true);

        View overlay = d.findViewById(R.id.shutdown_dim_overlay);
        if (overlay != null) {
            overlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    d.dismiss();
                }
            });
        }

        View dialogBox = d.findViewById(R.id.shutdown_dialog_box);
        if (dialogBox != null) {
            dialogBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Do not dismiss when clicking modal dialog
                }
            });
        }

        // Stand By
        View btnStandby = d.findViewById(R.id.btn_action_standby);
        if (btnStandby != null) {
            btnStandby.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    d.dismiss();
                    performStandby();
                }
            });
        }

        // Turn Off
        View btnTurnOff = d.findViewById(R.id.btn_action_turn_off);
        if (btnTurnOff != null) {
            btnTurnOff.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    d.dismiss();
                    performShutdown();
                }
            });
        }

        // Restart
        View btnRestart = d.findViewById(R.id.btn_action_restart);
        if (btnRestart != null) {
            btnRestart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    d.dismiss();
                    performReboot();
                }
            });
        }

        // Cancel
        View btnCancel = d.findViewById(R.id.btn_action_cancel);
        if (btnCancel != null) {
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    d.dismiss();
                }
            });
        }

        shutdownDialog = d;
        d.show();
    }

    private void performStandby() {
        Toast.makeText(this, "Preparing to Stand By...", Toast.LENGTH_SHORT).show();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    Runtime.getRuntime().exec("input keyevent 26");
                } catch (Exception e) {
                    try {
                        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                        pm.getClass().getMethod("goToSleep", long.class).invoke(pm, SystemClock.uptimeMillis());
                    } catch (Exception ignored) {}
                }
            }
        }, 400);
    }

    private void performShutdown() {
        Toast.makeText(this, "Windows MK is shutting down...", Toast.LENGTH_LONG).show();
        writePowerAction("shutdown");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 1. ACTION_REQUEST_SHUTDOWN Intent
                try {
                    Intent shutdown = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
                    shutdown.putExtra("android.intent.extra.KEY_CONFIRM", false);
                    shutdown.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(shutdown);
                } catch (Exception ignored) {}

                // 2. IPowerManager.shutdown
                try {
                    Class<?> sm = Class.forName("android.os.ServiceManager");
                    IBinder b = (IBinder) sm.getMethod("getService", String.class).invoke(null, "power");
                    Class<?> ipm = Class.forName("android.os.IPowerManager$Stub");
                    Object service = ipm.getMethod("asInterface", IBinder.class).invoke(null, b);
                    service.getClass().getMethod("shutdown", boolean.class, boolean.class).invoke(service, false, false);
                } catch (Exception ignored) {}

                // 3. Shell fallback
                try {
                    Runtime.getRuntime().exec(new String[]{"reboot", "-p"});
                } catch (Exception ignored) {}
            }
        }, 600);
    }

    private void performReboot() {
        Toast.makeText(this, "Windows MK is restarting...", Toast.LENGTH_LONG).show();
        writePowerAction("reboot");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 1. IPowerManager.reboot
                try {
                    Class<?> sm = Class.forName("android.os.ServiceManager");
                    IBinder b = (IBinder) sm.getMethod("getService", String.class).invoke(null, "power");
                    Class<?> ipm = Class.forName("android.os.IPowerManager$Stub");
                    Object service = ipm.getMethod("asInterface", IBinder.class).invoke(null, b);
                    service.getClass().getMethod("reboot", boolean.class, String.class, boolean.class).invoke(service, false, null, false);
                } catch (Exception ignored) {}

                // 2. Broadcast Intents
                try {
                    sendBroadcast(new Intent(Intent.ACTION_REBOOT));
                    sendBroadcast(new Intent("sec.android.intent.action.REBOOT"));
                } catch (Exception ignored) {}

                // 3. Shell fallback
                try {
                    Runtime.getRuntime().exec(new String[]{"reboot"});
                } catch (Exception ignored) {}
            }
        }, 600);
    }

    private void writePowerAction(String action) {
        try {
            File f = new File("/sdcard/power_action");
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(action.getBytes());
            fos.flush();
            fos.close();
        } catch (Exception ignored) {}
        try {
            File f = new File("/data/local/tmp/power_action");
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(action.getBytes());
            fos.flush();
            fos.close();
        } catch (Exception ignored) {}
    }

    private void setupVolumeControl() {
        volumePopupContainer = (LinearLayout) findViewById(R.id.volume_popup_container);
        seekMasterVolume = (SeekBar) findViewById(R.id.seek_master_volume);
        tvVolumePercent = (TextView) findViewById(R.id.tv_volume_percent);
        chkVolumeMute = (CheckBox) findViewById(R.id.chk_volume_mute);
        trayVolume = (ImageView) findViewById(R.id.tray_volume);
        View btnCloseVolume = findViewById(R.id.btn_close_volume);

        final AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (am == null) return;

        final int maxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        if (seekMasterVolume != null) {
            seekMasterVolume.setMax(maxVol);
            int curVol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
            seekMasterVolume.setProgress(curVol);
            updateVolumeUI(curVol, maxVol);

            seekMasterVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        am.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                        updateVolumeUI(progress, maxVol);
                        resetVolumeAutoHide();
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    handler.removeCallbacks(hideVolumeRunnable);
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    resetVolumeAutoHide();
                }
            });
        }

        if (chkVolumeMute != null) {
            chkVolumeMute.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    am.setStreamMute(AudioManager.STREAM_MUSIC, isChecked);
                    if (trayVolume != null) {
                        trayVolume.setImageResource(isChecked ? R.drawable.ic_tray_volume_mute : R.drawable.ic_tray_volume);
                    }
                    resetVolumeAutoHide();
                }
            });
        }

        if (trayVolume != null) {
            trayVolume.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleVolumeControlPopup();
                }
            });
        }

        if (btnCloseVolume != null) {
            btnCloseVolume.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (volumePopupContainer != null) {
                        volumePopupContainer.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    private void updateVolumeUI(int curVol, int maxVol) {
        if (tvVolumePercent != null) {
            int pct = maxVol > 0 ? (curVol * 100 / maxVol) : 0;
            tvVolumePercent.setText(pct + "%");
        }
        if (trayVolume != null) {
            boolean isMuted = (curVol == 0);
            if (chkVolumeMute != null && chkVolumeMute.isChecked()) {
                isMuted = true;
            }
            trayVolume.setImageResource(isMuted ? R.drawable.ic_tray_volume_mute : R.drawable.ic_tray_volume);
        }
    }

    public void closeVolumePopup() {
        if (volumePopupContainer != null && volumePopupContainer.getVisibility() == View.VISIBLE) {
            volumePopupContainer.setVisibility(View.GONE);
            handler.removeCallbacks(hideVolumeRunnable);
        }
    }

    private void resetVolumeAutoHide(long delayMillis) {
        handler.removeCallbacks(hideVolumeRunnable);
        if (delayMillis > 0) {
            handler.postDelayed(hideVolumeRunnable, delayMillis);
        }
    }

    private void resetVolumeAutoHide() {
        resetVolumeAutoHide(6000);
    }

    private void toggleVolumeControlPopup() {
        if (volumePopupContainer == null) return;
        if (volumePopupContainer.getVisibility() == View.VISIBLE) {
            closeVolumePopup();
        } else {
            showVolumeControlPopup(false);
        }
    }

    private void showVolumeControlPopup(boolean autoHide) {
        if (volumePopupContainer == null) return;
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (am != null) {
            int curVol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
            int maxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            if (seekMasterVolume != null) {
                seekMasterVolume.setMax(maxVol);
                seekMasterVolume.setProgress(curVol);
            }
            updateVolumeUI(curVol, maxVol);
        }
        volumePopupContainer.setVisibility(View.VISIBLE);
        volumePopupContainer.bringToFront();
        if (autoHide) {
            resetVolumeAutoHide(6000);
        } else {
            handler.removeCallbacks(hideVolumeRunnable);
        }
    }

    private void adjustVolume(int direction) {
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (am != null) {
            am.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, 0);
            showVolumeControlPopup(true);
        }
    }

    private void updateClock() {
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.US);
        trayClock.setText(sdf.format(new Date()));
    }

    private void refreshTaskbarTabs() {
        taskbarTabsContainer.removeAllViews();

        // 1. Render Tabs for Open Floating Windows
        for (final WindowFrameView window : openWindows) {
            LinearLayout tab = new LinearLayout(this);
            tab.setOrientation(LinearLayout.HORIZONTAL);
            tab.setGravity(android.view.Gravity.CENTER_VERTICAL);

            boolean isCurrentActive = (window == activeWindow && window.getVisibility() == View.VISIBLE);
            if (isCurrentActive) {
                tab.setBackgroundResource(R.drawable.taskbar_tab_active);
            } else {
                tab.setBackgroundResource(R.drawable.taskbar_tab_bg);
            }
            tab.setPadding(dpToPx(8), dpToPx(2), dpToPx(10), dpToPx(2));
            tab.setClickable(true);

            ImageView img = new ImageView(this);
            int iconRes = window.getIconRes();
            img.setImageResource(iconRes);

            LinearLayout.LayoutParams imgP = new LinearLayout.LayoutParams(dpToPx(16), dpToPx(16));
            tab.addView(img, imgP);

            TextView tv = new TextView(this);
            tv.setText(window.getTitle());
            tv.setTextColor(0xFFFFFFFF);
            tv.setTextSize(11);
            tv.setMaxLines(1);
            LinearLayout.LayoutParams tvP = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            tvP.leftMargin = dpToPx(4);
            tab.addView(tv, tvP);

            LinearLayout.LayoutParams tabParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            tabParams.rightMargin = dpToPx(3);
            tabParams.topMargin = dpToPx(2);
            tabParams.bottomMargin = dpToPx(2);

            tab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (window == activeWindow && window.getVisibility() == View.VISIBLE) {
                        window.minimize();
                    } else {
                        window.restoreFromMinimize();
                        activateWindow(window);
                    }
                }
            });

            taskbarTabsContainer.addView(tab, tabParams);
        }

        // 2. Render Tabs for Background Android Tasks
        try {
            List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(6);
            if (tasks == null) return;

            for (final ActivityManager.RunningTaskInfo task : tasks) {
                if (task.baseActivity == null) continue;
                final String pkg = task.baseActivity.getPackageName();
                if (getPackageName().equals(pkg)) continue;

                final CharSequence label;
                Drawable icon;
                try {
                    label = packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0));
                    icon = packageManager.getApplicationIcon(pkg);
                } catch (Exception e) {
                    continue;
                }

                LinearLayout tab = new LinearLayout(this);
                tab.setOrientation(LinearLayout.HORIZONTAL);
                tab.setGravity(android.view.Gravity.CENTER_VERTICAL);
                tab.setBackgroundResource(R.drawable.taskbar_tab_bg);
                tab.setPadding(dpToPx(8), dpToPx(2), dpToPx(10), dpToPx(2));
                tab.setClickable(true);

                ImageView img = new ImageView(this);
                img.setImageDrawable(icon);
                LinearLayout.LayoutParams imgP = new LinearLayout.LayoutParams(dpToPx(16), dpToPx(16));
                tab.addView(img, imgP);

                TextView tv = new TextView(this);
                tv.setText(label);
                tv.setTextColor(0xFFFFFFFF);
                tv.setTextSize(11);
                tv.setMaxLines(1);
                LinearLayout.LayoutParams tvP = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                tvP.leftMargin = dpToPx(4);
                tab.addView(tv, tvP);

                LinearLayout.LayoutParams tabParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
                tabParams.rightMargin = dpToPx(3);
                tabParams.topMargin = dpToPx(2);
                tabParams.bottomMargin = dpToPx(2);

                tab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            activityManager.moveTaskToFront(task.id, ActivityManager.MOVE_TASK_WITH_HOME);
                            Intent overlay = new Intent(LauncherActivity.this, FloatingWindowOverlayService.class);
                            overlay.putExtra(FloatingWindowOverlayService.EXTRA_PACKAGE_NAME, pkg);
                            overlay.putExtra(FloatingWindowOverlayService.EXTRA_APP_LABEL, label.toString());
                            startService(overlay);
                        } catch (Exception ignored) {}
                    }
                });

                taskbarTabsContainer.addView(tab, tabParams);
            }
        } catch (Exception ignored) {}
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }
}
