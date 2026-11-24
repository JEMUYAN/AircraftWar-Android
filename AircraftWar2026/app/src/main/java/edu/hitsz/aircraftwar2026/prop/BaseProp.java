package edu.hitsz.aircraftwar2026.prop;

import edu.hitsz.aircraftwar2026.aircraft.HeroAircraft;
import edu.hitsz.aircraftwar2026.basic.AbstractFlyingObject;
import edu.hitsz.aircraftwar2026.basic.Bounds;

/**
 * 道具基类。
 */
public abstract class BaseProp extends AbstractFlyingObject {

    public BaseProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    /** 道具类型 */
    public abstract PropType getType();

    /**
     * 道具生效。
     *
     * @param hero      拾取道具的英雄机
     * @param publisher 道具事件发布者
     */
    public abstract void getEffect(HeroAircraft hero, PropEventPublisher publisher);

    @Override
    public void forward(Bounds bounds) {
        super.forward(bounds);
        // 旧版道具飞出屏幕后不会被清除（持续累积），此处飞出底部即失效
        if (locationY >= bounds.getHeight()) {
            vanish();
        }
    }
}
