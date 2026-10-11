package com.windows.xp.launcher.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.IBinder;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.windows.xp.launcher.LauncherActivity;
import com.windows.xp.launcher.R;
import com.windows.xp.launcher.theme.ThemeManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FloatingWindowOverlayService extends Service implements ThemeManager.OnThemeChangeListener {

    public static final String EXTRA_PACKAGE_NAME = "extra_package_name";
    public static final String EXTRA_APP_LABEL = "extra_app_label";
    public static final String ACTION_CLOSE_OVERLAY = "com.windows.xp.launcher.ACTION_CLOSE_OVERLAY";

    private WindowManager windowManager;

    // Top Titlebar Overlay
    private RelativeLayout titleBarView;
    private WindowManager.LayoutParams titleParams;
    private TextView tvTitle;
    private ImageView imgIcon;
    private ImageView btnMin;
    private ImageView btnMax;
    private ImageView btnClose;

    // Bottom Taskbar Overlay
    private RelativeLayout taskbarOverlayView;
    private WindowManager.LayoutParams taskbarParams;
    private ImageView btnStartOverlay;
    private LinearLayout overlayTabsContainer;
    private TextView tvTrayClock;

    private String packageName = "";
    private String appLabel = "Application";
    private boolean isRestored = false;

    // Drag-to-move coordinates
    private int initialX, initialY;
    private float initialTouchX, initialTouchY;

    private final Handler handler = new Handler();
    private boolean isPollingActive = false;

    private final Runnable taskWatcher = new Runnable() {
        @Override
        public void run() {
            try {
                ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                if (am != null) {
                    List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
                    if (tasks != null && !tasks.isEmpty()) {
                        ActivityManager.RunningTaskInfo topTask = tasks.get(0);
                        ComponentName cn = topTask.topActivity != null ? topTask.topActivity : topTask.baseActivity;
                        if (cn != null) {
                            String topPkg = cn.getPackageName();
                            if (getPackageName().equals(topPkg) || topPkg.contains("keyguard")) {
                                // User is on desktop or lockscreen - hide overlays
                                hideOverlay();
                            } else {
                                // User is in an external app
                                if (!topPkg.equals(packageName)) {
                                    updateForPackage(topPkg);
                                }
                                showOverlay();
                                refreshOverlayTabs();
                                updateOverlayClock();
                            }
                        }
                    }
                }
            } catch (Exception ignored) {}
            if (isPollingActive) {
                handler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        ThemeManager.getInstance().addListener(this);

        createTitleBarOverlay();
        createTaskbarOverlay();

        isPollingActive = true;
        handler.post(taskWatcher);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (ACTION_CLOSE_OVERLAY.equals(intent.getAction())) {
                hideOverlay();
                return START_NOT_STICKY;
            }
            if (intent.hasExtra(EXTRA_PACKAGE_NAME)) {
                String pkg = intent.getStringExtra(EXTRA_PACKAGE_NAME);
                if (pkg != null && !pkg.isEmpty()) {
                    updateForPackage(pkg);
                }
            }
            if (intent.hasExtra(EXTRA_APP_LABEL)) {
                String lbl = intent.getStringExtra(EXTRA_APP_LABEL);
                if (lbl != null && !lbl.isEmpty()) {
                    appLabel = lbl;
                    if (tvTitle != null) tvTitle.setText(appLabel);
                }
            }
        }
        showOverlay();
        refreshOverlayTabs();
        updateOverlayClock();
        return START_NOT_STICKY;
    }

    private void updateForPackage(String pkg) {
        this.packageName = pkg;
        PackageManager pm = getPackageManager();
        try {
            CharSequence label = pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0));
            this.appLabel = label != null ? label.toString() : pkg;
            if (tvTitle != null) {
                tvTitle.setText(this.appLabel);
            }
            Drawable icon = pm.getApplicationIcon(pkg);
            if (icon != null && imgIcon != null) {
                imgIcon.setImageDrawable(icon);
            }
        } catch (Exception e) {
            this.appLabel = pkg;
            if (tvTitle != null) tvTitle.setText(this.appLabel);
        }
    }

    private void createTitleBarOverlay() {
        int heightPx = dpToPx(28);

        // TYPE_SYSTEM_ERROR (layer 111000) renders ABOVE the Samsung status bar (layer 2000)
        titleParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                heightPx,
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);

        titleParams.gravity = Gravity.TOP | Gravity.LEFT;
        titleParams.x = 0;
        titleParams.y = 0;

        // Title bar (28dp high, matching WindowFrameView)
        titleBarView = new RelativeLayout(this);
        titleBarView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, heightPx));
        titleBarView.setPadding(dpToPx(4), dpToPx(2), dpToPx(4), dpToPx(2));
        applyThemeColors();

        // App Icon (18dp)
        imgIcon = new ImageView(this);
        imgIcon.setId(2001);
        int iconSize = dpToPx(18);
        RelativeLayout.LayoutParams iconLp = new RelativeLayout.LayoutParams(iconSize, iconSize);
        iconLp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        iconLp.addRule(RelativeLayout.CENTER_VERTICAL);
        iconLp.leftMargin = dpToPx(2);
        imgIcon.setLayoutParams(iconLp);
        imgIcon.setImageResource(R.drawable.ic_my_computer);
        titleBarView.addView(imgIcon);

        // Right button container (Min, Max/Restore, Close)
        LinearLayout btnContainer = new LinearLayout(this);
        btnContainer.setId(2002);
        btnContainer.setOrientation(LinearLayout.HORIZONTAL);
        btnContainer.setGravity(Gravity.CENTER_VERTICAL);
        RelativeLayout.LayoutParams btnLp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        btnLp.addRule(RelativeLayout.CENTER_VERTICAL);
        btnContainer.setLayoutParams(btnLp);

        int ctrlSize = dpToPx(21);
        int ctrlMargin = dpToPx(2);

        // Minimize
        btnMin = new ImageView(this);
        btnMin.setImageResource(R.drawable.btn_min);
        LinearLayout.LayoutParams minLp = new LinearLayout.LayoutParams(ctrlSize, ctrlSize);
        minLp.rightMargin = ctrlMargin;
        btnMin.setLayoutParams(minLp);
        btnContainer.addView(btnMin);

        // Maximize / Restore
        btnMax = new ImageView(this);
        btnMax.setImageResource(R.drawable.btn_max);
        LinearLayout.LayoutParams maxLp = new LinearLayout.LayoutParams(ctrlSize, ctrlSize);
        maxLp.rightMargin = ctrlMargin;
        btnMax.setLayoutParams(maxLp);
        btnContainer.addView(btnMax);

        // Close
        btnClose = new ImageView(this);
        btnClose.setImageResource(R.drawable.btn_close);
        LinearLayout.LayoutParams closeLp = new LinearLayout.LayoutParams(ctrlSize, ctrlSize);
        btnClose.setLayoutParams(closeLp);
        btnContainer.addView(btnClose);

        titleBarView.addView(btnContainer);

        // Title text
        tvTitle = new TextView(this);
        RelativeLayout.LayoutParams textLp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textLp.addRule(RelativeLayout.RIGHT_OF, 2001);
        textLp.addRule(RelativeLayout.LEFT_OF, 2002);
        textLp.addRule(RelativeLayout.CENTER_VERTICAL);
        textLp.leftMargin = dpToPx(4);
        textLp.rightMargin = dpToPx(4);
        tvTitle.setLayoutParams(textLp);
        tvTitle.setText(appLabel);
        tvTitle.setTextColor(Color.WHITE);
        tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        tvTitle.setSingleLine(true);
        tvTitle.setShadowLayer(2, 1, 1, Color.argb(180, 0, 0, 0));
        titleBarView.addView(tvTitle);

        setupTitleBarListeners();
    }

    private void createTaskbarOverlay() {
        int taskbarHeight = dpToPx(30);

        taskbarParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                taskbarHeight,
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        taskbarParams.gravity = Gravity.BOTTOM | Gravity.LEFT;
        taskbarParams.x = 0;
        taskbarParams.y = 0;

        taskbarOverlayView = new RelativeLayout(this);
        ThemeManager tm = ThemeManager.getInstance();
        taskbarOverlayView.setBackgroundResource(tm.getTaskbarBgRes());

        // 1. Start Button (Left)
        btnStartOverlay = new ImageView(this);
        btnStartOverlay.setId(3001);
        btnStartOverlay.setScaleType(ImageView.ScaleType.FIT_XY);
        btnStartOverlay.setImageResource(tm.getAuthenticStartButtonRes());
        int startW = getStartButtonWidth(tm.getCurrentTheme());
        RelativeLayout.LayoutParams startLp = new RelativeLayout.LayoutParams(startW, taskbarHeight);
        startLp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        startLp.addRule(RelativeLayout.CENTER_VERTICAL);
        btnStartOverlay.setLayoutParams(startLp);
        btnStartOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                minimizeApp();
            }
        });
        taskbarOverlayView.addView(btnStartOverlay);

        // 2. System Tray (Right)
        LinearLayout trayLayout = new LinearLayout(this);
        trayLayout.setId(3002);
        trayLayout.setOrientation(LinearLayout.HORIZONTAL);
        trayLayout.setGravity(Gravity.CENTER_VERTICAL);
        trayLayout.setPadding(dpToPx(6), 0, dpToPx(8), 0);
        RelativeLayout.LayoutParams trayLp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, taskbarHeight);
        trayLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        trayLp.addRule(RelativeLayout.CENTER_VERTICAL);
        trayLayout.setLayoutParams(trayLp);

        // Volume icon
        ImageView trayVol = new ImageView(this);
        trayVol.setImageResource(R.drawable.ic_tray_volume);
        LinearLayout.LayoutParams volLp = new LinearLayout.LayoutParams(dpToPx(16), dpToPx(16));
        volLp.rightMargin = dpToPx(6);
        trayLayout.addView(trayVol, volLp);

        // Clock
        tvTrayClock = new TextView(this);
        tvTrayClock.setTextColor(Color.WHITE);
        tvTrayClock.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        trayLayout.addView(tvTrayClock);

        taskbarOverlayView.addView(trayLayout);

        // 3. Taskbar Tabs (Center)
        overlayTabsContainer = new LinearLayout(this);
        overlayTabsContainer.setOrientation(LinearLayout.HORIZONTAL);
        overlayTabsContainer.setGravity(Gravity.CENTER_VERTICAL);
        RelativeLayout.LayoutParams tabsLp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, taskbarHeight);
        tabsLp.addRule(RelativeLayout.RIGHT_OF, 3001);
        tabsLp.addRule(RelativeLayout.LEFT_OF, 3002);
        tabsLp.leftMargin = dpToPx(4);
        tabsLp.rightMargin = dpToPx(4);
        overlayTabsContainer.setLayoutParams(tabsLp);
        taskbarOverlayView.addView(overlayTabsContainer);
    }

    private int getStartButtonWidth(ThemeManager.Theme theme) {
        if (theme == ThemeManager.Theme.VISTA || theme == ThemeManager.Theme.WIN7_AERO) {
            return dpToPx(48);
        } else if (theme == ThemeManager.Theme.CLASSIC) {
            return dpToPx(65);
        } else {
            return dpToPx(106);
        }
    }

    private void updateOverlayClock() {
        if (tvTrayClock != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.US);
            tvTrayClock.setText(sdf.format(new Date()));
        }
    }

    private void refreshOverlayTabs() {
        if (overlayTabsContainer == null) return;
        overlayTabsContainer.removeAllViews();
        try {
            ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            if (am == null) return;
            List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(6);
            if (tasks == null) return;
            PackageManager pm = getPackageManager();

            for (final ActivityManager.RunningTaskInfo task : tasks) {
                ComponentName cn = task.topActivity != null ? task.topActivity : task.baseActivity;
                if (cn == null) continue;
                final String pkg = cn.getPackageName();
                if (getPackageName().equals(pkg)) continue;

                final CharSequence label;
                Drawable icon;
                try {
                    label = pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0));
                    icon = pm.getApplicationIcon(pkg);
                } catch (Exception e) {
                    continue;
                }

                LinearLayout tab = new LinearLayout(this);
                tab.setOrientation(LinearLayout.HORIZONTAL);
                tab.setGravity(Gravity.CENTER_VERTICAL);
                boolean isActive = pkg.equals(packageName);
                tab.setBackgroundResource(isActive ? R.drawable.taskbar_tab_active : R.drawable.taskbar_tab_bg);
                tab.setPadding(dpToPx(8), dpToPx(2), dpToPx(10), dpToPx(2));
                tab.setClickable(true);

                ImageView img = new ImageView(this);
                img.setImageDrawable(icon);
                LinearLayout.LayoutParams imgP = new LinearLayout.LayoutParams(dpToPx(16), dpToPx(16));
                tab.addView(img, imgP);

                TextView tv = new TextView(this);
                tv.setText(label);
                tv.setTextColor(Color.WHITE);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
                tv.setSingleLine(true);
                LinearLayout.LayoutParams tvP = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                tvP.leftMargin = dpToPx(4);
                tab.addView(tv, tvP);

                LinearLayout.LayoutParams tabParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                tabParams.rightMargin = dpToPx(3);
                tabParams.topMargin = dpToPx(2);
                tabParams.bottomMargin = dpToPx(2);

                tab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (pkg.equals(packageName)) {
                            minimizeApp();
                        } else {
                            try {
                                ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                                if (am != null) {
                                    am.moveTaskToFront(task.id, ActivityManager.MOVE_TASK_WITH_HOME);
                                }
                                updateForPackage(pkg);
                                refreshOverlayTabs();
                            } catch (Exception ignored) {}
                        }
                    }
                });

                overlayTabsContainer.addView(tab, tabParams);
            }
        } catch (Exception ignored) {}
    }

    private void setupTitleBarListeners() {
        btnMin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                minimizeApp();
            }
        });

        btnMax.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleRestoreMode();
            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeApp();
            }
        });

        // Drag to move window frame
        titleBarView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // If touching within the right button area, let buttons handle clicks
                if (v.getWidth() > 0 && event.getX() > v.getWidth() - dpToPx(85)) {
                    return false;
                }
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = titleParams.x;
                        initialY = titleParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int screenW = getResources().getDisplayMetrics().widthPixels;
                        int screenH = getResources().getDisplayMetrics().heightPixels;
                        int newX = initialX + (int) (event.getRawX() - initialTouchX);
                        int newY = initialY + (int) (event.getRawY() - initialTouchY);
                        int curW = (titleParams.width == WindowManager.LayoutParams.MATCH_PARENT) ? screenW : titleParams.width;
                        int curH = titleParams.height;
                        newX = Math.max(0, Math.min(newX, screenW - curW));
                        newY = Math.max(0, Math.min(newY, screenH - curH));
                        titleParams.x = newX;
                        titleParams.y = newY;
                        if (titleBarView != null && titleBarView.getParent() != null) {
                            windowManager.updateViewLayout(titleBarView, titleParams);
                        }
                        return true;
                }
                return false;
            }
        });
    }

    private void toggleRestoreMode() {
        if (isRestored) {
            // Return to Maximized Mode
            isRestored = false;
            titleParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            titleParams.height = dpToPx(28);
            titleParams.x = 0;
            titleParams.y = 0;
            btnMax.setImageResource(R.drawable.btn_max);
        } else {
            // Switch to Restored Floating Frame Mode
            isRestored = true;
            titleParams.width = dpToPx(520);
            titleParams.height = dpToPx(28);
            titleParams.x = dpToPx(40);
            titleParams.y = dpToPx(30);
            btnMax.setImageResource(R.drawable.btn_restore);
        }
        applyThemeColors();
        if (titleBarView != null && titleBarView.getParent() != null) {
            windowManager.updateViewLayout(titleBarView, titleParams);
        }
    }

    private void minimizeApp() {
        Intent home = new Intent(this, LauncherActivity.class);
        home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(home);
        hideOverlay();
        Toast.makeText(this, appLabel + " minimized to taskbar", Toast.LENGTH_SHORT).show();
    }

    private void closeApp() {
        Intent home = new Intent(this, LauncherActivity.class);
        home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(home);
        hideOverlay();
        if (packageName != null && !packageName.isEmpty()) {
            try {
                ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                if (am != null) {
                    am.killBackgroundProcesses(packageName);
                }
            } catch (Exception ignored) {}
        }
    }

    private void showOverlay() {
        try {
            if (titleBarView != null && titleBarView.getParent() == null) {
                windowManager.addView(titleBarView, titleParams);
            }
        } catch (Exception ignored) {}
        try {
            if (taskbarOverlayView != null && taskbarOverlayView.getParent() == null) {
                windowManager.addView(taskbarOverlayView, taskbarParams);
            }
        } catch (Exception ignored) {}
    }

    private void hideOverlay() {
        try {
            if (titleBarView != null && titleBarView.getParent() != null) {
                windowManager.removeView(titleBarView);
            }
        } catch (Exception ignored) {}
        try {
            if (taskbarOverlayView != null && taskbarOverlayView.getParent() != null) {
                windowManager.removeView(taskbarOverlayView);
            }
        } catch (Exception ignored) {}
    }

    private void applyThemeColors() {
        if (titleBarView == null) return;
        ThemeManager tm = ThemeManager.getInstance();
        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{tm.getTitlebarActiveStart(), tm.getTitlebarActiveEnd()});
        if (isRestored) {
            gd.setCornerRadius(dpToPx(4));
            gd.setStroke(dpToPx(1), tm.getWindowBorderColor());
        } else {
            gd.setCornerRadii(new float[]{dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4), 0, 0, 0, 0});
        }
        titleBarView.setBackgroundDrawable(gd);
        if (tvTitle != null) {
            tvTitle.setTextColor(tm.getTitlebarTextColor());
        }
        if (taskbarOverlayView != null) {
            taskbarOverlayView.setBackgroundResource(tm.getTaskbarBgRes());
        }
        if (btnStartOverlay != null) {
            btnStartOverlay.setImageResource(tm.getAuthenticStartButtonRes());
            ViewGroup.LayoutParams lp = btnStartOverlay.getLayoutParams();
            if (lp != null) {
                lp.width = getStartButtonWidth(tm.getCurrentTheme());
                btnStartOverlay.setLayoutParams(lp);
            }
        }
    }

    @Override
    public void onThemeChanged(ThemeManager.Theme newTheme) {
        applyThemeColors();
        refreshOverlayTabs();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isPollingActive = false;
        handler.removeCallbacks(taskWatcher);
        ThemeManager.getInstance().removeListener(this);
        hideOverlay();
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }
}
