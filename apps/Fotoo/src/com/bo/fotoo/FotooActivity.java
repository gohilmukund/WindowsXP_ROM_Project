package com.bo.fotoo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FotooActivity extends Activity {

    private ImageView imageView;
    private TextView titleText;
    private TextView clockText;
    private TextView filenameText;
    private View titleBar;
    private View toolbar;
    private Button btnSlideshow;

    private List<File> imageFiles = new ArrayList<>();
    private int currentIndex = 0;
    private int currentRotation = 0;
    private boolean isSlideshowRunning = false;
    private boolean isControlsVisible = true;

    private Handler handler = new Handler();
    private Runnable clockRunnable = new Runnable() {
        @Override
        public void run() {
            String timeStr = new SimpleDateFormat("hh:mm a", Locale.US).format(new Date());
            clockText.setText(timeStr);
            handler.postDelayed(this, 1000);
        }
    };

    private Runnable slideshowRunnable = new Runnable() {
        @Override
        public void run() {
            if (isSlideshowRunning && !imageFiles.isEmpty()) {
                nextImage();
                handler.postDelayed(this, 4000);
            }
        }
    };

    private static final String PICTURES_DIR = "/sdcard/Pictures";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fotoo);

        imageView = (ImageView) findViewById(R.id.fotoo_image);
        titleText = (TextView) findViewById(R.id.fotoo_title);
        clockText = (TextView) findViewById(R.id.fotoo_clock);
        filenameText = (TextView) findViewById(R.id.fotoo_filename);
        titleBar = findViewById(R.id.fotoo_title_bar);
        toolbar = findViewById(R.id.fotoo_toolbar);
        btnSlideshow = (Button) findViewById(R.id.fotoo_btn_slideshow);

        setupWindowControls();
        setupToolbarButtons();

        findViewById(R.id.fotoo_root).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleControls();
            }
        });

        handler.post(clockRunnable);
        loadImages();
    }

    private void setupWindowControls() {
        findViewById(R.id.fotoo_btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.fotoo_btn_minimize).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                minimizeWindow();
            }
        });

        findViewById(R.id.fotoo_btn_maximize).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleControls();
            }
        });
    }

    private void setupToolbarButtons() {
        findViewById(R.id.fotoo_btn_prev).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prevImage();
            }
        });

        findViewById(R.id.fotoo_btn_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextImage();
            }
        });

        btnSlideshow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSlideshow();
            }
        });

        findViewById(R.id.fotoo_btn_rotate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentRotation = (currentRotation + 90) % 360;
                displayCurrentImage();
            }
        });

        findViewById(R.id.fotoo_btn_fullscreen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleControls();
            }
        });
    }

    private void toggleControls() {
        isControlsVisible = !isControlsVisible;
        int visibility = isControlsVisible ? View.VISIBLE : View.GONE;
        titleBar.setVisibility(visibility);
        toolbar.setVisibility(visibility);
    }

    private void toggleSlideshow() {
        isSlideshowRunning = !isSlideshowRunning;
        if (isSlideshowRunning) {
            btnSlideshow.setText("⏹ Pause");
            Toast.makeText(this, "Slideshow started (4s interval)", Toast.LENGTH_SHORT).show();
            handler.postDelayed(slideshowRunnable, 4000);
        } else {
            btnSlideshow.setText("▶ Slideshow");
            handler.removeCallbacks(slideshowRunnable);
            Toast.makeText(this, "Slideshow paused", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadImages() {
        File dir = new File(PICTURES_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        imageFiles.clear();
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                String name = f.getName().toLowerCase();
                if (f.isFile() && (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".bmp") || name.endsWith(".webp"))) {
                    imageFiles.add(f);
                }
            }
        }

        if (imageFiles.isEmpty()) {
            createSamplePictures(dir);
            // Re-read
            files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isFile()) imageFiles.add(f);
                }
            }
        }

        if (!imageFiles.isEmpty()) {
            currentIndex = 0;
            displayCurrentImage();
        } else {
            filenameText.setText("No pictures found in " + PICTURES_DIR);
        }
    }

    private void createSamplePictures(File dir) {
        try {
            File bliss = new File(dir, "Windows XP Bliss.jpg");
            // If wallpaper exists, copy or create
            File wp = new File("/mnt/c/Users/gohil/Downloads/Samsung_Tab3_SM-T210_Tools/WindowsXP_ROM_Project/apps/LunaLauncher/res/drawable/bliss_wallpaper.png");
            if (wp.exists()) {
                Bitmap bmp = BitmapFactory.decodeFile(wp.getAbsolutePath());
                if (bmp != null) {
                    FileOutputStream fos = new FileOutputStream(bliss);
                    bmp.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                    fos.close();
                }
            }
        } catch (Exception ignored) {}
    }

    private void prevImage() {
        if (imageFiles.isEmpty()) return;
        currentIndex = (currentIndex - 1 + imageFiles.size()) % imageFiles.size();
        currentRotation = 0;
        displayCurrentImage();
    }

    private void nextImage() {
        if (imageFiles.isEmpty()) return;
        currentIndex = (currentIndex + 1) % imageFiles.size();
        currentRotation = 0;
        displayCurrentImage();
    }

    private void displayCurrentImage() {
        if (currentIndex < 0 || currentIndex >= imageFiles.size()) return;
        File current = imageFiles.get(currentIndex);
        try {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = 1;
            Bitmap bmp = BitmapFactory.decodeFile(current.getAbsolutePath(), opts);

            if (bmp != null) {
                if (currentRotation != 0) {
                    Matrix m = new Matrix();
                    m.postRotate(currentRotation);
                    bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, true);
                }
                imageView.setImageBitmap(bmp);
                titleText.setText(current.getName() + " - Windows Picture Viewer");
                filenameText.setText(current.getName() + " (" + (currentIndex + 1) + " of " + imageFiles.size() + ")");
            }
        } catch (Exception e) {
            filenameText.setText("Error loading: " + current.getName());
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            minimizeWindow();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        minimizeWindow();
    }

    private void minimizeWindow() {
        sendBroadcast(new Intent("com.windows.xp.ACTION_MINIMIZE"));
        moveTaskToBack(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(clockRunnable);
        handler.removeCallbacks(slideshowRunnable);
    }
}
