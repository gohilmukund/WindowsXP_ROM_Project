package com.windows.xp.launcher.window;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.windows.xp.launcher.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

public class ExplorerWindowView extends LinearLayout {

    public interface FileActionListener {
        void onOpenTextFile(File file);
    }

    private TextView tvAddress;
    private GridView gridFiles;
    private FileAdapter adapter;

    private String currentPath = "computer://";
    private final Stack<String> backStack = new Stack<>();
    private FileActionListener fileActionListener;

    public ExplorerWindowView(Context context) {
        super(context);
        init(context);
    }

    public ExplorerWindowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void setFileActionListener(FileActionListener l) {
        this.fileActionListener = l;
    }

    private void init(final Context context) {
        setOrientation(VERTICAL);
        setBackgroundColor(Color.WHITE);

        // 1. Navigation Toolbar (Back, Up, Address Bar)
        LinearLayout toolbar = new LinearLayout(context);
        toolbar.setOrientation(HORIZONTAL);
        toolbar.setBackgroundColor(0xFFECE9D8);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        toolbar.setPadding(dpToPx(6), dpToPx(3), dpToPx(6), dpToPx(3));

        TextView btnBack = createNavButton(context, "◀ Back");
        TextView btnUp = createNavButton(context, "▲ Up");

        btnBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!backStack.isEmpty()) {
                    navigateTo(backStack.pop(), false);
                }
            }
        });

        btnUp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateUp();
            }
        });

        toolbar.addView(btnBack);
        toolbar.addView(btnUp);

        // Address Bar
        TextView lblAddress = new TextView(context);
        lblAddress.setText("Address: ");
        lblAddress.setTextColor(0xFF555555);
        lblAddress.setTextSize(11);
        lblAddress.setPadding(dpToPx(6), 0, dpToPx(2), 0);
        toolbar.addView(lblAddress);

        tvAddress = new TextView(context);
        LayoutParams addrParams = new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        tvAddress.setLayoutParams(addrParams);
        tvAddress.setBackgroundColor(Color.WHITE);
        tvAddress.setTextColor(Color.BLACK);
        tvAddress.setTextSize(11);
        tvAddress.setPadding(dpToPx(6), dpToPx(2), dpToPx(6), dpToPx(2));
        tvAddress.setSingleLine(true);
        toolbar.addView(tvAddress);

        addView(toolbar);

        // Divider
        View div = new View(context);
        div.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
        div.setBackgroundColor(0xFFD0D0D0);
        addView(div);

        // 2. Main Two-Pane Body
        LinearLayout body = new LinearLayout(context);
        body.setOrientation(HORIZONTAL);
        LayoutParams bodyParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f);
        body.setLayoutParams(bodyParams);

        // Left Pane: Other Places Links
        LinearLayout leftPane = new LinearLayout(context);
        leftPane.setOrientation(VERTICAL);
        leftPane.setLayoutParams(new LayoutParams(dpToPx(140), ViewGroup.LayoutParams.MATCH_PARENT));
        leftPane.setBackgroundColor(0xFF7A96DF);
        leftPane.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));

        TextView tvHeader = new TextView(context);
        tvHeader.setText("Other Places");
        tvHeader.setTextColor(Color.WHITE);
        tvHeader.setTextSize(11);
        tvHeader.setTypeface(null, android.graphics.Typeface.BOLD);
        leftPane.addView(tvHeader);

        leftPane.addView(createPlaceLink(context, "My Computer", "computer://"));
        leftPane.addView(createPlaceLink(context, "Local Disk (C:)", "/sdcard"));
        leftPane.addView(createPlaceLink(context, "My Documents", "/sdcard/Documents"));
        leftPane.addView(createPlaceLink(context, "My Pictures", "/sdcard/Pictures"));

        body.addView(leftPane);

        // Right Pane: Grid of Files & Folders
        gridFiles = new GridView(context);
        LayoutParams gridParams = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
        gridFiles.setLayoutParams(gridParams);
        gridFiles.setNumColumns(3);
        gridFiles.setVerticalSpacing(dpToPx(8));
        gridFiles.setHorizontalSpacing(dpToPx(8));
        gridFiles.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
        gridFiles.setBackgroundColor(Color.WHITE);

        adapter = new FileAdapter(context);
        gridFiles.setAdapter(adapter);

        body.addView(gridFiles);
        addView(body);

        navigateTo("computer://", false);
    }

    private TextView createNavButton(Context context, String label) {
        TextView tv = new TextView(context);
        tv.setText(label);
        tv.setTextColor(Color.BLACK);
        tv.setTextSize(11);
        tv.setBackgroundColor(0xFFE0DFD5);
        tv.setPadding(dpToPx(6), dpToPx(2), dpToPx(6), dpToPx(2));
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.rightMargin = dpToPx(4);
        tv.setLayoutParams(lp);
        tv.setClickable(true);
        return tv;
    }

    private TextView createPlaceLink(final Context context, final String label, final String path) {
        TextView tv = new TextView(context);
        tv.setText("• " + label);
        tv.setTextColor(0xFF00138C);
        tv.setTextSize(11);
        tv.setPadding(0, dpToPx(4), 0, dpToPx(4));
        tv.setClickable(true);
        tv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateTo(path, true);
            }
        });
        return tv;
    }

    public void navigateTo(String path, boolean pushBack) {
        if (pushBack && currentPath != null && !currentPath.equals(path)) {
            backStack.push(currentPath);
        }
        this.currentPath = path;

        if (path.equals("computer://")) {
            tvAddress.setText("My Computer");
            adapter.loadComputerDrives();
        } else {
            String display = path.replace("/sdcard", "C:").replace('/', '\\');
            tvAddress.setText(display);
            adapter.loadDirectory(new File(path));
        }
    }

    private void navigateUp() {
        if (currentPath.equals("computer://")) {
            return;
        }
        File f = new File(currentPath);
        File parent = f.getParentFile();
        if (parent == null || currentPath.equals("/sdcard") || currentPath.equals("/storage/emulated/0")) {
            navigateTo("computer://", true);
        } else {
            navigateTo(parent.getAbsolutePath(), true);
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    // Inner Item Model
    private static class DisplayItem {
        String name;
        String path;
        int iconRes;
        boolean isDir;
        boolean isDrive;

        DisplayItem(String name, String path, int iconRes, boolean isDir, boolean isDrive) {
            this.name = name;
            this.path = path;
            this.iconRes = iconRes;
            this.isDir = isDir;
            this.isDrive = isDrive;
        }
    }

    // Inner Grid Adapter
    private class FileAdapter extends BaseAdapter {
        private final Context ctx;
        private final List<DisplayItem> items = new ArrayList<>();

        FileAdapter(Context ctx) {
            this.ctx = ctx;
        }

        void loadComputerDrives() {
            items.clear();
            items.add(new DisplayItem("Local Disk (C:)", "/sdcard", R.drawable.ic_drive_c, true, true));
            items.add(new DisplayItem("Removable Disk (D:)", "/storage/extSdCard", R.drawable.ic_drive_d, true, true));
            items.add(new DisplayItem("System (S:)", "/system", R.drawable.ic_drive_system, true, true));
            notifyDataSetChanged();
        }

        void loadDirectory(File dir) {
            items.clear();
            if (dir.exists() && dir.isDirectory()) {
                File[] list = dir.listFiles();
                if (list != null) {
                    Arrays.sort(list, new Comparator<File>() {
                        @Override
                        public int compare(File a, File b) {
                            if (a.isDirectory() && !b.isDirectory()) return -1;
                            if (!a.isDirectory() && b.isDirectory()) return 1;
                            return a.getName().compareToIgnoreCase(b.getName());
                        }
                    });

                    for (File f : list) {
                        int icon = R.drawable.ic_folder;
                        if (!f.isDirectory()) {
                            String n = f.getName().toLowerCase();
                            if (n.endsWith(".txt") || n.endsWith(".log")) icon = R.drawable.ic_notepad;
                            else if (n.endsWith(".png") || n.endsWith(".jpg")) icon = R.drawable.ic_paint;
                            else if (n.endsWith(".mp3") || n.endsWith(".ogg")) icon = R.drawable.ic_wmp;
                            else if (n.endsWith(".apk")) icon = R.drawable.ic_cmd;
                            else icon = R.drawable.ic_folder;
                        }
                        items.add(new DisplayItem(f.getName(), f.getAbsolutePath(), icon, f.isDirectory(), false));
                    }
                }
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount() { return items.size(); }
        @Override
        public Object getItem(int pos) { return items.get(pos); }
        @Override
        public long getItemId(int pos) { return pos; }

        @Override
        public View getView(int pos, View convertView, ViewGroup parent) {
            LinearLayout v;
            if (convertView == null) {
                v = new LinearLayout(ctx);
                v.setOrientation(VERTICAL);
                v.setGravity(Gravity.CENTER);
                v.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));

                ImageView img = new ImageView(ctx);
                img.setId(2001);
                LayoutParams ip = new LayoutParams(dpToPx(40), dpToPx(40));
                img.setLayoutParams(ip);
                v.addView(img);

                TextView tv = new TextView(ctx);
                tv.setId(2002);
                tv.setTextColor(Color.BLACK);
                tv.setTextSize(11);
                tv.setGravity(Gravity.CENTER);
                tv.setSingleLine(true);
                tv.setEllipsize(android.text.TextUtils.TruncateAt.END);
                LayoutParams tp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                tp.topMargin = dpToPx(2);
                tv.setLayoutParams(tp);
                v.addView(tv);
            } else {
                v = (LinearLayout) convertView;
            }

            final DisplayItem item = items.get(pos);
            ImageView img = (ImageView) v.findViewById(2001);
            TextView tv = (TextView) v.findViewById(2002);

            img.setImageResource(item.iconRes);
            tv.setText(item.name);

            v.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (item.isDir || item.isDrive) {
                        navigateTo(item.path, true);
                    } else {
                        // Open file
                        if (item.name.toLowerCase().endsWith(".txt") && fileActionListener != null) {
                            fileActionListener.onOpenTextFile(new File(item.path));
                        } else {
                            Toast.makeText(ctx, "Opening " + item.name, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

            return v;
        }
    }
}
