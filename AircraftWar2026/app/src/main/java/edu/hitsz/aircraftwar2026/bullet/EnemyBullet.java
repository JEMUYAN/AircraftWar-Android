package edu.hitsz.aircraftwar2026.bullet;

import edu.hitsz.aircraftwar2026.basic.GameConfig;
import edu.hitsz.aircraftwar2026.basic.ImageKey;

/**
 * 敌机子弹：炸弹清除，冰冻静止 5 秒后恢复。
 */
public class EnemyBullet extends BaseBullet {

    /** 冰冻时长（毫秒） */
    public static final int FROZEN_MS = 5000;

    public EnemyBullet(int locationX, int locationY, int speedX, int speedY, int power) {
        super(locationX, locationY, speedX, speedY, power);
    }

    @Override
    public ImageKey getImageKey() {
        return ImageKey.ENEMY_BULLET;
    }

    @Override
    public void onBombEvent() {
        vanish();
    }

    @Override
    public void onFrozenEvent() {
        freeze(GameConfig.msToTicks(FROZEN_MS));
    }
}
