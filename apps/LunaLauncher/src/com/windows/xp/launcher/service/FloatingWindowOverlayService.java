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

    // Window Frame Border Overlay (for Restored Floating Window)
    private View windowBorderView;
    private WindowManager.LayoutParams borderParams;

    // Immersive Mode Exit Pill
    private LinearLayout immersivePillView;
    private WindowManager.LayoutParams pillParams;

    // Bottom Taskbar Overlay
    private RelativeLayout taskbarOverlayView;
    private WindowManager.LayoutParams taskbarParams;
    private ImageView btnStartOverlay;
    private LinearLayout overlayTabsContainer;
    private LinearLayout trayOverlayLayout;
    private TextView tvTrayClock;

    private String packageName = "";
    private String appLabel = "Application";
    private boolean isRestored = false;
    private boolean isImmersive = false;

    // Restored window coordinates
    private int winX = 112;
    private int winY = 32;
    private int winW = 800;
    private int winH = 480;

    // Drag-to-move coordinates
    private int initialX, initialY;
    private float initialTouchX, initialTouchY;

    private static final java.util.concurrent.ExecutorService OVERSCAN_EXECUTOR =
            java.util.concurrent.Executors.newSingleThreadExecutor();
    private static java.lang.reflect.Method sSetOverscanMethod = null;
    private static Object sWindowManagerService = null;
    private static boolean sReflectionInitialized = false;

    private String lastTasksCacheKey = "";

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
                                // User returned to desktop or lockscreen - hide overlays and clear overscan
                                isRestored = false;
                                isImmersive = false;
                                if (btnMax != null) {
                                    btnMax.setImageResource(R.drawable.btn_max);
                                }
                                resetOverscan();
                                hideOverlay();
                                hideBorder();
                                hideImmersivePill();
                            } else {
                                // User is in an external app
                                if (!topPkg.equals(packageName)) {
                                    updateForPackage(topPkg);
                                    isImmersive = false;
                                    if (btnMax != null) {
                                        btnMax.setImageResource(R.drawable.btn_max);
                                    }
                                }
                                if (!isImmersive) {
                                    showOverlay();
                                    hideBorder();
                                    refreshOverlayTabs();
                                    updateOverlayClock();
                                }
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
        createWindowBorderOverlay();
        createImmersivePillOverlay();
        createTaskbarOverlay();

        isPollingActive = true;
        handler.post(taskWatcher);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (ACTION_CLOSE_OVERLAY.equals(intent.getAction())) {
                isRestored = false;
                isImmersive = false;
                if (btnMax != null) {
                    btnMax.setImageResource(R.drawable.btn_max);
                }
                resetOverscan();
                hideOverlay();
                hideBorder();
                hideImmersivePill();
                return START_NOT_STICKY;
            }
            if (intent.hasExtra(EXTRA_PACKAGE_NAME)) {
                String pkg = intent.getStringExtra(EXTRA_PACKAGE_NAME);
                if (pkg != null && !pkg.isEmpty()) {
                    if (!pkg.equals(packageName)) {
                        isImmersive = false;
                        if (btnMax != null) {
                            btnMax.setImageResource(R.drawable.btn_max);
                        }
                    }
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
        if (!isImmersive) {
            showOverlay();
            hideBorder();
            refreshOverlayTabs();
            updateOverlayClock();
        }
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

        // Maximize / Restore (starts in windowed mode, so icon is maximize)
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
        applyThemeColors();
    }

    private void createWindowBorderOverlay() {
        int screenW = getResources().getDisplayMetrics().widthPixels;
        winW = dpToPx(760);
        winH = dpToPx(480);
        winX = (screenW - winW) / 2;
        winY = dpToPx(32);

        windowBorderView = new View(this);
        borderParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        borderParams.gravity = Gravity.TOP | Gravity.LEFT;
        borderParams.x = 0;
        borderParams.y = 0;
        updateBorderDrawable();
    }

    private void updateBorderDrawable() {
        if (windowBorderView == null) return;
        ThemeManager tm = ThemeManager.getInstance();
        GradientDrawable border = new GradientDrawable();
        border.setColor(Color.TRANSPARENT);
        border.setStroke(dpToPx(3), tm.getWindowBorderColor());
        border.setCornerRadius(dpToPx(4));
        windowBorderView.setBackgroundDrawable(border);
    }

    private void createImmersivePillOverlay() {
        immersivePillView = new LinearLayout(this);
        immersivePillView.setOrientation(LinearLayout.HORIZONTAL);
        immersivePillView.setGravity(Gravity.CENTER);
        immersivePillView.setPadding(dpToPx(10), dpToPx(3), dpToPx(10), dpToPx(3));

        GradientDrawable pillBg = new GradientDrawable();
        pillBg.setColor(0xCC1A2436);
        pillBg.setCornerRadii(new float[]{0, 0, 0, 0, dpToPx(6), dpToPx(6), dpToPx(6), dpToPx(6)});
        pillBg.setStroke(dpToPx(1), 0x88FFFFFF);
        immersivePillView.setBackgroundDrawable(pillBg);

        ImageView pillIcon = new ImageView(this);
        pillIcon.setImageResource(R.drawable.btn_restore);
        LinearLayout.LayoutParams icLp = new LinearLayout.LayoutParams(dpToPx(14), dpToPx(14));
        icLp.rightMargin = dpToPx(6);
        immersivePillView.addView(pillIcon, icLp);

        TextView pillText = new TextView(this);
        pillText.setText("Exit Fullscreen");
        pillText.setTextColor(Color.WHITE);
        pillText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        pillText.setTypeface(Typeface.DEFAULT_BOLD);
        immersivePillView.addView(pillText);

        pillParams = new WindowManager.LayoutParams(
                dpToPx(120),
                dpToPx(24),
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        pillParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        pillParams.y = 0;

        immersivePillView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleImmersiveMode();
            }
        });
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
        int startW = dpToPx(tm.getStartButtonWidthDp());
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
        trayOverlayLayout = new LinearLayout(this);
        trayOverlayLayout.setId(3002);
        trayOverlayLayout.setOrientation(LinearLayout.HORIZONTAL);
        trayOverlayLayout.setGravity(Gravity.CENTER_VERTICAL);
        trayOverlayLayout.setPadding(dpToPx(6), 0, dpToPx(8), 0);
        trayOverlayLayout.setBackgroundResource(tm.getTaskbarTrayBgRes());
        RelativeLayout.LayoutParams trayLp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, taskbarHeight);
        trayLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        trayLp.addRule(RelativeLayout.CENTER_VERTICAL);
        trayOverlayLayout.setLayoutParams(trayLp);

        // Volume icon
        ImageView trayVol = new ImageView(this);
        trayVol.setImageResource(R.drawable.ic_tray_volume);
        LinearLayout.LayoutParams volLp = new LinearLayout.LayoutParams(dpToPx(16), dpToPx(16));
        volLp.rightMargin = dpToPx(6);
        trayOverlayLayout.addView(trayVol, volLp);

        // Clock
        tvTrayClock = new TextView(this);
        tvTrayClock.setTextColor(tm.getTaskbarTrayTextColor());
        tvTrayClock.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        trayOverlayLayout.addView(tvTrayClock);

        taskbarOverlayView.addView(trayOverlayLayout);

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
        return dpToPx(ThemeManager.getInstance().getStartButtonWidthDp());
    }

    private void updateOverlayClock() {
        if (tvTrayClock != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.US);
            tvTrayClock.setText(sdf.format(new Date()));
        }
    }

    private void refreshOverlayTabs() {
        if (overlayTabsContainer == null) return;
        try {
            ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            if (am == null) return;
            List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(6);
            if (tasks == null) return;

            StringBuilder sb = new StringBuilder();
            for (ActivityManager.RunningTaskInfo task : tasks) {
                ComponentName cn = task.topActivity != null ? task.topActivity : task.baseActivity;
                if (cn != null) sb.append(cn.getPackageName()).append(";");
            }
            String currentKey = sb.toString();
            if (currentKey.equals(lastTasksCacheKey) && overlayTabsContainer.getChildCount() > 0) {
                return;
            }
            lastTasksCacheKey = currentKey;

            overlayTabsContainer.removeAllViews();
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
                ThemeManager tm = ThemeManager.getInstance();
                boolean isActive = pkg.equals(packageName);
                tab.setBackgroundResource(isActive ? tm.getTaskbarTabActiveRes() : tm.getTaskbarTabBgRes());
                tab.setPadding(dpToPx(8), dpToPx(2), dpToPx(10), dpToPx(2));
                tab.setClickable(true);

                ImageView img = new ImageView(this);
                img.setImageDrawable(icon);
                LinearLayout.LayoutParams imgP = new LinearLayout.LayoutParams(dpToPx(16), dpToPx(16));
                tab.addView(img, imgP);

                TextView tv = new TextView(this);
                tv.setText(label);
                tv.setTextColor(tm.getTaskbarTabTextColor());
                if (isActive) {
                    tv.setTypeface(Typeface.DEFAULT_BOLD);
                }
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

        // Click to toggle Windowed / Immersive Fullscreen
        btnMax.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.util.Log.i("FloatingOverlay", "btnMax CLICKED! isImmersive=" + isImmersive);
                toggleRestoreMode();
            }
        });

        // Long click to toggle Windowed / Immersive Fullscreen
        btnMax.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                android.util.Log.i("FloatingOverlay", "btnMax LONG CLICKED! isImmersive=" + isImmersive);
                toggleRestoreMode();
                return true;
            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.util.Log.i("FloatingOverlay", "btnClose CLICKED!");
                closeApp();
            }
        });

        // Drag to move window frame
        titleBarView.setOnTouchListener(new View.OnTouchListener() {
            private int dragWinX, dragWinY;
            private boolean isDragging = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // If touching within the right button area, let buttons handle clicks
                int btnAreaWidth = dpToPx(90);
                if (v.getWidth() > 0 && event.getX() > (v.getWidth() - btnAreaWidth)) {
                    return false;
                }
                if (!isRestored) {
                    return false;
                }
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = winX;
                        initialY = winY;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        dragWinX = winX;
                        dragWinY = winY;
                        isDragging = false;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        int screenW = getResources().getDisplayMetrics().widthPixels;
                        int screenH = getResources().getDisplayMetrics().heightPixels;
                        int dx = (int) (event.getRawX() - initialTouchX);
                        int dy = (int) (event.getRawY() - initialTouchY);
                        if (Math.abs(dx) > dpToPx(6) || Math.abs(dy) > dpToPx(6)) {
                            isDragging = true;
                        }
                        dragWinX = Math.max(0, Math.min(initialX + dx, screenW - winW));
                        dragWinY = Math.max(0, Math.min(initialY + dy, screenH - winH));
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (isDragging) {
                            winX = dragWinX;
                            winY = dragWinY;
                            resetOverscan();
                        }
                        return true;
                }
                return false;
            }
        });
    }

    private void updateBorderLayout() {
        if (windowBorderView != null && windowBorderView.getParent() != null && borderParams != null) {
            borderParams.x = 0;
            borderParams.y = 0;
            borderParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            borderParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            windowManager.updateViewLayout(windowBorderView, borderParams);
        }
    }

    private void applyAppViewportOverscan() {
        resetOverscan();
    }

    private void applyOverscan(int screenLeft, int screenTop, int screenRight, int screenBottom) {
        int rotation = android.view.Surface.ROTATION_90;
        try {
            if (windowManager != null && windowManager.getDefaultDisplay() != null) {
                rotation = windowManager.getDefaultDisplay().getRotation();
            }
        } catch (Exception ignored) {}

        int nativeLeft, nativeTop, nativeRight, nativeBottom;
        switch (rotation) {
            case android.view.Surface.ROTATION_0:
                nativeLeft = screenLeft;
                nativeTop = screenTop;
                nativeRight = screenRight;
                nativeBottom = screenBottom;
                break;
            case android.view.Surface.ROTATION_90: // Samsung Tab 3 standard landscape
                nativeLeft = screenBottom;
                nativeTop = screenLeft;
                nativeRight = screenTop;
                nativeBottom = screenRight;
                break;
            case android.view.Surface.ROTATION_180:
                nativeLeft = screenRight;
                nativeTop = screenBottom;
                nativeRight = screenLeft;
                nativeBottom = screenTop;
                break;
            case android.view.Surface.ROTATION_270:
                nativeLeft = screenTop;
                nativeTop = screenRight;
                nativeRight = screenBottom;
                nativeBottom = screenLeft;
                break;
            default:
                nativeLeft = screenBottom;
                nativeTop = screenLeft;
                nativeRight = screenTop;
                nativeBottom = screenRight;
                break;
        }
        setOverscanInternal(nativeLeft, nativeTop, nativeRight, nativeBottom);
    }

    private void resetOverscan() {
        setOverscanInternal(0, 0, 0, 0);
    }

    private void setOverscanInternal(final int left, final int top, final int right, final int bottom) {
        OVERSCAN_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                boolean success = false;
                try {
                    synchronized (FloatingWindowOverlayService.class) {
                        if (!sReflectionInitialized) {
                            try {
                                Class<?> smClass = Class.forName("android.os.ServiceManager");
                                java.lang.reflect.Method getServiceMethod = smClass.getMethod("getService", String.class);
                                android.os.IBinder binder = (android.os.IBinder) getServiceMethod.invoke(null, Context.WINDOW_SERVICE);
                                Class<?> stubClass = Class.forName("android.view.IWindowManager$Stub");
                                java.lang.reflect.Method asInterfaceMethod = stubClass.getMethod("asInterface", android.os.IBinder.class);
                                sWindowManagerService = asInterfaceMethod.invoke(null, binder);
                                if (sWindowManagerService != null) {
                                    sSetOverscanMethod = sWindowManagerService.getClass().getMethod("setOverscan", int.class, int.class, int.class, int.class, int.class);
                                }
                            } catch (Throwable e) {
                                android.util.Log.w("FloatingOverlay", "Failed to resolve setOverscan reflection: " + e.getMessage());
                            }
                            sReflectionInitialized = true;
                        }
                    }

                    if (sSetOverscanMethod != null && sWindowManagerService != null) {
                        sSetOverscanMethod.invoke(sWindowManagerService, 0, left, top, right, bottom);
                        success = true;
                        android.util.Log.i("FloatingOverlay", "setOverscan IPC succeeded: " + left + "," + top + "," + right + "," + bottom);
                    }
                } catch (Throwable t) {
                    android.util.Log.w("FloatingOverlay", "setOverscan IPC error: " + t.getMessage());
                }

                if (!success) {
                    try {
                        String cmd = (left == 0 && top == 0 && right == 0 && bottom == 0)
                                ? "wm overscan reset"
                                : "wm overscan " + left + "," + top + "," + right + "," + bottom;
                        Process p = Runtime.getRuntime().exec(cmd);
                        p.waitFor();
                    } catch (Throwable ignored) {}
                }
            }
        });
    }

    private void toggleRestoreMode() {
        toggleImmersiveMode();
    }

    public void toggleImmersiveMode() {
        isImmersive = !isImmersive;
        resetOverscan();
        hideBorder();
        if (isImmersive) {
            if (btnMax != null) {
                btnMax.setImageResource(R.drawable.btn_restore);
            }
            hideOverlay();
            showImmersivePill();
            Toast.makeText(this, "Immersive Mode (Tap top pill to exit)", Toast.LENGTH_SHORT).show();
            android.util.Log.i("FloatingOverlay", "Switched to Immersive Fullscreen Mode");
        } else {
            if (btnMax != null) {
                btnMax.setImageResource(R.drawable.btn_max);
            }
            hideImmersivePill();
            showOverlay();
            refreshOverlayTabs();
            updateOverlayClock();
            android.util.Log.i("FloatingOverlay", "Switched to Windowed Mode");
        }
        applyThemeColors();
    }

    private void minimizeApp() {
        isRestored = false;
        isImmersive = false;
        if (btnMax != null) {
            btnMax.setImageResource(R.drawable.btn_max);
        }
        resetOverscan();
        Intent home = new Intent(this, LauncherActivity.class);
        home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(home);
        hideOverlay();
        hideBorder();
        hideImmersivePill();
        Toast.makeText(this, appLabel + " minimized to taskbar", Toast.LENGTH_SHORT).show();
    }

    private void closeApp() {
        isRestored = false;
        isImmersive = false;
        if (btnMax != null) {
            btnMax.setImageResource(R.drawable.btn_max);
        }
        resetOverscan();
        Intent home = new Intent(this, LauncherActivity.class);
        home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(home);
        hideOverlay();
        hideBorder();
        hideImmersivePill();
        if (packageName != null && !packageName.isEmpty()) {
            try {
                ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                if (am != null) {
                    am.killBackgroundProcesses(packageName);
                }
            } catch (Exception ignored) {}
        }
    }

    private void showTaskbar() {
        try {
            if (taskbarOverlayView != null && taskbarOverlayView.getParent() == null) {
                windowManager.addView(taskbarOverlayView, taskbarParams);
            }
        } catch (Exception ignored) {}
    }

    private void hideTaskbar() {
        try {
            if (taskbarOverlayView != null && taskbarOverlayView.getParent() != null) {
                windowManager.removeView(taskbarOverlayView);
            }
        } catch (Exception ignored) {}
    }

    private void showOverlay() {
        try {
            if (titleBarView != null && titleBarView.getParent() == null) {
                windowManager.addView(titleBarView, titleParams);
            }
        } catch (Exception ignored) {}
        showTaskbar();
    }

    private void hideOverlay() {
        try {
            if (titleBarView != null && titleBarView.getParent() != null) {
                windowManager.removeView(titleBarView);
            }
        } catch (Exception ignored) {}
        hideTaskbar();
    }

    private void showBorder() {
        try {
            if (windowBorderView != null && windowBorderView.getParent() == null) {
                windowManager.addView(windowBorderView, borderParams);
            }
        } catch (Exception ignored) {}
    }

    private void hideBorder() {
        try {
            if (windowBorderView != null && windowBorderView.getParent() != null) {
                windowManager.removeView(windowBorderView);
            }
        } catch (Exception ignored) {}
    }

    private void showImmersivePill() {
        try {
            if (immersivePillView != null && immersivePillView.getParent() == null) {
                windowManager.addView(immersivePillView, pillParams);
            }
        } catch (Exception ignored) {}
    }

    private void hideImmersivePill() {
        try {
            if (immersivePillView != null && immersivePillView.getParent() != null) {
                windowManager.removeView(immersivePillView);
            }
        } catch (Exception ignored) {}
    }

    private void applyThemeColors() {
        if (titleBarView == null) return;
        ThemeManager tm = ThemeManager.getInstance();
        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{tm.getTitlebarActiveStart(), tm.getTitlebarActiveEnd()});
        gd.setCornerRadii(new float[]{dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4), 0, 0, 0, 0});
        titleBarView.setBackgroundDrawable(gd);
        if (tvTitle != null) {
            tvTitle.setTextColor(tm.getTitlebarTextColor());
        }
        updateBorderDrawable();
        if (taskbarOverlayView != null) {
            taskbarOverlayView.setBackgroundResource(tm.getTaskbarBgRes());
        }
        if (trayOverlayLayout != null) {
            trayOverlayLayout.setBackgroundResource(tm.getTaskbarTrayBgRes());
        }
        if (tvTrayClock != null) {
            tvTrayClock.setTextColor(tm.getTaskbarTrayTextColor());
        }
        if (btnStartOverlay != null) {
            btnStartOverlay.setImageResource(tm.getAuthenticStartButtonRes());
            ViewGroup.LayoutParams lp = btnStartOverlay.getLayoutParams();
            if (lp != null) {
                lp.width = dpToPx(tm.getStartButtonWidthDp());
                btnStartOverlay.setLayoutParams(lp);
            }
        }
    }

    @Override
    public void onThemeChanged(ThemeManager.Theme newTheme) {
        lastTasksCacheKey = "";
        applyThemeColors();
        refreshOverlayTabs();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isPollingActive = false;
        handler.removeCallbacks(taskWatcher);
        ThemeManager.getInstance().removeListener(this);
        resetOverscan();
        hideOverlay();
        hideBorder();
        hideImmersivePill();
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }
}
