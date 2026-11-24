package edu.hitsz.aircraftwar2026.shoot;

import edu.hitsz.aircraftwar2026.aircraft.AbstractAircraft;
import edu.hitsz.aircraftwar2026.bullet.BaseBullet;

import java.util.LinkedList;
import java.util.List;

/**
 * 直射：按飞机的 shootNum 横向并排发射。
 */
public class DirectShoot implements ShootStrategy {

    private final AbstractAircraft aircraft;

    public DirectShoot(AbstractAircraft aircraft) {
        this.aircraft = aircraft;
    }

    @Override
    public List<BaseBullet> shoot() {
        List<BaseBullet> res = new LinkedList<>();
        int shootNum = aircraft.getShootNum();
        int power = aircraft.getPower();
        int direction = aircraft.getDirection();
        int x = aircraft.getLocationX();
        int y = aircraft.getLocationY() + direction * 2;
        int speedX = 0;
        int speedY = aircraft.getSpeedY() + direction * 5;
        for (int i = 0; i < shootNum; i++) {
            res.add(aircraft.createBullet(x + (i * 2 - shootNum + 1) * 10, y, speedX, speedY, power));
        }
        return res;
    }
}
