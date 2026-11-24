package edu.hitsz.aircraftwar2026.aircraft;

/**
 * 敌机工厂接口（工厂方法模式）。
 */
public interface EnemyFactory {

    AbstractAircraft createEnemy(int locationX, int locationY, int speedX, int speedY, int hp);
}
