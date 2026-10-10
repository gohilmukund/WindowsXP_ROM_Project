package com.windows.xp.mspaint;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

public class PaintActivity extends Activity {

    private PaintView paintCanvas;
    private View colorSelectedPreview;

    private static final int[] PALETTE_ROW1 = new int[]{
            0xFF000000, 0xFF787878, 0xFF780000, 0xFF787800, 0xFF007800, 0xFF007878, 0xFF000078,
            0xFF780078, 0xFF787838, 0xFF003838, 0xFF3878F8, 0xFF003878, 0xFF7800F8, 0xFF783800
    };

    private static final int[] PALETTE_ROW2 = new int[]{
            0xFFFFFFFF, 0xFFB8B8B8, 0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF, 0xFF0000FF,
            0xFFFF00FF, 0xFFFFFF78, 0xFF00FF78, 0xFF78FFFF, 0xFF7878FF, 0xFFFF7878, 0xFFFF7838
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paint);

        paintCanvas = (PaintView) findViewById(R.id.paint_canvas);
        colorSelectedPreview = findViewById(R.id.color_selected_preview);

        setupWindowControls();
        setupTools();
        setupPalette();
        setupMenu();
    }

    private void setupWindowControls() {
        findViewById(R.id.paint_btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.paint_btn_minimize).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                minimizeWindow();
            }
        });

        findViewById(R.id.paint_btn_maximize).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(PaintActivity.this, "Window maximized", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupTools() {
        findViewById(R.id.tool_btn_pencil).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintCanvas.setTool(PaintView.Tool.PENCIL);
                Toast.makeText(PaintActivity.this, "Pencil", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.tool_btn_brush).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintCanvas.setTool(PaintView.Tool.BRUSH);
                Toast.makeText(PaintActivity.this, "Brush", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.tool_btn_eraser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintCanvas.setTool(PaintView.Tool.ERASER);
                Toast.makeText(PaintActivity.this, "Eraser", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.tool_btn_bucket).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintCanvas.floodFill(Color.WHITE);
                Toast.makeText(PaintActivity.this, "Canvas Cleared", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.tool_btn_line).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintCanvas.setTool(PaintView.Tool.LINE);
                Toast.makeText(PaintActivity.this, "Line Tool", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.tool_btn_rect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintCanvas.setTool(PaintView.Tool.RECTANGLE);
                Toast.makeText(PaintActivity.this, "Rectangle Tool", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.tool_btn_circle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintCanvas.setTool(PaintView.Tool.ELLIPSE);
                Toast.makeText(PaintActivity.this, "Ellipse Tool", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupPalette() {
        LinearLayout container = (LinearLayout) findViewById(R.id.palette_swatches_container);
        if (container == null) return;

        LinearLayout rowsContainer = new LinearLayout(this);
        rowsContainer.setOrientation(LinearLayout.VERTICAL);

        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        for (final int c : PALETTE_ROW1) {
            View swatch = createSwatchView(c);
            row1.addView(swatch);
        }

        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.setPadding(0, 2, 0, 0);
        for (final int c : PALETTE_ROW2) {
            View swatch = createSwatchView(c);
            row2.addView(swatch);
        }

        rowsContainer.addView(row1);
        rowsContainer.addView(row2);
        container.addView(rowsContainer);
    }

    private View createSwatchView(final int color) {
        View v = new View(this);
        v.setBackgroundColor(color);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(18, 16);
        lp.rightMargin = 2;
        v.setLayoutParams(lp);

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paintCanvas.setColor(color);
                colorSelectedPreview.setBackgroundColor(color);
            }
        });
        return v;
    }

    private void setupMenu() {
        findViewById(R.id.paint_menu_file).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] items = new String[]{"New", "Save to Pictures", "Exit"};
                AlertDialog.Builder b = new AlertDialog.Builder(PaintActivity.this);
                b.setTitle("File");
                b.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) paintCanvas.clearCanvas();
                        else if (which == 1) paintCanvas.saveImage(PaintActivity.this);
                        else if (which == 2) finish();
                    }
                });
                b.show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        minimizeWindow();
    }

    private void minimizeWindow() {
        sendBroadcast(new Intent("com.windows.xp.ACTION_MINIMIZE"));
        moveTaskToBack(true);
    }
}
