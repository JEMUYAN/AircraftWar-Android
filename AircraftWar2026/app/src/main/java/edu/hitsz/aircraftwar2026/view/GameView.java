package edu.hitsz.aircraftwar2026.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import edu.hitsz.aircraftwar2026.basic.Bounds;
import edu.hitsz.aircraftwar2026.basic.GameConfig;
import edu.hitsz.aircraftwar2026.basic.ImageKey;

/**
 * 游戏画面（Step 3：只画滚动背景与英雄机，验证游戏循环）。
 */
public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private static final String TAG = "GameView";

    private final SurfaceHolder holder;
    private final ImageRepository images = new ImageRepository();
    private final Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);

    private volatile boolean running = false;
    private Thread thread;

    private Bounds bounds;
    private float scale = 1f;
    private int bgTop = 0;

    public GameView(Context context) {
        super(context);
        holder = getHolder();
        holder.addCallback(this);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        // 一次性加载并缓存图片，循环中不再 decode
        images.load(getContext());
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        bounds = Bounds.fromScreen(width, height);
        scale = width / (float) bounds.getWidth();
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
                thread.join();
            } catch (InterruptedException e) {
                Log.w(TAG, "等待绘制线程结束时被中断", e);
                Thread.currentThread().interrupt();
            }
            thread = null;
        }
        images.release();
    }

    @Override
    public void run() {
        while (running) {
            long start = System.currentTimeMillis();
            Canvas canvas = holder.lockCanvas();
            if (canvas != null) {
                try {
                    drawFrame(canvas);
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
                    return;
                }
            }
        }
    }

    private void drawFrame(Canvas canvas) {
        canvas.save();
        canvas.scale(scale, scale);
        Bitmap bg = images.get(ImageKey.BG_EASY);
        int bgH = ImageKey.BG_EASY.getHeight();
        for (int y = bgTop - bgH; y < bounds.getHeight(); y += bgH) {
            canvas.drawBitmap(bg, 0, y, paint);
        }
        bgTop = (bgTop + 1) % bgH;
        Bitmap hero = images.get(ImageKey.HERO);
        canvas.drawBitmap(hero, bounds.getWidth() / 2f - hero.getWidth() / 2f,
                bounds.getHeight() - hero.getHeight() * 1.5f, paint);
        canvas.restore();
    }

}
