package edu.hitsz.aircraftwar2026.prop;

import edu.hitsz.aircraftwar2026.aircraft.HeroAircraft;
import edu.hitsz.aircraftwar2026.basic.ImageKey;

/**
 * 炸弹道具：向所有观察者广播炸弹事件。
 */
public class BombProp extends BaseProp {

    public BombProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public PropType getType() {
        return PropType.BOMB;
    }

    @Override
    public ImageKey getImageKey() {
        return ImageKey.PROP_BOMB;
    }

    @Override
    public void getEffect(HeroAircraft hero, PropEventPublisher publisher) {
        publisher.notifyBombEvent();
    }
}
