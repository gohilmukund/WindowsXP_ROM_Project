package com.windows.xp.minesweeper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.Random;

public class MinefieldView extends View {

    public interface OnGameEventListener {
        void onMinesLeftChanged(int count);
        void onTimerStart();
        void onGameWon();
        void onGameLost();
        void onSmileyStateChange(String smiley);
    }

    public enum Difficulty {
        BEGINNER(9, 9, 10),
        INTERMEDIATE(16, 16, 40),
        EXPERT(30, 16, 99);

        public final int cols;
        public final int rows;
        public final int mines;

        Difficulty(int cols, int rows, int mines) {
            this.cols = cols;
            this.rows = rows;
            this.mines = mines;
        }
    }

    private static class Cell {
        boolean isMine = false;
        boolean isRevealed = false;
        boolean isFlagged = false;
        boolean isExploded = false;
        int adjacentMines = 0;
    }

    private Difficulty currentDifficulty = Difficulty.INTERMEDIATE;
    private int cols = 16;
    private int rows = 16;
    private int totalMines = 40;
    private Cell[][] grid;

    private boolean isFirstClick = true;
    private boolean isGameOver = false;
    private boolean isGameWon = false;

    private float cellSize = 30f;
    private float gridOffsetX = 0f;
    private float gridOffsetY = 0f;

    private Paint fillPaint;
    private Paint lineLightPaint;
    private Paint lineDarkPaint;
    private Paint textPaint;
    private Paint minePaint;
    private Paint flagPaint;

    private GestureDetector gestureDetector;
    private OnGameEventListener listener;

    private static final int[] NUMBER_COLORS = new int[]{
            0,
            0xFF0000FF, // 1: Blue
            0xFF008000, // 2: Green
            0xFFFF0000, // 3: Red
            0xFF000080, // 4: Navy
            0xFF800000, // 5: Maroon
            0xFF008080, // 6: Teal
            0xFF000000, // 7: Black
            0xFF808080  // 8: Gray
    };

    public MinefieldView(Context context) {
        super(context);
        init(context);
    }

    public MinefieldView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        fillPaint = new Paint();
        lineLightPaint = new Paint();
        lineLightPaint.setColor(0xFFFFFFFF);
        lineLightPaint.setStrokeWidth(2f);

        lineDarkPaint = new Paint();
        lineDarkPaint.setColor(0xFF808080);
        lineDarkPaint.setStrokeWidth(2f);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        minePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        minePaint.setColor(0xFF000000);

        flagPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        flagPaint.setColor(0xFFFF0000);

        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                int col = (int) ((e.getX() - gridOffsetX) / cellSize);
                int row = (int) ((e.getY() - gridOffsetY) / cellSize);
                handleTap(col, row);
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                int col = (int) ((e.getX() - gridOffsetX) / cellSize);
                int row = (int) ((e.getY() - gridOffsetY) / cellSize);
                handleLongPress(col, row);
            }
        });

        startNewGame(Difficulty.INTERMEDIATE);
    }

    public void setOnGameEventListener(OnGameEventListener listener) {
        this.listener = listener;
        if (listener != null) {
            listener.onMinesLeftChanged(totalMines);
        }
    }

    public void startNewGame(Difficulty diff) {
        this.currentDifficulty = diff;
        this.cols = diff.cols;
        this.rows = diff.rows;
        this.totalMines = diff.mines;

        grid = new Cell[cols][rows];
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                grid[c][r] = new Cell();
            }
        }

        isFirstClick = true;
        isGameOver = false;
        isGameWon = false;

        if (listener != null) {
            listener.onMinesLeftChanged(totalMines);
            listener.onSmileyStateChange("🙂");
        }

        requestLayout();
        invalidate();
    }

    public void restartCurrentGame() {
        startNewGame(currentDifficulty);
    }

    private void generateMines(int safeCol, int safeRow) {
        Random rnd = new Random();
        int placed = 0;
        while (placed < totalMines) {
            int c = rnd.nextInt(cols);
            int r = rnd.nextInt(rows);
            // Ensure first clicked cell and its immediate neighbors are safe
            if (Math.abs(c - safeCol) <= 1 && Math.abs(r - safeRow) <= 1) {
                continue;
            }
            if (!grid[c][r].isMine) {
                grid[c][r].isMine = true;
                placed++;
            }
        }

        // Calculate adjacent counts
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                if (grid[c][r].isMine) continue;
                int count = 0;
                for (int dc = -1; dc <= 1; dc++) {
                    for (int dr = -1; dr <= 1; dr++) {
                        int nc = c + dc;
                        int nr = r + dr;
                        if (nc >= 0 && nc < cols && nr >= 0 && nr < rows && grid[nc][nr].isMine) {
                            count++;
                        }
                    }
                }
                grid[c][r].adjacentMines = count;
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float sizeX = (float) w / cols;
        float sizeY = (float) h / rows;
        cellSize = Math.min(sizeX, sizeY);

        gridOffsetX = (w - (cellSize * cols)) / 2f;
        gridOffsetY = (h - (cellSize * rows)) / 2f;
        textPaint.setTextSize(cellSize * 0.7f);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isGameOver || isGameWon) return false;

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (listener != null) listener.onSmileyStateChange("😮");
        } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            if (listener != null && !isGameOver && !isGameWon) listener.onSmileyStateChange("🙂");
        }

        return gestureDetector.onTouchEvent(event) || true;
    }

    private void handleTap(int col, int row) {
        if (col < 0 || col >= cols || row < 0 || row >= rows) return;
        if (isGameOver || isGameWon) return;

        Cell cell = grid[col][row];
        if (cell.isFlagged) return;

        if (cell.isRevealed) {
            // Chording: if touching revealed cell and flagged count equals adjacent mines, reveal neighbors
            if (cell.adjacentMines > 0) {
                chordCell(col, row);
            }
            return;
        }

        if (isFirstClick) {
            generateMines(col, row);
            isFirstClick = false;
            if (listener != null) listener.onTimerStart();
        }

        if (cell.isMine) {
            // Exploded mine!
            cell.isRevealed = true;
            cell.isExploded = true;
            isGameOver = true;
            revealAllMines();
            if (listener != null) {
                listener.onSmileyStateChange("😵");
                listener.onGameLost();
            }
            invalidate();
            return;
        }

        revealCell(col, row);
        checkWinCondition();
        invalidate();
    }

    private void handleLongPress(int col, int row) {
        if (col < 0 || col >= cols || row < 0 || row >= rows) return;
        if (isGameOver || isGameWon) return;

        Cell cell = grid[col][row];
        if (cell.isRevealed) return;

        cell.isFlagged = !cell.isFlagged;

        int remainingMines = totalMines;
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                if (grid[c][r].isFlagged) remainingMines--;
            }
        }
        if (listener != null) {
            listener.onMinesLeftChanged(remainingMines);
        }

        checkWinCondition();
        invalidate();
    }

    private void chordCell(int c, int r) {
        Cell cell = grid[c][r];
        int flagCount = 0;
        for (int dc = -1; dc <= 1; dc++) {
            for (int dr = -1; dr <= 1; dr++) {
                int nc = c + dc;
                int nr = r + dr;
                if (nc >= 0 && nc < cols && nr >= 0 && nr < rows && grid[nc][nr].isFlagged) {
                    flagCount++;
                }
            }
        }

        if (flagCount == cell.adjacentMines) {
            for (int dc = -1; dc <= 1; dc++) {
                for (int dr = -1; dr <= 1; dr++) {
                    int nc = c + dc;
                    int nr = r + dr;
                    if (nc >= 0 && nc < cols && nr >= 0 && nr < rows) {
                        Cell neighbor = grid[nc][nr];
                        if (!neighbor.isRevealed && !neighbor.isFlagged) {
                            if (neighbor.isMine) {
                                neighbor.isRevealed = true;
                                neighbor.isExploded = true;
                                isGameOver = true;
                                revealAllMines();
                                if (listener != null) {
                                    listener.onSmileyStateChange("😵");
                                    listener.onGameLost();
                                }
                                invalidate();
                                return;
                            } else {
                                revealCell(nc, nr);
                            }
                        }
                    }
                }
            }
            checkWinCondition();
            invalidate();
        }
    }

    private void revealCell(int c, int r) {
        if (c < 0 || c >= cols || r < 0 || r >= rows) return;
        Cell cell = grid[c][r];
        if (cell.isRevealed || cell.isFlagged) return;

        cell.isRevealed = true;

        if (cell.adjacentMines == 0 && !cell.isMine) {
            for (int dc = -1; dc <= 1; dc++) {
                for (int dr = -1; dr <= 1; dr++) {
                    if (dc != 0 || dr != 0) {
                        revealCell(c + dc, r + dr);
                    }
                }
            }
        }
    }

    private void revealAllMines() {
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                if (grid[c][r].isMine) {
                    grid[c][r].isRevealed = true;
                }
            }
        }
    }

    private void checkWinCondition() {
        int unrevealedSafeCells = 0;
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                if (!grid[c][r].isMine && !grid[c][r].isRevealed) {
                    unrevealedSafeCells++;
                }
            }
        }

        if (unrevealedSafeCells == 0) {
            isGameWon = true;
            for (int c = 0; c < cols; c++) {
                for (int r = 0; r < rows; r++) {
                    if (grid[c][r].isMine) grid[c][r].isFlagged = true;
                }
            }
            if (listener != null) {
                listener.onMinesLeftChanged(0);
                listener.onSmileyStateChange("😎");
                listener.onGameWon();
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                float left = gridOffsetX + (c * cellSize);
                float top = gridOffsetY + (r * cellSize);
                float right = left + cellSize;
                float bottom = top + cellSize;

                Cell cell = grid[c][r];

                if (!cell.isRevealed) {
                    // Raised 3D bevel
                    fillPaint.setColor(0xFFC0C0C0);
                    canvas.drawRect(left, top, right, bottom, fillPaint);

                    canvas.drawLine(left, top, right, top, lineLightPaint);
                    canvas.drawLine(left, top, left, bottom, lineLightPaint);
                    canvas.drawLine(right, top, right, bottom, lineDarkPaint);
                    canvas.drawLine(left, bottom, right, bottom, lineDarkPaint);

                    if (cell.isFlagged) {
                        drawFlag(canvas, left, top);
                    }
                } else {
                    // Sunken 3D bevel
                    if (cell.isExploded) {
                        fillPaint.setColor(0xFFFF0000); // Red explosion
                    } else {
                        fillPaint.setColor(0xFFC0C0C0);
                    }
                    canvas.drawRect(left, top, right, bottom, fillPaint);

                    canvas.drawLine(left, top, right, top, lineDarkPaint);
                    canvas.drawLine(left, top, left, bottom, lineDarkPaint);

                    if (cell.isMine) {
                        drawMine(canvas, left, top);
                    } else if (cell.adjacentMines > 0) {
                        textPaint.setColor(NUMBER_COLORS[cell.adjacentMines]);
                        float textX = left + (cellSize / 2f);
                        float textY = top + (cellSize / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f);
                        canvas.drawText(String.valueOf(cell.adjacentMines), textX, textY, textPaint);
                    }
                }
            }
        }
    }

    private void drawFlag(Canvas canvas, float left, float top) {
        float cx = left + (cellSize / 2f);
        float cy = top + (cellSize / 2f);
        float s = cellSize * 0.6f;

        // Black flagpole
        canvas.drawLine(cx - 2, cy - (s / 2), cx - 2, cy + (s / 2), lineDarkPaint);
        // Base
        canvas.drawLine(cx - (s / 3), cy + (s / 2), cx + (s / 3), cy + (s / 2), lineDarkPaint);

        // Red pennant flag
        Path path = new Path();
        path.moveTo(cx - 2, cy - (s / 2));
        path.lineTo(cx + (s / 3), cy - (s / 4));
        path.lineTo(cx - 2, cy);
        path.close();
        canvas.drawPath(path, flagPaint);
    }

    private void drawMine(Canvas canvas, float left, float top) {
        float cx = left + (cellSize / 2f);
        float cy = top + (cellSize / 2f);
        float r = cellSize * 0.28f;

        // Mine body
        canvas.drawCircle(cx, cy, r, minePaint);

        // Spikes
        float spikeLen = cellSize * 0.38f;
        canvas.drawLine(cx - spikeLen, cy, cx + spikeLen, cy, minePaint);
        canvas.drawLine(cx, cy - spikeLen, cx, cy + spikeLen, minePaint);
        canvas.drawLine(cx - (r * 1.2f), cy - (r * 1.2f), cx + (r * 1.2f), cy + (r * 1.2f), minePaint);
        canvas.drawLine(cx - (r * 1.2f), cy + (r * 1.2f), cx + (r * 1.2f), cy - (r * 1.2f), minePaint);

        // White highlight
        Paint white = new Paint();
        white.setColor(Color.WHITE);
        canvas.drawRect(cx - (r * 0.5f), cy - (r * 0.5f), cx - (r * 0.1f), cy - (r * 0.1f), white);
    }
}
