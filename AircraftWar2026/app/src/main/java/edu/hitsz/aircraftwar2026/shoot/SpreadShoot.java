package edu.hitsz.aircraftwar2026.shoot;

import edu.hitsz.aircraftwar2026.aircraft.AbstractAircraft;
import edu.hitsz.aircraftwar2026.bullet.BaseBullet;

import java.util.LinkedList;
import java.util.List;

/**
 * 散射：固定两发并排。
 */
public class SpreadShoot implements ShootStrategy {

    private static final int SHOOT_NUM = 2;

    private final AbstractAircraft aircraft;

    public SpreadShoot(AbstractAircraft aircraft) {
        this.aircraft = aircraft;
    }

    @Override
    public List<BaseBullet> shoot() {
        List<BaseBullet> res = new LinkedList<>();
        int power = aircraft.getPower();
        int direction = aircraft.getDirection();
        int x = aircraft.getLocationX();
        int y = aircraft.getLocationY() + direction * 2;
        int speedY = aircraft.getSpeedY() + direction * 5;
        for (int i = 0; i < SHOOT_NUM; i++) {
            res.add(aircraft.createBullet(x + (i * 2 - SHOOT_NUM + 1) * 10, y, 0, speedY, power));
        }
        return res;
    }
}
