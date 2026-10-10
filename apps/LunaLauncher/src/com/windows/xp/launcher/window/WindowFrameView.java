package com.windows.xp.launcher.window;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.windows.xp.launcher.R;
import com.windows.xp.launcher.theme.ThemeManager;

public class WindowFrameView extends RelativeLayout implements ThemeManager.OnThemeChangeListener {

    public interface WindowListener {
        void onWindowFocus(WindowFrameView window);
        void onWindowMinimize(WindowFrameView window);
        void onWindowMaximize(WindowFrameView window, boolean isMaximized);
        void onWindowClose(WindowFrameView window);
    }

    private RelativeLayout titleBar;
    private ImageView imgIcon;
    private TextView tvTitle;
    private ImageView btnMin;
    private ImageView btnMax;
    private ImageView btnClose;
    private FrameLayout contentContainer;
    private View resizeGrip;

    private WindowListener listener;
    private boolean isMaximized = false;
    private boolean isActive = true;
    private final Rect floatingBounds = new Rect();

    // Drag-to-move tracking
    private float initialTouchX, initialTouchY;
    private float initialWindowX, initialWindowY;

    // Resize tracking
    private float initialResizeTouchX, initialResizeTouchY;
    private int initialWidth, initialHeight;

    public WindowFrameView(Context context) {
        super(context);
        init(context);
    }

    public WindowFrameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setBackgroundResource(R.drawable.window_frame_border);

        // 1. Title bar (28dp high)
        int titleBarHeight = dpToPx(28);
        titleBar = new RelativeLayout(context);
        titleBar.setId(1001);
        RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, titleBarHeight);
        titleParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        titleBar.setLayoutParams(titleParams);
        titleBar.setBackgroundResource(R.drawable.luna_titlebar_active);
        titleBar.setPadding(dpToPx(4), dpToPx(2), dpToPx(4), dpToPx(2));

        // Icon
        imgIcon = new ImageView(context);
        imgIcon.setId(1002);
        int iconSize = dpToPx(18);
        RelativeLayout.LayoutParams iconParams = new RelativeLayout.LayoutParams(iconSize, iconSize);
        iconParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        iconParams.addRule(RelativeLayout.CENTER_VERTICAL);
        iconParams.leftMargin = dpToPx(2);
        imgIcon.setLayoutParams(iconParams);
        titleBar.addView(imgIcon);

        // Control buttons container (Right)
        LinearLayout btnContainer = new LinearLayout(context);
        btnContainer.setId(1003);
        btnContainer.setOrientation(LinearLayout.HORIZONTAL);
        btnContainer.setGravity(Gravity.CENTER_VERTICAL);
        RelativeLayout.LayoutParams btnParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        btnParams.addRule(RelativeLayout.CENTER_VERTICAL);
        btnContainer.setLayoutParams(btnParams);

        int ctrlSize = dpToPx(21);
        int ctrlMargin = dpToPx(2);

        // Minimize
        btnMin = new ImageView(context);
        btnMin.setImageResource(R.drawable.btn_min);
        LinearLayout.LayoutParams minLp = new LinearLayout.LayoutParams(ctrlSize, ctrlSize);
        minLp.rightMargin = ctrlMargin;
        btnMin.setLayoutParams(minLp);
        btnContainer.addView(btnMin);

        // Maximize / Restore
        btnMax = new ImageView(context);
        btnMax.setImageResource(R.drawable.btn_max);
        LinearLayout.LayoutParams maxLp = new LinearLayout.LayoutParams(ctrlSize, ctrlSize);
        maxLp.rightMargin = ctrlMargin;
        btnMax.setLayoutParams(maxLp);
        btnContainer.addView(btnMax);

        // Close
        btnClose = new ImageView(context);
        btnClose.setImageResource(R.drawable.btn_close);
        LinearLayout.LayoutParams closeLp = new LinearLayout.LayoutParams(ctrlSize, ctrlSize);
        btnClose.setLayoutParams(closeLp);
        btnContainer.addView(btnClose);

        titleBar.addView(btnContainer);

        // Title text
        tvTitle = new TextView(context);
        RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textParams.addRule(RelativeLayout.RIGHT_OF, 1002);
        textParams.addRule(RelativeLayout.LEFT_OF, 1003);
        textParams.addRule(RelativeLayout.CENTER_VERTICAL);
        textParams.leftMargin = dpToPx(4);
        textParams.rightMargin = dpToPx(4);
        tvTitle.setLayoutParams(textParams);
        tvTitle.setTextColor(Color.WHITE);
        tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        tvTitle.setSingleLine(true);
        tvTitle.setShadowLayer(2, 1, 1, Color.argb(180, 0, 0, 0));
        titleBar.addView(tvTitle);

        addView(titleBar);

        // 2. Content Container
        contentContainer = new FrameLayout(context);
        RelativeLayout.LayoutParams contentParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        contentParams.addRule(RelativeLayout.BELOW, 1001);
        contentParams.leftMargin = dpToPx(3);
        contentParams.rightMargin = dpToPx(3);
        contentParams.bottomMargin = dpToPx(3);
        contentContainer.setLayoutParams(contentParams);
        contentContainer.setBackgroundColor(Color.WHITE);
        addView(contentContainer);

        // 3. Resize Grip (bottom right)
        resizeGrip = new View(context);
        int gripSize = dpToPx(16);
        RelativeLayout.LayoutParams gripParams = new RelativeLayout.LayoutParams(gripSize, gripSize);
        gripParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        gripParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        resizeGrip.setLayoutParams(gripParams);
        resizeGrip.setBackgroundColor(Color.TRANSPARENT);
        addView(resizeGrip);

        setupListeners();
        ThemeManager.getInstance().addListener(this);
        applyThemeStyle();
    }

    private void setupListeners() {
        // Drag to move
        titleBar.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (listener != null) {
                    listener.onWindowFocus(WindowFrameView.this);
                }
                bringToFront();

                if (isMaximized) {
                    return false;
                }

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        initialWindowX = getX();
                        initialWindowY = getY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float dx = event.getRawX() - initialTouchX;
                        float dy = event.getRawY() - initialTouchY;

                        float newX = initialWindowX + dx;
                        float newY = initialWindowY + dy;

                        // Clamping
                        ViewGroup parent = (ViewGroup) getParent();
                        if (parent != null) {
                            int parentW = parent.getWidth();
                            int parentH = parent.getHeight();
                            newX = Math.max(0, Math.min(newX, parentW - getWidth()));
                            newY = Math.max(0, Math.min(newY, parentH - titleBar.getHeight()));
                        }

                        setX(newX);
                        setY(newY);
                        return true;
                }
                return false;
            }
        });

        // Touch anywhere on window brings to front
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    bringToFront();
                    if (listener != null) {
                        listener.onWindowFocus(WindowFrameView.this);
                    }
                }
                return false;
            }
        });

        // Corner resize
        resizeGrip.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isMaximized) return false;

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialResizeTouchX = event.getRawX();
                        initialResizeTouchY = event.getRawY();
                        initialWidth = getWidth();
                        initialHeight = getHeight();
                        bringToFront();
                        if (listener != null) listener.onWindowFocus(WindowFrameView.this);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float dw = event.getRawX() - initialResizeTouchX;
                        float dh = event.getRawY() - initialResizeTouchY;

                        int newW = Math.max(dpToPx(320), (int) (initialWidth + dw));
                        int newH = Math.max(dpToPx(240), (int) (initialHeight + dh));

                        ViewGroup parent = (ViewGroup) getParent();
                        if (parent != null) {
                            newW = Math.min(newW, (int) (parent.getWidth() - getX()));
                            newH = Math.min(newH, (int) (parent.getHeight() - getY()));
                        }

                        ViewGroup.LayoutParams lp = getLayoutParams();
                        lp.width = newW;
                        lp.height = newH;
                        setLayoutParams(lp);
                        return true;
                }
                return false;
            }
        });

        // Button controls
        btnMin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                minimize();
            }
        });

        btnMax.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleMaximize();
            }
        });

        btnClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });
    }

    public void setWindowListener(WindowListener l) {
        this.listener = l;
    }

    public void setTitle(String title) {
        tvTitle.setText(title);
    }

    public String getTitle() {
        return tvTitle != null ? tvTitle.getText().toString() : "";
    }

    private int iconRes = R.drawable.ic_my_computer;

    public void setIcon(int resId) {
        this.iconRes = resId;
        imgIcon.setImageResource(resId);
    }

    public int getIconRes() {
        return iconRes;
    }

    public void setWindowContent(View view) {
        contentContainer.removeAllViews();
        contentContainer.addView(view, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void setActive(boolean active) {
        this.isActive = active;
        applyThemeStyle();
    }

    @Override
    public void onThemeChanged(ThemeManager.Theme newTheme) {
        applyThemeStyle();
    }

    public void applyThemeStyle() {
        ThemeManager tm = ThemeManager.getInstance();
        GradientDrawable gd;
        if (isActive) {
            gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                    new int[]{tm.getTitlebarActiveStart(), tm.getTitlebarActiveEnd()});
        } else {
            gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                    new int[]{tm.getTitlebarInactiveStart(), tm.getTitlebarInactiveEnd()});
        }
        gd.setCornerRadii(new float[]{dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4), 0, 0, 0, 0});
        titleBar.setBackgroundDrawable(gd);
        if (tvTitle != null) {
            tvTitle.setTextColor(tm.getTitlebarTextColor());
        }

        GradientDrawable border = new GradientDrawable();
        border.setStroke(dpToPx(3), tm.getWindowBorderColor());
        border.setColor(Color.TRANSPARENT);
        border.setCornerRadius(dpToPx(4));
        setBackgroundDrawable(border);
    }

    public boolean isActive() {
        return isActive;
    }

    public void minimize() {
        setVisibility(View.GONE);
        setActive(false);
        if (listener != null) {
            listener.onWindowMinimize(this);
        }
    }

    public void restoreFromMinimize() {
        setVisibility(View.VISIBLE);
        bringToFront();
        setActive(true);
        if (listener != null) {
            listener.onWindowFocus(this);
        }
    }

    public void toggleMaximize() {
        ViewGroup parent = (ViewGroup) getParent();
        if (parent == null) return;

        if (!isMaximized) {
            // Store floating bounds
            floatingBounds.set((int) getX(), (int) getY(), (int) getX() + getWidth(), (int) getY() + getHeight());
            setX(0);
            setY(0);

            ViewGroup.LayoutParams lp = getLayoutParams();
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            setLayoutParams(lp);

            btnMax.setImageResource(R.drawable.btn_restore);
            resizeGrip.setVisibility(View.GONE);
            isMaximized = true;
        } else {
            // Restore floating bounds
            setX(floatingBounds.left);
            setY(floatingBounds.top);

            ViewGroup.LayoutParams lp = getLayoutParams();
            lp.width = floatingBounds.width();
            lp.height = floatingBounds.height();
            setLayoutParams(lp);

            btnMax.setImageResource(R.drawable.btn_max);
            resizeGrip.setVisibility(View.VISIBLE);
            isMaximized = false;
        }

        if (listener != null) {
            listener.onWindowMaximize(this, isMaximized);
        }
    }

    public void close() {
        ThemeManager.getInstance().removeListener(this);
        if (listener != null) {
            listener.onWindowClose(this);
        }
        ViewGroup parent = (ViewGroup) getParent();
        if (parent != null) {
            parent.removeView(this);
        }
    }

    public boolean isMaximized() {
        return isMaximized;
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }
}
