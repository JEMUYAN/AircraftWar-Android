package edu.hitsz.aircraftwar2026.basic;

/**
 * 全局游戏常量（逻辑层，不依赖 android.*）。
 */
public final class GameConfig {

    /** 逻辑世界宽度，与旧版窗口宽度一致，保证速度、尺寸的手感不变 */
    public static final int WORLD_WIDTH = 512;

    /** 逻辑帧间隔（毫秒），与旧版 Timer 周期一致 */
    public static final int TICK_MS = 40;

    private GameConfig() {
    }

    /**
     * 毫秒换算为逻辑帧数。道具、冻结等效果的计时统一基于帧数，
     * 不再使用额外线程（优化清单 #1）。
     */
    public static int msToTicks(int ms) {
        return Math.max(1, ms / TICK_MS);
    }
}
