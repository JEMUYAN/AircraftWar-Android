package edu.hitsz.aircraftwar2026;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import edu.hitsz.aircraftwar2026.game.Difficulty;
import edu.hitsz.aircraftwar2026.game.GameEventListener;
import edu.hitsz.aircraftwar2026.view.GameView;

/**
 * 入口 Activity：保持精简，只负责承载 GameView，并把游戏线程的事件切回主线程。
 * 难度选择页将在 Lab3 加入，目前固定启动简单难度。
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GameEventListener listener = new GameEventListener() {
            @Override
            public void onGameOver(int score) {
                // 回调在游戏线程，UI 操作必须切回主线程
                Log.i(TAG, "游戏结束，得分 " + score);
                mainHandler.post(() -> Toast.makeText(getApplicationContext(),
                        "Game Over，得分 " + score, Toast.LENGTH_SHORT).show());
            }
        };
        setContentView(new GameView(this, Difficulty.EASY, listener));
    }
}
