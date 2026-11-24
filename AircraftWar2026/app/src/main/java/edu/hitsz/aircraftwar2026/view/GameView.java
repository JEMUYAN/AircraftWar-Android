package edu.hitsz.aircraftwar2026.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowInsets;

import androidx.annotation.NonNull;

import edu.hitsz.aircraftwar2026.basic.Bounds;
import edu.hitsz.aircraftwar2026.basic.GameConfig;
import edu.hitsz.aircraftwar2026.game.Difficulty;
import edu.hitsz.aircraftwar2026.game.GameEngine;
import edu.hitsz.aircraftwar2026.game.GameEventListener;

/**
 * 游戏画面：SurfaceView + 子线程游戏循环（平台适配层）。
 * <p>
 * 子线程只负责"算（engine.update）+ 画（renderer.render）"；
 * 需要通知界面时通过 {@link GameEventListener} 回调，由 Activity 切回主线程处理。
 */
public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private static final String TAG = "GameView";

    private final SurfaceHolder holder;
    private final Difficulty difficulty;
    private final GameEventListener listener;
    private final ImageRepository images = new ImageRepository();

    /** 绘制线程标志位，surfaceDestroyed 中置 false 并等待线程结束 */
    private volatile boolean running = false;
    private Thread thread;

    private volatile GameEngine engine;
    private Renderer renderer;
    private int screenWidth;
    private int screenHeight;
    /** 状态栏等系统栏的顶部高度（像素），文字避开它 */
    private volatile int topInset = 0;

    /**
     * @param context    任意 Context
     * @param difficulty 难度
     * @param listener   游戏事件回调（在游戏线程触发，不要在其中直接操作 UI）
     */
    public GameView(Context context, Difficulty difficulty, GameEventListener listener) {
        super(context);
        this.difficulty = difficulty;
        this.listener = listener;
        holder = getHolder();
        holder.addCallback(this);
        setFocusable(true);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        // 一次性加载并缓存图片，循环中不再 decode
        images.load(getContext());
        renderer = new Renderer(images, difficulty.getBackground());
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        screenWidth = width;
        screenHeight = height;
        if (engine == null) {
            engine = new GameEngine(difficulty, Bounds.fromScreen(width, height), listener);
        }
        if (thread == null) {
            running = true;
            thread = new Thread(this, "game-loop");
            thread.start();
        }
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        running = false;
        if (thread != null) {
            try {
                // 等线程真正结束，避免退出后仍在绘制
                thread.join();
            } catch (InterruptedException e) {
                Log.w(TAG, "等待游戏线程结束时被中断", e);
                Thread.currentThread().interrupt();
            }
            thread = null;
        }
        images.release();
    }

    @Override
    public void run() {
        Log.d(TAG, "游戏线程启动");
        while (running) {
            long start = System.currentTimeMillis();
            GameEngine current = engine;
            current.update();
            Canvas canvas = holder.lockCanvas();
            if (canvas != null) {
                try {
                    renderer.render(canvas, current.getWorld(), screenWidth, topInset, current.isGameOver());
                } finally {
                    holder.unlockCanvasAndPost(canvas);
                }
            }
            long cost = System.currentTimeMillis() - start;
            if (cost < GameConfig.TICK_MS) {
                try {
                    Thread.sleep(GameConfig.TICK_MS - cost);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        Log.d(TAG, "游戏线程退出");
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        topInset = insets.getInsets(WindowInsets.Type.systemBars()).top;
        return super.onApplyWindowInsets(insets);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        GameEngine current = engine;
        if (current == null) {
            return true;
        }
        if (current.isGameOver()) {
            // 游戏结束后点击重新开始
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                engine = new GameEngine(difficulty, Bounds.fromScreen(screenWidth, screenHeight), listener);
            }
            return true;
        }
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            // 屏幕像素 -> 逻辑坐标，按屏幕宽度等比换算
            float scale = GameConfig.WORLD_WIDTH / (float) screenWidth;
            current.setHeroTarget((int) (event.getX() * scale), (int) (event.getY() * scale));
        }
        return true;
    }
}
