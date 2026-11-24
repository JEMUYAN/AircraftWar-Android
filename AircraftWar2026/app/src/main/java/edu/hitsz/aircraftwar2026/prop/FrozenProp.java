package edu.hitsz.aircraftwar2026.prop;

import edu.hitsz.aircraftwar2026.aircraft.HeroAircraft;
import edu.hitsz.aircraftwar2026.basic.ImageKey;

/**
 * 冰冻道具：向所有观察者广播冰冻事件。
 */
public class FrozenProp extends BaseProp {

    public FrozenProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public PropType getType() {
        return PropType.FROZEN;
    }

    @Override
    public ImageKey getImageKey() {
        return ImageKey.PROP_FREEZE;
    }

    @Override
    public void getEffect(HeroAircraft hero, PropEventPublisher publisher) {
        publisher.notifyFrozenEvent();
    }
}
