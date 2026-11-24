package edu.hitsz.aircraftwar2026.view;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import edu.hitsz.aircraftwar2026.basic.AbstractFlyingObject;
import edu.hitsz.aircraftwar2026.basic.Bounds;
import edu.hitsz.aircraftwar2026.basic.ImageKey;
import edu.hitsz.aircraftwar2026.game.GameWorld;

import java.util.List;

/**
 * 渲染器：把 {@link GameWorld} 画到 Canvas 上。逻辑坐标（宽度固定 512）按屏幕宽度等比放大。
 */
public class Renderer {

    private final ImageRepository images;
    private final ImageKey background;
    private final Paint bitmapPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /** 背景滚动偏移，每帧 +1，与旧版一致 */
    private int bgTop = 0;

    public Renderer(ImageRepository images, ImageKey background) {
        this.images = images;
        this.background = background;
        textPaint.setColor(Color.RED);
        textPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        textPaint.setTextSize(22);
    }

    /**
     * 绘制一帧。
     *
     * @param screenWidth 屏幕（Surface）宽度，用于换算缩放比例
     * @param topInset    顶部系统栏高度（像素），分数文字从其下方开始
     * @param gameOver    是否显示游戏结束提示
     */
    public void render(Canvas canvas, GameWorld world, int screenWidth, int topInset, boolean gameOver) {
        Bounds bounds = world.getBounds();
        float scale = screenWidth / (float) bounds.getWidth();
        canvas.save();
        canvas.scale(scale, scale);

        drawBackground(canvas, bounds);
        drawAll(canvas, world.getEnemyBullets());
        drawAll(canvas, world.getHeroBullets());
        drawAll(canvas, world.getEnemies());
        drawAll(canvas, world.getProps());
        drawObject(canvas, world.getHero());

        textPaint.setTextAlign(Paint.Align.LEFT);
        float textTop = topInset / scale;
        canvas.drawText("SCORE: " + world.getScore(), 10, textTop + 25, textPaint);
        canvas.drawText("LIFE: " + world.getHero().getHp(), 10, textTop + 50, textPaint);

        if (gameOver) {
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTextSize(40);
            canvas.drawText("GAME OVER", bounds.getWidth() / 2f, bounds.getHeight() / 2f, textPaint);
            textPaint.setTextSize(22);
            canvas.drawText("SCORE: " + world.getScore(), bounds.getWidth() / 2f,
                    bounds.getHeight() / 2f + 35, textPaint);
            canvas.drawText("TAP TO RESTART", bounds.getWidth() / 2f,
                    bounds.getHeight() / 2f + 70, textPaint);
        }
        canvas.restore();
    }

    /** 背景纵向循环滚动；逻辑高度随屏幕比例变化，所以按需平铺多张 */
    private void drawBackground(Canvas canvas, Bounds bounds) {
        Bitmap bg = images.get(background);
        int bgHeight = background.getHeight();
        for (int y = bgTop - bgHeight; y < bounds.getHeight(); y += bgHeight) {
            canvas.drawBitmap(bg, 0, y, bitmapPaint);
        }
        bgTop = (bgTop + 1) % bgHeight;
    }

    private void drawAll(Canvas canvas, List<? extends AbstractFlyingObject> objects) {
        for (AbstractFlyingObject object : objects) {
            drawObject(canvas, object);
        }
    }

    /** 以对象中心位置绘制 */
    private void drawObject(Canvas canvas, AbstractFlyingObject object) {
        Bitmap bitmap = images.get(object.getImageKey());
        canvas.drawBitmap(bitmap,
                object.getLocationX() - bitmap.getWidth() / 2f,
                object.getLocationY() - bitmap.getHeight() / 2f,
                bitmapPaint);
    }
}
