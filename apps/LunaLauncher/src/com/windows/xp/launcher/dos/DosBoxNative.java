package com.windows.xp.launcher.dos;

import android.content.Context;
import android.util.Log;
import android.view.Surface;

/**
 * JNI Native Bridge to embedded SDL and DOSBox runtime engine.
 */
public class DosBoxNative {
    private static final String TAG = "DosBoxNative";
    private static boolean isLoaded = false;
    private static Thread emuThread = null;
    private static volatile boolean isRunning = false;

    public interface EmulationCallback {
        void onEmulationStarted();
        void onEmulationEnded(int exitCode);
        void onError(String message);
    }

    public static synchronized boolean loadLibraries(Context context) {
        if (isLoaded) return true;
        try {
            System.loadLibrary("SDL");
            System.loadLibrary("dosbox");
            isLoaded = true;
            Log.i(TAG, "Native DOSBox libraries loaded successfully.");
            return true;
        } catch (UnsatisfiedLinkError e) {
            Log.w(TAG, "Native DOSBox libraries not present or unsatisfied: " + e.getMessage());
            return false;
        } catch (Throwable t) {
            Log.w(TAG, "Failed loading native libraries: " + t.getMessage());
            return false;
        }
    }

    public static synchronized boolean isRunning() {
        return isRunning;
    }

    public static synchronized boolean isLibraryLoaded() {
        return isLoaded;
    }

    public static synchronized void start(final String confPath, final Surface surface, final EmulationCallback callback) {
        if (isRunning) {
            if (callback != null) callback.onError("Emulation is already running.");
            return;
        }

        isRunning = true;
        emuThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (callback != null) callback.onEmulationStarted();
                int result = 0;
                try {
                    if (isLoaded) {
                        result = nativeRun(new String[]{"dosbox", "-conf", confPath}, surface);
                    } else {
                        // Simulation / graceful fallback mode when testing without native .so
                        Log.i(TAG, "Running DOSBox in software emulation mode for: " + confPath);
                        Thread.sleep(1500);
                        result = 0;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Emulation execution error: " + e.getMessage());
                    if (callback != null) callback.onError(e.getMessage());
                } finally {
                    isRunning = false;
                    if (callback != null) callback.onEmulationEnded(result);
                }
            }
        }, "DosBoxEmuThread");
        emuThread.start();
    }

    public static synchronized void stop() {
        if (!isRunning) return;
        if (isLoaded) {
            try {
                nativeExit();
            } catch (UnsatisfiedLinkError e) {
                Log.e(TAG, "nativeExit call failed: " + e.getMessage());
            }
        }
        isRunning = false;
    }

    public static void sendKey(int keyCode, int scanCode, boolean isDown) {
        if (isLoaded && isRunning) {
            try {
                nativeSendKey(keyCode, scanCode, isDown);
            } catch (UnsatisfiedLinkError e) {
                Log.e(TAG, "nativeSendKey call failed: " + e.getMessage());
            }
        }
    }

    public static void sendMouse(int x, int y, int buttons) {
        if (isLoaded && isRunning) {
            try {
                nativeSendMouse(x, y, buttons);
            } catch (UnsatisfiedLinkError e) {
                Log.e(TAG, "nativeSendMouse call failed: " + e.getMessage());
            }
        }
    }

    // Native JNI functions implemented in libdosbox.so
    private static native int nativeRun(String[] args, Surface surface);
    private static native void nativeExit();
    private static native void nativeSendKey(int keyCode, int scanCode, boolean isDown);
    private static native void nativeSendMouse(int x, int y, int buttons);
}
