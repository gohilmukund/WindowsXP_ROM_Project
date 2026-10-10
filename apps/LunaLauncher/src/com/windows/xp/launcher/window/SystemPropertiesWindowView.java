package com.windows.xp.launcher.window;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.windows.xp.launcher.R;

import java.io.File;

public class SystemPropertiesWindowView extends LinearLayout {

    public interface OnDialogActionListener {
        void onClose();
    }

    private OnDialogActionListener actionListener;
    private FrameLayout tabContentContainer;
    private View tabGeneral;
    private View tabHardware;
    private View tabAdvanced;

    private TextView btnTabGeneral;
    private TextView btnTabHardware;
    private TextView btnTabAdvanced;

    public SystemPropertiesWindowView(Context context) {
        super(context);
        init(context);
    }

    public SystemPropertiesWindowView(Context context, AttributeSet attrs) {
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

        // 1. Tab Bar
        LinearLayout tabBar = new LinearLayout(context);
        tabBar.setOrientation(HORIZONTAL);
        tabBar.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(26)));

        btnTabGeneral = createTabButton(context, "General", true);
        btnTabHardware = createTabButton(context, "Hardware", false);
        btnTabAdvanced = createTabButton(context, "Advanced", false);

        btnTabGeneral.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) { selectTab(0); }
        });
        btnTabHardware.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) { selectTab(1); }
        });
        btnTabAdvanced.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) { selectTab(2); }
        });

        tabBar.addView(btnTabGeneral);
        tabBar.addView(btnTabHardware);
        tabBar.addView(btnTabAdvanced);
        addView(tabBar);

        // 2. Tab Content Container
        tabContentContainer = new FrameLayout(context);
        LayoutParams contentParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f);
        tabContentContainer.setLayoutParams(contentParams);
        tabContentContainer.setBackgroundColor(0xFFECE9D8);
        tabContentContainer.setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10));

        buildGeneralTab(context);
        buildHardwareTab(context);
        buildAdvancedTab(context);

        tabContentContainer.addView(tabGeneral);
        tabContentContainer.addView(tabHardware);
        tabContentContainer.addView(tabAdvanced);

        addView(tabContentContainer);

        // 3. Bottom Button Row (OK, Cancel, Apply)
        LinearLayout btnRow = new LinearLayout(context);
        btnRow.setOrientation(HORIZONTAL);
        btnRow.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        btnRow.setPadding(0, dpToPx(8), 0, 0);

        Button btnOk = createDialogButton(context, "OK");
        Button btnCancel = createDialogButton(context, "Cancel");
        Button btnApply = createDialogButton(context, "Apply");

        btnOk.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) {
                if (actionListener != null) actionListener.onClose();
            }
        });
        btnCancel.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) {
                if (actionListener != null) actionListener.onClose();
            }
        });
        btnApply.setEnabled(false);

        btnRow.addView(btnOk);
        btnRow.addView(btnCancel);
        btnRow.addView(btnApply);
        addView(btnRow);

        selectTab(0);
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
        updateTabButtonStyle(btnTabGeneral, index == 0);
        updateTabButtonStyle(btnTabHardware, index == 1);
        updateTabButtonStyle(btnTabAdvanced, index == 2);

        tabGeneral.setVisibility(index == 0 ? VISIBLE : GONE);
        tabHardware.setVisibility(index == 1 ? VISIBLE : GONE);
        tabAdvanced.setVisibility(index == 2 ? VISIBLE : GONE);
    }

    private void buildGeneralTab(Context context) {
        ScrollView scroll = new ScrollView(context);
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(VERTICAL);

        // Top Row: Logo + System Specs
        LinearLayout rowTop = new LinearLayout(context);
        rowTop.setOrientation(HORIZONTAL);
        rowTop.setGravity(Gravity.CENTER_VERTICAL);

        ImageView imgLogo = new ImageView(context);
        imgLogo.setLayoutParams(new LayoutParams(dpToPx(80), dpToPx(80)));
        imgLogo.setImageResource(R.drawable.ic_my_computer);
        rowTop.addView(imgLogo);

        LinearLayout sysText = new LinearLayout(context);
        sysText.setOrientation(VERTICAL);
        sysText.setPadding(dpToPx(16), 0, 0, 0);

        TextView tvSysHeader = new TextView(context);
        tvSysHeader.setText("System:");
        tvSysHeader.setTypeface(null, Typeface.BOLD);
        tvSysHeader.setTextSize(12);
        tvSysHeader.setTextColor(Color.BLACK);
        sysText.addView(tvSysHeader);

        TextView tvSysBody = new TextView(context);
        tvSysBody.setText("Microsoft Windows XP\nProfessional\nVersion 2002\nService Pack 3");
        tvSysBody.setTextSize(11);
        tvSysBody.setTextColor(Color.BLACK);
        sysText.addView(tvSysBody);

        rowTop.addView(sysText);
        layout.addView(rowTop);

        // Registered to
        TextView tvRegHeader = new TextView(context);
        tvRegHeader.setText("\nRegistered to:");
        tvRegHeader.setTypeface(null, Typeface.BOLD);
        tvRegHeader.setTextSize(12);
        tvRegHeader.setTextColor(Color.BLACK);
        layout.addView(tvRegHeader);

        TextView tvRegBody = new TextView(context);
        tvRegBody.setText("Administrator\nSamsung Tab 3 (SM-T210)\n76487-OEM-0119851-00102");
        tvRegBody.setTextSize(11);
        tvRegBody.setTextColor(Color.BLACK);
        layout.addView(tvRegBody);

        // Computer hardware info
        TextView tvCompHeader = new TextView(context);
        tvCompHeader.setText("\nComputer:");
        tvCompHeader.setTypeface(null, Typeface.BOLD);
        tvCompHeader.setTextSize(12);
        tvCompHeader.setTextColor(Color.BLACK);
        layout.addView(tvCompHeader);

        String ramStr = getLiveRamString(context);
        TextView tvCompBody = new TextView(context);
        tvCompBody.setText("Samsung Galaxy Tab 3 7.0 Wi-Fi\nMarvell PXA986 Dual-Core\n1.20 GHz, " + ramStr + "\nAndroid 4.4.2 KitKat (Linux 3.4.5)");
        tvCompBody.setTextSize(11);
        tvCompBody.setTextColor(Color.BLACK);
        layout.addView(tvCompBody);

        scroll.addView(layout);
        tabGeneral = scroll;
    }

    private void buildHardwareTab(Context context) {
        ScrollView scroll = new ScrollView(context);
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(VERTICAL);

        TextView tvTitle = new TextView(context);
        tvTitle.setText("Device Manager");
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setTextSize(13);
        tvTitle.setTextColor(0xFF00138C);
        layout.addView(tvTitle);

        TextView tvDesc = new TextView(context);
        tvDesc.setText("The Device Manager lists all the hardware devices installed on this computer.\n");
        tvDesc.setTextSize(11);
        tvDesc.setTextColor(Color.BLACK);
        layout.addView(tvDesc);

        addHardwareEntry(layout, "Display", "7.0\" TFT WSVGA (1024x600 @ 60Hz, 160 DPI)");
        addHardwareEntry(layout, "Processors", "Dual-Core ARM Cortex-A9 @ 1.20 GHz");
        addHardwareEntry(layout, "Display Adapters", "Vivante GC1000 OpenGL ES 2.0 GPU");
        addHardwareEntry(layout, "Network Adapters", "Broadcom BCM4334 802.11a/b/g/n Wi-Fi + Bluetooth 4.0");
        addHardwareEntry(layout, "Storage Controllers", "Samsung 8GB eMMC Flash Memory Controller");
        addHardwareEntry(layout, "Batteries", "Samsung 4,000 mAh Lithium-Ion Internal Battery");
        addHardwareEntry(layout, "Audio Devices", "Wolfson WM1811 High Definition Audio Codec");

        scroll.addView(layout);
        tabHardware = scroll;
    }

    private void addHardwareEntry(LinearLayout parent, String category, String spec) {
        TextView tvCat = new TextView(parent.getContext());
        tvCat.setText("▶ " + category);
        tvCat.setTypeface(null, Typeface.BOLD);
        tvCat.setTextSize(11);
        tvCat.setTextColor(0xFF00138C);
        parent.addView(tvCat);

        TextView tvSpec = new TextView(parent.getContext());
        tvSpec.setText("    " + spec);
        tvSpec.setTextSize(11);
        tvSpec.setTextColor(0xFF333333);
        tvSpec.setPadding(0, 0, 0, dpToPx(6));
        parent.addView(tvSpec);
    }

    private void buildAdvancedTab(Context context) {
        ScrollView scroll = new ScrollView(context);
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(VERTICAL);

        TextView tvTitle = new TextView(context);
        tvTitle.setText("Performance & Storage");
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setTextSize(13);
        tvTitle.setTextColor(0xFF00138C);
        layout.addView(tvTitle);

        TextView tvDesc = new TextView(context);
        tvDesc.setText("Visual effects, processor scheduling, memory usage, and storage volumes.\n");
        tvDesc.setTextSize(11);
        tvDesc.setTextColor(Color.BLACK);
        layout.addView(tvDesc);

        // Partition Stats
        addStorageRow(layout, "Local Disk (C: - SD Card)", "/sdcard");
        addStorageRow(layout, "System Partition (S:)", "/system");
        addStorageRow(layout, "Data Storage (/data)", "/data");

        scroll.addView(layout);
        tabAdvanced = scroll;
    }

    private void addStorageRow(LinearLayout parent, String name, String path) {
        TextView tvName = new TextView(parent.getContext());
        tvName.setText("• " + name + ":");
        tvName.setTypeface(null, Typeface.BOLD);
        tvName.setTextSize(11);
        tvName.setTextColor(Color.BLACK);
        parent.addView(tvName);

        String stats = getStorageStats(path);
        TextView tvStats = new TextView(parent.getContext());
        tvStats.setText("    " + stats);
        tvStats.setTextSize(11);
        tvStats.setTextColor(0xFF444444);
        tvStats.setPadding(0, 0, 0, dpToPx(6));
        parent.addView(tvStats);
    }

    private String getStorageStats(String path) {
        try {
            File f = new File(path);
            if (!f.exists()) return "Not Mounted";
            StatFs stat = new StatFs(path);
            long blockSize = stat.getBlockSize();
            long total = stat.getBlockCount() * blockSize;
            long free = stat.getAvailableBlocks() * blockSize;
            return "Free: " + formatSize(free) + "  |  Total: " + formatSize(total);
        } catch (Exception e) {
            return "Unavailable";
        }
    }

    private String getLiveRamString(Context context) {
        try {
            ActivityManager actManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
            actManager.getMemoryInfo(memInfo);
            return formatSize(memInfo.totalMem) + " of RAM (" + formatSize(memInfo.availMem) + " free)";
        } catch (Exception e) {
            return "1.00 GB of RAM";
        }
    }

    private String formatSize(long bytes) {
        if (bytes <= 0) return "0 MB";
        double mb = bytes / (1024.0 * 1024.0);
        if (mb >= 1024) {
            return String.format("%.2f GB", mb / 1024.0);
        }
        return String.format("%.1f MB", mb);
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
}
