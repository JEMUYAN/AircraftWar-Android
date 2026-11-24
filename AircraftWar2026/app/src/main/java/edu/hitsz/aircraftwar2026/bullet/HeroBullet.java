package edu.hitsz.aircraftwar2026.bullet;

import edu.hitsz.aircraftwar2026.basic.ImageKey;

/**
 * 英雄机子弹。
 */
public class HeroBullet extends BaseBullet {

    public HeroBullet(int locationX, int locationY, int speedX, int speedY, int power) {
        super(locationX, locationY, speedX, speedY, power);
    }

    @Override
    public ImageKey getImageKey() {
        return ImageKey.HERO_BULLET;
    }
}
