package com.windows.xp.launcher.dos;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Generates dynamic, optimized DOSBox configuration files for launching
 * MS-DOS applications directly within LunaLauncher.
 */
public class DosBoxConfigGenerator {

    public static final String DEFAULT_DOS_ROOT = "/sdcard/DOS";

    /**
     * Resolves an Android filesystem path to a standard MS-DOS path relative to drive C.
     */
    public static String resolveDosPath(File baseMountDir, File targetFile) {
        String basePath = baseMountDir.getAbsolutePath().replace('\\', '/');
        String targetPath = targetFile.getAbsolutePath().replace('\\', '/');

        if (targetPath.startsWith(basePath)) {
            String rel = targetPath.substring(basePath.length());
            if (rel.startsWith("/")) rel = rel.substring(1);
            return "C:\\" + rel.replace('/', '\\');
        }
        return "C:\\" + targetFile.getName();
    }

    /**
     * Builds an authentic dosbox.conf file tailored for the Samsung Galaxy Tab 3 hardware
     * and sets up autoexec to run the requested target executable.
     */
    public static String buildConfigString(File baseMountDir, File targetExe) {
        String relDir = "";
        File parent = targetExe.getParentFile();
        if (parent != null && !parent.getAbsolutePath().equals(baseMountDir.getAbsolutePath())) {
            String basePath = baseMountDir.getAbsolutePath().replace('\\', '/');
            String parentPath = parent.getAbsolutePath().replace('\\', '/');
            if (parentPath.startsWith(basePath)) {
                relDir = parentPath.substring(basePath.length()).replace('/', '\\');
                if (relDir.startsWith("\\")) relDir = relDir.substring(1);
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[sdl]\n");
        sb.append("fullscreen=false\n");
        sb.append("fulldouble=false\n");
        sb.append("output=surface\n");
        sb.append("autolock=true\n");
        sb.append("sensitivity=100\n\n");

        sb.append("[dosbox]\n");
        sb.append("memsize=16\n\n");

        sb.append("[render]\n");
        sb.append("aspect=true\n");
        sb.append("scaler=normal2x\n\n");

        sb.append("[cpu]\n");
        sb.append("core=dynamic\n");
        sb.append("cputype=auto\n");
        sb.append("cycles=max\n\n");

        sb.append("[mixer]\n");
        sb.append("nosound=false\n");
        sb.append("rate=22050\n");
        sb.append("blocksize=1024\n");
        sb.append("prebuffer=20\n\n");

        sb.append("[sblaster]\n");
        sb.append("sbtype=sb16\n");
        sb.append("sbbase=220\n");
        sb.append("irq=7\n");
        sb.append("dma=1\n");
        sb.append("hdma=5\n");
        sb.append("sbmixer=true\n");
        sb.append("oplmode=auto\n");
        sb.append("oplemu=default\n");
        sb.append("oplrate=22050\n\n");

        sb.append("[autoexec]\n");
        sb.append("@echo off\n");
        sb.append("mount c \"").append(baseMountDir.getAbsolutePath().replace('\\', '/')).append("\"\n");
        sb.append("c:\n");
        if (!relDir.isEmpty()) {
            sb.append("cd \"").append(relDir).append("\"\n");
        }
        sb.append(targetExe.getName()).append("\n");
        sb.append("exit\n");

        return sb.toString();
    }

    /**
     * Writes out the generated config to a specified file.
     */
    public static File writeConfigFile(File destinationConf, File baseMountDir, File targetExe) throws IOException {
        String content = buildConfigString(baseMountDir, targetExe);
        FileWriter writer = new FileWriter(destinationConf);
        writer.write(content);
        writer.close();
        return destinationConf;
    }
}
