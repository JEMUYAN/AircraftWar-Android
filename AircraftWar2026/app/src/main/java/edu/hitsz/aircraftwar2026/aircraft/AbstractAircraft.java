package edu.hitsz.aircraftwar2026.aircraft;

import edu.hitsz.aircraftwar2026.basic.AbstractFlyingObject;
import edu.hitsz.aircraftwar2026.basic.Bounds;
import edu.hitsz.aircraftwar2026.bullet.BaseBullet;
import edu.hitsz.aircraftwar2026.prop.BaseProp;
import edu.hitsz.aircraftwar2026.prop.PropEventObserver;
import edu.hitsz.aircraftwar2026.prop.PropFactory;
import edu.hitsz.aircraftwar2026.prop.PropType;
import edu.hitsz.aircraftwar2026.shoot.ShootStrategy;

import java.util.LinkedList;
import java.util.List;

/**
 * 所有飞机的抽象基类：英雄机、敌机。
 * 冻结、减速的计时由 {@link #tick()} 按帧推进，不再使用额外线程。
 */
public abstract class AbstractAircraft extends AbstractFlyingObject implements PropEventObserver {

    /** 计时器取该值时表示效果不会自动结束 */
    public static final int FOREVER = -1;

    protected int maxHp;
    protected int hp;
    /** 一次射击发射的子弹数 */
    protected int shootNum;
    /** 子弹威力 */
    protected int power;
    /** 射击方向：向上 -1，向下 1 */
    protected int direction;
    protected ShootStrategy shootStrategy;

    protected boolean frozen = false;
    protected boolean slowed = false;
    protected int originalSpeedX;
    protected int originalSpeedY;
    /** 因炸弹而坠毁（用于计分但不掉落道具） */
    protected boolean bombDestroyed = false;

    private int frozenTicksLeft = FOREVER;
    private int slowTicksLeft = FOREVER;

    public AbstractAircraft(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY);
        this.hp = hp;
        this.maxHp = hp;
        this.originalSpeedX = speedX;
        this.originalSpeedY = speedY;
    }

    @Override
    protected int hitHeightDivisor() {
        return 2;
    }

    @Override
    public void forward(Bounds bounds) {
        super.forward(bounds);
        // 飞出屏幕底部即消失
        if (locationY >= bounds.getHeight()) {
            vanish();
        }
    }

    /**
     * 推进冻结、减速计时器。
     */
    @Override
    public void tick() {
        if (frozen && frozenTicksLeft > 0 && --frozenTicksLeft == 0) {
            unfreeze();
        }
        if (slowed && slowTicksLeft > 0 && --slowTicksLeft == 0) {
            unslow();
        }
    }

    public void decreaseHp(int decrease) {
        hp -= decrease;
        if (hp <= 0) {
            hp = 0;
            vanish();
        }
    }

    public void creaseHp(int crease) {
        hp += crease;
        if (hp >= maxHp) {
            hp = maxHp;
        }
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    /**
     * 创建本机种发射的子弹（英雄机发射英雄子弹，敌机发射敌机子弹），
     * 取代旧版射击策略中的 getClass() 判断。
     */
    public abstract BaseBullet createBullet(int x, int y, int speedX, int speedY, int power);

    /** 按当前射击策略射击 */
    public List<BaseBullet> strategyShoot() {
        return shootStrategy.shoot();
    }

    public void setShootStrategy(ShootStrategy strategy) {
        this.shootStrategy = strategy;
    }

    /** 该敌机可掉落的道具池 */
    public abstract PropType[] getPropPool();

    /**
     * 被击毁时掉落道具：50% 概率不掉落，否则从道具池随机选一个。
     */
    public List<BaseProp> dropProps() {
        List<BaseProp> res = new LinkedList<>();
        PropType[] pool = getPropPool();
        if (Math.random() > 0.5 || pool.length == 0) {
            return res;
        }
        PropType type = pool[(int) (Math.random() * pool.length)];
        res.add(PropFactory.createProp(type, getLocationX(), getLocationY(), 0, getSpeedY() + 5));
        return res;
    }

    /** 是否为 Boss */
    public boolean isBoss() {
        return false;
    }

    public int getPower() {
        return power;
    }

    public int getShootNum() {
        return shootNum;
    }

    public int getDirection() {
        return direction;
    }

    /**
     * 冻结：速度归零。
     *
     * @param durationTicks 冻结帧数；{@link #FOREVER} 表示不自动解冻
     */
    public void freeze(int durationTicks) {
        frozen = true;
        frozenTicksLeft = durationTicks;
        speedX = 0;
        speedY = 0;
    }

    /** 解冻，按是否处于减速状态恢复速度 */
    public void unfreeze() {
        frozen = false;
        frozenTicksLeft = FOREVER;
        if (!slowed) {
            speedX = originalSpeedX;
            speedY = originalSpeedY;
        } else {
            speedX = originalSpeedX / 2;
            speedY = originalSpeedY / 2;
        }
    }

    /**
     * 减速为原速度的一半。
     *
     * @param durationTicks 减速帧数；{@link #FOREVER} 表示不自动结束
     */
    public void slow(int durationTicks) {
        slowed = true;
        slowTicksLeft = durationTicks;
        speedX = originalSpeedX / 2;
        speedY = originalSpeedY / 2;
    }

    /** 结束减速 */
    public void unslow() {
        slowed = false;
        slowTicksLeft = FOREVER;
        if (!frozen) {
            speedX = originalSpeedX;
            speedY = originalSpeedY;
        }
    }

    public boolean isFrozen() {
        return frozen;
    }

    public boolean isSlowed() {
        return slowed;
    }

    public boolean isBombDestroyed() {
        return bombDestroyed;
    }

    @Override
    public boolean isAlive() {
        return !notValid();
    }

    @Override
    public void onBombEvent() {
        // 默认不响应
    }

    @Override
    public void onFrozenEvent() {
        // 默认不响应
    }
}
