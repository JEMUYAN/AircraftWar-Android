package edu.hitsz.aircraftwar2026.basic;

/**
 * 游戏世界的逻辑边界（不可变）。
 * <p>
 * 宽度固定为 {@link GameConfig#WORLD_WIDTH}，高度按屏幕宽高比换算，
 * 由此替代旧版写死的 512x768（优化清单 #9）。
 */
public final class Bounds {

    private final int width;
    private final int height;

    public Bounds(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * 按屏幕像素尺寸生成逻辑边界：宽度固定，高度保持屏幕比例。
     *
     * @param screenWidth  屏幕（Surface）宽度，像素
     * @param screenHeight 屏幕（Surface）高度，像素
     */
    public static Bounds fromScreen(int screenWidth, int screenHeight) {
        int h = Math.round(GameConfig.WORLD_WIDTH * (float) screenHeight / Math.max(1, screenWidth));
        return new Bounds(GameConfig.WORLD_WIDTH, h);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
