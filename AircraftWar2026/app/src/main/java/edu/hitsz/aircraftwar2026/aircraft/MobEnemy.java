package edu.hitsz.aircraftwar2026.aircraft;

import edu.hitsz.aircraftwar2026.basic.ImageKey;
import edu.hitsz.aircraftwar2026.bullet.BaseBullet;
import edu.hitsz.aircraftwar2026.bullet.EnemyBullet;
import edu.hitsz.aircraftwar2026.prop.PropType;
import edu.hitsz.aircraftwar2026.shoot.NoShoot;

/**
 * 普通敌机：不射击，不掉落道具；炸弹坠毁，冰冻后不再恢复。
 */
public class MobEnemy extends AbstractAircraft {

    public MobEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.shootNum = 0;
        this.power = 0;
        this.direction = 1;
        this.shootStrategy = new NoShoot();
    }

    @Override
    public ImageKey getImageKey() {
        return ImageKey.MOB;
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
        freeze(FOREVER);
    }
}
