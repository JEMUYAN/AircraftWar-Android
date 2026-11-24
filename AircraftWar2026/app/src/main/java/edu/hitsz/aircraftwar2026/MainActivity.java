package edu.hitsz.aircraftwar2026;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import edu.hitsz.aircraftwar2026.view.GameView;

/**
 * 入口 Activity：保持精简，只负责承载 GameView。
 * 难度选择页将在 Lab3 加入，目前固定启动简单难度。
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new GameView(this));
    }
}
