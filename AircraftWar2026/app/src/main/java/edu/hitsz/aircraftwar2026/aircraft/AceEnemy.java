package edu.hitsz.aircraftwar2026.aircraft;

import edu.hitsz.aircraftwar2026.basic.GameConfig;
import edu.hitsz.aircraftwar2026.basic.ImageKey;
import edu.hitsz.aircraftwar2026.bullet.BaseBullet;
import edu.hitsz.aircraftwar2026.bullet.EnemyBullet;
import edu.hitsz.aircraftwar2026.prop.PropType;
import edu.hitsz.aircraftwar2026.shoot.FanShoot;

/**
 * 王牌敌机：扇形射击；炸弹损失 30 血，冰冻减速 5 秒。
 */
public class AceEnemy extends AbstractAircraft {

    /** 炸弹造成的伤害 */
    public static final int BOMB_DAMAGE = 30;
    /** 减速时长（毫秒） */
    public static final int SLOW_MS = 5000;

    public AceEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.direction = 1;
        this.power = 30;
        this.shootNum = 3;
        this.shootStrategy = new FanShoot(this);
    }

    @Override
    public ImageKey getImageKey() {
        return ImageKey.ACE;
    }

    @Override
    public BaseBullet createBullet(int x, int y, int speedX, int speedY, int power) {
        return new EnemyBullet(x, y, speedX, speedY, power);
    }

    @Override
    public PropType[] getPropPool() {
        return new PropType[]{PropType.HP, PropType.POWER, PropType.SUPER_POWER, PropType.BOMB, PropType.FROZEN};
    }

    @Override
    public void onBombEvent() {
        decreaseHp(BOMB_DAMAGE);
    }

    @Override
    public void onFrozenEvent() {
        slow(GameConfig.msToTicks(SLOW_MS));
    }
}
