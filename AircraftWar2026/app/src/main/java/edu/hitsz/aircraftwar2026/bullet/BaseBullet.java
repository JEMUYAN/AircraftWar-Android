package edu.hitsz.aircraftwar2026.bullet;

import edu.hitsz.aircraftwar2026.basic.AbstractFlyingObject;
import edu.hitsz.aircraftwar2026.basic.Bounds;
import edu.hitsz.aircraftwar2026.prop.PropEventObserver;

/**
 * 子弹基类。冰冻时停止移动，冻结时长由帧计数器倒计时。
 */
public abstract class BaseBullet extends AbstractFlyingObject implements PropEventObserver {

    /** 冻结时长：一直冻结，直到被解除 */
    public static final int FOREVER = -1;

    private final int power;
    protected boolean frozen = false;
    protected int originalSpeedX;
    protected int originalSpeedY;
    /** 剩余冻结帧数；FOREVER 表示不自动解冻 */
    private int frozenTicksLeft = FOREVER;

    public BaseBullet(int locationX, int locationY, int speedX, int speedY, int power) {
        super(locationX, locationY, speedX, speedY);
        this.power = power;
        this.originalSpeedX = speedX;
        this.originalSpeedY = speedY;
    }

    @Override
    public void forward(Bounds bounds) {
        if (frozen) {
            return;
        }
        super.forward(bounds);
        // 子弹出界即消失
        if (locationX <= 0 || locationX >= bounds.getWidth()) {
            vanish();
        }
        if (speedY > 0 && locationY >= bounds.getHeight()) {
            vanish();
        } else if (locationY <= 0) {
            vanish();
        }
    }

    @Override
    public void tick() {
        if (frozen && frozenTicksLeft > 0 && --frozenTicksLeft == 0) {
            unfreeze();
        }
    }

    public int getPower() {
        return power;
    }

    /**
     * 冻结子弹。
     *
     * @param durationTicks 冻结帧数；{@link #FOREVER} 表示不自动解冻
     */
    public void freeze(int durationTicks) {
        frozen = true;
        frozenTicksLeft = durationTicks;
        speedX = 0;
        speedY = 0;
    }

    /** 解除冻结，恢复原速度 */
    public void unfreeze() {
        frozen = false;
        frozenTicksLeft = FOREVER;
        speedX = originalSpeedX;
        speedY = originalSpeedY;
    }

    public boolean isFrozen() {
        return frozen;
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
