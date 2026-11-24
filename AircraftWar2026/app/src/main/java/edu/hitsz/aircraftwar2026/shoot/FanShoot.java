package edu.hitsz.aircraftwar2026.shoot;

import edu.hitsz.aircraftwar2026.aircraft.AbstractAircraft;
import edu.hitsz.aircraftwar2026.bullet.BaseBullet;

import java.util.LinkedList;
import java.util.List;

/**
 * 扇形射击：三发，横向速度分别为 -2、0、2。
 */
public class FanShoot implements ShootStrategy {

    private static final int SHOOT_NUM = 3;

    private final AbstractAircraft aircraft;

    public FanShoot(AbstractAircraft aircraft) {
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
            res.add(aircraft.createBullet(x, y, (i - 1) * 2, speedY, power));
        }
        return res;
    }
}
