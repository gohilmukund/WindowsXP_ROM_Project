package tools.tests;

import com.windows.xp.launcher.dos.DosBoxConfigGenerator;
import java.io.File;

public class DosBoxConfigTest {

    public static void testPathResolution() {
        File baseDir = new File("/sdcard/DOS");
        File exe = new File("/sdcard/DOS/DOOM/DOOM.EXE");
        String dosPath = DosBoxConfigGenerator.resolveDosPath(baseDir, exe);
        if (!"C:\\DOOM\\DOOM.EXE".equalsIgnoreCase(dosPath)) {
            throw new AssertionError("Expected C:\\DOOM\\DOOM.EXE but got " + dosPath);
        }
    }

    public static void testConfigGeneration() {
        File baseDir = new File("/sdcard/DOS");
        File exe = new File("/sdcard/DOS/PRINCE/PRINCE.EXE");
        String conf = DosBoxConfigGenerator.buildConfigString(baseDir, exe);
        if (!conf.contains("core=dynamic")) {
            throw new AssertionError("Config missing core=dynamic");
        }
        if (!conf.contains("mount c \"/sdcard/DOS\"")) {
            throw new AssertionError("Config missing mount command");
        }
        if (!conf.contains("PRINCE.EXE")) {
            throw new AssertionError("Config missing executable launch command");
        }
        if (!conf.contains("exit")) {
            throw new AssertionError("Config missing exit command to auto-close window on quit");
        }
    }

    public static void testNestedSubdirectoryNavigation() {
        File baseDir = new File("/sdcard/DOS");
        File exe = new File("/sdcard/DOS/GAMES/ACTION/DOOM.EXE");
        String conf = DosBoxConfigGenerator.buildConfigString(baseDir, exe);
        if (!conf.contains("cd \"GAMES\\ACTION\"")) {
            throw new AssertionError("Config missing subdirectory cd command: " + conf);
        }
    }
}
