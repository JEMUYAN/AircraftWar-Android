package edu.hitsz.aircraftwar2026.shoot;

import edu.hitsz.aircraftwar2026.aircraft.AbstractAircraft;
import edu.hitsz.aircraftwar2026.bullet.BaseBullet;

import java.util.LinkedList;
import java.util.List;

/**
 * 环形射击：向四周均匀发射 20 发（Boss 使用）。
 */
public class CircleShoot implements ShootStrategy {

    private static final int SHOOT_NUM = 20;
    private static final double TOTAL_SPEED = 8.0;

    private final AbstractAircraft aircraft;

    public CircleShoot(AbstractAircraft aircraft) {
        this.aircraft = aircraft;
    }

    @Override
    public List<BaseBullet> shoot() {
        List<BaseBullet> res = new LinkedList<>();
        int power = aircraft.getPower();
        int direction = aircraft.getDirection();
        int x = aircraft.getLocationX();
        int y = aircraft.getLocationY() + direction * 2;
        for (int i = 0; i < SHOOT_NUM; i++) {
            double angle = (2 * Math.PI / SHOOT_NUM) * i;
            int speedX = (int) (TOTAL_SPEED * Math.cos(angle));
            int speedY = (int) (TOTAL_SPEED * Math.sin(angle));
            res.add(aircraft.createBullet(x, y, speedX, speedY, power));
        }
        return res;
    }
}
