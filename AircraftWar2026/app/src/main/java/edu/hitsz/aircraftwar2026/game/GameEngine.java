package edu.hitsz.aircraftwar2026.game;

import edu.hitsz.aircraftwar2026.aircraft.AbstractAircraft;
import edu.hitsz.aircraftwar2026.basic.AbstractFlyingObject;
import edu.hitsz.aircraftwar2026.basic.Bounds;
import edu.hitsz.aircraftwar2026.bullet.BaseBullet;
import edu.hitsz.aircraftwar2026.prop.BaseProp;

import java.util.List;
import java.util.Random;

/**
 * 游戏引擎：驱动一局游戏的逻辑帧（纯 Java，不依赖 android.*，可在 JVM 上单测）。
 * <p>
 * 流程沿用旧版 AbstractGame 的模板：难度提升 → 敌机生成 → 射击 → 移动 → 碰撞 → 后处理 → 结束判定；
 * 具体难度的差异由 {@link DifficultyRules} 提供。
 * 触屏输入（UI 线程）与 {@link #update()}（游戏线程）之间只通过 {@link #setHeroTarget} 交接。
 */
public class GameEngine {

    private final GameWorld world;
    private final DifficultyRules rules;
    private final DifficultyState state = new DifficultyState();
    private final Spawner spawner;
    private final CollisionSystem collisionSystem;
    private final GameEventListener listener;

    private int enemySpawnCounter = 0;
    private int shootCounter = 0;
    private boolean gameOver = false;

    private boolean hasHeroTarget = false;
    private int heroTargetX;
    private int heroTargetY;

    public GameEngine(Difficulty difficulty, Bounds bounds, GameEventListener listener) {
        this(difficulty, bounds, listener, new Random());
    }

    public GameEngine(Difficulty difficulty, Bounds bounds, GameEventListener listener, Random random) {
        this.listener = listener == null ? new GameEventListener() {
        } : listener;
        this.rules = difficulty.createRules();
        this.rules.configure(state);
        this.world = new GameWorld(bounds);
        this.spawner = new Spawner(rules, state, random, this.listener);
        this.collisionSystem = new CollisionSystem(this.listener);
    }

    public GameWorld getWorld() {
        return world;
    }

    public Difficulty getDifficulty() {
        return rules.getDifficulty();
    }

    public DifficultyState getState() {
        return state;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * 设置英雄机目标位置（逻辑坐标），在下一帧生效。可在任意线程调用。
     */
    public synchronized void setHeroTarget(int x, int y) {
        Bounds b = world.getBounds();
        heroTargetX = Math.max(0, Math.min(b.getWidth(), x));
        heroTargetY = Math.max(0, Math.min(b.getHeight(), y));
        hasHeroTarget = true;
    }

    /**
     * 推进一个逻辑帧。游戏结束后调用无效。
     */
    public void update() {
        if (gameOver) {
            return;
        }
        applyHeroTarget();

        // 难度随时间提升
        state.difficultyCounter++;
        rules.increaseDifficulty(state);

        // 推进冻结、道具效果等计时器
        tickAll();

        // 敌机生成
        enemySpawnCounter++;
        if (enemySpawnCounter >= state.enemySpawnCycle) {
            enemySpawnCounter = 0;
            spawner.spawnEnemy(world);
        }

        // 射击
        shootCounter++;
        if (shootCounter >= state.shootCycle) {
            shootCounter = 0;
            spawner.shoot(world);
        }

        // 移动
        Bounds bounds = world.getBounds();
        forwardAll(world.getHeroBullets(), bounds);
        forwardAll(world.getEnemyBullets(), bounds);
        forwardAll(world.getEnemies(), bounds);
        forwardAll(world.getProps(), bounds);

        // 碰撞
        collisionSystem.check(world);

        // 后处理
        postProcess();

        // 结束判定
        if (world.isHeroDead()) {
            gameOver = true;
            listener.onGameOver(world.getScore());
        }
    }

    private synchronized void applyHeroTarget() {
        if (hasHeroTarget) {
            world.getHero().setLocation(heroTargetX, heroTargetY);
            hasHeroTarget = false;
        }
    }

    private void tickAll() {
        world.getHero().tick();
        for (AbstractFlyingObject o : world.getEnemies()) {
            o.tick();
        }
        for (AbstractFlyingObject o : world.getEnemyBullets()) {
            o.tick();
        }
    }

    private static void forwardAll(List<? extends AbstractFlyingObject> objects, Bounds bounds) {
        for (AbstractFlyingObject o : objects) {
            o.forward(bounds);
        }
    }

    /**
     * 清除无效对象；清除前先从发布者注销观察者。
     */
    private void postProcess() {
        for (BaseBullet bullet : world.getEnemyBullets()) {
            if (bullet.notValid()) {
                world.getPublisher().removeObserver(bullet);
            }
        }
        for (AbstractAircraft enemy : world.getEnemies()) {
            if (enemy.notValid()) {
                world.getPublisher().removeObserver(enemy);
            }
        }
        world.getEnemyBullets().removeIf(AbstractFlyingObject::notValid);
        world.getHeroBullets().removeIf(AbstractFlyingObject::notValid);
        world.getEnemies().removeIf(AbstractFlyingObject::notValid);
        List<BaseProp> props = world.getProps();
        props.removeIf(AbstractFlyingObject::notValid);
    }
}
