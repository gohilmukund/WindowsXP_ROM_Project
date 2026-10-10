package com.windows.xp.mspaint;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PaintView extends View {

    public enum Tool {
        PENCIL, BRUSH, ERASER, LINE, RECTANGLE, ELLIPSE
    }

    private Tool currentTool = Tool.PENCIL;
    private int currentColor = Color.BLACK;

    private Bitmap bitmap;
    private Canvas bitmapCanvas;
    private final Path drawPath = new Path();
    private final Paint drawPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint canvasPaint = new Paint(Paint.DITHER_FLAG);

    private float startX, startY;
    private float lastX, lastY;

    public PaintView(Context context) {
        super(context);
        init();
    }

    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        drawPaint.setColor(currentColor);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        drawPaint.setStrokeWidth(3f);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            bitmapCanvas = new Canvas(bitmap);
            bitmapCanvas.drawColor(Color.WHITE);
        }
    }

    public void setTool(Tool tool) {
        this.currentTool = tool;
        updateStroke();
    }

    public void setColor(int color) {
        this.currentColor = color;
        updateStroke();
    }

    private void updateStroke() {
        drawPaint.setStyle(Paint.Style.STROKE);
        if (currentTool == Tool.ERASER) {
            drawPaint.setColor(Color.WHITE);
            drawPaint.setStrokeWidth(24f);
        } else if (currentTool == Tool.BRUSH) {
            drawPaint.setColor(currentColor);
            drawPaint.setStrokeWidth(12f);
        } else if (currentTool == Tool.PENCIL) {
            drawPaint.setColor(currentColor);
            drawPaint.setStrokeWidth(3f);
        } else {
            drawPaint.setColor(currentColor);
            drawPaint.setStrokeWidth(4f);
        }
    }

    public void clearCanvas() {
        if (bitmapCanvas != null) {
            bitmapCanvas.drawColor(Color.WHITE);
            invalidate();
        }
    }

    public void floodFill(int color) {
        this.currentColor = color;
        if (bitmapCanvas != null) {
            bitmapCanvas.drawColor(color);
            invalidate();
        }
    }

    public String saveImage(Context context) {
        if (bitmap == null) return null;
        try {
            File picturesDir = new File(Environment.getExternalStorageDirectory(), "Pictures");
            if (!picturesDir.exists()) picturesDir.mkdirs();

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            File imageFile = new File(picturesDir, "Paint_" + timeStamp + ".png");

            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();

            Toast.makeText(context, "Saved to " + imageFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
            return imageFile.getAbsolutePath();
        } catch (Exception e) {
            Toast.makeText(context, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, 0, 0, canvasPaint);
        }

        // Live preview of shape while dragging
        if (currentTool == Tool.LINE) {
            canvas.drawLine(startX, startY, lastX, lastY, drawPaint);
        } else if (currentTool == Tool.RECTANGLE) {
            canvas.drawRect(Math.min(startX, lastX), Math.min(startY, lastY),
                    Math.max(startX, lastX), Math.max(startY, lastY), drawPaint);
        } else if (currentTool == Tool.ELLIPSE) {
            RectF rect = new RectF(Math.min(startX, lastX), Math.min(startY, lastY),
                    Math.max(startX, lastX), Math.max(startY, lastY));
            canvas.drawOval(rect, drawPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = x;
                startY = y;
                lastX = x;
                lastY = y;
                if (currentTool == Tool.PENCIL || currentTool == Tool.BRUSH || currentTool == Tool.ERASER) {
                    drawPath.reset();
                    drawPath.moveTo(x, y);
                }
                invalidate();
                break;

            case MotionEvent.ACTION_MOVE:
                lastX = x;
                lastY = y;
                if (currentTool == Tool.PENCIL || currentTool == Tool.BRUSH || currentTool == Tool.ERASER) {
                    drawPath.quadTo(startX, startY, (x + startX) / 2, (y + startY) / 2);
                    startX = x;
                    startY = y;
                    if (bitmapCanvas != null) {
                        bitmapCanvas.drawPath(drawPath, drawPaint);
                        drawPath.reset();
                        drawPath.moveTo(x, y);
                    }
                }
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                lastX = x;
                lastY = y;
                if (bitmapCanvas != null) {
                    if (currentTool == Tool.PENCIL || currentTool == Tool.BRUSH || currentTool == Tool.ERASER) {
                        drawPath.lineTo(x, y);
                        bitmapCanvas.drawPath(drawPath, drawPaint);
                        drawPath.reset();
                    } else if (currentTool == Tool.LINE) {
                        bitmapCanvas.drawLine(startX, startY, x, y, drawPaint);
                    } else if (currentTool == Tool.RECTANGLE) {
                        bitmapCanvas.drawRect(Math.min(startX, x), Math.min(startY, y),
                                Math.max(startX, x), Math.max(startY, y), drawPaint);
                    } else if (currentTool == Tool.ELLIPSE) {
                        RectF rect = new RectF(Math.min(startX, x), Math.min(startY, y),
                                Math.max(startX, x), Math.max(startY, y));
                        bitmapCanvas.drawOval(rect, drawPaint);
                    }
                }
                invalidate();
                break;
        }
        return true;
    }
}
