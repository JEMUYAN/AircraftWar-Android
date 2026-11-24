package edu.hitsz.aircraftwar2026.shoot;

import edu.hitsz.aircraftwar2026.bullet.BaseBullet;

import java.util.LinkedList;
import java.util.List;

/**
 * 不射击（普通敌机）。
 */
public class NoShoot implements ShootStrategy {

    @Override
    public List<BaseBullet> shoot() {
        return new LinkedList<>();
    }
}
