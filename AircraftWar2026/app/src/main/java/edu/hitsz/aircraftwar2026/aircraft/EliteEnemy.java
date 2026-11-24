package edu.hitsz.aircraftwar2026.aircraft;

import edu.hitsz.aircraftwar2026.basic.GameConfig;
import edu.hitsz.aircraftwar2026.basic.ImageKey;
import edu.hitsz.aircraftwar2026.bullet.BaseBullet;
import edu.hitsz.aircraftwar2026.bullet.EnemyBullet;
import edu.hitsz.aircraftwar2026.prop.PropType;
import edu.hitsz.aircraftwar2026.shoot.DirectShoot;

/**
 * 精英敌机：直射；炸弹坠毁，冰冻静止 4 秒后恢复。
 */
public class EliteEnemy extends AbstractAircraft {

    /** 冰冻时长（毫秒） */
    public static final int FROZEN_MS = 4000;

    public EliteEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.shootNum = 1;
        this.power = 30;
        this.direction = 1;
        this.shootStrategy = new DirectShoot(this);
    }

    @Override
    public ImageKey getImageKey() {
        return ImageKey.ELITE;
    }

    @Override
    public BaseBullet createBullet(int x, int y, int speedX, int speedY, int power) {
        return new EnemyBullet(x, y, speedX, speedY, power);
    }

    @Override
    public PropType[] getPropPool() {
        return new PropType[0];
    }

    @Override
    public void onBombEvent() {
        bombDestroyed = true;
        vanish();
    }

    @Override
    public void onFrozenEvent() {
        freeze(GameConfig.msToTicks(FROZEN_MS));
    }
}
