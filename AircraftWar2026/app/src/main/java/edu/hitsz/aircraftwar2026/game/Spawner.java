package edu.hitsz.aircraftwar2026.game;

import edu.hitsz.aircraftwar2026.aircraft.AbstractAircraft;
import edu.hitsz.aircraftwar2026.aircraft.EnemyFactories;
import edu.hitsz.aircraftwar2026.basic.ImageKey;
import edu.hitsz.aircraftwar2026.bullet.BaseBullet;

import java.util.List;
import java.util.Random;

/**
 * 敌机、Boss 的生成，以及全体飞机的射击。
 */
public class Spawner {

    private final DifficultyRules rules;
    private final DifficultyState state;
    private final Random random;
    private final GameEventListener listener;

    public Spawner(DifficultyRules rules, DifficultyState state, Random random, GameEventListener listener) {
        this.rules = rules;
        this.state = state;
        this.random = random;
        this.listener = listener;
    }

    /**
     * 敌机生成：未达上限时先尝试 Boss，再生成普通敌机。
     */
    public void spawnEnemy(GameWorld world) {
        if (world.getEnemies().size() >= state.enemyMaxNumber) {
            return;
        }
        int width = world.getBounds().getWidth();
        int height = world.getBounds().getHeight();
        int locX = (int) (random.nextDouble() * (width - ImageKey.MOB.getWidth()));
        int locY = (int) (random.nextDouble() * height * 0.10);

        AbstractAircraft enemy = tryGenerateBoss(world, locX);
        if (enemy == null) {
            enemy = rules.generateEnemy(locX, locY, state, random.nextDouble(), random.nextDouble());
        }
        world.getEnemies().add(enemy);
        world.getPublisher().registerObserver(enemy);
    }

    /**
     * 达到分数阈值且场上没有 Boss 时生成 Boss。
     */
    private AbstractAircraft tryGenerateBoss(GameWorld world, int locX) {
        if (world.getBossState() == GameWorld.BossState.ACTIVE
                || world.getScore() < state.bossScoreThreshold) {
            return null;
        }
        world.setBossState(GameWorld.BossState.ACTIVE);
        state.bossSpawnCount++;
        int speedX = random.nextDouble() > 0.5 ? 2 : -2;
        int y = (int) (world.getBounds().getWidth() * 0.05);
        AbstractAircraft boss = EnemyFactories.BOSS.createEnemy(locX, y, speedX, 0,
                rules.bossHp(state.bossSpawnCount));
        state.bossScoreThreshold += state.bossScoreIncrement;
        listener.onBossSpawned();
        return boss;
    }

    /**
     * 英雄机与全部敌机各射击一轮。
     */
    public void shoot(GameWorld world) {
        world.getHeroBullets().addAll(world.getHero().strategyShoot());
        for (AbstractAircraft enemy : world.getEnemies()) {
            List<BaseBullet> bullets = enemy.strategyShoot();
            for (BaseBullet bullet : bullets) {
                world.getPublisher().registerObserver(bullet);
            }
            world.getEnemyBullets().addAll(bullets);
        }
    }
}
