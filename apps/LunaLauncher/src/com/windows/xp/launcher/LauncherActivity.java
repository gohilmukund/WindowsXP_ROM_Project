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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LauncherActivity extends Activity {

    private View startMenuContainer;
    private View btnStart;
    private LinearLayout taskbarTabsContainer;
    private TextView trayClock;
    private TextView trayBattery;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private AltTabDialog altTabDialog;
    private ActivityManager activityManager;
    private PackageManager packageManager;

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
            // Animate window down to desktop and refresh taskbar tabs
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

        // Close start menu when desktop is tapped
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
                if (startMenuContainer.getVisibility() == View.VISIBLE) {
                    startMenuContainer.setVisibility(View.GONE);
                }
                launchExplorer("computer://");
            }
        });

        findViewById(R.id.icon_my_documents).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startMenuContainer.getVisibility() == View.VISIBLE) {
                    startMenuContainer.setVisibility(View.GONE);
                }
                launchExplorer("/sdcard/Documents");
            }
        });

        setDesktopLauncher(R.id.icon_internet_explorer, "com.windows.xp.internetexplorer", new Runnable() {
            @Override
            public void run() {
                // Fallback to installed Firefox
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
                if (!launchApp("com.bo.fotoo") && !launchApp("com.boombuler.fotoo")) {
                    Toast.makeText(LauncherActivity.this, "Fotoo Digital Photo Frame (/sdcard/Pictures)", Toast.LENGTH_SHORT).show();
                }
            }
        });

        setDesktopLauncher(R.id.icon_paint, "com.windows.xp.mspaint", null);
        setDesktopLauncher(R.id.icon_notepad, "com.windows.xp.notepad", null);

        findViewById(R.id.icon_games).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGamesDialog();
            }
        });

        findViewById(R.id.icon_recycle_bin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LauncherActivity.this, "Recycle Bin is empty.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupStartMenuItems() {
        findViewById(R.id.sm_item_ie).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMenuContainer.setVisibility(View.GONE);
                if (!launchApp("com.windows.xp.internetexplorer")) {
                    launchApp("org.mozilla.firefox");
                }
            }
        });

        findViewById(R.id.sm_item_wmp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMenuContainer.setVisibility(View.GONE);
                if (!launchApp("com.windows.xp.wmp")) {
                    launchApp("org.schabi.newpipelegacy");
                }
            }
        });

        findViewById(R.id.sm_item_paint).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMenuContainer.setVisibility(View.GONE);
                launchApp("com.windows.xp.mspaint");
            }
        });

        findViewById(R.id.sm_item_notepad).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMenuContainer.setVisibility(View.GONE);
                launchApp("com.windows.xp.notepad");
            }
        });

        findViewById(R.id.sm_item_fotoo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMenuContainer.setVisibility(View.GONE);
                if (!launchApp("com.bo.fotoo")) {
                    launchApp("com.boombuler.fotoo");
                }
            }
        });

        findViewById(R.id.sm_item_my_docs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMenuContainer.setVisibility(View.GONE);
                launchExplorer("/sdcard/Documents");
            }
        });

        findViewById(R.id.sm_item_my_pics).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMenuContainer.setVisibility(View.GONE);
                launchExplorer("/sdcard/Pictures");
            }
        });

        findViewById(R.id.sm_item_my_comp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMenuContainer.setVisibility(View.GONE);
                launchExplorer("computer://");
            }
        });

        findViewById(R.id.sm_item_control_panel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMenuContainer.setVisibility(View.GONE);
                startActivity(new Intent(Settings.ACTION_SETTINGS));
            }
        });

        findViewById(R.id.btn_sm_logoff).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMenuContainer.setVisibility(View.GONE);
                Toast.makeText(LauncherActivity.this, "Windows XP: Session Locked.", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btn_sm_shutdown).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMenuContainer.setVisibility(View.GONE);
                showShutdownDialog();
            }
        });
    }

    private void setDesktopLauncher(int viewId, final String pkg, final Runnable fallback) {
        View v = findViewById(viewId);
        if (v == null) return;
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startMenuContainer.getVisibility() == View.VISIBLE) {
                    startMenuContainer.setVisibility(View.GONE);
                }
                if (!launchApp(pkg) && fallback != null) {
                    fallback.run();
                }
            }
        });
    }

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

    private void launchExplorer(String path) {
        try {
            Intent intent = new Intent();
            intent.setClassName("com.windows.xp.explorer", "com.windows.xp.explorer.ExplorerActivity");
            if (path != null) {
                intent.putExtra("path", path);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            launchApp("com.windows.xp.explorer");
        }
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
        try {
            List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(6);
            if (tasks == null) return;

            for (final ActivityManager.RunningTaskInfo task : tasks) {
                if (task.baseActivity == null) continue;
                String pkg = task.baseActivity.getPackageName();
                if (getPackageName().equals(pkg)) continue; // Don't show launcher itself as tab

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
                tab.setPadding(12, 4, 16, 4);
                tab.setClickable(true);

                ImageView img = new ImageView(this);
                img.setImageDrawable(icon);
                LinearLayout.LayoutParams imgP = new LinearLayout.LayoutParams(20, 20);
                tab.addView(img, imgP);

                TextView tv = new TextView(this);
                tv.setText(label);
                tv.setTextColor(0xFFFFFFFF);
                tv.setTextSize(11);
                tv.setMaxLines(1);
                LinearLayout.LayoutParams tvP = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                tvP.leftMargin = 6;
                tab.addView(tv, tvP);

                LinearLayout.LayoutParams tabParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
                tabParams.rightMargin = 4;
                tabParams.topMargin = 2;
                tabParams.bottomMargin = 2;

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
}
