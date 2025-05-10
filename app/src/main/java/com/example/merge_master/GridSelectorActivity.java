package com.example.merge_master;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class GridSelectorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_selector);

        Button btn4 = findViewById(R.id.btn_grid_4);
        Button btn5 = findViewById(R.id.btn_grid_5);
        Button btn6 = findViewById(R.id.btn_grid_6);

        btn4.setOnClickListener(v -> startGameWithGridSize(4));
        btn5.setOnClickListener(v -> startGameWithGridSize(5));
        btn6.setOnClickListener(v -> startGameWithGridSize(6));
    }

    private void startGameWithGridSize(int gridSize) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("GRID_SIZE", gridSize);
        startActivity(intent);
    }
}
