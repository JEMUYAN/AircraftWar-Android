package edu.hitsz.aircraftwar2026.shoot;

import edu.hitsz.aircraftwar2026.bullet.BaseBullet;

import java.util.List;

/**
 * 射击策略接口（策略模式）。
 */
public interface ShootStrategy {

    /** 按策略产生一轮子弹 */
    List<BaseBullet> shoot();
}
