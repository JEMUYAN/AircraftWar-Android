package edu.hitsz.aircraftwar2026.game;

import edu.hitsz.aircraftwar2026.prop.PropType;

/**
 * 游戏事件监听。逻辑层只发出事件，音效、跳转等由平台层自行处理
 * （Lab3 的音频与游戏结束跳转通过它接入）。回调发生在游戏线程。
 */
public interface GameEventListener {

    /** 子弹击中飞机（英雄子弹击中敌机，或敌机子弹击中英雄机） */
    default void onBulletHit() {
    }

    /** 英雄机拾取道具 */
    default void onPropCollected(PropType type) {
    }

    /** Boss 出现 */
    default void onBossSpawned() {
    }

    /** Boss 被击毁 */
    default void onBossDefeated() {
    }

    /** 游戏结束 */
    default void onGameOver(int score) {
    }
}
