package com.windows.xp.launcher.window;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.windows.xp.launcher.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WMPWindowView extends LinearLayout {

    public static class MediaTrack {
        public long id;
        public String title;
        public String path;
        public int duration;
        public Uri uri;
        public boolean isVideo;

        public MediaTrack(long id, String title, String path, int duration, Uri uri, boolean isVideo) {
            this.id = id;
            this.title = title;
            this.path = path;
            this.duration = duration;
            this.uri = uri;
            this.isVideo = isVideo;
        }
    }

    private VideoView videoView;
    private MediaPlayer audioPlayer;
    private VisualizerView visualizerView;
    private TextView tvTrackStatus;
    private TextView tvTimeDisplay;
    private SeekBar seekBar;
    private ImageView btnPrev;
    private ImageView btnPlayPause;
    private ImageView btnStop;
    private ImageView btnNext;
    private ImageView btnOpenFile;
    private ImageView btnFullscreen;
    private RelativeLayout visualizerContainer;
    private ListView lvPlaylist;
    private TextView tvPlaylistHeader;
    private PlaylistAdapter playlistAdapter;
    private LinearLayout navBar;
    private LinearLayout playlistContainer;
    private View divider;
    private LinearLayout transportBar;
    private boolean isWmpFullscreen = false;

    private final List<MediaTrack> playlist = new ArrayList<MediaTrack>();
    private int currentTrackIndex = -1;
    private boolean isPlaying = false;
    private final Handler progressHandler = new Handler();
    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            if (isPlaying) {
                int current = 0;
                int total = 0;
                if (isCurrentTrackVideo()) {
                    if (videoView != null) {
                        current = videoView.getCurrentPosition();
                        total = videoView.getDuration();
                    }
                } else {
                    if (audioPlayer != null) {
                        try {
                            current = audioPlayer.getCurrentPosition();
                            total = audioPlayer.getDuration();
                        } catch (Exception ignored) {}
                    }
                }
                if (total > 0) {
                    seekBar.setMax(total);
                    seekBar.setProgress(current);
                    tvTimeDisplay.setText(formatTime(current) + " / " + formatTime(total));
                }
                progressHandler.postDelayed(this, 500);
            }
        }
    };

    private boolean isCurrentTrackVideo() {
        if (currentTrackIndex >= 0 && currentTrackIndex < playlist.size()) {
            return playlist.get(currentTrackIndex).isVideo;
        }
        return false;
    }

    public WMPWindowView(Context context) {
        super(context);
        init(context);
    }

    private void init(final Context context) {
        setOrientation(VERTICAL);
        setClickable(true);
        setBackgroundColor(0xFF0F1724); // WMP 11 dark slate background

        // 1. WMP 11 Navigation Header (Now Playing | Library | Rip | Burn | Sync)
        navBar = new LinearLayout(context);
        navBar.setOrientation(HORIZONTAL);
        navBar.setBackgroundColor(0xFF0A101A);
        navBar.setPadding(dpToPx(8), dpToPx(5), dpToPx(8), dpToPx(5));

        String[] tabs = new String[]{"Now Playing", "Library", "Rip", "Burn", "Sync"};
        for (int i = 0; i < tabs.length; i++) {
            TextView tv = new TextView(context);
            tv.setText(tabs[i]);
            tv.setTextSize(11);
            tv.setTextColor(i == 0 ? 0xFFFFFFFF : 0xFF7E92A9);
            tv.setPadding(dpToPx(10), dpToPx(3), dpToPx(10), dpToPx(3));
            if (i == 0) {
                tv.setBackgroundColor(0xFF1E3250);
            }
            navBar.addView(tv);
        }
        addView(navBar);

        // 2. Middle Area: Left = Visualizer / VideoView, Right = Soundtrack Playlist
        LinearLayout middleContainer = new LinearLayout(context);
        middleContainer.setOrientation(HORIZONTAL);
        LayoutParams middleParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f);
        middleContainer.setLayoutParams(middleParams);

        // Left Pane: Visualizer or VideoView
        visualizerContainer = new RelativeLayout(context);
        visualizerContainer.setClickable(true);
        LayoutParams stageParams = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.15f);
        visualizerContainer.setLayoutParams(stageParams);
        visualizerContainer.setBackgroundColor(0xFF030A18);

        // Video playback surface
        videoView = new VideoView(context);
        videoView.setClickable(true);
        RelativeLayout.LayoutParams videoLp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        videoLp.addRule(RelativeLayout.CENTER_IN_PARENT);
        videoView.setLayoutParams(videoLp);
        videoView.setVisibility(GONE);
        visualizerContainer.addView(videoView);

        // Retro Animated Spectrum Visualizer
        visualizerView = new VisualizerView(context);
        RelativeLayout.LayoutParams vizLp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        visualizerView.setLayoutParams(vizLp);
        visualizerView.setVisibility(VISIBLE);
        visualizerContainer.addView(visualizerView);

        middleContainer.addView(visualizerContainer);

        // Sleek Vertical Divider
        divider = new View(context);
        divider.setLayoutParams(new LayoutParams(dpToPx(1), ViewGroup.LayoutParams.MATCH_PARENT));
        divider.setBackgroundColor(0xFF090E17);
        middleContainer.addView(divider);

        // Right Pane: Playlist
        playlistContainer = new LinearLayout(context);
        playlistContainer.setOrientation(VERTICAL);
        LayoutParams playlistLp = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 0.85f);
        playlistContainer.setLayoutParams(playlistLp);
        playlistContainer.setBackgroundColor(0xFF0D1420);

        tvPlaylistHeader = new TextView(context);
        tvPlaylistHeader.setText("Now Playing - Playlist");
        tvPlaylistHeader.setTextColor(0xFFFFFFFF);
        tvPlaylistHeader.setTextSize(11);
        tvPlaylistHeader.setBackgroundColor(0xFF080D15);
        tvPlaylistHeader.setPadding(dpToPx(8), dpToPx(6), dpToPx(8), dpToPx(6));
        playlistContainer.addView(tvPlaylistHeader);

        lvPlaylist = new ListView(context);
        lvPlaylist.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        lvPlaylist.setBackgroundColor(0xFF0D1420);
        lvPlaylist.setDivider(null);
        playlistAdapter = new PlaylistAdapter();
        lvPlaylist.setAdapter(playlistAdapter);
        playlistContainer.addView(lvPlaylist);

        middleContainer.addView(playlistContainer);
        addView(middleContainer);

        // 3. Transport Control Panel (Authentic WMP 11 Dark Glass Look)
        transportBar = new LinearLayout(context);
        transportBar.setOrientation(VERTICAL);
        transportBar.setBackgroundResource(R.drawable.wmp11_transport_bg);
        transportBar.setPadding(dpToPx(8), dpToPx(6), dpToPx(8), dpToPx(6));

        // Track Name & Time Status
        LinearLayout infoBar = new LinearLayout(context);
        infoBar.setOrientation(HORIZONTAL);
        infoBar.setGravity(Gravity.CENTER_VERTICAL);

        tvTrackStatus = new TextView(context);
        tvTrackStatus.setText("Windows Media Player 11 - Ready");
        tvTrackStatus.setTextColor(0xFFFFFFFF);
        tvTrackStatus.setTextSize(11);
        LayoutParams statusLp = new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        tvTrackStatus.setLayoutParams(statusLp);
        infoBar.addView(tvTrackStatus);

        tvTimeDisplay = new TextView(context);
        tvTimeDisplay.setText("00:00 / 00:00");
        tvTimeDisplay.setTextColor(0xFF5CD2E6); // WMP 11 Cyan glowing clock
        tvTimeDisplay.setTextSize(11);
        infoBar.addView(tvTimeDisplay);

        transportBar.addView(infoBar);

        // Seeker Bar
        seekBar = new SeekBar(context);
        seekBar.setPadding(dpToPx(8), dpToPx(3), dpToPx(8), dpToPx(3));
        transportBar.addView(seekBar);

        // Action Buttons Row: [Open] [Prev] [▶ Play] [Next] [Stop]
        LinearLayout btnRow = new LinearLayout(context);
        btnRow.setOrientation(HORIZONTAL);
        btnRow.setGravity(Gravity.CENTER);
        btnRow.setPadding(0, dpToPx(3), 0, dpToPx(5));

        // Open File button
        btnOpenFile = new ImageView(context);
        btnOpenFile.setImageResource(R.drawable.ic_wmp_open);
        // btnOpenFile.setBackgroundResource(R.drawable.wmp11_circle_btn_bg);
        int padCircleSmall = dpToPx(6);
        btnOpenFile.setPadding(padCircleSmall, padCircleSmall, padCircleSmall, padCircleSmall);
        LayoutParams openLp = new LayoutParams(dpToPx(30), dpToPx(30));
        openLp.rightMargin = dpToPx(16);
        btnOpenFile.setLayoutParams(openLp);
        btnOpenFile.setClickable(true);
        btnOpenFile.setFocusable(true);
        btnRow.addView(btnOpenFile);

          // Circular Stop button (Authentic WMP 11)
        btnStop = new ImageView(context);
        btnStop.setImageResource(R.drawable.ic_wmp_stop);
        // btnStop.setBackgroundResource(R.drawable.wmp11_circle_btn_bg);
        btnStop.setPadding(padCircleSmall, padCircleSmall, padCircleSmall, padCircleSmall);
        LayoutParams stopLp = new LayoutParams(dpToPx(30), dpToPx(30));
        stopLp.leftMargin = dpToPx(16);
        btnStop.setLayoutParams(stopLp);
        btnStop.setClickable(true);
        btnStop.setFocusable(true);
        btnRow.addView(btnStop);

        // Circular Prev button (Authentic WMP 11)
        btnPrev = new ImageView(context);
        btnPrev.setImageResource(R.drawable.ic_wmp_prev);
        // btnPrev.setBackgroundResource(R.drawable.wmp11_circle_btn_bg);
        // int padCircle = dpToPx(8);
        // btnPrev.setPadding(padCircle, padCircle, padCircle, padCircle);
        LayoutParams prevLp = new LayoutParams(dpToPx(38), dpToPx(38));
        prevLp.rightMargin = dpToPx(-5);
        btnPrev.setLayoutParams(prevLp);
        btnPrev.setClickable(true);
        btnPrev.setFocusable(true);
        btnRow.addView(btnPrev);

        // Glowing Circular Play/Pause button (Authentic WMP 11)
        btnPlayPause = new ImageView(context);
        btnPlayPause.setImageResource(R.drawable.ic_wmp_play);
        btnPlayPause.setBackgroundResource(R.drawable.wmp11_play_bg);
        // int padPlay = dpToPx(1);
        // btnPlayPause.setPadding(padPlay, padPlay, padPlay, padPlay);
        LayoutParams playLp = new LayoutParams(dpToPx(50), dpToPx(50));
        btnPlayPause.setLayoutParams(playLp);
        btnPlayPause.setClickable(true);
        btnPlayPause.setFocusable(true);
        btnRow.addView(btnPlayPause);

        // Circular Next button (Authentic WMP 11)
        btnNext = new ImageView(context);
        btnNext.setImageResource(R.drawable.ic_wmp_next);
        // btnNext.setBackgroundResource(R.drawable.wmp11_circle_btn_bg);
        // btnNext.setPadding(padCircle, padCircle, padCircle, padCircle);
        LayoutParams nextLp = new LayoutParams(dpToPx(38), dpToPx(38));
        nextLp.leftMargin = dpToPx(-5);
        btnNext.setLayoutParams(nextLp);
        btnNext.setClickable(true);
        btnNext.setFocusable(true);
        btnRow.addView(btnNext);

      

        // Fullscreen / Immersive Button (Authentic WMP 11)
        btnFullscreen = new ImageView(context);
        btnFullscreen.setImageResource(R.drawable.ic_wmp_fullscreen);
        // btnFullscreen.setBackgroundResource(R.drawable.wmp11_circle_btn_bg);
        btnFullscreen.setPadding(padCircleSmall, padCircleSmall, padCircleSmall, padCircleSmall);
        LayoutParams fsLp = new LayoutParams(dpToPx(30), dpToPx(30));
        fsLp.leftMargin = dpToPx(16);
        btnFullscreen.setLayoutParams(fsLp);
        btnFullscreen.setClickable(true);
        btnFullscreen.setFocusable(true);
        btnRow.addView(btnFullscreen);

        transportBar.addView(btnRow);
        addView(transportBar);

        setupListeners(context);
        loadSoundtracks(context);
    }

    private void loadSoundtracks(Context context) {
        playlist.clear();
        ContentResolver cr = context.getContentResolver();

        // 1. Audio tracks from MediaStore (Internal)
        try {
            Uri internalUri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
            String[] projection = new String[]{
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.DURATION
            };
            Cursor cursor = cr.query(internalUri, projection,
                    MediaStore.Audio.Media.DURATION + " >= 3000",
                    null, MediaStore.Audio.Media.TITLE + " ASC");

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(0);
                    String title = cursor.getString(1);
                    String path = cursor.getString(2);
                    int dur = cursor.getInt(3);
                    Uri uri = Uri.withAppendedPath(internalUri, String.valueOf(id));
                    if (title != null && !title.trim().isEmpty()) {
                        playlist.add(new MediaTrack(id, title, path, dur, uri, false));
                    }
                }
                cursor.close();
            }
        } catch (Exception ignored) {}

        // 2. Audio tracks from MediaStore (External)
        try {
            Uri externalUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String[] projection = new String[]{
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.DURATION
            };
            Cursor cursor = cr.query(externalUri, projection, null, null, MediaStore.Audio.Media.TITLE + " ASC");
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(0);
                    String title = cursor.getString(1);
                    String path = cursor.getString(2);
                    int dur = cursor.getInt(3);
                    Uri uri = Uri.withAppendedPath(externalUri, String.valueOf(id));
                    if (title != null && !title.trim().isEmpty()) {
                        playlist.add(new MediaTrack(id, title, path, dur, uri, false));
                    }
                }
                cursor.close();
            }
        } catch (Exception ignored) {}

        // 3. Scan system media audio ringtones & notifications directory (ensuring all authentic tracks are present)
        String[] sysAudioDirs = new String[]{"/system/media/audio/ringtones", "/system/media/audio/notifications"};
        for (String dirPath : sysAudioDirs) {
            File aDir = new File(dirPath);
            if (aDir.exists() && aDir.listFiles() != null) {
                for (File f : aDir.listFiles()) {
                    String name = f.getName().toLowerCase();
                    if (name.endsWith(".ogg") || name.endsWith(".mp3") || name.endsWith(".wav")) {
                        String cleanTitle = f.getName().replace(".ogg", "").replace(".mp3", "").replace(".wav", "").replace("S_", "").replace("_", " ");
                        boolean exists = false;
                        for (MediaTrack t : playlist) {
                            if (f.getAbsolutePath().equals(t.path)) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            playlist.add(new MediaTrack(playlist.size(), cleanTitle, f.getAbsolutePath(), 30000, Uri.fromFile(f), false));
                        }
                    }
                }
            }
        }

        // 4. Scan local user audio directories (/sdcard/Music, /sdcard/Download)
        String[] userAudioDirs = new String[]{"/sdcard/Music", "/sdcard/Download"};
        for (String dirPath : userAudioDirs) {
            File uDir = new File(dirPath);
            if (uDir.exists() && uDir.listFiles() != null) {
                for (File f : uDir.listFiles()) {
                    String name = f.getName().toLowerCase();
                    if (name.endsWith(".mp3") || name.endsWith(".ogg") || name.endsWith(".wav") || name.endsWith(".m4a")) {
                        boolean exists = false;
                        for (MediaTrack t : playlist) {
                            if (f.getAbsolutePath().equals(t.path)) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            playlist.add(new MediaTrack(playlist.size(), f.getName(), f.getAbsolutePath(), 0, Uri.fromFile(f), false));
                        }
                    }
                }
            }
        }

        // 5. Video tracks from MediaStore (External & Internal)
        try {
            Uri extVideoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            String[] vProj = new String[]{
                    MediaStore.Video.Media._ID,
                    MediaStore.Video.Media.TITLE,
                    MediaStore.Video.Media.DATA,
                    MediaStore.Video.Media.DURATION
            };
            Cursor vCursor = cr.query(extVideoUri, vProj, null, null, MediaStore.Video.Media.TITLE + " ASC");
            if (vCursor != null) {
                while (vCursor.moveToNext()) {
                    long id = vCursor.getLong(0);
                    String title = vCursor.getString(1);
                    String path = vCursor.getString(2);
                    int dur = vCursor.getInt(3);
                    Uri uri = Uri.withAppendedPath(extVideoUri, String.valueOf(id));
                    if (title != null && !title.trim().isEmpty()) {
                        playlist.add(new MediaTrack(id, "[Video] " + title, path, dur, uri, true));
                    }
                }
                vCursor.close();
            }
        } catch (Exception ignored) {}

        // 6. Scan local video directories (/sdcard/Movies, /sdcard/DCIM, /sdcard/Download)
        String[] videoDirs = new String[]{"/sdcard/Movies", "/sdcard/DCIM/Camera", "/sdcard/Download", "/sdcard/Video"};
        for (String dirPath : videoDirs) {
            File vDir = new File(dirPath);
            if (vDir.exists() && vDir.listFiles() != null) {
                for (File f : vDir.listFiles()) {
                    String n = f.getName().toLowerCase();
                    if (n.endsWith(".mp4") || n.endsWith(".3gp") || n.endsWith(".mkv") || n.endsWith(".webm")) {
                        playlist.add(new MediaTrack(playlist.size(), "[Video] " + f.getName(), f.getAbsolutePath(), 0, Uri.fromFile(f), true));
                    }
                }
            }
        }

        tvPlaylistHeader.setText("Now Playing (" + playlist.size() + " items)");
        playlistAdapter.notifyDataSetChanged();

        // Select first track or Over the Horizon
        for (int i = 0; i < playlist.size(); i++) {
            if (playlist.get(i).title.toLowerCase().contains("over the horizon")) {
                currentTrackIndex = i;
                tvTrackStatus.setText(playlist.get(i).title);
                visualizerView.setTrackInfo(playlist.get(i).title, "Windows Media Player 11");
                return;
            }
        }
        if (!playlist.isEmpty()) {
            currentTrackIndex = 0;
            tvTrackStatus.setText(playlist.get(0).title);
            visualizerView.setTrackInfo(playlist.get(0).title, "Windows Media Player 11");
        }
    }

    private void playTrack(int index) {
        if (index < 0 || index >= playlist.size()) return;
        currentTrackIndex = index;
        MediaTrack track = playlist.get(index);
        tvTrackStatus.setText(track.title);

        if (track.isVideo) {
            if (audioPlayer != null && audioPlayer.isPlaying()) {
                try {
                    audioPlayer.stop();
                    audioPlayer.reset();
                } catch (Exception ignored) {}
            }
            visualizerView.setVisibility(GONE);
            visualizerView.setPlaying(false);
            videoView.setVisibility(VISIBLE);
            videoView.setVideoURI(track.uri);
            videoView.start();
            isPlaying = true;
            btnPlayPause.setImageResource(R.drawable.ic_wmp_pause);
            progressHandler.post(progressRunnable);
        } else {
            if (videoView != null && videoView.isPlaying()) {
                videoView.stopPlayback();
            }
            videoView.setVisibility(GONE);
            visualizerView.setVisibility(VISIBLE);
            visualizerView.setTrackInfo(track.title, "Windows Media Player 11");
            visualizerView.setPlaying(true);

            try {
                if (audioPlayer == null) {
                    audioPlayer = new MediaPlayer();
                    audioPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    audioPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            if (!playlist.isEmpty()) {
                                int next = (currentTrackIndex + 1) % playlist.size();
                                playTrack(next);
                            } else {
                                isPlaying = false;
                                btnPlayPause.setImageResource(R.drawable.ic_wmp_play);
                                visualizerView.setPlaying(false);
                            }
                        }
                    });
                    audioPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                        @Override
                        public boolean onError(MediaPlayer mp, int what, int extra) {
                            isPlaying = false;
                            btnPlayPause.setImageResource(R.drawable.ic_wmp_play);
                            visualizerView.setPlaying(false);
                            return true;
                        }
                    });
                } else {
                    audioPlayer.reset();
                }
                if (track.path != null && new File(track.path).exists()) {
                    audioPlayer.setDataSource(track.path);
                } else {
                    audioPlayer.setDataSource(getContext(), track.uri);
                }
                audioPlayer.prepare();
                audioPlayer.start();
                isPlaying = true;
                btnPlayPause.setImageResource(R.drawable.ic_wmp_pause);
                progressHandler.post(progressRunnable);
            } catch (Exception e) {
                e.printStackTrace();
                isPlaying = false;
                visualizerView.setPlaying(false);
                btnPlayPause.setImageResource(R.drawable.ic_wmp_play);
                Toast.makeText(getContext(), "Error playing audio: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        playlistAdapter.notifyDataSetChanged();
    }

    private void setupListeners(final Context context) {
        lvPlaylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                playTrack(position);
            }
        });

        btnPlayPause.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    if (isCurrentTrackVideo()) {
                        if (videoView != null) videoView.pause();
                    } else {
                        if (audioPlayer != null) audioPlayer.pause();
                    }
                    isPlaying = false;
                    btnPlayPause.setImageResource(R.drawable.ic_wmp_play);
                    visualizerView.setPlaying(false);
                } else {
                    if (currentTrackIndex >= 0 && currentTrackIndex < playlist.size()) {
                        boolean isVideo = playlist.get(currentTrackIndex).isVideo;
                        if (isVideo) {
                            if (videoView.getCurrentPosition() > 0) {
                                videoView.start();
                                isPlaying = true;
                                btnPlayPause.setImageResource(R.drawable.ic_wmp_pause);
                                progressHandler.post(progressRunnable);
                            } else {
                                playTrack(currentTrackIndex);
                            }
                        } else {
                            if (audioPlayer != null) {
                                try {
                                    audioPlayer.start();
                                    isPlaying = true;
                                    btnPlayPause.setImageResource(R.drawable.ic_wmp_pause);
                                    visualizerView.setPlaying(true);
                                    progressHandler.post(progressRunnable);
                                } catch (Exception e) {
                                    playTrack(currentTrackIndex);
                                }
                            } else {
                                playTrack(currentTrackIndex);
                            }
                        }
                    } else if (!playlist.isEmpty()) {
                        playTrack(0);
                    }
                }
            }
        });

        btnPrev.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playlist.isEmpty()) return;
                int prev = currentTrackIndex - 1;
                if (prev < 0) prev = playlist.size() - 1;
                playTrack(prev);
            }
        });

        btnNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playlist.isEmpty()) return;
                int next = (currentTrackIndex + 1) % playlist.size();
                playTrack(next);
            }
        });

        btnStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoView != null) {
                    videoView.stopPlayback();
                }
                if (audioPlayer != null) {
                    try {
                        audioPlayer.stop();
                        audioPlayer.reset();
                    } catch (Exception ignored) {}
                }
                isPlaying = false;
                btnPlayPause.setImageResource(R.drawable.ic_wmp_play);
                visualizerView.setPlaying(false);
                tvTimeDisplay.setText("00:00 / 00:00");
                seekBar.setProgress(0);
                progressHandler.removeCallbacks(progressRunnable);
            }
        });

        btnFullscreen.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFullscreen();
            }
        });

        visualizerContainer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isWmpFullscreen && transportBar != null) {
                    if (transportBar.getVisibility() == VISIBLE) {
                        transportBar.setVisibility(GONE);
                    } else {
                        transportBar.setVisibility(VISIBLE);
                    }
                }
            }
        });

        btnOpenFile.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] scanDirs = new String[]{"/sdcard/Music", "/sdcard/Movies", "/sdcard/Download"};
                for (String dir : scanDirs) {
                    File dirFile = new File(dir);
                    if (dirFile.exists() && dirFile.listFiles() != null) {
                        for (File f : dirFile.listFiles()) {
                            String n = f.getName().toLowerCase();
                            if (n.endsWith(".mp3") || n.endsWith(".wav") || n.endsWith(".mp4") || n.endsWith(".m4a") || n.endsWith(".ogg") || n.endsWith(".3gp")) {
                                playFile(f);
                                return;
                            }
                        }
                    }
                }
                Toast.makeText(context, "No media files found in /sdcard/Music or /sdcard/Movies", Toast.LENGTH_SHORT).show();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (isCurrentTrackVideo()) {
                        if (videoView != null) videoView.seekTo(progress);
                    } else {
                        if (audioPlayer != null) {
                            try {
                                audioPlayer.seekTo(progress);
                            } catch (Exception ignored) {}
                        }
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (!playlist.isEmpty()) {
                    int next = (currentTrackIndex + 1) % playlist.size();
                    playTrack(next);
                } else {
                    isPlaying = false;
                    btnPlayPause.setImageResource(R.drawable.ic_wmp_play);
                    visualizerView.setPlaying(false);
                }
            }
        });
    }

    public void toggleFullscreen() {
        isWmpFullscreen = !isWmpFullscreen;
        android.view.ViewParent p = getParent();
        while (p != null && !(p instanceof WindowFrameView)) {
            p = p.getParent();
        }
        WindowFrameView frame = (p instanceof WindowFrameView) ? (WindowFrameView) p : null;

        if (isWmpFullscreen) {
            if (navBar != null) navBar.setVisibility(GONE);
            if (playlistContainer != null) playlistContainer.setVisibility(GONE);
            if (divider != null) divider.setVisibility(GONE);
            if (frame != null && !frame.isImmersive()) {
                frame.toggleImmersive();
            }
        } else {
            if (navBar != null) navBar.setVisibility(VISIBLE);
            if (playlistContainer != null) playlistContainer.setVisibility(VISIBLE);
            if (divider != null) divider.setVisibility(VISIBLE);
            if (transportBar != null) transportBar.setVisibility(VISIBLE);
            if (frame != null && frame.isImmersive()) {
                frame.toggleImmersive();
            }
        }
    }

    public void playFile(File file) {
        if (file == null || !file.exists()) return;
        String name = file.getName().toLowerCase();
        boolean isVideo = name.endsWith(".mp4") || name.endsWith(".3gp") || name.endsWith(".mkv") || name.endsWith(".webm") || name.endsWith(".avi");
        MediaTrack customTrack = new MediaTrack(playlist.size(), (isVideo ? "[Video] " : "") + file.getName(), file.getAbsolutePath(), 0, Uri.fromFile(file), isVideo);
        playlist.add(customTrack);
        playlistAdapter.notifyDataSetChanged();
        playTrack(playlist.size() - 1);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        onDestroy();
    }

    public void onDestroy() {
        progressHandler.removeCallbacks(progressRunnable);
        if (visualizerView != null) {
            visualizerView.setPlaying(false);
        }
        if (videoView != null) {
            videoView.stopPlayback();
        }
        if (audioPlayer != null) {
            try {
                audioPlayer.stop();
                audioPlayer.release();
            } catch (Exception ignored) {}
            audioPlayer = null;
        }
    }

    private String formatTime(int ms) {
        int sec = (ms / 1000) % 60;
        int min = (ms / (1000 * 60)) % 60;
        return String.format("%02d:%02d", min, sec);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getContext().getResources().getDisplayMetrics().density + 0.5f);
    }

    private class PlaylistAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return playlist.size();
        }

        @Override
        public Object getItem(int position) {
            return playlist.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout row;
            if (convertView == null) {
                row = new LinearLayout(getContext());
                row.setOrientation(HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(dpToPx(8), dpToPx(5), dpToPx(8), dpToPx(5));

                TextView tvNum = new TextView(getContext());
                tvNum.setId(101);
                tvNum.setTextSize(10);
                tvNum.setTextColor(0xFF6B809B);
                tvNum.setWidth(dpToPx(24));
                row.addView(tvNum);

                LinearLayout textCol = new LinearLayout(getContext());
                textCol.setOrientation(VERTICAL);
                LayoutParams colLp = new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
                textCol.setLayoutParams(colLp);

                TextView tvTitle = new TextView(getContext());
                tvTitle.setId(102);
                tvTitle.setTextSize(11);
                tvTitle.setTextColor(0xFFFFFFFF);
                textCol.addView(tvTitle);

                TextView tvDuration = new TextView(getContext());
                tvDuration.setId(103);
                tvDuration.setTextSize(9);
                tvDuration.setTextColor(0xFF607792);
                textCol.addView(tvDuration);

                row.addView(textCol);
            } else {
                row = (LinearLayout) convertView;
            }

            MediaTrack track = playlist.get(position);
            TextView tvNum = (TextView) row.findViewById(101);
            TextView tvTitle = (TextView) row.findViewById(102);
            TextView tvDuration = (TextView) row.findViewById(103);

            tvNum.setText(String.valueOf(position + 1));
            tvTitle.setText(track.title);
            tvDuration.setText(track.duration > 0 ? formatTime(track.duration) : (track.isVideo ? "Video" : "Audio"));

            if (position == currentTrackIndex) {
                row.setBackgroundColor(0xFF1B3D6B); // WMP 11 active track dark blue glass highlight
                tvTitle.setTextColor(0xFF5CD2E6); // Cyan active title
            } else {
                row.setBackgroundColor(position % 2 == 0 ? 0xFF0E1624 : 0xFF0B121E);
                tvTitle.setTextColor(0xFFFFFFFF);
            }

            return row;
        }
    }
}
