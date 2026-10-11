package com.windows.xp.launcher.window;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.windows.xp.launcher.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PictureViewerWindowView extends LinearLayout {

    private ImageView imageView;
    private TextView tvStatus;
    private ImageView btnPrev;
    private ImageView btnNext;
    private ImageView btnZoomIn;
    private ImageView btnZoomOut;
    private ImageView btnFit;
    private ImageView btnRotateLeft;
    private ImageView btnRotateRight;
    private ImageView btnSlideshow;
    private ImageView btnOpen;

    private final List<File> imageFiles = new ArrayList<File>();
    private int currentIndex = 0;
    private float currentRotation = 0f;
    private float currentScale = 1.0f;
    private boolean isFit = true;
    private boolean isSlideshowRunning = false;

    private final Handler slideHandler = new Handler();
    private final Runnable slideRunnable = new Runnable() {
        @Override
        public void run() {
            if (isSlideshowRunning && !imageFiles.isEmpty()) {
                showNextImage();
                slideHandler.postDelayed(this, 3000);
            }
        }
    };

    public PictureViewerWindowView(Context context) {
        super(context);
        init(context);
    }

    private void init(final Context context) {
        setOrientation(VERTICAL);
        setClickable(true);
        setBackgroundColor(0xFFECE9D8); // Windows XP classic gray

        // 1. Stage Area (Dark neutral picture canvas)
        RelativeLayout stageLayout = new RelativeLayout(context);
        stageLayout.setClickable(true);
        LayoutParams stageParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f);
        stageLayout.setLayoutParams(stageParams);
        stageLayout.setBackgroundColor(0xFF282828);

        imageView = new ImageView(context);
        imageView.setClickable(true);
        RelativeLayout.LayoutParams ivParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ivParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        imageView.setLayoutParams(ivParams);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        stageLayout.addView(imageView);

        addView(stageLayout);

        // 2. Status Information Bar
        tvStatus = new TextView(context);
        tvStatus.setText("Windows Picture and Fax Viewer - No pictures loaded");
        tvStatus.setTextColor(0xFF333333);
        tvStatus.setTextSize(11);
        tvStatus.setPadding(dpToPx(8), dpToPx(3), dpToPx(8), dpToPx(3));
        tvStatus.setBackgroundColor(0xFFD4D0C8);
        addView(tvStatus);

        // 3. Toolbar Container (Authentic XP Picture and Fax Viewer Toolbar)
        LinearLayout toolbar = new LinearLayout(context);
        toolbar.setOrientation(HORIZONTAL);
        toolbar.setGravity(Gravity.CENTER);
        toolbar.setBackgroundColor(0xFFECE9D8);
        toolbar.setPadding(dpToPx(4), dpToPx(6), dpToPx(4), dpToPx(6));

        // Prev & Next Buttons
        btnPrev = createToolButton(context, R.drawable.ic_pv_prev, "Previous");
        btnNext = createToolButton(context, R.drawable.ic_pv_next, "Next");
        toolbar.addView(btnPrev);
        toolbar.addView(btnNext);
        toolbar.addView(createSeparator(context));

        // Zoom In, Zoom Out, Best Fit
        btnZoomIn = createToolButton(context, R.drawable.ic_pv_zoom_in, "Zoom In");
        btnZoomOut = createToolButton(context, R.drawable.ic_pv_zoom_out, "Zoom Out");
        btnFit = createToolButton(context, R.drawable.ic_pv_fit, "Best Fit");
        toolbar.addView(btnZoomIn);
        toolbar.addView(btnZoomOut);
        toolbar.addView(btnFit);
        toolbar.addView(createSeparator(context));

        // Rotate Counterclockwise & Clockwise
        btnRotateLeft = createToolButton(context, R.drawable.ic_pv_rotate_left, "Rotate Counterclockwise");
        btnRotateRight = createToolButton(context, R.drawable.ic_pv_rotate_right, "Rotate Clockwise");
        toolbar.addView(btnRotateLeft);
        toolbar.addView(btnRotateRight);
        toolbar.addView(createSeparator(context));

        // Slideshow & Open Folder
        btnSlideshow = createToolButton(context, R.drawable.ic_pv_slideshow, "Slideshow");
        btnOpen = createToolButton(context, R.drawable.ic_pv_open, "Open Pictures Folder");
        toolbar.addView(btnSlideshow);
        toolbar.addView(btnOpen);

        addView(toolbar);

        setupListeners(context);
        loadDefaultImages();
    }

    private ImageView createToolButton(Context context, int resId, String tooltip) {
        ImageView iv = new ImageView(context);
        iv.setImageResource(resId);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        iv.setBackgroundResource(R.drawable.pv_btn_bg);
        int pad = dpToPx(4);
        iv.setPadding(pad, pad, pad, pad);
        LayoutParams lp = new LayoutParams(dpToPx(34), dpToPx(34));
        lp.leftMargin = dpToPx(3);
        lp.rightMargin = dpToPx(3);
        iv.setLayoutParams(lp);
        iv.setClickable(true);
        iv.setFocusable(true);
        return iv;
    }

    private View createSeparator(Context context) {
        View sep = new View(context);
        LayoutParams lp = new LayoutParams(dpToPx(1), dpToPx(24));
        lp.leftMargin = dpToPx(5);
        lp.rightMargin = dpToPx(5);
        lp.gravity = Gravity.CENTER_VERTICAL;
        sep.setLayoutParams(lp);
        sep.setBackgroundColor(0xFFB0A898);
        return sep;
    }

    private void setupListeners(final Context context) {
        btnPrev.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showPrevImage();
            }
        });

        btnNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showNextImage();
            }
        });

        btnZoomIn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                currentScale *= 1.25f;
                applyTransforms();
            }
        });

        btnZoomOut.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                currentScale /= 1.25f;
                applyTransforms();
            }
        });

        btnFit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                currentScale = 1.0f;
                currentRotation = 0f;
                isFit = true;
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                applyTransforms();
            }
        });

        btnRotateRight.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                currentRotation = (currentRotation + 90f) % 360f;
                applyTransforms();
            }
        });

        btnRotateLeft.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                currentRotation = (currentRotation - 90f + 360f) % 360f;
                applyTransforms();
            }
        });

        btnSlideshow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSlideshowRunning) {
                    isSlideshowRunning = false;
                    btnSlideshow.clearColorFilter();
                    slideHandler.removeCallbacks(slideRunnable);
                    Toast.makeText(context, "Slideshow stopped", Toast.LENGTH_SHORT).show();
                } else {
                    isSlideshowRunning = true;
                    btnSlideshow.setColorFilter(0xFF316AC5);
                    slideHandler.post(slideRunnable);
                    Toast.makeText(context, "Slideshow running", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnOpen.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                scanPicturesDir(new File("/sdcard/Pictures"));
                if (imageFiles.isEmpty()) {
                    scanPicturesDir(new File("/sdcard/DCIM"));
                }
            }
        });
    }

    private void loadDefaultImages() {
        // Scan standard Pictures, DCIM, and wallpapers
        scanPicturesDir(new File("/sdcard/Pictures"));
        if (imageFiles.isEmpty()) {
            scanPicturesDir(new File("/sdcard/DCIM"));
        }
        if (imageFiles.isEmpty()) {
            imageView.setImageResource(R.drawable.bliss_wallpaper);
            tvStatus.setText("Bliss - Windows XP Wallpaper (Default)");
        }
    }

    public void scanPicturesDir(File dir) {
        imageFiles.clear();
        if (dir.exists() && dir.listFiles() != null) {
            for (File f : dir.listFiles()) {
                String n = f.getName().toLowerCase();
                if (n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".png") || n.endsWith(".webp") || n.endsWith(".bmp")) {
                    imageFiles.add(f);
                }
            }
        }
        if (!imageFiles.isEmpty()) {
            currentIndex = 0;
            displayCurrentImage();
        }
    }

    public void openImage(File file) {
        if (file == null || !file.exists()) return;
        imageFiles.clear();
        imageFiles.add(file);
        // Also add sibling images in same folder
        File parent = file.getParentFile();
        if (parent != null && parent.listFiles() != null) {
            for (File f : parent.listFiles()) {
                String n = f.getName().toLowerCase();
                if (!f.equals(file) && (n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".png") || n.endsWith(".webp"))) {
                    imageFiles.add(f);
                }
            }
        }
        currentIndex = 0;
        displayCurrentImage();
    }

    private void showNextImage() {
        if (imageFiles.isEmpty()) return;
        currentIndex = (currentIndex + 1) % imageFiles.size();
        displayCurrentImage();
    }

    private void showPrevImage() {
        if (imageFiles.isEmpty()) return;
        currentIndex = (currentIndex - 1 + imageFiles.size()) % imageFiles.size();
        displayCurrentImage();
    }

    private void displayCurrentImage() {
        if (currentIndex < 0 || currentIndex >= imageFiles.size()) return;
        File f = imageFiles.get(currentIndex);
        try {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = 1;
            // Downsample if file is massive
            if (f.length() > 2 * 1024 * 1024) {
                opts.inSampleSize = 2;
            }
            Bitmap bmp = BitmapFactory.decodeFile(f.getAbsolutePath(), opts);
            if (bmp != null) {
                imageView.setImageBitmap(bmp);
                tvStatus.setText(String.format("%s (%d of %d) - %dx%d",
                        f.getName(), currentIndex + 1, imageFiles.size(), bmp.getWidth(), bmp.getHeight()));
                currentScale = 1.0f;
                currentRotation = 0f;
                applyTransforms();
            }
        } catch (Exception e) {
            tvStatus.setText("Error loading: " + f.getName());
        }
    }

    private void applyTransforms() {
        imageView.setRotation(currentRotation);
        imageView.setScaleX(currentScale);
        imageView.setScaleY(currentScale);
    }

    public void onDestroy() {
        slideHandler.removeCallbacks(slideRunnable);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getContext().getResources().getDisplayMetrics().density + 0.5f);
    }
}
