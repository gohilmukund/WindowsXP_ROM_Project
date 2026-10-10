package com.windows.xp.launcher.window;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

public class NotepadWindowView extends LinearLayout {

    private EditText etEditor;
    private TextView tvStatus;
    private File currentFile = null;

    public NotepadWindowView(Context context) {
        super(context);
        init(context);
    }

    public NotepadWindowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(final Context context) {
        setOrientation(VERTICAL);
        setBackgroundColor(Color.WHITE);

        // 1. Classic XP Menu Bar
        LinearLayout menuBar = new LinearLayout(context);
        menuBar.setOrientation(HORIZONTAL);
        menuBar.setBackgroundColor(0xFFECE9D8);
        menuBar.setPadding(dpToPx(6), dpToPx(3), dpToPx(6), dpToPx(3));

        TextView btnNew = createMenuItem(context, "New");
        TextView btnOpen = createMenuItem(context, "Open");
        TextView btnSave = createMenuItem(context, "Save");
        TextView btnAbout = createMenuItem(context, "About");

        btnNew.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                etEditor.setText("");
                currentFile = null;
                Toast.makeText(context, "New document created.", Toast.LENGTH_SHORT).show();
            }
        });

        btnOpen.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                File doc = new File("/sdcard/Documents/Welcome to Windows XP.txt");
                if (doc.exists()) {
                    loadFile(doc);
                } else {
                    Toast.makeText(context, "Opening default text document...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFile();
            }
        });

        btnAbout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Notepad - Microsoft Windows XP Luna Edition", Toast.LENGTH_SHORT).show();
            }
        });

        menuBar.addView(btnNew);
        menuBar.addView(btnOpen);
        menuBar.addView(btnSave);
        menuBar.addView(btnAbout);
        addView(menuBar);

        // Menu bar divider line
        View divider = new View(context);
        divider.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
        divider.setBackgroundColor(0xFFD0D0D0);
        addView(divider);

        // 2. Monospace Text Editor
        etEditor = new EditText(context);
        LayoutParams editorParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f);
        etEditor.setLayoutParams(editorParams);
        etEditor.setGravity(Gravity.TOP | Gravity.LEFT);
        etEditor.setTypeface(Typeface.MONOSPACE);
        etEditor.setTextSize(13);
        etEditor.setTextColor(0xFF000000);
        etEditor.setBackgroundColor(Color.WHITE);
        etEditor.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
        addView(etEditor);

        // 3. Status Bar
        LinearLayout statusBar = new LinearLayout(context);
        statusBar.setOrientation(HORIZONTAL);
        statusBar.setBackgroundColor(0xFFECE9D8);
        statusBar.setPadding(dpToPx(8), dpToPx(3), dpToPx(8), dpToPx(3));

        tvStatus = new TextView(context);
        tvStatus.setText("Ln 1, Col 1");
        tvStatus.setTextColor(0xFF333333);
        tvStatus.setTextSize(11);
        statusBar.addView(tvStatus);
        addView(statusBar);

        // Cursor position tracking
        etEditor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateCursorPosition();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Load welcome text if available
        File welcome = new File("/sdcard/Documents/Welcome to Windows XP.txt");
        if (welcome.exists()) {
            loadFile(welcome);
        } else {
            etEditor.setText("Welcome to Windows XP Professional Luna Edition!\n\nThis is Notepad, running inside the new multi-window desktop manager.\nYou can drag this window around, minimize it to the taskbar, or maximize it.\n");
        }
    }

    private TextView createMenuItem(Context context, String title) {
        TextView tv = new TextView(context);
        tv.setText(title);
        tv.setTextColor(0xFF000000);
        tv.setTextSize(12);
        tv.setPadding(dpToPx(8), dpToPx(2), dpToPx(8), dpToPx(2));
        tv.setClickable(true);
        return tv;
    }

    private void updateCursorPosition() {
        int pos = etEditor.getSelectionStart();
        String text = etEditor.getText().toString();
        int line = 1;
        int col = 1;
        for (int i = 0; i < pos && i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                line++;
                col = 1;
            } else {
                col++;
            }
        }
        tvStatus.setText("Ln " + line + ", Col " + col);
    }

    private void loadFile(File f) {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
            StringBuilder sb = new StringBuilder();
            String l;
            while ((l = r.readLine()) != null) {
                sb.append(l).append("\n");
            }
            r.close();
            etEditor.setText(sb.toString());
            currentFile = f;
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error opening file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveFile() {
        try {
            File dir = new File("/sdcard/Documents");
            if (!dir.exists()) dir.mkdirs();

            if (currentFile == null) {
                currentFile = new File(dir, "Document.txt");
            }

            FileOutputStream fos = new FileOutputStream(currentFile);
            fos.write(etEditor.getText().toString().getBytes());
            fos.close();
            Toast.makeText(getContext(), "Saved to " + currentFile.getName(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Save error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }
}
