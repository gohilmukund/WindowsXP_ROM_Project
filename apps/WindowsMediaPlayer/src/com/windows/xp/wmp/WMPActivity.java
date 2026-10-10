package com.windows.xp.wmp;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WMPActivity extends Activity {

    private VisualizerView visualizerView;
    private ListView playlistView;
    private SeekBar seekBar;
    private TextView timeCurrent;
    private TextView timeTotal;
    private ImageView btnPlayIcon;

    private MediaPlayer mediaPlayer;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isPlaying = false;

    private static class Track {
        String title;
        String artist;
        Uri uri;

        Track(String title, String artist, Uri uri) {
            this.title = title;
            this.artist = artist;
            this.uri = uri;
        }

        @Override
        public String toString() {
            return title + " - " + artist;
        }
    }

    private final List<Track> playlist = new ArrayList<Track>();
    private int currentTrackIndex = 0;

    private final Runnable seekRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && isPlaying) {
                int pos = mediaPlayer.getCurrentPosition();
                int dur = mediaPlayer.getDuration();
                if (dur > 0) {
                    seekBar.setProgress((pos * 100) / dur);
                    timeCurrent.setText(formatTime(pos));
                    timeTotal.setText(formatTime(dur));
                }
            }
            handler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wmp);

        initViews();
        setupWindowControls();
        setupPlaylist();
        setupTransport();

        // Load and prepare initial track
        if (!playlist.isEmpty()) {
            loadTrack(0, false);
        }

        handler.post(seekRunnable);
    }

    private void initViews() {
        visualizerView = (VisualizerView) findViewById(R.id.wmp_visualizer);
        playlistView = (ListView) findViewById(R.id.wmp_playlist);
        seekBar = (SeekBar) findViewById(R.id.wmp_seekbar);
        timeCurrent = (TextView) findViewById(R.id.wmp_time_current);
        timeTotal = (TextView) findViewById(R.id.wmp_time_total);
        btnPlayIcon = (ImageView) findViewById(R.id.wmp_btn_play_icon);
    }

    private void setupWindowControls() {
        findViewById(R.id.wmp_btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopPlayback();
                finish();
            }
        });

        findViewById(R.id.wmp_btn_minimize).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                minimizeWindow();
            }
        });

        findViewById(R.id.wmp_btn_maximize).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(WMPActivity.this, "Window maximized", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupPlaylist() {
        // Sample Track 1: Windows XP Welcome Sound
        Uri welcomeUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.welcome);
        playlist.add(new Track("Windows XP Welcome", "Microsoft Windows", welcomeUri));

        // Scan local storage for MP3/OGG audio files
        try {
            ContentResolver cr = getContentResolver();
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            Cursor cursor = cr.query(uri, null, MediaStore.Audio.Media.IS_MUSIC + " != 0", null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int titleCol = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                    int artistCol = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                    int dataCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);

                    String title = (titleCol != -1) ? cursor.getString(titleCol) : "Unknown";
                    String artist = (artistCol != -1) ? cursor.getString(artistCol) : "Unknown Artist";
                    String path = (dataCol != -1) ? cursor.getString(dataCol) : null;

                    if (path != null && new File(path).exists()) {
                        playlist.add(new Track(title, artist, Uri.fromFile(new File(path))));
                    }
                }
                cursor.close();
            }
        } catch (Exception ignored) {}

        ArrayAdapter<Track> adapter = new ArrayAdapter<Track>(
                this,
                android.R.layout.simple_list_item_1,
                playlist);
        playlistView.setAdapter(adapter);

        playlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                loadTrack(position, true);
            }
        });
    }

    private void setupTransport() {
        findViewById(R.id.wmp_btn_play_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePlayPause();
            }
        });

        findViewById(R.id.wmp_btn_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopPlayback();
            }
        });

        findViewById(R.id.wmp_btn_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playlist.isEmpty()) return;
                currentTrackIndex = (currentTrackIndex + 1) % playlist.size();
                loadTrack(currentTrackIndex, true);
            }
        });

        findViewById(R.id.wmp_btn_prev).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playlist.isEmpty()) return;
                currentTrackIndex = (currentTrackIndex - 1 + playlist.size()) % playlist.size();
                loadTrack(currentTrackIndex, true);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    int dur = mediaPlayer.getDuration();
                    if (dur > 0) {
                        int newPos = (dur * progress) / 100;
                        mediaPlayer.seekTo(newPos);
                    }
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar sb) {}
            @Override
            public void onStopTrackingTouch(SeekBar sb) {}
        });
    }

    private void loadTrack(int index, boolean autoPlay) {
        if (index < 0 || index >= playlist.size()) return;
        currentTrackIndex = index;
        Track track = playlist.get(index);

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        try {
            mediaPlayer = MediaPlayer.create(this, track.uri);
            if (mediaPlayer != null) {
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        // Advance to next track
                        currentTrackIndex = (currentTrackIndex + 1) % playlist.size();
                        loadTrack(currentTrackIndex, true);
                    }
                });

                int dur = mediaPlayer.getDuration();
                timeTotal.setText(formatTime(dur));
                timeCurrent.setText("00:00");
                seekBar.setProgress(0);
            }
        } catch (Exception ignored) {}

        visualizerView.setTrackInfo(track.title, track.artist);

        if (autoPlay) {
            startPlayback();
        } else {
            isPlaying = false;
            visualizerView.setPlaying(false);
            btnPlayIcon.setImageResource(R.drawable.ic_play);
        }
    }

    private void togglePlayPause() {
        if (isPlaying) {
            pausePlayback();
        } else {
            startPlayback();
        }
    }

    private void startPlayback() {
        if (mediaPlayer == null && !playlist.isEmpty()) {
            loadTrack(currentTrackIndex, false);
        }
        if (mediaPlayer != null) {
            mediaPlayer.start();
            isPlaying = true;
            visualizerView.setPlaying(true);
            btnPlayIcon.setImageResource(R.drawable.ic_pause);
        }
    }

    private void pausePlayback() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
        isPlaying = false;
        visualizerView.setPlaying(false);
        btnPlayIcon.setImageResource(R.drawable.ic_play);
    }

    private void stopPlayback() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);
        }
        isPlaying = false;
        visualizerView.setPlaying(false);
        btnPlayIcon.setImageResource(R.drawable.ic_play);
        seekBar.setProgress(0);
        timeCurrent.setText("00:00");
    }

    @Override
    public void onBackPressed() {
        // Hardware back button minimizes active window to taskbar (playback continues!)
        minimizeWindow();
    }

    private void minimizeWindow() {
        sendBroadcast(new Intent("com.windows.xp.ACTION_MINIMIZE"));
        moveTaskToBack(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(seekRunnable);
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private String formatTime(int ms) {
        int seconds = (ms / 1000) % 60;
        int minutes = (ms / (1000 * 60)) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
