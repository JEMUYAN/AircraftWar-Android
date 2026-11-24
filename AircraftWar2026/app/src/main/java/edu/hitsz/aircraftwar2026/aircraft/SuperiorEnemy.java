package edu.hitsz.aircraftwar2026.aircraft;

import edu.hitsz.aircraftwar2026.basic.GameConfig;
import edu.hitsz.aircraftwar2026.basic.ImageKey;
import edu.hitsz.aircraftwar2026.bullet.BaseBullet;
import edu.hitsz.aircraftwar2026.bullet.EnemyBullet;
import edu.hitsz.aircraftwar2026.prop.PropType;
import edu.hitsz.aircraftwar2026.shoot.SpreadShoot;

/**
 * 超级精英敌机：散射；炸弹坠毁，冰冻静止 3 秒后恢复。
 */
public class SuperiorEnemy extends AbstractAircraft {

    /** 冰冻时长（毫秒） */
    public static final int FROZEN_MS = 3000;

    public SuperiorEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.shootNum = 2;
        this.power = 30;
        this.direction = 1;
        this.shootStrategy = new SpreadShoot(this);
    }

    @Override
    public ImageKey getImageKey() {
        return ImageKey.SUPERIOR;
    }

    @Override
    public BaseBullet createBullet(int x, int y, int speedX, int speedY, int power) {
        return new EnemyBullet(x, y, speedX, speedY, power);
    }

    @Override
    public PropType[] getPropPool() {
        return new PropType[]{PropType.HP, PropType.POWER, PropType.SUPER_POWER, PropType.BOMB};
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
