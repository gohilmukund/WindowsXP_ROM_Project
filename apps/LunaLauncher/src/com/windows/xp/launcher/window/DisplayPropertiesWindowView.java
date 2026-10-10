package com.windows.xp.launcher.window;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.windows.xp.launcher.theme.ThemeManager;

public class DisplayPropertiesWindowView extends LinearLayout {

    public interface OnDialogActionListener {
        void onClose();
    }

    private OnDialogActionListener actionListener;
    private ThemeManager.Theme selectedTheme;
    private CrtPreviewView crtPreview;
    private FrameLayout tabContentContainer;
    private View tabThemes;
    private View tabDesktop;

    private TextView btnTabThemes;
    private TextView btnTabDesktop;

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

        selectedTheme = ThemeManager.getInstance().getCurrentTheme();

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
        final Button btnApply = createDialogButton(context, "Apply");

        btnOk.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) {
                applyTheme();
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
                applyTheme();
                btnApply.setEnabled(false);
            }
        });

        btnRow.addView(btnOk);
        btnRow.addView(btnCancel);
        btnRow.addView(btnApply);
        addView(btnRow);

        selectTab(0);
    }

    private void applyTheme() {
        ThemeManager.getInstance().setTheme(selectedTheme);
        Toast.makeText(getContext(), "Theme Applied: " + selectedTheme.getDisplayName(), Toast.LENGTH_SHORT).show();
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

        // Theme Dropdown / Selector
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
                if (crtPreview != null) {
                    crtPreview.setPreviewTheme(selectedTheme);
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

        crtPreview = new CrtPreviewView(context);
        crtPreview.setLayoutParams(new LayoutParams(dpToPx(240), dpToPx(140)));
        crtPreview.setPreviewTheme(selectedTheme);
        layout.addView(crtPreview);

        scroll.addView(layout);
        tabThemes = scroll;
    }

    private void buildDesktopTab(Context context) {
        ScrollView scroll = new ScrollView(context);
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(VERTICAL);

        TextView tvDesc = new TextView(context);
        tvDesc.setText("Select a background wallpaper for your desktop:\n");
        tvDesc.setTextSize(11);
        tvDesc.setTextColor(Color.BLACK);
        layout.addView(tvDesc);

        addWallpaperOption(layout, "Bliss (Windows XP Default)", ThemeManager.Theme.LUNA_BLUE);
        addWallpaperOption(layout, "Windows Classic Teal", ThemeManager.Theme.CLASSIC);
        addWallpaperOption(layout, "Windows Vista Aurora", ThemeManager.Theme.VISTA);
        addWallpaperOption(layout, "Windows 7 Harmony Blue", ThemeManager.Theme.WIN7_AERO);

        scroll.addView(layout);
        tabDesktop = scroll;
    }

    private void addWallpaperOption(LinearLayout parent, final String name, final ThemeManager.Theme theme) {
        TextView tv = new TextView(parent.getContext());
        tv.setText("• " + name);
        tv.setTextSize(12);
        tv.setTextColor(0xFF00138C);
        tv.setPadding(0, dpToPx(6), 0, dpToPx(6));
        tv.setClickable(true);
        tv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedTheme = theme;
                if (crtPreview != null) crtPreview.setPreviewTheme(selectedTheme);
                applyTheme();
            }
        });
        parent.addView(tv);
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
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        public CrtPreviewView(Context context) {
            super(context);
        }

        public void setPreviewTheme(ThemeManager.Theme t) {
            this.previewTheme = t;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            int w = getWidth();
            int h = getHeight();

            // 1. CRT Monitor Beige Bezel
            paint.setColor(0xFFD6D0C0);
            RectF bezel = new RectF(4, 4, w - 4, h - 20);
            canvas.drawRoundRect(bezel, 8, 8, paint);

            // Stand
            paint.setColor(0xFFB8B0A0);
            canvas.drawRect(w / 2 - 20, h - 20, w / 2 + 20, h - 6, paint);
            canvas.drawRoundRect(new RectF(w / 2 - 40, h - 8, w / 2 + 40, h), 4, 4, paint);

            // 2. Inner Screen
            int screenX = 14;
            int screenY = 14;
            int screenW = w - 28;
            int screenH = h - 38;

            int wallColor = 0xFF2B5B9C;
            int taskbarColor = 0xFF245EDC;
            int titleStart = 0xFF0055EA;
            int titleEnd = 0xFF00138C;
            int startBtnColor = 0xFF3C9E35;

            switch (previewTheme) {
                case LUNA_OLIVE:
                    wallColor = 0xFF587A3E;
                    taskbarColor = 0xFF678540;
                    titleStart = 0xFF759954;
                    titleEnd = 0xFF45672E;
                    startBtnColor = 0xFF6A8A45;
                    break;
                case LUNA_SILVER:
                    wallColor = 0xFF70788C;
                    taskbarColor = 0xFFB4BAC9;
                    titleStart = 0xFFE2E4EB;
                    titleEnd = 0xFFA6ABB8;
                    startBtnColor = 0xFFB8BED0;
                    break;
                case CLASSIC:
                    wallColor = 0xFF008080;
                    taskbarColor = 0xFFD4D0C8;
                    titleStart = 0xFF0A246A;
                    titleEnd = 0xFFA6CAF0;
                    startBtnColor = 0xFFD4D0C8;
                    break;
                case VISTA:
                    wallColor = 0xFF14243B;
                    taskbarColor = 0xFF141920;
                    titleStart = 0xFF1B222A;
                    titleEnd = 0xFF0D1217;
                    startBtnColor = 0xFF263342;
                    break;
                case WIN7_AERO:
                    wallColor = 0xFF1A4676;
                    taskbarColor = 0xFF143657;
                    titleStart = 0xFF1A385C;
                    titleEnd = 0xFF28588E;
                    startBtnColor = 0xFF2164A8;
                    break;
            }

            // Desktop wallpaper
            paint.setColor(wallColor);
            canvas.drawRect(screenX, screenY, screenX + screenW, screenY + screenH, paint);

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
        }
    }
}
