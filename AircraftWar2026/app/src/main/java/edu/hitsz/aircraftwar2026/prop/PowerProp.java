package edu.hitsz.aircraftwar2026.prop;

import edu.hitsz.aircraftwar2026.aircraft.HeroAircraft;
import edu.hitsz.aircraftwar2026.basic.ImageKey;
import edu.hitsz.aircraftwar2026.shoot.SpreadShoot;

/**
 * 火力道具：英雄机切换为散射，持续 10 秒。
 */
public class PowerProp extends BaseProp {

    /** 效果持续时间（毫秒） */
    public static final int EFFECT_DURATION_MS = 10000;

    public PowerProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public PropType getType() {
        return PropType.POWER;
    }

    @Override
    public ImageKey getImageKey() {
        return ImageKey.PROP_BULLET;
    }

    @Override
    public void getEffect(HeroAircraft hero, PropEventPublisher publisher) {
        hero.activateEffect(new SpreadShoot(hero), EFFECT_DURATION_MS);
    }
}
