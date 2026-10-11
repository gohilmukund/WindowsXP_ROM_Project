package com.windows.xp.launcher.window;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

public class VisualizerView extends View {

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint peakPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint wavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final int NUM_BARS = 24;
    private final float[] barHeights = new float[NUM_BARS];
    private final float[] peakHeights = new float[NUM_BARS];
    private final Random random = new Random();

    private boolean isPlaying = false;
    private String trackTitle = "Windows XP - Audio Track";
    private String trackArtist = "Windows Media Player";
    private int animTick = 0;

    public VisualizerView(Context context) {
        super(context);
        init();
    }

    public VisualizerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        barPaint.setStyle(Paint.Style.FILL);
        peakPaint.setColor(0xFFFFEE44);
        peakPaint.setStyle(Paint.Style.FILL);

        wavePaint.setColor(0x8000E5FF);
        wavePaint.setStyle(Paint.Style.STROKE);
        wavePaint.setStrokeWidth(2.5f);

        textPaint.setColor(0xFFFFFFFF);
        textPaint.setTextSize(20);
        textPaint.setFakeBoldText(true);
        textPaint.setShadowLayer(4, 1, 1, 0xFF000000);
    }

    public void setPlaying(boolean playing) {
        this.isPlaying = playing;
        invalidate();
    }

    public void setTrackInfo(String title, String artist) {
        this.trackTitle = (title != null && !title.isEmpty()) ? title : "Audio Track";
        this.trackArtist = (artist != null && !artist.isEmpty()) ? artist : "Windows Media Player";
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        // Dark background (WMP 11 dark stage)
        canvas.drawColor(0xFF030A18);

        // Draw track text overlay in top left
        canvas.drawText(trackTitle, 24, 38, textPaint);
        Paint subPaint = new Paint(textPaint);
        subPaint.setTextSize(14);
        subPaint.setColor(0xFFA0C0E0);
        canvas.drawText(trackArtist, 24, 60, subPaint);

        // Visualizer baseline
        float baseline = h - 30;
        float barWidth = (w - 60f) / NUM_BARS;

        if (isPlaying) {
            animTick++;
            for (int i = 0; i < NUM_BARS; i++) {
                // Harmonic wave + noise
                float wave = (float) Math.sin((animTick * 0.15f) + (i * 0.4f)) * 0.5f + 0.5f;
                float target = (wave * (h * 0.55f)) + (random.nextFloat() * (h * 0.2f));

                // Smooth approach
                barHeights[i] = (barHeights[i] * 0.7f) + (target * 0.3f);
                if (barHeights[i] > peakHeights[i]) {
                    peakHeights[i] = barHeights[i];
                } else {
                    peakHeights[i] = Math.max(0, peakHeights[i] - 3.5f);
                }
            }
        } else {
            // Decay bars to 0 when paused
            for (int i = 0; i < NUM_BARS; i++) {
                barHeights[i] = Math.max(0, barHeights[i] * 0.85f);
                peakHeights[i] = Math.max(0, peakHeights[i] - 4f);
            }
        }

        // Draw retro frequency spectrum bars (green with yellow top)
        for (int i = 0; i < NUM_BARS; i++) {
            float left = 30 + (i * barWidth) + 3;
            float right = left + barWidth - 6;
            float top = baseline - barHeights[i];

            // Green spectrum bar
            barPaint.setColor(0xFF00E640);
            canvas.drawRect(left, top, right, baseline, barPaint);

            // Yellow/orange peak dot
            if (peakHeights[i] > 2) {
                float peakTop = baseline - peakHeights[i] - 4;
                canvas.drawRect(left, peakTop, right, peakTop + 3, peakPaint);
            }
        }

        // Draw glowing oscilloscope waveform line across center
        float centerY = (h / 2f) + 20;
        for (int x = 20; x < w - 20; x += 4) {
            float yOffset = 0;
            if (isPlaying) {
                yOffset = (float) Math.sin((animTick * 0.2f) + (x * 0.03f)) * 18f;
            }
            canvas.drawCircle(x, centerY + yOffset, 1.5f, wavePaint);
        }

        if (isPlaying || barHeights[0] > 1) {
            postInvalidateDelayed(33); // ~30 fps loop
        }
    }
}
