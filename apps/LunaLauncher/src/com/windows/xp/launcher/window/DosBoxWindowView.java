package com.windows.xp.launcher.window;

import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.windows.xp.launcher.R;
import com.windows.xp.launcher.dos.DosBoxConfigGenerator;
import com.windows.xp.launcher.dos.DosBoxNative;

import java.io.File;

/**
 * Embedded Window View for running DOSBox and MS-DOS executables
 * inside a movable/resizable Windows XP WindowFrameView.
 */
public class DosBoxWindowView extends RelativeLayout implements SurfaceHolder.Callback {

    public interface OnDosSessionListener {
        void onSessionFinished();
    }

    private SurfaceView surfaceView;
    private OnDosSessionListener sessionListener;
    private File targetExe;
    private File confFile;

    public DosBoxWindowView(Context context, File targetExe, OnDosSessionListener listener) {
        super(context);
        this.targetExe = targetExe;
        this.sessionListener = listener;
        init(context);
    }

    private void init(final Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_dosbox_window, this, true);
        surfaceView = (SurfaceView) findViewById(R.id.dosbox_surface);
        surfaceView.getHolder().addCallback(this);

        setupQuickBar();
    }

    private void setupQuickBar() {
        View esc = findViewById(R.id.btn_dos_esc);
        if (esc != null) {
            esc.setOnClickListener(new OnClickListener() {
                @Override public void onClick(View v) { sendDosKey(KeyEvent.KEYCODE_ESCAPE); }
            });
        }

        View enter = findViewById(R.id.btn_dos_enter);
        if (enter != null) {
            enter.setOnClickListener(new OnClickListener() {
                @Override public void onClick(View v) { sendDosKey(KeyEvent.KEYCODE_ENTER); }
            });
        }

        View space = findViewById(R.id.btn_dos_space);
        if (space != null) {
            space.setOnClickListener(new OnClickListener() {
                @Override public void onClick(View v) { sendDosKey(KeyEvent.KEYCODE_SPACE); }
            });
        }

        View up = findViewById(R.id.btn_dos_up);
        if (up != null) {
            up.setOnClickListener(new OnClickListener() {
                @Override public void onClick(View v) { sendDosKey(KeyEvent.KEYCODE_DPAD_UP); }
            });
        }

        View down = findViewById(R.id.btn_dos_down);
        if (down != null) {
            down.setOnClickListener(new OnClickListener() {
                @Override public void onClick(View v) { sendDosKey(KeyEvent.KEYCODE_DPAD_DOWN); }
            });
        }

        View left = findViewById(R.id.btn_dos_left);
        if (left != null) {
            left.setOnClickListener(new OnClickListener() {
                @Override public void onClick(View v) { sendDosKey(KeyEvent.KEYCODE_DPAD_LEFT); }
            });
        }

        View right = findViewById(R.id.btn_dos_right);
        if (right != null) {
            right.setOnClickListener(new OnClickListener() {
                @Override public void onClick(View v) { sendDosKey(KeyEvent.KEYCODE_DPAD_RIGHT); }
            });
        }

        View kbd = findViewById(R.id.btn_dos_keyboard);
        if (kbd != null) {
            kbd.setOnClickListener(new OnClickListener() {
                @Override public void onClick(View v) { toggleSoftKeyboard(); }
            });
        }
    }

    private void sendDosKey(int keyCode) {
        DosBoxNative.sendKey(keyCode, 0, true);
        DosBoxNative.sendKey(keyCode, 0, false);
    }

    private void toggleSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        DosBoxNative.loadLibraries(getContext());
        try {
            File dosRoot = new File(DosBoxConfigGenerator.DEFAULT_DOS_ROOT);
            if (!dosRoot.exists()) dosRoot.mkdirs();

            confFile = new File(getContext().getFilesDir(), "dosbox_run.conf");
            DosBoxConfigGenerator.writeConfigFile(confFile, dosRoot, targetExe);

            DosBoxNative.start(confFile.getAbsolutePath(), holder.getSurface(), new DosBoxNative.EmulationCallback() {
                @Override
                public void onEmulationStarted() {}

                @Override
                public void onEmulationEnded(final int exitCode) {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            if (sessionListener != null) {
                                sessionListener.onSessionFinished();
                            }
                        }
                    });
                }

                @Override
                public void onError(final String message) {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), "DOS Error: " + message, Toast.LENGTH_LONG).show();
                            if (sessionListener != null) {
                                sessionListener.onSessionFinished();
                            }
                        }
                    });
                }
            });
        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed to configure DOSBox: " + e.getMessage(), Toast.LENGTH_LONG).show();
            if (sessionListener != null) sessionListener.onSessionFinished();
        }
    }

    @Override public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        DosBoxNative.stop();
    }
}
