package com.windows.xp.launcher.window;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.windows.xp.launcher.R;

import java.util.ArrayList;
import java.util.List;

public class ControlPanelWindowView extends LinearLayout {

    public interface ControlPanelListener {
        void onOpenDisplayProperties();
        void onOpenSystemProperties();
        void onOpenNetworkConnections();
        void onOpenDateTime();
        void onOpenAppsManager();
    }

    private boolean isClassicView = false;
    private LinearLayout taskPane;
    private TextView tvSwitchView;
    private ScrollView scrollCategoryView;
    private GridView gridClassicView;
    private ControlPanelListener listener;

    public ControlPanelWindowView(Context context) {
        super(context);
        init(context);
    }

    public ControlPanelWindowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void setControlPanelListener(ControlPanelListener l) {
        this.listener = l;
    }

    private void init(final Context context) {
        setOrientation(HORIZONTAL);
        setBackgroundColor(Color.WHITE);

        int pad = dpToPx(8);

        // 1. Left Task Pane (Blue XP Panel)
        taskPane = new LinearLayout(context);
        taskPane.setOrientation(VERTICAL);
        taskPane.setLayoutParams(new LayoutParams(dpToPx(180), ViewGroup.LayoutParams.MATCH_PARENT));
        taskPane.setBackgroundColor(0xFF6B8EC6);
        taskPane.setPadding(pad, pad, pad, pad);

        // Task Card: Control Panel Tasks
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(VERTICAL);
        card.setBackgroundColor(0xFFD6DFF7);
        card.setPadding(dpToPx(6), dpToPx(6), dpToPx(6), dpToPx(6));

        TextView tvCardHeader = new TextView(context);
        tvCardHeader.setText("Control Panel");
        tvCardHeader.setTextColor(0xFF0C327D);
        tvCardHeader.setTypeface(null, Typeface.BOLD);
        tvCardHeader.setTextSize(11);
        card.addView(tvCardHeader);

        tvSwitchView = new TextView(context);
        tvSwitchView.setText("• Switch to Classic View");
        tvSwitchView.setTextColor(0xFF00138C);
        tvSwitchView.setTextSize(11);
        tvSwitchView.setPadding(0, dpToPx(6), 0, dpToPx(6));
        tvSwitchView.setClickable(true);
        tvSwitchView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleViewMode();
            }
        });
        card.addView(tvSwitchView);

        TextView tvTrouble = new TextView(context);
        tvTrouble.setText("• Troubleshooters\n• Windows Update");
        tvTrouble.setTextColor(0xFF555555);
        tvTrouble.setTextSize(10);
        card.addView(tvTrouble);

        taskPane.addView(card);
        addView(taskPane);

        // 2. Right Pane: Category View (Default)
        scrollCategoryView = new ScrollView(context);
        scrollCategoryView.setLayoutParams(new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f));
        scrollCategoryView.setBackgroundColor(Color.WHITE);

        LinearLayout catContainer = new LinearLayout(context);
        catContainer.setOrientation(VERTICAL);
        catContainer.setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12));

        TextView tvPick = new TextView(context);
        tvPick.setText("Pick a category");
        tvPick.setTextColor(0xFF00138C);
        tvPick.setTypeface(null, Typeface.BOLD);
        tvPick.setTextSize(16);
        tvPick.setPadding(0, 0, 0, dpToPx(12));
        catContainer.addView(tvPick);

        // Category 1: Appearance and Themes
        catContainer.addView(createCategoryItem(context, R.drawable.ic_paint,
                "Appearance and Themes",
                "Change the computer's theme or desktop background, or choose a screen saver.",
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) listener.onOpenDisplayProperties();
                    }
                }));

        // Category 2: Network and Internet Connections
        catContainer.addView(createCategoryItem(context, R.drawable.ic_ie,
                "Network and Internet Connections",
                "Connect to Wi-Fi networks, view connection status, and configure IP addresses.",
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) listener.onOpenNetworkConnections();
                    }
                }));

        // Category 3: Performance and Maintenance
        catContainer.addView(createCategoryItem(context, R.drawable.ic_my_computer,
                "Performance and Maintenance",
                "View system hardware, RAM, CPU information, and free up disk space.",
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) listener.onOpenSystemProperties();
                    }
                }));

        // Category 4: Date, Time, Language, and Regional Options
        catContainer.addView(createCategoryItem(context, R.drawable.ic_control_panel,
                "Date, Time, Language, and Regional Options",
                "Change the date, time, and time zone for your computer.",
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) listener.onOpenDateTime();
                    }
                }));

        // Category 5: Add or Remove Programs
        catContainer.addView(createCategoryItem(context, R.drawable.ic_folder,
                "Add or Remove Programs",
                "Install or remove programs and Android packages from your system.",
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) listener.onOpenAppsManager();
                    }
                }));

        scrollCategoryView.addView(catContainer);
        addView(scrollCategoryView);

        // 3. Right Pane: Classic View (Grid of 32-bit Applet Icons)
        gridClassicView = new GridView(context);
        gridClassicView.setLayoutParams(new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f));
        gridClassicView.setNumColumns(3);
        gridClassicView.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));
        gridClassicView.setVerticalSpacing(dpToPx(12));
        gridClassicView.setHorizontalSpacing(dpToPx(12));
        gridClassicView.setBackgroundColor(Color.WHITE);
        gridClassicView.setVisibility(GONE);

        final List<ClassicItem> classicItems = new ArrayList<ClassicItem>();
        classicItems.add(new ClassicItem("Display", R.drawable.ic_paint, new Runnable() {
            @Override public void run() { if (listener != null) listener.onOpenDisplayProperties(); }
        }));
        classicItems.add(new ClassicItem("Network Connections", R.drawable.ic_ie, new Runnable() {
            @Override public void run() { if (listener != null) listener.onOpenNetworkConnections(); }
        }));
        classicItems.add(new ClassicItem("System", R.drawable.ic_my_computer, new Runnable() {
            @Override public void run() { if (listener != null) listener.onOpenSystemProperties(); }
        }));
        classicItems.add(new ClassicItem("Date and Time", R.drawable.ic_control_panel, new Runnable() {
            @Override public void run() { if (listener != null) listener.onOpenDateTime(); }
        }));
        classicItems.add(new ClassicItem("Add/Remove Programs", R.drawable.ic_folder, new Runnable() {
            @Override public void run() { if (listener != null) listener.onOpenAppsManager(); }
        }));
        classicItems.add(new ClassicItem("Sounds and Audio", R.drawable.ic_wmp, new Runnable() {
            @Override public void run() { Toast.makeText(context, "Audio Volume: 100%", Toast.LENGTH_SHORT).show(); }
        }));

        gridClassicView.setAdapter(new BaseAdapter() {
            @Override public int getCount() { return classicItems.size(); }
            @Override public Object getItem(int position) { return classicItems.get(position); }
            @Override public long getItemId(int position) { return position; }
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final ClassicItem item = classicItems.get(position);
                LinearLayout itemLayout = new LinearLayout(context);
                itemLayout.setOrientation(VERTICAL);
                itemLayout.setGravity(Gravity.CENTER);
                itemLayout.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
                itemLayout.setBackgroundResource(R.drawable.menu_item_selector);
                itemLayout.setClickable(true);
                itemLayout.setOnClickListener(new OnClickListener() {
                    @Override public void onClick(View v) { item.action.run(); }
                });

                ImageView iv = new ImageView(context);
                iv.setLayoutParams(new LayoutParams(dpToPx(36), dpToPx(36)));
                iv.setImageResource(item.iconRes);
                itemLayout.addView(iv);

                TextView tv = new TextView(context);
                tv.setText(item.title);
                tv.setTextColor(Color.BLACK);
                tv.setTextSize(11);
                tv.setGravity(Gravity.CENTER);
                tv.setPadding(0, dpToPx(4), 0, 0);
                itemLayout.addView(tv);

                return itemLayout;
            }
        });

        addView(gridClassicView);
    }

    private View createCategoryItem(Context context, int iconRes, String title, String subtitle, OnClickListener onClick) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dpToPx(6), 0, dpToPx(6));
        row.setBackgroundResource(R.drawable.menu_item_selector);
        row.setClickable(true);
        row.setOnClickListener(onClick);

        ImageView iv = new ImageView(context);
        iv.setLayoutParams(new LayoutParams(dpToPx(36), dpToPx(36)));
        iv.setImageResource(iconRes);
        row.addView(iv);

        LinearLayout textLayout = new LinearLayout(context);
        textLayout.setOrientation(VERTICAL);
        textLayout.setPadding(dpToPx(10), 0, 0, 0);

        TextView tvTitle = new TextView(context);
        tvTitle.setText(title);
        tvTitle.setTextColor(0xFF00138C);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setTextSize(13);
        textLayout.addView(tvTitle);

        TextView tvSub = new TextView(context);
        tvSub.setText(subtitle);
        tvSub.setTextColor(0xFF555555);
        tvSub.setTextSize(11);
        textLayout.addView(tvSub);

        row.addView(textLayout);
        return row;
    }

    private void toggleViewMode() {
        isClassicView = !isClassicView;
        if (isClassicView) {
            scrollCategoryView.setVisibility(GONE);
            gridClassicView.setVisibility(VISIBLE);
            tvSwitchView.setText("• Switch to Category View");
        } else {
            gridClassicView.setVisibility(GONE);
            scrollCategoryView.setVisibility(VISIBLE);
            tvSwitchView.setText("• Switch to Classic View");
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    private static class ClassicItem {
        String title;
        int iconRes;
        Runnable action;

        ClassicItem(String title, int iconRes, Runnable action) {
            this.title = title;
            this.iconRes = iconRes;
            this.action = action;
        }
    }
}
