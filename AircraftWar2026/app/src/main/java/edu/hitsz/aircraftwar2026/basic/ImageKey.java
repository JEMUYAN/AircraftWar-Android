package edu.hitsz.aircraftwar2026.basic;

/**
 * 图片标识。逻辑层只持有该枚举（不持有 Bitmap），由渲染层根据它取图。
 * <p>
 * 宽高为素材的原始像素尺寸，也是碰撞判定使用的逻辑尺寸；
 * 素材文件替换时需同步修改这里。
 */
public enum ImageKey {
    /** 简单难度背景 */
    BG_EASY(512, 768),
    /** 普通难度背景 */
    BG_NORMAL(512, 768),
    /** 困难难度背景 */
    BG_HARD(512, 768),

    HERO(100, 83),
    MOB(75, 50),
    ELITE(105, 68),
    SUPERIOR(105, 68),
    ACE(100, 83),
    BOSS(300, 200),

    HERO_BULLET(10, 25),
    ENEMY_BULLET(10, 18),

    PROP_BLOOD(33, 30),
    PROP_BOMB(34, 34),
    PROP_BULLET(35, 33),
    PROP_BULLETPLUS(34, 34),
    PROP_FREEZE(35, 35);

    private final int width;
    private final int height;

    ImageKey(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /** 素材宽度（像素） */
    public int getWidth() {
        return width;
    }

    /** 素材高度（像素） */
    public int getHeight() {
        return height;
    }
}
