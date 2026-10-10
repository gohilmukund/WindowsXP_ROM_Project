package com.windows.xp.launcher.window;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PaintWindowView extends LinearLayout {

    private SimpleDrawingCanvas canvasView;
    private View activeColorIndicator;

    // Authentic 28-color Windows XP Paint Palette
    private static final int[] COLORS = {
            0xFF000000, 0xFF7B7B7B, 0xFF840000, 0xFF848200, 0xFF008200, 0xFF008284, 0xFF000084, 0xFF840084,
            0xFF848242, 0xFF004142, 0xFF0082FF, 0xFF004184, 0xFF8400FF, 0xFF844100,
            0xFFFFFFFF, 0xFFB5B5B5, 0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF, 0xFF0000FF, 0xFFFF00FF,
            0xFFFFFF84, 0xFF00FF84, 0xFF84FFFF, 0xFF8482FF, 0xFFFF0084, 0xFFFF8242
    };

    public PaintWindowView(Context context) {
        super(context);
        init(context);
    }

    public PaintWindowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(final Context context) {
        setOrientation(VERTICAL);
        setBackgroundColor(0xFFECE9D8);

        // 1. Toolbar: Tools and Actions
        LinearLayout toolbar = new LinearLayout(context);
        toolbar.setOrientation(HORIZONTAL);
        toolbar.setBackgroundColor(0xFFECE9D8);
        toolbar.setPadding(dpToPx(6), dpToPx(3), dpToPx(6), dpToPx(3));

        TextView btnPencil = createToolButton(context, "Pencil");
        TextView btnBrush = createToolButton(context, "Brush");
        TextView btnEraser = createToolButton(context, "Eraser");
        TextView btnClear = createToolButton(context, "Clear");
        TextView btnSave = createToolButton(context, "Save PNG");

        toolbar.addView(btnPencil);
        toolbar.addView(btnBrush);
        toolbar.addView(btnEraser);
        toolbar.addView(btnClear);
        toolbar.addView(btnSave);
        addView(toolbar);

        // 2. Touch Drawing Canvas
        canvasView = new SimpleDrawingCanvas(context);
        LayoutParams canvasParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f);
        canvasParams.setMargins(dpToPx(4), dpToPx(2), dpToPx(4), dpToPx(2));
        canvasView.setLayoutParams(canvasParams);
        addView(canvasView);

        // 3. 28-Color Palette at bottom
        LinearLayout bottomBar = new LinearLayout(context);
        bottomBar.setOrientation(HORIZONTAL);
        bottomBar.setBackgroundColor(0xFFECE9D8);
        bottomBar.setPadding(dpToPx(6), dpToPx(4), dpToPx(6), dpToPx(4));

        // Active color indicator box
        activeColorIndicator = new View(context);
        LayoutParams indParams = new LayoutParams(dpToPx(28), dpToPx(28));
        indParams.rightMargin = dpToPx(8);
        activeColorIndicator.setLayoutParams(indParams);
        activeColorIndicator.setBackgroundColor(0xFF000000);
        bottomBar.addView(activeColorIndicator);

        // 2 rows of 14 color boxes
        LinearLayout paletteGrid = new LinearLayout(context);
        paletteGrid.setOrientation(VERTICAL);

        LinearLayout row1 = new LinearLayout(context);
        row1.setOrientation(HORIZONTAL);
        LinearLayout row2 = new LinearLayout(context);
        row2.setOrientation(HORIZONTAL);

        for (int i = 0; i < COLORS.length; i++) {
            final int color = COLORS[i];
            View colorBox = new View(context);
            LayoutParams p = new LayoutParams(dpToPx(14), dpToPx(14));
            p.setMargins(dpToPx(1), dpToPx(1), dpToPx(1), dpToPx(1));
            colorBox.setLayoutParams(p);
            colorBox.setBackgroundColor(color);
            colorBox.setClickable(true);
            colorBox.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    canvasView.setColor(color);
                    activeColorIndicator.setBackgroundColor(color);
                }
            });

            if (i < 14) {
                row1.addView(colorBox);
            } else {
                row2.addView(colorBox);
            }
        }

        paletteGrid.addView(row1);
        paletteGrid.addView(row2);
        bottomBar.addView(paletteGrid);
        addView(bottomBar);

        // Tool listeners
        btnPencil.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                canvasView.setStrokeWidth(dpToPx(2));
                canvasView.setEraser(false);
            }
        });

        btnBrush.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                canvasView.setStrokeWidth(dpToPx(8));
                canvasView.setEraser(false);
            }
        });

        btnEraser.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                canvasView.setEraser(true);
            }
        });

        btnClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                canvasView.clear();
            }
        });

        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBitmap();
            }
        });
    }

    private TextView createToolButton(Context context, String label) {
        TextView tv = new TextView(context);
        tv.setText(label);
        tv.setTextColor(0xFF000000);
        tv.setTextSize(12);
        tv.setBackgroundColor(0xFFE0DFD5);
        tv.setPadding(dpToPx(8), dpToPx(3), dpToPx(8), dpToPx(3));
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.rightMargin = dpToPx(6);
        tv.setLayoutParams(lp);
        tv.setClickable(true);
        return tv;
    }

    private void saveBitmap() {
        try {
            File dir = new File("/sdcard/Pictures");
            if (!dir.exists()) dir.mkdirs();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
            File f = new File(dir, "Paint_" + sdf.format(new Date()) + ".png");

            Bitmap bmp = canvasView.getBitmap();
            FileOutputStream fos = new FileOutputStream(f);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            Toast.makeText(getContext(), "Saved to Pictures/" + f.getName(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error saving image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    // Inner Custom Canvas View
    private static class SimpleDrawingCanvas extends View {
        private Bitmap bitmap;
        private Canvas canvas;
        private final Path path = new Path();
        private final Paint paint = new Paint();
        private final Paint canvasPaint = new Paint(Paint.DITHER_FLAG);
        private int currentColor = 0xFF000000;
        private boolean isEraser = false;

        public SimpleDrawingCanvas(Context context) {
            super(context);
            paint.setAntiAlias(true);
            paint.setColor(currentColor);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(4);
            setBackgroundColor(Color.WHITE);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            if (w > 0 && h > 0) {
                bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                canvas = new Canvas(bitmap);
                canvas.drawColor(Color.WHITE);
            }
        }

        @Override
        protected void onDraw(Canvas c) {
            if (bitmap != null) {
                c.drawBitmap(bitmap, 0, 0, canvasPaint);
            }
            c.drawPath(path, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    path.moveTo(x, y);
                    invalidate();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    path.lineTo(x, y);
                    invalidate();
                    return true;
                case MotionEvent.ACTION_UP:
                    canvas.drawPath(path, paint);
                    path.reset();
                    invalidate();
                    return true;
            }
            return false;
        }

        public void setColor(int color) {
            this.currentColor = color;
            this.isEraser = false;
            paint.setColor(color);
        }

        public void setStrokeWidth(float width) {
            paint.setStrokeWidth(width);
        }

        public void setEraser(boolean eraser) {
            this.isEraser = eraser;
            if (eraser) {
                paint.setColor(Color.WHITE);
                paint.setStrokeWidth(24);
            } else {
                paint.setColor(currentColor);
                paint.setStrokeWidth(4);
            }
        }

        public void clear() {
            if (canvas != null) {
                canvas.drawColor(Color.WHITE);
                invalidate();
            }
        }

        public Bitmap getBitmap() {
            return bitmap;
        }
    }
}
