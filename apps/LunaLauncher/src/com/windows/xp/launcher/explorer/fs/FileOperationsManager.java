package com.windows.xp.launcher.explorer.fs;

import android.os.Environment;
import android.os.StatFs;
import com.windows.xp.launcher.R;
import com.windows.xp.launcher.explorer.model.FileItem;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FileOperationsManager {

    public static final String VIRTUAL_MY_COMPUTER = "computer://";

    public List<FileItem> getDrives() {
        List<FileItem> drives = new ArrayList<FileItem>();

        // Drive C: (Internal Storage)
        File sdcard = new File("/sdcard");
        if (!sdcard.exists() || !sdcard.canRead()) {
            sdcard = Environment.getExternalStorageDirectory();
        }
        if (sdcard != null && sdcard.exists()) {
            long total = 0;
            long free = 0;
            try {
                StatFs stat = new StatFs(sdcard.getPath());
                long blockSize = stat.getBlockSize();
                long totalBlocks = stat.getBlockCount();
                long availBlocks = stat.getAvailableBlocks();
                total = totalBlocks * blockSize;
                free = availBlocks * blockSize;
            } catch (Exception ignored) {}

            String sub = FileItem.formatSize(free) + " free of " + FileItem.formatSize(total);
            drives.add(new FileItem("Local Disk (C:)", sdcard.getAbsolutePath(), true, true,
                    total, sdcard.lastModified(), R.drawable.ic_drive_c, "Local Fixed Disk", sub));
        }

        // Drive D: (External MicroSD Card)
        File extSd = new File("/storage/extSdCard");
        if (extSd.exists() && extSd.canRead()) {
            long total = 0;
            long free = 0;
            try {
                StatFs stat = new StatFs(extSd.getPath());
                long blockSize = stat.getBlockSize();
                long totalBlocks = stat.getBlockCount();
                long availBlocks = stat.getAvailableBlocks();
                total = totalBlocks * blockSize;
                free = availBlocks * blockSize;
            } catch (Exception ignored) {}

            String sub = FileItem.formatSize(free) + " free of " + FileItem.formatSize(total);
            drives.add(new FileItem("Removable Disk (D:)", extSd.getAbsolutePath(), true, true,
                    total, extSd.lastModified(), R.drawable.ic_drive_d, "Removable Disk", sub));
        }

        // Drive S: (System Partition)
        File sys = new File("/system");
        if (sys.exists() && sys.canRead()) {
            long total = 0;
            long free = 0;
            try {
                StatFs stat = new StatFs(sys.getPath());
                long blockSize = stat.getBlockSize();
                long totalBlocks = stat.getBlockCount();
                long availBlocks = stat.getAvailableBlocks();
                total = totalBlocks * blockSize;
                free = availBlocks * blockSize;
            } catch (Exception ignored) {}

            String sub = FileItem.formatSize(free) + " free of " + FileItem.formatSize(total);
            drives.add(new FileItem("System (S:)", sys.getAbsolutePath(), true, true,
                    total, sys.lastModified(), R.drawable.ic_drive_c, "System Partition", sub));
        }

        return drives;
    }

    public List<FileItem> scanDirectory(String path) {
        if (path == null || path.equals(VIRTUAL_MY_COMPUTER)) {
            return getDrives();
        }

        File dir = new File(path);
        File[] files = dir.listFiles();
        if ((files == null || files.length == 0) && (path.equals("/sdcard") || path.equals("/storage/emulated/legacy") || path.equals("/storage/emulated/0"))) {
            File alt1 = new File("/sdcard");
            files = alt1.listFiles();
            if (files == null || files.length == 0) {
                File alt2 = new File("/storage/emulated/legacy");
                files = alt2.listFiles();
            }
            if (files == null || files.length == 0) {
                File alt3 = Environment.getExternalStorageDirectory();
                if (alt3 != null) files = alt3.listFiles();
            }
        }
        if (files == null) {
            return new ArrayList<FileItem>();
        }

        List<FileItem> folders = new ArrayList<FileItem>();
        List<FileItem> fileItems = new ArrayList<FileItem>();

        for (File f : files) {
            if (f.getName().startsWith(".")) continue; // skip hidden files

            int iconRes = MimeTypeResolver.getIconForFile(f);
            String typeDesc = MimeTypeResolver.getTypeDescription(f);

            if (f.isDirectory()) {
                folders.add(new FileItem(f.getName(), f.getAbsolutePath(), true, false,
                        0, f.lastModified(), iconRes, typeDesc, "File Folder"));
            } else {
                fileItems.add(new FileItem(f.getName(), f.getAbsolutePath(), false, false,
                        f.length(), f.lastModified(), iconRes, typeDesc, FileItem.formatSize(f.length())));
            }
        }

        Collections.sort(folders, new Comparator<FileItem>() {
            @Override
            public int compare(FileItem o1, FileItem o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });

        Collections.sort(fileItems, new Comparator<FileItem>() {
            @Override
            public int compare(FileItem o1, FileItem o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });

        List<FileItem> result = new ArrayList<FileItem>();
        result.addAll(folders);
        result.addAll(fileItems);
        return result;
    }

    public boolean createFolder(String parentPath, String folderName) {
        File f = new File(parentPath, folderName);
        return f.mkdirs();
    }

    public boolean deleteItem(String path) {
        File f = new File(path);
        return deleteRecursive(f);
    }

    private boolean deleteRecursive(File fileOrDir) {
        if (fileOrDir.isDirectory()) {
            File[] children = fileOrDir.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        return fileOrDir.delete();
    }

    public boolean renameItem(String oldPath, String newName) {
        File oldFile = new File(oldPath);
        File newFile = new File(oldFile.getParent(), newName);
        return oldFile.renameTo(newFile);
    }

    public String getFreeSpaceSummary(String path) {
        try {
            File target = (path == null || path.equals(VIRTUAL_MY_COMPUTER))
                    ? Environment.getExternalStorageDirectory()
                    : new File(path);
            if (target != null && target.exists()) {
                StatFs stat = new StatFs(target.getPath());
                long free = (long) stat.getAvailableBlocks() * stat.getBlockSize();
                return "Free Space: " + FileItem.formatSize(free);
            }
        } catch (Exception ignored) {}
        return "";
    }

    public List<FileItem> getDirectoryContents(File dir) {
        return scanDirectory(dir.getAbsolutePath());
    }

    public boolean deleteFileOrFolder(File file) {
        return deleteItem(file.getAbsolutePath());
    }

    public long getDriveFreeSpace(String path) {
        try {
            StatFs stat = new StatFs(path);
            return (long) stat.getAvailableBlocks() * stat.getBlockSize();
        } catch (Exception e) {
            return 0;
        }
    }

    public String toWindowsPath(String path) {
        if (path == null) return "";
        if (path.startsWith("/storage/emulated/legacy")) {
            String sub = path.substring("/storage/emulated/legacy".length());
            return "C:" + (sub.startsWith("/") ? sub.replace('/', '\\') : (sub.isEmpty() ? "\\" : "\\" + sub.replace('/', '\\')));
        } else if (path.startsWith("/storage/emulated/0")) {
            String sub = path.substring("/storage/emulated/0".length());
            return "C:" + (sub.startsWith("/") ? sub.replace('/', '\\') : (sub.isEmpty() ? "\\" : "\\" + sub.replace('/', '\\')));
        } else if (path.startsWith("/sdcard")) {
            String sub = path.substring("/sdcard".length());
            return "C:" + (sub.startsWith("/") ? sub.replace('/', '\\') : (sub.isEmpty() ? "\\" : "\\" + sub.replace('/', '\\')));
        } else if (path.startsWith("/storage/sdcard0")) {
            String sub = path.substring("/storage/sdcard0".length());
            return "C:" + (sub.startsWith("/") ? sub.replace('/', '\\') : (sub.isEmpty() ? "\\" : "\\" + sub.replace('/', '\\')));
        } else if (path.startsWith("/storage/extSdCard")) {
            String sub = path.substring("/storage/extSdCard".length());
            return "D:" + (sub.startsWith("/") ? sub.replace('/', '\\') : (sub.isEmpty() ? "\\" : "\\" + sub.replace('/', '\\')));
        } else if (path.startsWith("/system")) {
            String sub = path.substring("/system".length());
            return "S:" + (sub.startsWith("/") ? sub.replace('/', '\\') : (sub.isEmpty() ? "\\" : "\\" + sub.replace('/', '\\')));
        }
        return path.replace('/', '\\');
    }

    public String toAndroidPath(String winPath) {
        if (winPath == null) return "/sdcard";
        String normalized = winPath.trim().replace('\\', '/');
        if (normalized.startsWith("C:/") || normalized.startsWith("c:/")) {
            return "/sdcard/" + normalized.substring(3);
        } else if (normalized.equalsIgnoreCase("C:") || normalized.equalsIgnoreCase("C:/")) {
            return "/sdcard";
        } else if (normalized.startsWith("D:/") || normalized.startsWith("d:/")) {
            return "/storage/extSdCard/" + normalized.substring(3);
        } else if (normalized.equalsIgnoreCase("D:") || normalized.equalsIgnoreCase("D:/")) {
            return "/storage/extSdCard";
        } else if (normalized.startsWith("S:/") || normalized.startsWith("s:/")) {
            return "/system/" + normalized.substring(3);
        } else if (normalized.equalsIgnoreCase("S:") || normalized.equalsIgnoreCase("S:/")) {
            return "/system";
        }
        return winPath;
    }

    public String formatSize(long bytes) {
        return FileItem.formatSize(bytes);
    }
}

