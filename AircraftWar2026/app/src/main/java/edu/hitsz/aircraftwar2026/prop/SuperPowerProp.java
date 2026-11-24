package edu.hitsz.aircraftwar2026.prop;

import edu.hitsz.aircraftwar2026.aircraft.HeroAircraft;
import edu.hitsz.aircraftwar2026.basic.ImageKey;
import edu.hitsz.aircraftwar2026.shoot.FanShoot;

/**
 * 超级火力道具：英雄机切换为扇形射击，持续 15 秒。
 */
public class SuperPowerProp extends BaseProp {

    /** 效果持续时间（毫秒） */
    public static final int EFFECT_DURATION_MS = 15000;

    public SuperPowerProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public PropType getType() {
        return PropType.SUPER_POWER;
    }

    @Override
    public ImageKey getImageKey() {
        return ImageKey.PROP_BULLETPLUS;
    }

    @Override
    public void getEffect(HeroAircraft hero, PropEventPublisher publisher) {
        hero.activateEffect(new FanShoot(hero), EFFECT_DURATION_MS);
    }
}
