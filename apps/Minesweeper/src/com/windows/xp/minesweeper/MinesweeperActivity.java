package com.windows.xp.minesweeper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MinesweeperActivity extends Activity implements MinefieldView.OnGameEventListener {

    private MinefieldView minefieldView;
    private TextView ledMines;
    private TextView ledTimer;
    private Button btnSmiley;

    private int secondsElapsed = 0;
    private boolean isTimerRunning = false;
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isTimerRunning) {
                secondsElapsed++;
                if (secondsElapsed > 999) secondsElapsed = 999;
                ledTimer.setText(String.format(Locale.US, "%03d", secondsElapsed));
                timerHandler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minesweeper);

        minefieldView = (MinefieldView) findViewById(R.id.ms_minefield);
        ledMines = (TextView) findViewById(R.id.ms_led_mines);
        ledTimer = (TextView) findViewById(R.id.ms_led_timer);
        btnSmiley = (Button) findViewById(R.id.ms_btn_smiley);

        minefieldView.setOnGameEventListener(this);

        setupWindowControls();
        setupMenus();

        btnSmiley.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetGame();
            }
        });
    }

    private void setupWindowControls() {
        findViewById(R.id.ms_btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.ms_btn_minimize).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                minimizeWindow();
            }
        });

        findViewById(R.id.ms_btn_maximize).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MinesweeperActivity.this, "Window maximized", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupMenus() {
        findViewById(R.id.ms_menu_game).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] items = new String[]{
                        "New (F2)",
                        "Beginner (9x9, 10 Mines)",
                        "Intermediate (16x16, 40 Mines)",
                        "Expert (30x16, 99 Mines)",
                        "Exit"
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(MinesweeperActivity.this);
                builder.setTitle("Game");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                resetGame();
                                break;
                            case 1:
                                stopTimer();
                                secondsElapsed = 0;
                                ledTimer.setText("000");
                                minefieldView.startNewGame(MinefieldView.Difficulty.BEGINNER);
                                break;
                            case 2:
                                stopTimer();
                                secondsElapsed = 0;
                                ledTimer.setText("000");
                                minefieldView.startNewGame(MinefieldView.Difficulty.INTERMEDIATE);
                                break;
                            case 3:
                                stopTimer();
                                secondsElapsed = 0;
                                ledTimer.setText("000");
                                minefieldView.startNewGame(MinefieldView.Difficulty.EXPERT);
                                break;
                            case 4:
                                finish();
                                break;
                        }
                    }
                });
                builder.show();
            }
        });

        findViewById(R.id.ms_menu_help).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder b = new AlertDialog.Builder(MinesweeperActivity.this);
                b.setTitle("About Minesweeper");
                b.setMessage("Microsoft Windows XP Professional\nVersion 5.1 (Build 2600.xpsp_sp3_gdr.120504-1619)\n\n" +
                        "Minesweeper for Samsung Galaxy Tab 3 7.0.\n(c) Microsoft Corporation. All rights reserved.");
                b.setPositiveButton("OK", null);
                b.show();
            }
        });
    }

    private void resetGame() {
        stopTimer();
        secondsElapsed = 0;
        ledTimer.setText("000");
        minefieldView.restartCurrentGame();
    }

    private void startTimer() {
        if (!isTimerRunning) {
            isTimerRunning = true;
            timerHandler.postDelayed(timerRunnable, 1000);
        }
    }

    private void stopTimer() {
        isTimerRunning = false;
        timerHandler.removeCallbacks(timerRunnable);
    }

    @Override
    public void onMinesLeftChanged(int count) {
        if (count < 0) {
            ledMines.setText(String.format(Locale.US, "-%02d", Math.abs(count)));
        } else {
            ledMines.setText(String.format(Locale.US, "%03d", count));
        }
    }

    @Override
    public void onTimerStart() {
        startTimer();
    }

    @Override
    public void onGameWon() {
        stopTimer();
        Toast.makeText(this, "Congratulations! You won in " + secondsElapsed + " seconds!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGameLost() {
        stopTimer();
        Toast.makeText(this, "Game Over!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSmileyStateChange(String smiley) {
        btnSmiley.setText(smiley);
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
        stopTimer();
    }
}
