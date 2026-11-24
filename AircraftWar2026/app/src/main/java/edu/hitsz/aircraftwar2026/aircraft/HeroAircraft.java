package edu.hitsz.aircraftwar2026.aircraft;

import edu.hitsz.aircraftwar2026.basic.Bounds;
import edu.hitsz.aircraftwar2026.basic.GameConfig;
import edu.hitsz.aircraftwar2026.basic.ImageKey;
import edu.hitsz.aircraftwar2026.bullet.BaseBullet;
import edu.hitsz.aircraftwar2026.bullet.HeroBullet;
import edu.hitsz.aircraftwar2026.prop.PropType;
import edu.hitsz.aircraftwar2026.shoot.DirectShoot;
import edu.hitsz.aircraftwar2026.shoot.ShootStrategy;

/**
 * 英雄机。
 * <p>
 * 不再是全局单例（优化清单 #3）：每局游戏由 GameWorld 创建一个实例，
 * 重开局只需新建，单测也无需反射重置。
 */
public class HeroAircraft extends AbstractAircraft {

    public static final int DEFAULT_HP = 1000;

    /** 当前射击效果剩余帧数，0 表示无效果 */
    private int effectTicksLeft = 0;

    public HeroAircraft(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.shootNum = 1;
        this.power = 30;
        this.direction = -1;
        this.shootStrategy = new DirectShoot(this);
    }

    @Override
    public ImageKey getImageKey() {
        return ImageKey.HERO;
    }

    /** 英雄机位置由触屏控制，不随速度移动 */
    @Override
    public void forward(Bounds bounds) {
    }

    @Override
    public BaseBullet createBullet(int x, int y, int speedX, int speedY, int power) {
        return new HeroBullet(x, y, speedX, speedY, power);
    }

    @Override
    public PropType[] getPropPool() {
        return new PropType[0];
    }

    /**
     * 激活射击效果：切换射击策略，持续指定时间后自动恢复直射。
     * 重复激活会覆盖并重新计时。
     *
     * @param effectStrategy 效果对应的射击策略
     * @param durationMs     持续时间（毫秒）
     */
    public void activateEffect(ShootStrategy effectStrategy, int durationMs) {
        setShootStrategy(effectStrategy);
        effectTicksLeft = GameConfig.msToTicks(durationMs);
    }

    /** 恢复默认射击策略（直射） */
    public void restoreDefaultShootStrategy() {
        setShootStrategy(new DirectShoot(this));
        effectTicksLeft = 0;
    }

    public boolean hasEffect() {
        return effectTicksLeft > 0;
    }

    @Override
    public void tick() {
        super.tick();
        if (effectTicksLeft > 0 && --effectTicksLeft == 0) {
            restoreDefaultShootStrategy();
        }
    }
}
