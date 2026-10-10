package com.windows.xp.explorer.fs;

import com.windows.xp.explorer.R;
import java.io.File;

public class MimeTypeResolver {

    public static int getIconForFile(File file) {
        if (file.isDirectory()) {
            String name = file.getName().toLowerCase();
            if (name.equals("documents")) return R.drawable.ic_folder_docs;
            if (name.equals("pictures") || name.equals("dcim")) return R.drawable.ic_folder_pics;
            if (name.equals("music")) return R.drawable.ic_folder_music;
            return R.drawable.ic_folder;
        }

        String ext = getExtension(file.getName()).toLowerCase();
        if (ext.equals("txt") || ext.equals("log") || ext.equals("cfg") || ext.equals("ini")) {
            return R.drawable.ic_file_text;
        }
        if (ext.equals("png") || ext.equals("jpg") || ext.equals("jpeg") || ext.equals("bmp") || ext.equals("gif")) {
            return R.drawable.ic_folder_pics;
        }
        if (ext.equals("mp3") || ext.equals("wav") || ext.equals("ogg") || ext.equals("m4a")) {
            return R.drawable.ic_folder_music;
        }
        return R.drawable.ic_file_generic;
    }

    public static String getTypeDescription(File file) {
        if (file.isDirectory()) {
            return "File Folder";
        }
        String ext = getExtension(file.getName()).toLowerCase();
        if (ext.equals("txt")) return "Text Document";
        if (ext.equals("log")) return "Log File";
        if (ext.equals("ini")) return "Configuration Settings";
        if (ext.equals("png")) return "PNG Image";
        if (ext.equals("jpg") || ext.equals("jpeg")) return "JPEG Image";
        if (ext.equals("bmp")) return "Bitmap Image";
        if (ext.equals("mp3")) return "MP3 Audio Track";
        if (ext.equals("wav")) return "Waveform Audio";
        if (ext.equals("ogg")) return "Ogg Vorbis Audio";
        if (ext.equals("apk")) return "Android Package";
        if (ext.equals("html") || ext.equals("htm")) return "HTML Document";
        if (ext.equals("zip")) return "Compressed (zipped) Folder";
        if (!ext.isEmpty()) {
            return ext.toUpperCase() + " File";
        }
        return "File";
    }

    public static String getMimeType(File file) {
        String ext = getExtension(file.getName()).toLowerCase();
        if (ext.equals("txt") || ext.equals("log") || ext.equals("cfg") || ext.equals("ini")) {
            return "text/plain";
        }
        if (ext.equals("png")) return "image/png";
        if (ext.equals("jpg") || ext.equals("jpeg")) return "image/jpeg";
        if (ext.equals("bmp")) return "image/bmp";
        if (ext.equals("gif")) return "image/gif";
        if (ext.equals("mp3")) return "audio/mpeg";
        if (ext.equals("wav")) return "audio/x-wav";
        if (ext.equals("ogg")) return "audio/ogg";
        if (ext.equals("apk")) return "application/vnd.android.package-archive";
        if (ext.equals("html") || ext.equals("htm")) return "text/html";
        return "*/*";
    }

    public static String getExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot >= 0 && dot < fileName.length() - 1) {
            return fileName.substring(dot + 1);
        }
        return "";
    }
}
