package edu.hitsz.aircraftwar2026.aircraft;

import edu.hitsz.aircraftwar2026.basic.ImageKey;
import edu.hitsz.aircraftwar2026.bullet.BaseBullet;
import edu.hitsz.aircraftwar2026.bullet.EnemyBullet;
import edu.hitsz.aircraftwar2026.prop.BaseProp;
import edu.hitsz.aircraftwar2026.prop.PropFactory;
import edu.hitsz.aircraftwar2026.prop.PropType;
import edu.hitsz.aircraftwar2026.shoot.CircleShoot;

import java.util.LinkedList;
import java.util.List;

/**
 * Boss 敌机：环形射击，不受炸弹和冰冻影响，被击毁必掉落 3 个道具。
 */
public class BossEnemy extends AbstractAircraft {

    public BossEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.shootNum = 20;
        this.power = 30;
        this.direction = 1;
        this.shootStrategy = new CircleShoot(this);
    }

    @Override
    public ImageKey getImageKey() {
        return ImageKey.BOSS;
    }

    @Override
    public boolean isBoss() {
        return true;
    }

    @Override
    public BaseBullet createBullet(int x, int y, int speedX, int speedY, int power) {
        return new EnemyBullet(x, y, speedX, speedY, power);
    }

    @Override
    public List<BaseProp> dropProps() {
        List<BaseProp> res = new LinkedList<>();
        PropType[] pool = getPropPool();
        for (int i = 0; i < 3; i++) {
            PropType type = pool[(int) (Math.random() * pool.length)];
            res.add(PropFactory.createProp(type,
                    getLocationX() + (1 - i) * 10, getLocationY(),
                    0, getSpeedY() + 5));
        }
        return res;
    }

    @Override
    public PropType[] getPropPool() {
        return new PropType[]{PropType.HP, PropType.POWER, PropType.SUPER_POWER, PropType.BOMB, PropType.FROZEN};
    }
}
