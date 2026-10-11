package com.windows.xp.launcher.window;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.windows.xp.launcher.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CmdPromptView extends LinearLayout {

    public interface OnExitListener {
        void onExit();
    }

    private ScrollView scrollTerminal;
    private TextView tvTerminalOutput;
    private TextView tvPromptLabel;
    private EditText etCmdInput;

    private String currentDir = "/sdcard";
    private boolean isDosBoxMode = false;
    private OnExitListener exitListener;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final StringBuilder outputBuffer = new StringBuilder();

    public CmdPromptView(Context context) {
        super(context);
        init(context);
    }

    public CmdPromptView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_cmd_prompt, this, true);

        scrollTerminal = (ScrollView) findViewById(R.id.scroll_terminal);
        tvTerminalOutput = (TextView) findViewById(R.id.tv_terminal_output);
        tvPromptLabel = (TextView) findViewById(R.id.tv_prompt_label);
        etCmdInput = (EditText) findViewById(R.id.et_cmd_input);

        // Print XP Startup Banner
        appendOutput("Microsoft Windows XP [Version 5.1.2600]\n(C) Copyright 1985-2001 Microsoft Corp.\n\nType 'HELP' for a list of commands, or 'DOSBOX' for x86 DOS environment.\n\n");
        updatePromptLabel();

        etCmdInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    String cmd = etCmdInput.getText().toString().trim();
                    etCmdInput.setText("");
                    executeCommand(cmd);
                    return true;
                }
                return false;
            }
        });

        // Request input focus
        post(new Runnable() {
            @Override
            public void run() {
                etCmdInput.requestFocus();
            }
        });
    }

    public void setOnExitListener(OnExitListener listener) {
        this.exitListener = listener;
    }

    private void updatePromptLabel() {
        if (isDosBoxMode) {
            tvPromptLabel.setText("Z:\\> ");
        } else {
            if (currentDir.equals("/sdcard") || currentDir.equals("/storage/emulated/0")) {
                tvPromptLabel.setText("C:\\Documents and Settings\\Administrator> ");
            } else {
                String winPath = currentDir.replace("/sdcard", "C:").replace('/', '\\');
                tvPromptLabel.setText(winPath + "> ");
            }
        }
    }

    private void appendOutput(final String text) {
        outputBuffer.append(text);
        tvTerminalOutput.setText(outputBuffer.toString());
        scrollTerminal.post(new Runnable() {
            @Override
            public void run() {
                scrollTerminal.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private void executeCommand(String rawCmd) {
        String prompt = tvPromptLabel.getText().toString();
        appendOutput(prompt + rawCmd + "\n");

        if (rawCmd.isEmpty()) {
            return;
        }

        String[] parts = rawCmd.split("\\s+");
        String command = parts[0].toUpperCase(Locale.US);

        if (isDosBoxMode) {
            handleDosBoxCommand(command, parts, rawCmd);
            return;
        }

        // Handle Windows XP / DOS Commands
        if (command.equals("CLS") || command.equals("CLEAR")) {
            outputBuffer.setLength(0);
            tvTerminalOutput.setText("");
        } else if (command.equals("VER")) {
            appendOutput("\nMicrosoft Windows XP [Version 5.1.2600]\n\n");
        } else if (command.equals("DATE")) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MM/dd/yyyy", Locale.US);
            appendOutput("\nThe current date is: " + sdf.format(new Date()) + "\n\n");
        } else if (command.equals("TIME")) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SS", Locale.US);
            appendOutput("\nThe current time is: " + sdf.format(new Date()) + "\n\n");
        } else if (command.equals("DIR") || command.equals("LS")) {
            executeDir();
        } else if (command.equals("CD") || command.equals("CHDIR")) {
            if (parts.length > 1) {
                executeCd(rawCmd.substring(rawCmd.indexOf(' ') + 1).trim());
            } else {
                appendOutput(currentDir + "\n\n");
            }
        } else if (command.equals("TYPE") || command.equals("CAT")) {
            if (parts.length > 1) {
                executeType(parts[1]);
            } else {
                appendOutput("The syntax of the command is incorrect.\n\n");
            }
        } else if (command.equals("ECHO")) {
            if (parts.length > 1) {
                appendOutput(rawCmd.substring(rawCmd.indexOf(' ') + 1) + "\n\n");
            } else {
                appendOutput("ECHO is on.\n\n");
            }
        } else if (command.equals("MD") || command.equals("MKDIR")) {
            if (parts.length > 1) {
                File d = new File(currentDir, parts[1]);
                if (d.mkdirs()) {
                    appendOutput("\n");
                } else {
                    appendOutput("A subdirectory or file " + parts[1] + " already exists.\n\n");
                }
            } else {
                appendOutput("The syntax of the command is incorrect.\n\n");
            }
        } else if (command.equals("RD") || command.equals("RMDIR")) {
            if (parts.length > 1) {
                File d = new File(currentDir, parts[1]);
                if (d.delete()) {
                    appendOutput("\n");
                } else {
                    appendOutput("The directory is not empty or cannot be removed.\n\n");
                }
            } else {
                appendOutput("The syntax of the command is incorrect.\n\n");
            }
        } else if (command.equals("DEL") || command.equals("ERASE") || command.equals("RM")) {
            if (parts.length > 1) {
                File f = new File(currentDir, parts[1]);
                if (f.delete()) {
                    appendOutput("\n");
                } else {
                    appendOutput("Could Not Find " + f.getAbsolutePath() + "\n\n");
                }
            } else {
                appendOutput("The syntax of the command is incorrect.\n\n");
            }
        } else if (command.equals("HELP")) {
            appendOutput("\nFor more information on a specific command, type HELP command-name\n" +
                    "CD             Displays the name of or changes the current directory.\n" +
                    "CLS            Clears the screen.\n" +
                    "DATE           Displays the date.\n" +
                    "DEL            Deletes one or more files.\n" +
                    "DIR            Displays a list of files and subdirectories in a directory.\n" +
                    "DOSBOX         Starts emulated 16-bit/32-bit x86 MS-DOS environment.\n" +
                    "ECHO           Displays messages, or turns command echoing on or off.\n" +
                    "EXIT           Quits the CMD.EXE program (command interpreter).\n" +
                    "MD             Creates a directory.\n" +
                    "RD             Removes a directory.\n" +
                    "TIME           Displays the system time.\n" +
                    "TYPE           Displays the contents of a text file.\n" +
                    "VER            Displays the Windows version.\n" +
                    "UNAME / DF     Standard system shell commands are supported.\n\n");
        } else if (command.equals("DOSBOX")) {
            startDosBoxMode();
        } else if (command.equals("EXIT")) {
            if (exitListener != null) {
                exitListener.onExit();
            }
        } else {
            // Check if command is a local DOS binary (.exe, .com, .bat)
            File targetExe = new File(currentDir, rawCmd);
            if (!targetExe.exists()) {
                if (!rawCmd.toLowerCase(Locale.US).endsWith(".exe")) {
                    File exeTry = new File(currentDir, rawCmd + ".exe");
                    if (exeTry.exists() && exeTry.isFile()) targetExe = exeTry;
                }
                if (!targetExe.exists() && !rawCmd.toLowerCase(Locale.US).endsWith(".com")) {
                    File comTry = new File(currentDir, rawCmd + ".com");
                    if (comTry.exists() && comTry.isFile()) targetExe = comTry;
                }
                if (!targetExe.exists() && !rawCmd.toLowerCase(Locale.US).endsWith(".bat")) {
                    File batTry = new File(currentDir, rawCmd + ".bat");
                    if (batTry.exists() && batTry.isFile()) targetExe = batTry;
                }
            }

            if (targetExe.exists() && targetExe.isFile()) {
                appendOutput("\nStarting MS-DOS executable: " + targetExe.getName() + "...\n\n");
                if (getContext() instanceof com.windows.xp.launcher.LauncherActivity) {
                    ((com.windows.xp.launcher.LauncherActivity) getContext()).openDosApplication(targetExe, null, R.drawable.ic_dos_app);
                }
                updatePromptLabel();
                return;
            }

            // Bridge to Android shell process execution
            executeShellProcess(rawCmd);
        }

        updatePromptLabel();
    }

    private void startDosBoxMode() {
        isDosBoxMode = true;
        appendOutput("\nDOSBox version 0.74-3\n" +
                "Copyright 2002-2023 DOSBox Team, published under GNU GPL.\n" +
                "---\n" +
                "CONFIG: Loading primary settings from config file.\n" +
                "MIDI: Opened device:none.\n" +
                "Drive C is mounted as local directory " + currentDir + "\n" +
                "For a short introduction for new users type: INTRO\n" +
                "For supported shell commands type: HELP\n\n");
        updatePromptLabel();
    }

    private void handleDosBoxCommand(String command, String[] parts, String rawCmd) {
        if (command.equals("EXIT")) {
            isDosBoxMode = false;
            appendOutput("\nExiting DOSBox. Returning to Windows XP Command Prompt.\n\n");
            updatePromptLabel();
        } else if (command.equals("VER")) {
            appendOutput("\nDOSBox version 0.74-3. Reported DOS version 5.00.\n\n");
        } else if (command.equals("INTRO")) {
            appendOutput("\nWelcome to DOSBox, an x86 realmode with DOS emulator!\n" +
                    "Drive C: is mapped to device internal storage.\n" +
                    "Run RETRO games by typing their .EXE or .COM filename.\n\n");
        } else if (command.equals("DIR")) {
            executeDir();
        } else if (command.equals("CLS")) {
            outputBuffer.setLength(0);
            tvTerminalOutput.setText("");
        } else if (command.equals("HELP")) {
            appendOutput("\nSupported DOSBox commands:\n" +
                    "DIR, CD, CLS, TYPE, VER, INTRO, MOUNT, MEM, EXIT\n\n");
        } else {
            appendOutput("Bad command or file name: " + rawCmd + "\n\n");
        }
        updatePromptLabel();
    }

    private void executeDir() {
        File dir = new File(currentDir);
        File[] files = dir.listFiles();

        long freeBytes = dir.getFreeSpace();
        long totalFileBytes = 0;
        int fileCount = 0;
        int dirCount = 0;

        StringBuilder sb = new StringBuilder();
        sb.append("\n Volume in drive C is LOCAL DISK\n");
        sb.append(" Directory of ").append(currentDir.replace('/', '\\')).append("\n\n");

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy  hh:mm a", Locale.US);

        if (files != null) {
            for (File f : files) {
                String dateStr = sdf.format(new Date(f.lastModified()));
                if (f.isDirectory()) {
                    dirCount++;
                    sb.append(String.format("%s    <DIR>          %s\n", dateStr, f.getName()));
                } else {
                    fileCount++;
                    totalFileBytes += f.length();
                    sb.append(String.format("%s         %,10d %s\n", dateStr, f.length(), f.getName()));
                }
            }
        }

        sb.append(String.format("              %d File(s)    %,12d bytes\n", fileCount, totalFileBytes));
        sb.append(String.format("              %d Dir(s)     %,12d bytes free\n\n", dirCount, freeBytes));

        appendOutput(sb.toString());
    }

    private void executeCd(String target) {
        File newDir;
        if (target.equals("..")) {
            newDir = new File(currentDir).getParentFile();
            if (newDir == null || !newDir.exists()) newDir = new File("/");
        } else if (target.equals("\\") || target.equals("/")) {
            newDir = new File("/sdcard");
        } else if (target.startsWith("/")) {
            newDir = new File(target);
        } else if (target.toUpperCase().startsWith("C:\\")) {
            String sub = target.substring(3).replace('\\', '/');
            newDir = new File("/sdcard/" + sub);
        } else {
            newDir = new File(currentDir, target);
        }

        if (newDir != null && newDir.exists() && newDir.isDirectory()) {
            currentDir = newDir.getAbsolutePath();
            appendOutput("\n");
        } else {
            appendOutput("The system cannot find the path specified: " + target + "\n\n");
        }
    }

    private void executeType(String filename) {
        File f = new File(currentDir, filename);
        if (!f.exists() || !f.isFile()) {
            appendOutput("The system cannot find the file specified.\n\n");
            return;
        }

        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
            StringBuilder content = new StringBuilder("\n");
            String line;
            while ((line = r.readLine()) != null) {
                content.append(line).append("\n");
            }
            r.close();
            content.append("\n");
            appendOutput(content.toString());
        } catch (Exception e) {
            appendOutput("Error reading file: " + e.getMessage() + "\n\n");
        }
    }

    private void executeShellProcess(final String cmd) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Process process = Runtime.getRuntime().exec(new String[]{"sh", "-c", cmd}, null, new File(currentDir));
                    BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    BufferedReader stdErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));

                    final StringBuilder result = new StringBuilder("\n");
                    String line;
                    while ((line = stdOut.readLine()) != null) {
                        result.append(line).append("\n");
                    }
                    while ((line = stdErr.readLine()) != null) {
                        result.append(line).append("\n");
                    }
                    result.append("\n");

                    process.waitFor();

                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            appendOutput(result.toString());
                        }
                    });
                } catch (final Exception e) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            appendOutput("'" + cmd + "' is not recognized as an internal or external command,\noperable program or batch file.\n\n");
                        }
                    });
                }
            }
        }).start();
    }
}
