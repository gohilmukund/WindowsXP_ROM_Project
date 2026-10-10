package com.windows.xp.solitaire;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class SolitaireActivity extends Activity implements SolitaireView.OnSolitaireEventListener {

    private SolitaireView solitaireView;
    private TextView statusScore;
    private TextView statusTime;

    private int secondsElapsed = 0;
    private boolean isTimerRunning = false;
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isTimerRunning) {
                secondsElapsed++;
                statusTime.setText("Time: " + secondsElapsed);
                timerHandler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solitaire);

        solitaireView = (SolitaireView) findViewById(R.id.sol_table_view);
        statusScore = (TextView) findViewById(R.id.sol_status_score);
        statusTime = (TextView) findViewById(R.id.sol_status_time);

        solitaireView.setOnSolitaireEventListener(this);

        setupWindowControls();
        setupMenus();
        startTimer();
    }

    private void setupWindowControls() {
        findViewById(R.id.sol_btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.sol_btn_minimize).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                minimizeWindow();
            }
        });

        findViewById(R.id.sol_btn_maximize).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SolitaireActivity.this, "Window maximized", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupMenus() {
        findViewById(R.id.sol_menu_game).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] items = new String[]{"Deal (F2)", "Restart Game", "Exit"};
                AlertDialog.Builder builder = new AlertDialog.Builder(SolitaireActivity.this);
                builder.setTitle("Game");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0 || which == 1) {
                            secondsElapsed = 0;
                            statusTime.setText("Time: 0");
                            solitaireView.startNewDeal();
                            startTimer();
                        } else if (which == 2) {
                            finish();
                        }
                    }
                });
                builder.show();
            }
        });

        findViewById(R.id.sol_menu_help).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder b = new AlertDialog.Builder(SolitaireActivity.this);
                b.setTitle("About Solitaire");
                b.setMessage("Microsoft Windows XP Professional\nVersion 5.1 (Build 2600.xpsp_sp3_gdr.120504-1619)\n\n" +
                        "Solitaire (Klondike) for Samsung Galaxy Tab 3 7.0.\n(c) Microsoft Corporation. All rights reserved.");
                b.setPositiveButton("OK", null);
                b.show();
            }
        });
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
    public void onScoreChanged(int score) {
        statusScore.setText("Score: " + score);
    }

    @Override
    public void onGameWon() {
        stopTimer();
        Toast.makeText(this, "Congratulations! You won in " + secondsElapsed + " seconds!", Toast.LENGTH_LONG).show();
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
