package com.windows.xp.launcher;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class AltTabDialog extends Dialog {

    private final Context context;
    private final ActivityManager activityManager;
    private final PackageManager packageManager;

    public AltTabDialog(Context context) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        this.context = context;
        this.activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        this.packageManager = context.getPackageManager();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_alt_tab);

        Window window = getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            window.setGravity(Gravity.CENTER);
        }

        populateRunningTasks();
    }

    private void populateRunningTasks() {
        LinearLayout container = (LinearLayout) findViewById(R.id.alt_tab_items_container);
        if (container == null) return;
        container.removeAllViews();

        try {
            List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(10);
            if (tasks == null || tasks.isEmpty()) return;

            for (final ActivityManager.RunningTaskInfo task : tasks) {
                if (task.baseActivity == null) continue;
                String pkgName = task.baseActivity.getPackageName();

                Drawable icon;
                CharSequence label;
                try {
                    icon = packageManager.getApplicationIcon(pkgName);
                    label = packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkgName, 0));
                } catch (Exception e) {
                    continue;
                }

                LinearLayout itemLayout = new LinearLayout(context);
                itemLayout.setOrientation(LinearLayout.VERTICAL);
                itemLayout.setGravity(Gravity.CENTER);
                itemLayout.setPadding(16, 12, 16, 12);
                itemLayout.setBackgroundResource(R.drawable.desktop_icon_selector);
                itemLayout.setClickable(true);

                ImageView img = new ImageView(context);
                img.setImageDrawable(icon);
                LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(54, 54);
                itemLayout.addView(img, imgParams);

                TextView tv = new TextView(context);
                tv.setText(label);
                tv.setTextColor(0xFFFFFFFF);
                tv.setTextSize(11);
                tv.setGravity(Gravity.CENTER);
                tv.setMaxLines(1);
                LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                textParams.topMargin = 4;
                itemLayout.addView(tv, textParams);

                itemLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            activityManager.moveTaskToFront(task.id, ActivityManager.MOVE_TASK_WITH_HOME);
                        } catch (Exception ignored) {}
                        dismiss();
                    }
                });

                container.addView(itemLayout);
            }
        } catch (Exception ignored) {}
    }
}
