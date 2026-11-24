package edu.hitsz.aircraftwar2026.prop;

import edu.hitsz.aircraftwar2026.aircraft.HeroAircraft;
import edu.hitsz.aircraftwar2026.basic.ImageKey;

/**
 * 加血道具：恢复英雄机 20 点血量。
 */
public class HpProp extends BaseProp {

    public static final int HEAL = 20;

    public HpProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public PropType getType() {
        return PropType.HP;
    }

    @Override
    public ImageKey getImageKey() {
        return ImageKey.PROP_BLOOD;
    }

    @Override
    public void getEffect(HeroAircraft hero, PropEventPublisher publisher) {
        hero.creaseHp(HEAL);
    }
}
