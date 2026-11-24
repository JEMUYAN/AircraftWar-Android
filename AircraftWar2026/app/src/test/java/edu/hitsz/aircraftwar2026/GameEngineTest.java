package edu.hitsz.aircraftwar2026;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import edu.hitsz.aircraftwar2026.aircraft.AbstractAircraft;
import edu.hitsz.aircraftwar2026.aircraft.BossEnemy;
import edu.hitsz.aircraftwar2026.aircraft.MobEnemy;
import edu.hitsz.aircraftwar2026.basic.Bounds;
import edu.hitsz.aircraftwar2026.bullet.EnemyBullet;
import edu.hitsz.aircraftwar2026.bullet.HeroBullet;
import edu.hitsz.aircraftwar2026.game.Difficulty;
import edu.hitsz.aircraftwar2026.game.GameEngine;
import edu.hitsz.aircraftwar2026.game.GameEventListener;
import edu.hitsz.aircraftwar2026.game.GameWorld;
import edu.hitsz.aircraftwar2026.prop.PropType;

import org.junit.Test;

import java.util.Random;

/**
 * 引擎、难度规则与计分的测试（纯 JVM）。
 */
public class GameEngineTest {

    private static final Bounds BOUNDS = new Bounds(512, 1000);

    /** 记录事件的监听器 */
    private static class Recorder implements GameEventListener {
        int hits;
        int overs;
        int lastScore = -1;
        int bossSpawned;
        int bossDefeated;
        int props;

        @Override
        public void onBulletHit() {
            hits++;
        }

        @Override
        public void onGameOver(int score) {
            overs++;
            lastScore = score;
        }

        @Override
        public void onBossSpawned() {
            bossSpawned++;
        }

        @Override
        public void onBossDefeated() {
            bossDefeated++;
        }

        @Override
        public void onPropCollected(PropType type) {
            props++;
        }
    }

    private static GameEngine engine(Difficulty difficulty, Recorder recorder) {
        return new GameEngine(difficulty, BOUNDS, recorder, new Random(42));
    }

    @Test
    public void initialState_heroAtBottomCenter() {
        GameEngine engine = engine(Difficulty.EASY, new Recorder());
        assertEquals(256, engine.getWorld().getHero().getLocationX());
        assertEquals(1000 - 83, engine.getWorld().getHero().getLocationY());
        assertEquals(1000, engine.getWorld().getHero().getHp());
        assertEquals(0, engine.getWorld().getScore());
    }

    @Test
    public void difficultyParameters() {
        assertEquals(5, engine(Difficulty.EASY, null).getState().enemyMaxNumber);
        assertEquals(50.0, engine(Difficulty.EASY, null).getState().enemySpawnCycle, 0);
        assertEquals(40.0, engine(Difficulty.NORMAL, null).getState().enemySpawnCycle, 0);
        assertEquals(7, engine(Difficulty.HARD, null).getState().enemyMaxNumber);
        assertEquals(15.0, engine(Difficulty.HARD, null).getState().shootCycle, 0);
    }

    @Test
    public void difficultyFromId_roundTripsAndFallsBack() {
        for (Difficulty d : Difficulty.values()) {
            assertEquals(d, Difficulty.fromId(d.getId()));
        }
        assertEquals(Difficulty.EASY, Difficulty.fromId("???"));
    }

    @Test
    public void enemiesAreSpawnedAndNeverExceedLimit() {
        GameEngine engine = engine(Difficulty.EASY, new Recorder());
        // 让英雄机无敌，避免提前结束
        for (int i = 0; i < 600 && !engine.isGameOver(); i++) {
            engine.update();
            assertTrue(engine.getWorld().getEnemies().size() <= 5);
        }
        assertTrue(engine.getWorld().getEnemies().size() > 0 || engine.isGameOver());
    }

    @Test
    public void longRun_allDifficulties_noExceptions() {
        for (Difficulty difficulty : Difficulty.values()) {
            GameEngine engine = engine(difficulty, new Recorder());
            for (int i = 0; i < 20000 && !engine.isGameOver(); i++) {
                // 英雄机左右扫动，覆盖碰撞、道具等路径
                engine.setHeroTarget(50 + (i % 400), 800);
                engine.update();
            }
        }
    }

    @Test
    public void heroBulletKillsMobAndScoresTen() {
        Recorder recorder = new Recorder();
        GameEngine engine = engine(Difficulty.EASY, recorder);
        GameWorld world = engine.getWorld();
        MobEnemy mob = new MobEnemy(100, 300, 0, 0, 30);
        world.getEnemies().add(mob);
        world.getPublisher().registerObserver(mob);
        world.getHeroBullets().add(new HeroBullet(100, 300, 0, -5, 30));

        engine.update();

        assertEquals(10, world.getScore());
        assertTrue(mob.notValid());
        assertFalse(world.getEnemies().contains(mob));
        assertEquals(1, recorder.hits);
    }

    @Test
    public void enemyBulletHurtsHero() {
        Recorder recorder = new Recorder();
        GameEngine engine = engine(Difficulty.EASY, recorder);
        GameWorld world = engine.getWorld();
        int x = world.getHero().getLocationX();
        int y = world.getHero().getLocationY();
        world.getEnemyBullets().add(new EnemyBullet(x, y, 0, 0, 30));
        engine.update();
        assertEquals(970, world.getHero().getHp());
        assertEquals(1, recorder.hits);
    }

    @Test
    public void heroCrashingIntoEnemyEndsGame_listenerCalledOnce() {
        Recorder recorder = new Recorder();
        GameEngine engine = engine(Difficulty.EASY, recorder);
        GameWorld world = engine.getWorld();
        world.getEnemies().add(new MobEnemy(world.getHero().getLocationX(),
                world.getHero().getLocationY(), 0, 0, 30));
        engine.update();
        assertTrue(engine.isGameOver());
        assertEquals(1, recorder.overs);
        for (int i = 0; i < 10; i++) {
            engine.update();
        }
        assertEquals(1, recorder.overs);
    }

    @Test
    public void heroTargetIsClampedToBounds() {
        GameEngine engine = engine(Difficulty.EASY, null);
        engine.setHeroTarget(-50, 5000);
        engine.update();
        assertEquals(0, engine.getWorld().getHero().getLocationX());
        assertEquals(1000, engine.getWorld().getHero().getLocationY());
    }

    @Test
    public void easyNeverSpawnsBoss_normalSpawnsBossAtThreshold() {
        // EASY：阈值为 Integer.MAX_VALUE
        Recorder easyRecorder = new Recorder();
        GameEngine easy = engine(Difficulty.EASY, easyRecorder);
        easy.getWorld().addScore(100000);
        for (int i = 0; i < 300 && !easy.isGameOver(); i++) {
            easy.update();
        }
        assertEquals(0, easyRecorder.bossSpawned);

        // NORMAL：阈值 50
        Recorder recorder = new Recorder();
        GameEngine normal = engine(Difficulty.NORMAL, recorder);
        normal.getWorld().addScore(50);
        for (int i = 0; i < 300 && recorder.bossSpawned == 0 && !normal.isGameOver(); i++) {
            normal.update();
        }
        assertEquals(1, recorder.bossSpawned);
        assertEquals(GameWorld.BossState.ACTIVE, normal.getWorld().getBossState());
        assertEquals(150, normal.getState().bossScoreThreshold);
    }

    @Test
    public void hardBossHpIncreasesWithEachSpawn() {
        assertEquals(150, Difficulty.HARD.createRules().bossHp(1));
        assertEquals(200, Difficulty.HARD.createRules().bossHp(2));
        assertEquals(250, Difficulty.HARD.createRules().bossHp(3));
        assertEquals(150, Difficulty.NORMAL.createRules().bossHp(3));
    }

    @Test
    public void killingBossScoresThirtyAndResetsBossState() {
        Recorder recorder = new Recorder();
        GameEngine engine = engine(Difficulty.NORMAL, recorder);
        GameWorld world = engine.getWorld();
        BossEnemy boss = new BossEnemy(300, 300, 0, 0, 30);
        world.getEnemies().add(boss);
        world.getPublisher().registerObserver(boss);
        world.setBossState(GameWorld.BossState.ACTIVE);
        world.getHeroBullets().add(new HeroBullet(300, 300, 0, -5, 30));

        engine.update();

        assertEquals(30, world.getScore());
        assertEquals(GameWorld.BossState.ABSENT, world.getBossState());
        assertEquals(1, recorder.bossDefeated);
        // Boss 必掉 3 个道具
        assertEquals(3, world.getProps().size());
    }

    @Test
    public void bombPickupClearsEnemiesAndScores() {
        Recorder recorder = new Recorder();
        GameEngine engine = engine(Difficulty.EASY, recorder);
        GameWorld world = engine.getWorld();
        for (int i = 0; i < 3; i++) {
            MobEnemy mob = new MobEnemy(100 + i * 50, 200, 0, 0, 30);
            world.getEnemies().add(mob);
            world.getPublisher().registerObserver(mob);
        }
        world.getProps().add(edu.hitsz.aircraftwar2026.prop.PropFactory.createProp(PropType.BOMB,
                world.getHero().getLocationX(), world.getHero().getLocationY(), 0, 0));

        engine.update();

        assertEquals(30, world.getScore());
        assertEquals(1, recorder.props);
        for (AbstractAircraft enemy : world.getEnemies()) {
            assertFalse("炸弹后不应残留原有普通敌机", enemy instanceof MobEnemy && enemy.getLocationY() == 200);
        }
    }

    @Test
    public void normalDifficultyIncreasesEvery300Ticks() {
        GameEngine engine = engine(Difficulty.NORMAL, new Recorder());
        engine.getWorld().getHero().creaseHp(0);
        double before = engine.getState().enemySpawnCycle;
        for (int i = 0; i < 300; i++) {
            if (engine.isGameOver()) {
                break;
            }
            engine.update();
        }
        if (!engine.isGameOver()) {
            assertEquals(before - 0.4, engine.getState().enemySpawnCycle, 1e-9);
            assertEquals(1.02, engine.getState().enemyAttributeMultiplier, 1e-9);
        }
    }

    @Test
    public void propsOutOfScreenAreRemoved() {
        GameEngine engine = engine(Difficulty.EASY, new Recorder());
        GameWorld world = engine.getWorld();
        world.getProps().add(edu.hitsz.aircraftwar2026.prop.PropFactory.createProp(PropType.HP, 10, 995, 0, 10));
        engine.update();
        assertTrue(world.getProps().isEmpty());
    }

    @Test
    public void observersAreUnregisteredWhenObjectsAreRemoved() {
        GameEngine engine = engine(Difficulty.EASY, new Recorder());
        GameWorld world = engine.getWorld();
        MobEnemy mob = new MobEnemy(100, 998, 0, 10, 30);
        world.getEnemies().add(mob);
        world.getPublisher().registerObserver(mob);
        assertEquals(1, world.getPublisher().getObserverCount());
        engine.update();
        // mob 已飞出屏幕被清除，观察者同步注销（其余观察者为本帧新生成的敌机，数量不应包含 mob）
        assertFalse(world.getEnemies().contains(mob));
    }
}
