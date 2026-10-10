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
import android.graphics.drawable.Drawable;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.windows.xp.launcher.window.CmdPromptView;
import com.windows.xp.launcher.window.ExplorerWindowView;
import com.windows.xp.launcher.window.NotepadWindowView;
import com.windows.xp.launcher.window.PaintWindowView;
import com.windows.xp.launcher.window.WindowFrameView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LauncherActivity extends Activity {

    private View startMenuContainer;
    private View btnStart;
    private LinearLayout taskbarTabsContainer;
    private TextView trayClock;
    private TextView trayBattery;
    private FrameLayout desktopWindowContainer;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private AltTabDialog altTabDialog;
    private ActivityManager activityManager;
    private PackageManager packageManager;

    private final List<WindowFrameView> openWindows = new ArrayList<WindowFrameView>();
    private WindowFrameView activeWindow = null;

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
        setContentView(R.layout.activity_launcher);

        activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        packageManager = getPackageManager();

        startMenuContainer = findViewById(R.id.start_menu_container);
        btnStart = findViewById(R.id.btn_start);
        taskbarTabsContainer = (LinearLayout) findViewById(R.id.taskbar_tabs_container);
        trayClock = (TextView) findViewById(R.id.tray_clock);
        trayBattery = (TextView) findViewById(R.id.tray_battery);
        desktopWindowContainer = (FrameLayout) findViewById(R.id.desktop_window_container);

        setupStartButton();
        setupDesktopIcons();
        setupStartMenuItems();

        // Register receivers
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        registerReceiver(minimizeReceiver, new IntentFilter("com.windows.xp.ACTION_MINIMIZE"));

        handler.post(clockRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshTaskbarTabs();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(clockRunnable);
        try {
            unregisterReceiver(batteryReceiver);
            unregisterReceiver(minimizeReceiver);
        } catch (Exception ignored) {}
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (Intent.ACTION_MAIN.equals(intent.getAction()) && intent.hasCategory(Intent.CATEGORY_HOME)) {
            toggleStartMenu();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
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
            if (startMenuContainer.getVisibility() == View.VISIBLE) {
                startMenuContainer.setVisibility(View.GONE);
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
            startMenuContainer.setVisibility(View.GONE);
        } else {
            startMenuContainer.setVisibility(View.VISIBLE);
            startMenuContainer.bringToFront();
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

        findViewById(R.id.root_desktop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startMenuContainer.getVisibility() == View.VISIBLE) {
                    startMenuContainer.setVisibility(View.GONE);
                }
            }
        });
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

        setDesktopLauncher(R.id.icon_internet_explorer, "com.windows.xp.internetexplorer", new Runnable() {
            @Override
            public void run() {
                launchApp("org.mozilla.firefox");
            }
        });

        setDesktopLauncher(R.id.icon_wmp, "com.windows.xp.wmp", new Runnable() {
            @Override
            public void run() {
                launchApp("org.schabi.newpipelegacy");
            }
        });

        findViewById(R.id.icon_fotoo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeStartMenu();
                if (!launchApp("com.bo.fotoo") && !launchApp("com.boombuler.fotoo")) {
                    Toast.makeText(LauncherActivity.this, "Fotoo Digital Photo Frame (/sdcard/Pictures)", Toast.LENGTH_SHORT).show();
                }
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
                if (!launchApp("com.windows.xp.internetexplorer")) {
                    launchApp("org.mozilla.firefox");
                }
            }
        });

        findViewById(R.id.sm_item_wmp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeStartMenu();
                if (!launchApp("com.windows.xp.wmp")) {
                    launchApp("org.schabi.newpipelegacy");
                }
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
                if (!launchApp("com.bo.fotoo")) {
                    launchApp("com.boombuler.fotoo");
                }
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
                openExplorer("/sdcard/Pictures");
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
                startActivity(new Intent(Settings.ACTION_SETTINGS));
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

    private void closeStartMenu() {
        if (startMenuContainer != null && startMenuContainer.getVisibility() == View.VISIBLE) {
            startMenuContainer.setVisibility(View.GONE);
        }
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
        WindowFrameView win = new WindowFrameView(this);
        ExplorerWindowView explorerView = new ExplorerWindowView(this);
        explorerView.setFileActionListener(new ExplorerWindowView.FileActionListener() {
            @Override
            public void onOpenTextFile(File file) {
                openNotepad(file);
            }
        });
        win.setWindowContent(explorerView);
        if (path != null) {
            explorerView.navigateTo(path, false);
        }
        openWindow(win, "Windows Explorer", R.drawable.ic_my_computer, 620, 440);
    }

    // --- App Launching & Helpers ---

    private boolean launchApp(String pkg) {
        try {
            Intent intent = packageManager.getLaunchIntentForPackage(pkg);
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
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

    private void showShutdownDialog() {
        String[] options = new String[]{"Turn Off", "Restart Computer", "Cancel"};
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Turn Off Computer");
        b.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    Intent shutdown = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
                    shutdown.putExtra("android.intent.extra.KEY_CONFIRM", false);
                    shutdown.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try { startActivity(shutdown); } catch (Exception e) {
                        Toast.makeText(LauncherActivity.this, "Shutting down...", Toast.LENGTH_LONG).show();
                    }
                } else if (which == 1) {
                    Intent reboot = new Intent(Intent.ACTION_REBOOT);
                    reboot.putExtra("nowait", 1);
                    reboot.putExtra("interval", 1);
                    reboot.putExtra("window", 0);
                    try { sendBroadcast(reboot); } catch (Exception e) {
                        Toast.makeText(LauncherActivity.this, "Restarting...", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        b.show();
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
            int iconRes = R.drawable.ic_my_computer;
            String t = window.getTitle().toLowerCase();
            if (t.contains("cmd") || t.contains("command")) iconRes = R.drawable.ic_cmd;
            else if (t.contains("notepad")) iconRes = R.drawable.ic_notepad;
            else if (t.contains("paint")) iconRes = R.drawable.ic_paint;
            else if (t.contains("explorer")) iconRes = R.drawable.ic_my_computer;
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
                String pkg = task.baseActivity.getPackageName();
                if (getPackageName().equals(pkg)) continue;

                CharSequence label;
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
