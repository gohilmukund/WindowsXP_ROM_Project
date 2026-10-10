package com.windows.xp.notepad;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotepadActivity extends Activity {

    private EditText editor;
    private TextView titleText;
    private TextView statusPos;
    private File currentFile = null;
    private boolean isModified = false;
    private boolean wordWrapEnabled = true;
    private String undoBuffer = "";

    private static final String DOCUMENTS_DIR = "/sdcard/Documents";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notepad);

        editor = (EditText) findViewById(R.id.notepad_editor);
        titleText = (TextView) findViewById(R.id.notepad_title);
        statusPos = (TextView) findViewById(R.id.notepad_status_pos);

        setupWindowControls();
        setupMenus();
        setupEditorListeners();

        ensureDocumentsDirectory();
    }

    private void setupWindowControls() {
        findViewById(R.id.notepad_btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.notepad_btn_minimize).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                minimizeWindow();
            }
        });

        findViewById(R.id.notepad_btn_maximize).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(NotepadActivity.this, "Window maximized", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupEditorListeners() {
        editor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (undoBuffer.isEmpty() || Math.abs(undoBuffer.length() - s.length()) > 10) {
                    undoBuffer = s.toString();
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isModified = true;
                updateCursorPosition();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        editor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCursorPosition();
            }
        });
    }

    private void updateCursorPosition() {
        int sel = editor.getSelectionStart();
        Layout layout = editor.getLayout();
        if (layout != null && sel >= 0 && sel <= editor.getText().length()) {
            int line = layout.getLineForOffset(sel);
            int col = sel - layout.getLineStart(line);
            statusPos.setText("Ln " + (line + 1) + ", Col " + (col + 1));
        } else {
            statusPos.setText("Ln 1, Col 1");
        }
    }

    private void setupMenus() {
        findViewById(R.id.notepad_menu_file).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileMenu();
            }
        });

        findViewById(R.id.notepad_menu_edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditMenu();
            }
        });

        findViewById(R.id.notepad_menu_format).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFormatMenu();
            }
        });

        findViewById(R.id.notepad_menu_help).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHelpDialog();
            }
        });
    }

    private void showFileMenu() {
        String[] items = new String[]{"New", "Open...", "Save", "Save As...", "Exit"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("File");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        actionNew();
                        break;
                    case 1:
                        actionOpen();
                        break;
                    case 2:
                        actionSave();
                        break;
                    case 3:
                        actionSaveAs();
                        break;
                    case 4:
                        finish();
                        break;
                }
            }
        });
        builder.show();
    }

    private void showEditMenu() {
        String[] items = new String[]{"Undo", "Cut", "Copy", "Paste", "Select All", "Time/Date (F5)"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                int start = editor.getSelectionStart();
                int end = editor.getSelectionEnd();
                if (start > end) {
                    int tmp = start;
                    start = end;
                    end = tmp;
                }

                switch (which) {
                    case 0: // Undo
                        if (!undoBuffer.isEmpty()) {
                            String current = editor.getText().toString();
                            editor.setText(undoBuffer);
                            undoBuffer = current;
                            Toast.makeText(NotepadActivity.this, "Undo applied", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 1: // Cut
                        if (start != end && cm != null) {
                            String selected = editor.getText().subSequence(start, end).toString();
                            cm.setPrimaryClip(ClipData.newPlainText("text", selected));
                            editor.getText().delete(start, end);
                        }
                        break;
                    case 2: // Copy
                        if (start != end && cm != null) {
                            String selected = editor.getText().subSequence(start, end).toString();
                            cm.setPrimaryClip(ClipData.newPlainText("text", selected));
                            Toast.makeText(NotepadActivity.this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 3: // Paste
                        if (cm != null && cm.hasPrimaryClip() && cm.getPrimaryClip().getItemCount() > 0) {
                            CharSequence text = cm.getPrimaryClip().getItemAt(0).getText();
                            if (text != null) {
                                if (start != end) {
                                    editor.getText().replace(start, end, text);
                                } else {
                                    editor.getText().insert(start, text);
                                }
                            }
                        }
                        break;
                    case 4: // Select All
                        editor.selectAll();
                        break;
                    case 5: // Time/Date
                        String dateStr = new SimpleDateFormat("hh:mm a MM/dd/yyyy", Locale.US).format(new Date());
                        if (start != end) {
                            editor.getText().replace(start, end, dateStr);
                        } else {
                            editor.getText().insert(start, dateStr);
                        }
                        break;
                }
            }
        });
        builder.show();
    }

    private void showFormatMenu() {
        String wrapStatus = wordWrapEnabled ? "Word Wrap [Enabled]" : "Word Wrap [Disabled]";
        String[] items = new String[]{wrapStatus, "Font Size..."};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Format");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    wordWrapEnabled = !wordWrapEnabled;
                    editor.setHorizontallyScrolling(!wordWrapEnabled);
                    Toast.makeText(NotepadActivity.this, wordWrapEnabled ? "Word wrap turned on" : "Word wrap turned off", Toast.LENGTH_SHORT).show();
                } else if (which == 1) {
                    showFontSizeDialog();
                }
            }
        });
        builder.show();
    }

    private void showFontSizeDialog() {
        final String[] sizes = new String[]{"Small (11sp)", "Regular (13sp)", "Medium (16sp)", "Large (20sp)"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Font Size");
        builder.setItems(sizes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                float sp = 13f;
                if (which == 0) sp = 11f;
                else if (which == 1) sp = 13f;
                else if (which == 2) sp = 16f;
                else if (which == 3) sp = 20f;
                editor.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
            }
        });
        builder.show();
    }

    private void showHelpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("About Notepad");
        builder.setMessage("Microsoft Windows XP Professional\nVersion 5.1 (Build 2600.xpsp_sp3_gdr.120504-1619 : Service Pack 3)\n\n" +
                "Notepad text editor for Samsung Galaxy Tab 3 7.0.\n(c) Microsoft Corporation. All rights reserved.");
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    private void actionNew() {
        if (isModified) {
            AlertDialog.Builder b = new AlertDialog.Builder(this);
            b.setTitle("Notepad");
            b.setMessage("The text has changed.\n\nDo you want to save changes?");
            b.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    actionSave();
                    resetEditor();
                }
            });
            b.setNegativeButton("Don't Save", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    resetEditor();
                }
            });
            b.setNeutralButton("Cancel", null);
            b.show();
        } else {
            resetEditor();
        }
    }

    private void resetEditor() {
        editor.setText("");
        currentFile = null;
        isModified = false;
        titleText.setText("Untitled - Notepad");
        updateCursorPosition();
    }

    private void actionOpen() {
        File docDir = new File(DOCUMENTS_DIR);
        if (!docDir.exists()) {
            docDir.mkdirs();
        }

        File[] files = docDir.listFiles();
        List<String> txtFiles = new ArrayList<>();
        if (files != null) {
            for (File f : files) {
                if (f.isFile() && f.getName().toLowerCase().endsWith(".txt")) {
                    txtFiles.add(f.getName());
                }
            }
        }

        if (txtFiles.isEmpty()) {
            createSampleDocument();
            txtFiles.add("Welcome to Windows XP.txt");
        }

        final String[] fileArray = txtFiles.toArray(new String[0]);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Open Document (/sdcard/Documents/)");
        builder.setItems(fileArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                File target = new File(DOCUMENTS_DIR, fileArray[which]);
                loadFile(target);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void loadFile(File file) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();

            editor.setText(sb.toString());
            currentFile = file;
            isModified = false;
            titleText.setText(file.getName() + " - Notepad");
            updateCursorPosition();
            Toast.makeText(this, "Opened " + file.getName(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error opening file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void actionSave() {
        if (currentFile != null) {
            saveToFile(currentFile);
        } else {
            actionSaveAs();
        }
    }

    private void actionSaveAs() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Save As (/sdcard/Documents/)");
        final EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setText(currentFile != null ? currentFile.getName() : "Notes.txt");
        input.setSelection(input.getText().length());
        builder.setView(input);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String filename = input.getText().toString().trim();
                if (filename.isEmpty()) filename = "Document.txt";
                if (!filename.toLowerCase().endsWith(".txt")) {
                    filename += ".txt";
                }

                File target = new File(DOCUMENTS_DIR, filename);
                saveToFile(target);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void saveToFile(File file) {
        try {
            File dir = file.getParentFile();
            if (dir != null && !dir.exists()) dir.mkdirs();

            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            writer.write(editor.getText().toString());
            writer.flush();
            writer.close();

            currentFile = file;
            isModified = false;
            titleText.setText(file.getName() + " - Notepad");
            Toast.makeText(this, "Saved to " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error saving file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void ensureDocumentsDirectory() {
        try {
            File dir = new File(DOCUMENTS_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File sample = new File(dir, "Welcome to Windows XP.txt");
            if (!sample.exists()) {
                createSampleDocument();
            }
        } catch (Exception ignored) {
        }
    }

    private void createSampleDocument() {
        try {
            File file = new File(DOCUMENTS_DIR, "Welcome to Windows XP.txt");
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            writer.write("=========================================\r\n");
            writer.write("Welcome to Windows XP Professional (SP3)\r\n");
            writer.write("Samsung Galaxy Tab 3 7.0 Edition\r\n");
            writer.write("=========================================\r\n\r\n");
            writer.write("Hardware Button Navigation:\r\n");
            writer.write("- Center Physical Home Button: Toggle Start Menu\r\n");
            writer.write("- Left Capacitive Menu Button: Windows XP Alt+Tab Switcher\r\n");
            writer.write("- Right Capacitive Back Button: Minimize Window to Taskbar\r\n\r\n");
            writer.write("Classic Windows Applications Included:\r\n");
            writer.write("1. Internet Explorer 6/7 (Modern HTML5 Engine)\r\n");
            writer.write("2. Windows Media Player 9/10 (Spectrum Visualizer)\r\n");
            writer.write("3. MS Paint (28-Color Palette & Drawing Tools)\r\n");
            writer.write("4. Notepad (Monospace Editor & CRLF UTF-8 I/O)\r\n");
            writer.write("5. Retro Games Suite (Pinball, Minesweeper, Solitaire)\r\n");
            writer.write("6. Fotoo (Digital Photo Frame)\r\n\r\n");
            writer.write("Enjoy your authentic retro computing experience!\r\n");
            writer.flush();
            writer.close();
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean dispatchKeyEvent(android.view.KeyEvent event) {
        if (event.getKeyCode() == android.view.KeyEvent.KEYCODE_BACK && event.getAction() == android.view.KeyEvent.ACTION_UP) {
            minimizeWindow();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        minimizeWindow();
    }

    private void minimizeWindow() {
        sendBroadcast(new Intent("com.windows.xp.ACTION_MINIMIZE"));
        moveTaskToBack(true);
    }
}
