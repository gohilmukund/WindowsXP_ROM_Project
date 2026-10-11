package com.windows.xp.launcher.window;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.windows.xp.launcher.R;
import com.windows.xp.launcher.theme.ThemeManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DisplayPropertiesWindowView extends LinearLayout {

    public interface OnDialogActionListener {
        void onClose();
    }

    private OnDialogActionListener actionListener;
    private ThemeManager.Theme selectedTheme;
    private ThemeManager.WallpaperItem selectedWallpaper;
    private boolean hasUserModifiedWallpaper = false;

    private CrtPreviewView crtThemePreview;
    private CrtPreviewView crtDesktopPreview;

    private FrameLayout tabContentContainer;
    private View tabThemes;
    private View tabDesktop;

    private TextView btnTabThemes;
    private TextView btnTabDesktop;
    private Button btnApply;

    private final List<ThemeManager.WallpaperItem> wallpaperList = new ArrayList<ThemeManager.WallpaperItem>();
    private LinearLayout wallpaperListContainer;

    public DisplayPropertiesWindowView(Context context) {
        super(context);
        init(context);
    }

    public DisplayPropertiesWindowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void setOnDialogActionListener(OnDialogActionListener l) {
        this.actionListener = l;
    }

    private void init(Context context) {
        setOrientation(VERTICAL);
        setBackgroundColor(0xFFECE9D8);
        setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(10));

        ThemeManager tm = ThemeManager.getInstance();
        tm.init(context);
        selectedTheme = tm.getCurrentTheme();

        // 1. Tab Bar
        LinearLayout tabBar = new LinearLayout(context);
        tabBar.setOrientation(HORIZONTAL);
        tabBar.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(26)));

        btnTabThemes = createTabButton(context, "Themes", true);
        btnTabDesktop = createTabButton(context, "Desktop", false);

        btnTabThemes.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) { selectTab(0); }
        });
        btnTabDesktop.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) { selectTab(1); }
        });

        tabBar.addView(btnTabThemes);
        tabBar.addView(btnTabDesktop);
        addView(tabBar);

        // 2. Tab Content Container
        tabContentContainer = new FrameLayout(context);
        LayoutParams contentParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f);
        tabContentContainer.setLayoutParams(contentParams);
        tabContentContainer.setBackgroundColor(0xFFECE9D8);
        tabContentContainer.setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10));

        buildThemesTab(context);
        buildDesktopTab(context);

        tabContentContainer.addView(tabThemes);
        tabContentContainer.addView(tabDesktop);
        addView(tabContentContainer);

        // 3. Bottom Button Row (OK, Cancel, Apply)
        LinearLayout btnRow = new LinearLayout(context);
        btnRow.setOrientation(HORIZONTAL);
        btnRow.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        btnRow.setPadding(0, dpToPx(8), 0, 0);

        Button btnOk = createDialogButton(context, "OK");
        Button btnCancel = createDialogButton(context, "Cancel");
        btnApply = createDialogButton(context, "Apply");
        btnApply.setEnabled(false);

        btnOk.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) {
                applyChanges();
                if (actionListener != null) actionListener.onClose();
            }
        });
        btnCancel.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) {
                if (actionListener != null) actionListener.onClose();
            }
        });
        btnApply.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) {
                applyChanges();
                btnApply.setEnabled(false);
            }
        });

        btnRow.addView(btnOk);
        btnRow.addView(btnCancel);
        btnRow.addView(btnApply);
        addView(btnRow);

        selectTab(0);
    }

    private void applyChanges() {
        ThemeManager tm = ThemeManager.getInstance();
        boolean changed = false;

        // Apply theme if changed
        if (selectedTheme != tm.getCurrentTheme()) {
            tm.setTheme(selectedTheme);
            changed = true;
        }

        // Apply wallpaper if explicitly selected
        if (hasUserModifiedWallpaper && selectedWallpaper != null) {
            if (selectedWallpaper.isCustom()) {
                tm.setUserWallpaperPath(selectedWallpaper.getFilePath());
            } else {
                tm.setUserWallpaperRes(selectedWallpaper.getResId());
            }
            changed = true;
        }
        hasUserModifiedWallpaper = false;

        if (changed) {
            Toast.makeText(getContext(), "Display settings applied", Toast.LENGTH_SHORT).show();
        }
    }

    private TextView createTabButton(Context context, String label, boolean active) {
        TextView tv = new TextView(context);
        tv.setText(label);
        tv.setTextSize(11);
        tv.setPadding(dpToPx(12), dpToPx(4), dpToPx(12), dpToPx(4));
        tv.setGravity(Gravity.CENTER);
        tv.setClickable(true);
        updateTabButtonStyle(tv, active);
        return tv;
    }

    private void updateTabButtonStyle(TextView tv, boolean active) {
        if (active) {
            tv.setBackgroundColor(0xFFECE9D8);
            tv.setTextColor(Color.BLACK);
            tv.setTypeface(null, Typeface.BOLD);
        } else {
            tv.setBackgroundColor(0xFFDCD8C8);
            tv.setTextColor(0xFF555555);
            tv.setTypeface(null, Typeface.NORMAL);
        }
    }

    private void selectTab(int index) {
        updateTabButtonStyle(btnTabThemes, index == 0);
        updateTabButtonStyle(btnTabDesktop, index == 1);

        tabThemes.setVisibility(index == 0 ? VISIBLE : GONE);
        tabDesktop.setVisibility(index == 1 ? VISIBLE : GONE);
    }

    private void buildThemesTab(Context context) {
        ScrollView scroll = new ScrollView(context);
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(VERTICAL);

        TextView tvDesc = new TextView(context);
        tvDesc.setText("A theme is a background plus a set of sounds, icons, and elements to help you personalize your computer.\n");
        tvDesc.setTextSize(11);
        tvDesc.setTextColor(Color.BLACK);
        layout.addView(tvDesc);

        TextView tvLbl = new TextView(context);
        tvLbl.setText("Theme:");
        tvLbl.setTypeface(null, Typeface.BOLD);
        tvLbl.setTextSize(11);
        tvLbl.setTextColor(Color.BLACK);
        layout.addView(tvLbl);

        Spinner spinner = new Spinner(context);
        final ThemeManager.Theme[] themes = ThemeManager.Theme.values();
        String[] themeNames = new String[themes.length];
        int selectedIndex = 0;
        for (int i = 0; i < themes.length; i++) {
            themeNames[i] = themes[i].getDisplayName();
            if (themes[i] == selectedTheme) {
                selectedIndex = i;
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, themeNames);
        spinner.setAdapter(adapter);
        spinner.setSelection(selectedIndex);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTheme = themes[position];
                if (crtThemePreview != null) {
                    crtThemePreview.setPreviewTheme(selectedTheme);
                }
                if (crtDesktopPreview != null) {
                    crtDesktopPreview.setPreviewTheme(selectedTheme);
                }
                if (selectedTheme != ThemeManager.getInstance().getCurrentTheme() || hasUserModifiedWallpaper) {
                    if (btnApply != null) btnApply.setEnabled(true);
                }
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        layout.addView(spinner);

        // Live CRT Monitor Preview
        TextView tvSample = new TextView(context);
        tvSample.setText("\nSample Preview:");
        tvSample.setTypeface(null, Typeface.BOLD);
        tvSample.setTextSize(11);
        tvSample.setTextColor(Color.BLACK);
        layout.addView(tvSample);

        crtThemePreview = new CrtPreviewView(context);
        crtThemePreview.setLayoutParams(new LayoutParams(dpToPx(240), dpToPx(130)));
        crtThemePreview.setPreviewTheme(selectedTheme);
        layout.addView(crtThemePreview);

        scroll.addView(layout);
        tabThemes = scroll;
    }

    private void buildDesktopTab(final Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(VERTICAL);

        // 1. Top: CRT Monitor Preview
        LinearLayout previewContainer = new LinearLayout(context);
        previewContainer.setGravity(Gravity.CENTER);
        previewContainer.setPadding(0, 0, 0, dpToPx(6));

        crtDesktopPreview = new CrtPreviewView(context);
        crtDesktopPreview.setLayoutParams(new LayoutParams(dpToPx(200), dpToPx(115)));
        crtDesktopPreview.setPreviewTheme(selectedTheme);
        previewContainer.addView(crtDesktopPreview);
        layout.addView(previewContainer);

        // 2. Middle: "Background:" Label
        TextView tvLabel = new TextView(context);
        tvLabel.setText("Background:");
        tvLabel.setTextSize(11);
        tvLabel.setTypeface(null, Typeface.BOLD);
        tvLabel.setTextColor(Color.BLACK);
        tvLabel.setPadding(0, 0, 0, dpToPx(4));
        layout.addView(tvLabel);

        // 3. Selection row: Wallpaper List Box (Left) + Buttons (Right)
        LinearLayout selectRow = new LinearLayout(context);
        selectRow.setOrientation(HORIZONTAL);
        selectRow.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));

        // Bordered scrollable list box (XP Style inset)
        FrameLayout listBoxBorder = new FrameLayout(context);
        LayoutParams listLp = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
        listBoxBorder.setLayoutParams(listLp);
        listBoxBorder.setBackgroundColor(Color.WHITE);

        // 3D Inset border
        GradientDrawable borderDrawable = new GradientDrawable();
        borderDrawable.setColor(Color.WHITE);
        borderDrawable.setStroke(dpToPx(1), 0xFF7F9DB9);
        listBoxBorder.setBackgroundDrawable(borderDrawable);

        ScrollView listScroll = new ScrollView(context);
        listScroll.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        wallpaperListContainer = new LinearLayout(context);
        wallpaperListContainer.setOrientation(VERTICAL);
        wallpaperListContainer.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        populateWallpaperList(context);

        listScroll.addView(wallpaperListContainer);
        listBoxBorder.addView(listScroll);
        selectRow.addView(listBoxBorder);

        // Right side button panel
        LinearLayout rightButtons = new LinearLayout(context);
        rightButtons.setOrientation(VERTICAL);
        LayoutParams rightLp = new LayoutParams(dpToPx(85), ViewGroup.LayoutParams.WRAP_CONTENT);
        rightLp.leftMargin = dpToPx(8);
        rightButtons.setLayoutParams(rightLp);

        Button btnBrowse = createDialogButton(context, "Browse...");
        LayoutParams bLp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(28));
        bLp.leftMargin = 0;
        btnBrowse.setLayoutParams(bLp);
        btnBrowse.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                openBrowseDialog(context);
            }
        });
        rightButtons.addView(btnBrowse);

        selectRow.addView(rightButtons);
        layout.addView(selectRow);

        tabDesktop = layout;
    }

    private void populateWallpaperList(Context context) {
        ThemeManager tm = ThemeManager.getInstance();
        wallpaperList.clear();

        // 1. If user currently has a custom wallpaper path, put it at top
        String customPath = tm.getUserWallpaperPath();
        if (customPath != null && new File(customPath).exists()) {
            File f = new File(customPath);
            ThemeManager.WallpaperItem customItem = new ThemeManager.WallpaperItem(f.getName(), customPath);
            wallpaperList.add(customItem);
            selectedWallpaper = customItem;
        }

        // 2. Built-in authentic wallpapers
        List<ThemeManager.WallpaperItem> builtIns = tm.getBuiltInWallpapers();
        wallpaperList.addAll(builtIns);

        // Determine initial selected wallpaper if not already set by custom
        if (selectedWallpaper == null) {
            int currentRes = tm.getUserWallpaperRes();
            if (currentRes > 0) {
                for (ThemeManager.WallpaperItem item : wallpaperList) {
                    if (item.getResId() == currentRes) {
                        selectedWallpaper = item;
                        break;
                    }
                }
            } else {
                int themeDefaultRes = tm.getWallpaperRes();
                for (ThemeManager.WallpaperItem item : wallpaperList) {
                    if (item.getResId() == themeDefaultRes) {
                        selectedWallpaper = item;
                        break;
                    }
                }
            }
            if (selectedWallpaper == null && !wallpaperList.isEmpty()) {
                selectedWallpaper = wallpaperList.get(0);
            }
        }

        // Set initial CRT preview
        if (crtDesktopPreview != null && selectedWallpaper != null) {
            crtDesktopPreview.setPreviewWallpaper(selectedWallpaper.getResId(), selectedWallpaper.getFilePath());
        }

        renderWallpaperListRows(context);
    }

    private void renderWallpaperListRows(final Context context) {
        if (wallpaperListContainer == null) return;
        wallpaperListContainer.removeAllViews();

        for (int i = 0; i < wallpaperList.size(); i++) {
            final ThemeManager.WallpaperItem item = wallpaperList.get(i);
            final LinearLayout row = new LinearLayout(context);
            row.setOrientation(HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dpToPx(6), dpToPx(4), dpToPx(6), dpToPx(4));
            row.setClickable(true);

            boolean isSelected = (selectedWallpaper != null && item == selectedWallpaper);

            // Icon
            ImageView ivIcon = new ImageView(context);
            ivIcon.setLayoutParams(new LayoutParams(dpToPx(16), dpToPx(16)));
            ivIcon.setImageResource(R.drawable.ic_display);
            row.addView(ivIcon);

            // Label
            TextView tv = new TextView(context);
            tv.setText(" " + item.getName());
            tv.setTextSize(11);
            tv.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            if (isSelected) {
                row.setBackgroundColor(0xFF316AC5); // XP selection blue
                tv.setTextColor(Color.WHITE);
            } else {
                row.setBackgroundColor(Color.TRANSPARENT);
                tv.setTextColor(Color.BLACK);
            }

            row.addView(tv);

            row.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    hasUserModifiedWallpaper = true;
                    selectedWallpaper = item;
                    if (crtDesktopPreview != null) {
                        crtDesktopPreview.setPreviewWallpaper(item.getResId(), item.getFilePath());
                    }
                    if (btnApply != null) {
                        btnApply.setEnabled(true);
                    }
                    renderWallpaperListRows(context);
                }
            });

            wallpaperListContainer.addView(row);
        }
    }

    private void openBrowseDialog(final Context context) {
        // Collect candidate image directories
        final List<File> candidateDirs = new ArrayList<File>();
        File picturesDir = new File("/sdcard/Pictures");
        if (picturesDir.exists()) candidateDirs.add(picturesDir);

        File dcimCamera = new File("/sdcard/DCIM/Camera");
        if (dcimCamera.exists()) candidateDirs.add(dcimCamera);

        File dcim = new File("/sdcard/DCIM");
        if (dcim.exists() && !candidateDirs.contains(dcimCamera)) candidateDirs.add(dcim);

        File downloadDir = new File("/sdcard/Download");
        if (downloadDir.exists()) candidateDirs.add(downloadDir);

        File sdcard = new File("/sdcard");
        if (sdcard.exists() && !candidateDirs.contains(sdcard)) candidateDirs.add(sdcard);

        // Gather all image files
        final List<File> imageFiles = new ArrayList<File>();
        for (File dir : candidateDirs) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isFile()) {
                        String name = f.getName().toLowerCase();
                        if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") ||
                            name.endsWith(".webp") || name.endsWith(".bmp")) {
                            imageFiles.add(f);
                        }
                    }
                }
            }
        }

        if (imageFiles.isEmpty()) {
            AlertDialog.Builder alert = new AlertDialog.Builder(context);
            alert.setTitle("Browse for Background");
            alert.setMessage("No image files found on device storage (/sdcard/Pictures, /sdcard/DCIM, /sdcard/Download).\n\nPlease add images to your Pictures folder first.");
            alert.setPositiveButton("OK", null);
            alert.show();
            return;
        }

        String[] fileNames = new String[imageFiles.size()];
        for (int i = 0; i < imageFiles.size(); i++) {
            File f = imageFiles.get(i);
            fileNames[i] = f.getName() + " (" + (f.length() / 1024) + " KB)";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select a Background Image");
        builder.setItems(fileNames, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                File chosen = imageFiles.get(which);
                ThemeManager.WallpaperItem customItem = new ThemeManager.WallpaperItem(chosen.getName(), chosen.getAbsolutePath());

                // Insert at top of list and select
                wallpaperList.add(0, customItem);
                selectedWallpaper = customItem;
                hasUserModifiedWallpaper = true;

                if (crtDesktopPreview != null) {
                    crtDesktopPreview.setPreviewWallpaper(0, chosen.getAbsolutePath());
                }
                if (btnApply != null) {
                    btnApply.setEnabled(true);
                }
                renderWallpaperListRows(context);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private Button createDialogButton(Context context, String text) {
        Button b = new Button(context);
        b.setText(text);
        b.setTextSize(11);
        LayoutParams lp = new LayoutParams(dpToPx(75), dpToPx(28));
        lp.leftMargin = dpToPx(6);
        b.setLayoutParams(lp);
        b.setPadding(0, 0, 0, 0);
        return b;
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    // --- Miniature CRT Monitor Preview View ---
    private static class CrtPreviewView extends View {
        private ThemeManager.Theme previewTheme = ThemeManager.Theme.LUNA_BLUE;
        private Drawable previewWallpaperDrawable = null;
        private Bitmap previewWallpaperBitmap = null;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        private final RectF bezelRect = new RectF();
        private final RectF standBaseRect = new RectF();
        private final Rect srcRect = new Rect();
        private final Rect dstRect = new Rect();

        public CrtPreviewView(Context context) {
            super(context);
            initDefaultWallpaper();
        }

        private void initDefaultWallpaper() {
            try {
                previewWallpaperDrawable = getResources().getDrawable(R.drawable.bliss_wallpaper);
            } catch (Exception ignored) {}
        }

        public void setPreviewTheme(ThemeManager.Theme t) {
            this.previewTheme = t;
            int wallRes = getThemeDefaultWallRes(t);
            setPreviewWallpaper(wallRes, null);
        }

        private int getThemeDefaultWallRes(ThemeManager.Theme t) {
            switch (t) {
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

        public void setPreviewWallpaper(int resId, String filePath) {
            if (filePath != null) {
                try {
                    previewWallpaperBitmap = ThemeManager.decodeSampledBitmapFromFile(filePath, 240, 140);
                    previewWallpaperDrawable = null;
                } catch (Exception e) {
                    previewWallpaperBitmap = null;
                }
            } else if (resId > 0) {
                previewWallpaperBitmap = null;
                try {
                    previewWallpaperDrawable = getResources().getDrawable(resId);
                } catch (Exception e) {
                    previewWallpaperDrawable = null;
                }
            }
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            int w = getWidth();
            int h = getHeight();

            // 1. CRT Monitor Beige Bezel
            paint.setColor(0xFFD6D0C0);
            bezelRect.set(4, 4, w - 4, h - 20);
            canvas.drawRoundRect(bezelRect, 8, 8, paint);

            // Stand
            paint.setColor(0xFFB8B0A0);
            canvas.drawRect(w / 2 - 20, h - 20, w / 2 + 20, h - 6, paint);
            standBaseRect.set(w / 2 - 40, h - 8, w / 2 + 40, h);
            canvas.drawRoundRect(standBaseRect, 4, 4, paint);

            // 2. Inner Screen
            int screenX = 14;
            int screenY = 14;
            int screenW = w - 28;
            int screenH = h - 38;

            int wallColor = 0xFF2B5B9C;
            int taskbarColor = 0xFF245EDC;
            int titleStart = 0xFF0055EA;
            int startBtnColor = 0xFF3C9E35;

            switch (previewTheme) {
                case LUNA_OLIVE:
                    wallColor = 0xFF587A3E;
                    taskbarColor = 0xFF678540;
                    titleStart = 0xFF759954;
                    startBtnColor = 0xFF6A8A45;
                    break;
                case LUNA_SILVER:
                    wallColor = 0xFF70788C;
                    taskbarColor = 0xFFB4BAC9;
                    titleStart = 0xFFE2E4EB;
                    startBtnColor = 0xFF3C9E35;
                    break;
                case ROYALE:
                    wallColor = 0xFF10488A;
                    taskbarColor = 0xFF003B99;
                    titleStart = 0xFF1073E6;
                    startBtnColor = 0xFF28A048;
                    break;
                case CLASSIC:
                    wallColor = 0xFF008080;
                    taskbarColor = 0xFFD4D0C8;
                    titleStart = 0xFF0A246A;
                    startBtnColor = 0xFFD4D0C8;
                    break;
                case VISTA:
                    wallColor = 0xFF14243B;
                    taskbarColor = 0xFF141920;
                    titleStart = 0xFF1B222A;
                    startBtnColor = 0xFF263342;
                    break;
                case WIN7_AERO:
                    wallColor = 0xFF1A4676;
                    taskbarColor = 0xFF143657;
                    titleStart = 0xFF1A385C;
                    startBtnColor = 0xFF2164A8;
                    break;
                case WIN11:
                    wallColor = 0xFFE0E8F5;
                    taskbarColor = 0xFFF3F3F3;
                    titleStart = 0xFFE8E8E8;
                    startBtnColor = 0xFF0078D4;
                    break;
            }

            // Desktop wallpaper
            canvas.save();
            canvas.clipRect(screenX, screenY, screenX + screenW, screenY + screenH);
            if (previewWallpaperBitmap != null && !previewWallpaperBitmap.isRecycled()) {
                srcRect.set(0, 0, previewWallpaperBitmap.getWidth(), previewWallpaperBitmap.getHeight());
                dstRect.set(screenX, screenY, screenX + screenW, screenY + screenH);
                canvas.drawBitmap(previewWallpaperBitmap, srcRect, dstRect, paint);
            } else if (previewWallpaperDrawable != null) {
                previewWallpaperDrawable.setBounds(screenX, screenY, screenX + screenW, screenY + screenH);
                previewWallpaperDrawable.draw(canvas);
            } else {
                paint.setColor(wallColor);
                canvas.drawRect(screenX, screenY, screenX + screenW, screenY + screenH, paint);
            }
            canvas.restore();

            // Sample Window on screen
            int winX = screenX + 24;
            int winY = screenY + 16;
            int winW = screenW - 48;
            int winH = screenH - 36;

            // Window content
            paint.setColor(Color.WHITE);
            canvas.drawRect(winX, winY, winX + winW, winY + winH, paint);

            // Window Titlebar
            paint.setColor(titleStart);
            canvas.drawRect(winX, winY, winX + winW, winY + 12, paint);

            // Window Close button
            paint.setColor(0xFFD03020);
            canvas.drawRect(winX + winW - 10, winY + 2, winX + winW - 2, winY + 10, paint);

            // Taskbar at bottom of screen
            paint.setColor(taskbarColor);
            canvas.drawRect(screenX, screenY + screenH - 10, screenX + screenW, screenY + screenH, paint);

            // Start button
            paint.setColor(startBtnColor);
            canvas.drawRect(screenX, screenY + screenH - 10, screenX + 26, screenY + screenH, paint);

            // Sample Active Taskbar Tab Button
            int tabColor = 0xFF16398A;
            switch (previewTheme) {
                case LUNA_OLIVE: tabColor = 0xFF465F29; break;
                case LUNA_SILVER: tabColor = 0xFF969CAE; break;
                case ROYALE: tabColor = 0xFF002B7A; break;
                case CLASSIC: tabColor = 0xFFC8C4BC; break;
                case VISTA: tabColor = 0xFF284A6E; break;
                case WIN7_AERO: tabColor = 0xFF4E92CC; break;
                case WIN11: tabColor = 0xFFD0D0D0; break;
            }
            paint.setColor(tabColor);
            canvas.drawRect(screenX + 28, screenY + screenH - 9, screenX + 62, screenY + screenH - 1, paint);
        }
    }
}
