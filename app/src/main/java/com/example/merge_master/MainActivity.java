package com.example.merge_master;


import java.util.ArrayList;
import java.util.List;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.GridLayout;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;
import android.view.View;


public class MainActivity extends AppCompatActivity {

    private static final int GRID_SIZE = 4;
    private GestureDetector gestureDetector;
    private TextView[][] tiles = new TextView[GRID_SIZE][GRID_SIZE];
    private int score = 0;
    private boolean soundEnabled = true;
    private boolean swipeHandled = false; // Added flag for swipe handling
    private SharedPreferences highScorePrefs;
    private int highScore = 0;
    private TextView highScoreText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().getDecorView().setBackgroundColor(Color.parseColor("#fdfaf6")); // Softer warm white background

        highScorePrefs = getSharedPreferences("GamePrefs", MODE_PRIVATE);
        highScore = highScorePrefs.getInt("high_score", 0);
        highScoreText = findViewById(R.id.highScoreTextView);
        highScoreText.setText("High Score: " + highScore);

        SharedPreferences prefs = getSharedPreferences("GamePrefs", MODE_PRIVATE);
        score = 0;
        SharedPreferences.Editor editor = getSharedPreferences("GamePrefs", MODE_PRIVATE).edit();
        editor.putInt("score", score);
        editor.apply();
        TextView scoreText = findViewById(R.id.scoreTextView);
        scoreText.setText("Score: " + score);


        gestureDetector = new GestureDetector(this, new GestureListener());

        GridLayout gridLayout = findViewById(R.id.gridLayout);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        int tileSize = screenWidth / GRID_SIZE - 30; // auto-scale with padding

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                TextView tile = new TextView(this);
                tile.setWidth(tileSize);
                tile.setHeight(tileSize);
                tile.setText("");
                tile.setTextSize(30);
                tile.setTextColor(Color.parseColor("#776e65"));
                tile.setBackgroundColor(Color.parseColor("#d6d6d6")); // neutral uniform tile background
                tile.setGravity(android.view.Gravity.CENTER);
                tile.setPadding(10, 10, 10, 10);
                // Removed elevation line: tile.setElevation(12f);
                tile.setTypeface(null, android.graphics.Typeface.BOLD);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.setMargins(10, 10, 10, 10);
                tile.setLayoutParams(params);

                gridLayout.addView(tile);
                tiles[row][col] = tile;
            }
        }

        // Spawn two initial tiles
        spawnRandomTile(gridLayout, tiles, false);
        spawnRandomTile(gridLayout, tiles, false);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            swipeHandled = false; // Reset swipe flag after gesture
        }
        return gestureDetector.onTouchEvent(event);
    }

    private int getTileColor(int value) {
        switch (value) {
            case 2: return Color.parseColor("#eee4da");
            case 4: return Color.parseColor("#ede0c8");
            case 8: return Color.parseColor("#f4b17a");
            case 16: return Color.parseColor("#f59563");
            case 32: return Color.parseColor("#f67c5f");
            case 64: return Color.parseColor("#f65e3b");
            case 128: return Color.parseColor("#edcf72");
            case 256: return Color.parseColor("#edcc61");
            case 512: return Color.parseColor("#edc850");
            default: return Color.parseColor("#d6d6d6"); // unified tile color
        }
    }

    private void spawnRandomTile(GridLayout gridLayout, TextView[][] tiles, boolean animate) {
        List<int[]> emptyTiles = new ArrayList<>();
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (tiles[i][j].getText().toString().isEmpty()) {
                    emptyTiles.add(new int[]{i, j});
                }
            }
        }

        if (!emptyTiles.isEmpty()) {
            int[] pos = emptyTiles.get((int)(Math.random() * emptyTiles.size()));
            int value = Math.random() < 0.9 ? 2 : 4;
            TextView tile = tiles[pos[0]][pos[1]];
            tile.setText(String.valueOf(value));
            tile.setBackgroundColor(getTileColor(value));
            if (animate) {
                tile.setScaleX(0.5f);
                tile.setScaleY(0.5f);
                tile.setAlpha(0f);
                tile.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setDuration(250)
                        .setInterpolator(new android.view.animation.BounceInterpolator())
                        .withEndAction(() -> {
                            // Removed sound playback
                        })
                        .start();
            }
        }
    }

    // Move moveTiles above GestureListener so it's accessible within the inner class
    private void moveTiles(String direction) {
        GridLayout gridLayout = findViewById(R.id.gridLayout);
        boolean moved = false;

        for (int i = 0; i < GRID_SIZE; i++) {
            List<Integer> line = new ArrayList<>();
            for (int j = 0; j < GRID_SIZE; j++) {
                int row = (direction.equals("Up") || direction.equals("Down")) ? j : i;
                int col = (direction.equals("Left") || direction.equals("Right")) ? j : i;

                if (direction.equals("Right") || direction.equals("Down")) {
                    row = GRID_SIZE - 1 - row;
                    col = GRID_SIZE - 1 - col;
                }

                String text = tiles[row][col].getText().toString();
                if (!text.isEmpty()) {
                    line.add(Integer.parseInt(text));
                }
            }

            // Merge tiles
            for (int k = 0; k < line.size() - 1; k++) {
                if (line.get(k).equals(line.get(k + 1))) {
                    score += line.get(k);
                    runOnUiThread(() -> {
                        TextView scoreText = findViewById(R.id.scoreTextView);
                        scoreText.setText("Score: " + score);
                    });
                    if (score > highScore) {
                        highScore = score;
                        highScoreText.setText("High Score: " + highScore);
                        SharedPreferences.Editor highScoreEditor = highScorePrefs.edit();
                        highScoreEditor.putInt("high_score", highScore);
                        highScoreEditor.apply();
                    }
                    line.set(k, line.get(k) * 2);
                    line.remove(k + 1);
                    moved = true;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(android.os.VibrationEffect.createOneShot(50, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
                    }
                }
            }

            while (line.size() < GRID_SIZE) {
                line.add(0);
            }

            for (int j = 0; j < GRID_SIZE; j++) {
                int row = (direction.equals("Up") || direction.equals("Down")) ? j : i;
                int col = (direction.equals("Left") || direction.equals("Right")) ? j : i;

                if (direction.equals("Right") || direction.equals("Down")) {
                    row = GRID_SIZE - 1 - row;
                    col = GRID_SIZE - 1 - col;
                }

                String oldText = tiles[row][col].getText().toString();
                int newVal = line.get(j);
                String newText = newVal == 0 ? "" : String.valueOf(newVal);

                if (!oldText.equals(newText)) {
                    moved = true;
                    final int finalRow = row;
                    final int finalCol = col;
                    final String finalText = newText;
                    final int finalVal = newVal;

                    // Update tile directly
                    tiles[finalRow][finalCol].setText(finalText);
                    tiles[finalRow][finalCol].setBackgroundColor(getTileColor(finalVal));
                    tiles[finalRow][finalCol].setScaleX(0.8f);
                    tiles[finalRow][finalCol].setScaleY(0.8f);
                    tiles[finalRow][finalCol].animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(150)
                            .start();
                }
            }
        }

        if (moved) {
            SharedPreferences.Editor editor = getSharedPreferences("GamePrefs", MODE_PRIVATE).edit();
            editor.putInt("score", score);
            editor.apply();
            gridLayout.postDelayed(() -> { // Added delay for smoother visuals
                spawnRandomTile(gridLayout, tiles, true);
                checkGameOver();
            }, 300); // delay after movement finishes
        }
    }

    private void checkGameOver() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                String current = tiles[i][j].getText().toString();
                if (current.isEmpty()) return; // Empty tile found, not game over

                int currentVal = Integer.parseInt(current);
                if ((i > 0 && tiles[i - 1][j].getText().toString().equals(current)) ||
                        (i < GRID_SIZE - 1 && tiles[i + 1][j].getText().toString().equals(current)) ||
                        (j > 0 && tiles[i][j - 1].getText().toString().equals(current)) ||
                        (j < GRID_SIZE - 1 && tiles[i][j + 1].getText().toString().equals(current))) {
                    return; // Mergeable neighbor found
                }
            }
        }

        runOnUiThread(() -> {
            new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                    .setTitle("Game Over")
                    .setMessage("No more moves left!")
                    .setCancelable(false)
                    .setPositiveButton("Restart", (dialog, which) -> recreate())
                    .show();
        });
    }

    public void resetGame() {
        GridLayout gridLayout = findViewById(R.id.gridLayout);
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                tiles[i][j].setText("");
                tiles[i][j].setBackgroundColor(Color.parseColor("#d6d6d6"));
            }
        }
        score = 0;
        TextView scoreText = findViewById(R.id.scoreTextView);
        scoreText.setText("Score: " + score);
        spawnRandomTile(gridLayout, tiles, false);
        spawnRandomTile(gridLayout, tiles, false);
    }




    public void onRestartClicked(View view) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Restart Game")
                .setMessage("Are you sure you want to restart the game?")
                .setPositiveButton("Yes", (dialog, which) -> resetGame())
                .setNegativeButton("Cancel", null)
                .show();
    }


    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = getSharedPreferences("GamePrefs", MODE_PRIVATE).edit();
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                editor.putString("tile_" + i + "_" + j, tiles[i][j].getText().toString());
            }
        }
        editor.putInt("score", score);
        editor.apply();
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 250;
        // private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (swipeHandled) return false; // Prevent multiple moves for a single swipe

            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();

            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > 250) {
                    swipeHandled = true;
                    if (diffX > 0) MainActivity.this.moveTiles("Right");
                    else MainActivity.this.moveTiles("Left");
                    return true;
                }
            } else {
                if (Math.abs(diffY) > 250) {
                    swipeHandled = true;
                    if (diffY > 0) MainActivity.this.moveTiles("Down");
                    else MainActivity.this.moveTiles("Up");
                    return true;
                }
            }
            return false;
        }
    }
}
