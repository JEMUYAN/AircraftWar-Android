package edu.hitsz.aircraftwar2026.game;

import edu.hitsz.aircraftwar2026.aircraft.AbstractAircraft;
import edu.hitsz.aircraftwar2026.aircraft.HeroAircraft;
import edu.hitsz.aircraftwar2026.bullet.BaseBullet;
import edu.hitsz.aircraftwar2026.prop.BaseProp;

/**
 * 碰撞检测与处理：英雄子弹打敌机、敌机撞英雄机、敌机子弹打英雄机、英雄机拾取道具。
 */
public class CollisionSystem {

    /** 击毁普通敌机得分 */
    public static final int SCORE_ENEMY = 10;
    /** 击毁 Boss 得分 */
    public static final int SCORE_BOSS = 30;

    private final GameEventListener listener;

    public CollisionSystem(GameEventListener listener) {
        this.listener = listener;
    }

    public void check(GameWorld world) {
        HeroAircraft hero = world.getHero();

        // 英雄子弹攻击敌机
        for (BaseBullet bullet : world.getHeroBullets()) {
            if (bullet.notValid()) {
                continue;
            }
            for (AbstractAircraft enemy : world.getEnemies()) {
                if (enemy.notValid()) {
                    continue;
                }
                if (enemy.crash(bullet)) {
                    enemy.decreaseHp(bullet.getPower());
                    bullet.vanish();
                    listener.onBulletHit();
                    if (enemy.notValid()) {
                        // 敌机损毁，掉落道具并计分
                        world.getProps().addAll(enemy.dropProps());
                        reward(world, enemy);
                    }
                }
            }
        }

        // 英雄机与敌机相撞，均损毁
        for (AbstractAircraft enemy : world.getEnemies()) {
            if (enemy.notValid()) {
                continue;
            }
            if (enemy.crash(hero) || hero.crash(enemy)) {
                enemy.vanish();
                hero.decreaseHp(Integer.MAX_VALUE);
            }
        }

        // 敌机子弹攻击英雄机
        for (BaseBullet bullet : world.getEnemyBullets()) {
            if (bullet.notValid()) {
                continue;
            }
            if (hero.crash(bullet)) {
                hero.decreaseHp(bullet.getPower());
                bullet.vanish();
                listener.onBulletHit();
            }
        }

        // 英雄机拾取道具，道具生效
        for (BaseProp prop : world.getProps()) {
            if (prop.notValid()) {
                continue;
            }
            if (prop.crash(hero) || hero.crash(prop)) {
                prop.getEffect(hero, world.getPublisher());
                prop.vanish();
                listener.onPropCollected(prop.getType());
            }
        }

        // 炸弹坠毁的敌机：得分但不掉落道具
        for (AbstractAircraft enemy : world.getEnemies()) {
            if (enemy.notValid() && enemy.isBombDestroyed()) {
                reward(world, enemy);
            }
        }
    }

    /** 击毁奖励：Boss 30 分，其它 10 分；Boss 被击毁后重置 Boss 状态 */
    private void reward(GameWorld world, AbstractAircraft enemy) {
        if (enemy.isBoss()) {
            world.addScore(SCORE_BOSS);
            world.setBossState(GameWorld.BossState.ABSENT);
            listener.onBossDefeated();
        } else {
            world.addScore(SCORE_ENEMY);
        }
    }
}
