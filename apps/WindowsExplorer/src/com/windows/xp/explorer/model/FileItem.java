package com.windows.xp.explorer.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileItem {
    private String name;
    private String path;
    private boolean isDirectory;
    private boolean isDrive;
    private long size;
    private long lastModified;
    private int iconResId;
    private String typeDescription;
    private String subText;

    public FileItem(String name, String path, boolean isDirectory, boolean isDrive,
                    long size, long lastModified, int iconResId,
                    String typeDescription, String subText) {
        this.name = name;
        this.path = path;
        this.isDirectory = isDirectory;
        this.isDrive = isDrive;
        this.size = size;
        this.lastModified = lastModified;
        this.iconResId = iconResId;
        this.typeDescription = typeDescription;
        this.subText = subText;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public boolean isDrive() {
        return isDrive;
    }

    public long getSize() {
        return size;
    }

    public long getLastModified() {
        return lastModified;
    }

    public int getIconResId() {
        return iconResId;
    }

    public String getTypeDescription() {
        return typeDescription;
    }

    public String getSubText() {
        if (subText != null && !subText.isEmpty()) {
            return subText;
        }
        if (isDirectory) {
            return "File Folder";
        }
        return formatSize(size);
    }

    public String getFormattedDate() {
        if (lastModified <= 0) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.US);
        return sdf.format(new Date(lastModified));
    }

    public static String formatSize(long bytes) {
        if (bytes <= 0) return "0 B";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) {
            return String.format(Locale.US, "%.1f KB", bytes / 1024.0);
        }
        if (bytes < 1024 * 1024 * 1024) {
            return String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0));
        }
        return String.format(Locale.US, "%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
}
