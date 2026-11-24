package edu.hitsz.aircraftwar2026;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import edu.hitsz.aircraftwar2026.aircraft.AbstractAircraft;
import edu.hitsz.aircraftwar2026.aircraft.AceEnemy;
import edu.hitsz.aircraftwar2026.aircraft.BossEnemy;
import edu.hitsz.aircraftwar2026.aircraft.EliteEnemy;
import edu.hitsz.aircraftwar2026.aircraft.HeroAircraft;
import edu.hitsz.aircraftwar2026.aircraft.MobEnemy;
import edu.hitsz.aircraftwar2026.aircraft.SuperiorEnemy;
import edu.hitsz.aircraftwar2026.basic.Bounds;
import edu.hitsz.aircraftwar2026.bullet.BaseBullet;
import edu.hitsz.aircraftwar2026.bullet.EnemyBullet;
import edu.hitsz.aircraftwar2026.bullet.HeroBullet;
import edu.hitsz.aircraftwar2026.prop.BaseProp;
import edu.hitsz.aircraftwar2026.prop.BombProp;
import edu.hitsz.aircraftwar2026.prop.FrozenProp;
import edu.hitsz.aircraftwar2026.prop.HpProp;
import edu.hitsz.aircraftwar2026.prop.PowerProp;
import edu.hitsz.aircraftwar2026.prop.PropEventPublisher;
import edu.hitsz.aircraftwar2026.prop.PropFactory;
import edu.hitsz.aircraftwar2026.prop.PropType;
import edu.hitsz.aircraftwar2026.prop.SuperPowerProp;
import edu.hitsz.aircraftwar2026.shoot.CircleShoot;
import edu.hitsz.aircraftwar2026.shoot.DirectShoot;
import edu.hitsz.aircraftwar2026.shoot.FanShoot;
import edu.hitsz.aircraftwar2026.shoot.SpreadShoot;

import org.junit.Test;

import java.util.List;

/**
 * 飞机、子弹、射击策略、道具与计时器的逻辑测试（纯 JVM，无需模拟器）。
 */
public class AircraftTest {

    private static final Bounds BOUNDS = new Bounds(512, 1000);

    private static HeroAircraft hero() {
        return new HeroAircraft(256, 900, 0, 0, 1000);
    }

    private static void tick(AbstractAircraft aircraft, int times) {
        for (int i = 0; i < times; i++) {
            aircraft.tick();
        }
    }

    // ---- 血量 ----

    @Test
    public void decreaseHp_clampsAtZeroAndVanishes() {
        HeroAircraft hero = hero();
        hero.decreaseHp(30);
        assertEquals(970, hero.getHp());
        assertFalse(hero.notValid());
        hero.decreaseHp(Integer.MAX_VALUE);
        assertEquals(0, hero.getHp());
        assertTrue(hero.notValid());
    }

    @Test
    public void creaseHp_clampsAtMax() {
        HeroAircraft hero = hero();
        hero.decreaseHp(10);
        hero.creaseHp(500);
        assertEquals(1000, hero.getHp());
    }

    @Test
    public void setLocation_truncatesToInt() {
        HeroAircraft hero = hero();
        hero.setLocation(10.9, 20.9);
        assertEquals(10, hero.getLocationX());
        assertEquals(20, hero.getLocationY());
    }

    // ---- 碰撞 ----

    @Test
    public void crash_overlappingObjectsHit() {
        MobEnemy mob = new MobEnemy(200, 200, 0, 0, 30);
        HeroBullet bullet = new HeroBullet(200, 200, 0, 0, 30);
        assertTrue(mob.crash(bullet));
    }

    @Test
    public void crash_aircraftOnlyUsesMiddleHalfOfHeight() {
        MobEnemy mob = new MobEnemy(200, 200, 0, 0, 30);
        // MOB 高 50，飞机纵向判定区间为 [y-12, y+12]，子弹高 25 再扩展 12 -> 约 18 以内
        assertTrue(mob.crash(new HeroBullet(200, 215, 0, 0, 30)));
        assertFalse(mob.crash(new HeroBullet(200, 235, 0, 0, 30)));
    }

    @Test
    public void crash_farApartDoesNotHit() {
        assertFalse(new MobEnemy(100, 100, 0, 0, 30).crash(new HeroBullet(400, 400, 0, 0, 30)));
    }

    // ---- 移动 ----

    @Test
    public void forward_bouncesAtHorizontalEdge() {
        MobEnemy mob = new MobEnemy(510, 100, 5, 0, 30);
        mob.forward(BOUNDS);
        assertEquals(-5, mob.getSpeedX());
    }

    @Test
    public void forward_enemyVanishesBelowBottom() {
        MobEnemy mob = new MobEnemy(100, 999, 0, 10, 30);
        mob.forward(BOUNDS);
        assertTrue(mob.notValid());
    }

    @Test
    public void forward_heroDoesNotMove() {
        HeroAircraft hero = hero();
        hero.forward(BOUNDS);
        assertEquals(256, hero.getLocationX());
        assertEquals(900, hero.getLocationY());
    }

    @Test
    public void forward_bulletVanishesOutOfScreen() {
        HeroBullet up = new HeroBullet(100, 3, 0, -5, 30);
        up.forward(BOUNDS);
        assertTrue(up.notValid());
        EnemyBullet down = new EnemyBullet(100, 998, 0, 5, 30);
        down.forward(BOUNDS);
        assertTrue(down.notValid());
    }

    @Test
    public void forward_propVanishesBelowBottom() {
        BaseProp prop = PropFactory.createProp(PropType.HP, 100, 999, 0, 5);
        prop.forward(BOUNDS);
        assertTrue(prop.notValid());
    }

    // ---- 射击策略 ----

    @Test
    public void directShoot_heroFiresOneHeroBulletUpwards() {
        HeroAircraft hero = hero();
        List<BaseBullet> bullets = new DirectShoot(hero).shoot();
        assertEquals(1, bullets.size());
        assertTrue(bullets.get(0) instanceof HeroBullet);
        assertEquals(-5, bullets.get(0).getSpeedY());
        assertEquals(30, bullets.get(0).getPower());
    }

    @Test
    public void directShoot_enemyFiresEnemyBulletDownwards() {
        EliteEnemy elite = new EliteEnemy(100, 100, 0, 10, 30);
        List<BaseBullet> bullets = elite.strategyShoot();
        assertEquals(1, bullets.size());
        assertTrue(bullets.get(0) instanceof EnemyBullet);
        assertEquals(15, bullets.get(0).getSpeedY());
    }

    @Test
    public void spreadShoot_fireTwoSideBySide() {
        List<BaseBullet> bullets = new SpreadShoot(hero()).shoot();
        assertEquals(2, bullets.size());
        assertEquals(246, bullets.get(0).getLocationX());
        assertEquals(266, bullets.get(1).getLocationX());
    }

    @Test
    public void fanShoot_hasThreeDifferentHorizontalSpeeds() {
        List<BaseBullet> bullets = new FanShoot(hero()).shoot();
        assertEquals(3, bullets.size());
        assertEquals(-2, bullets.get(0).getSpeedX());
        assertEquals(0, bullets.get(1).getSpeedX());
        assertEquals(2, bullets.get(2).getSpeedX());
    }

    @Test
    public void circleShoot_firesTwentyBullets() {
        BossEnemy boss = new BossEnemy(256, 50, 2, 0, 150);
        List<BaseBullet> bullets = new CircleShoot(boss).shoot();
        assertEquals(20, bullets.size());
        assertEquals(8, bullets.get(0).getSpeedX());
        assertEquals(0, bullets.get(0).getSpeedY());
    }

    @Test
    public void mobDoesNotShoot() {
        assertTrue(new MobEnemy(0, 0, 0, 10, 30).strategyShoot().isEmpty());
    }

    // ---- 道具与观察者 ----

    @Test
    public void hpProp_heals20() {
        HeroAircraft hero = hero();
        hero.decreaseHp(100);
        new HpProp(0, 0, 0, 5).getEffect(hero, new PropEventPublisher());
        assertEquals(920, hero.getHp());
    }

    @Test
    public void powerProp_switchesToSpreadThenRestoresAfter10Seconds() {
        HeroAircraft hero = hero();
        new PowerProp(0, 0, 0, 5).getEffect(hero, new PropEventPublisher());
        assertEquals(2, hero.strategyShoot().size());
        tick(hero, 249);
        assertEquals(2, hero.strategyShoot().size());
        tick(hero, 1);
        assertEquals(1, hero.strategyShoot().size());
        assertFalse(hero.hasEffect());
    }

    @Test
    public void newEffectOverridesAndRestartsTimer() {
        HeroAircraft hero = hero();
        PropEventPublisher publisher = new PropEventPublisher();
        new PowerProp(0, 0, 0, 5).getEffect(hero, publisher);
        tick(hero, 200);
        new SuperPowerProp(0, 0, 0, 5).getEffect(hero, publisher);
        assertEquals(3, hero.strategyShoot().size());
        tick(hero, 374);
        assertEquals(3, hero.strategyShoot().size());
        tick(hero, 1);
        assertEquals(1, hero.strategyShoot().size());
    }

    @Test
    public void bombProp_affectsEveryObserverAccordingToType() {
        PropEventPublisher publisher = new PropEventPublisher();
        MobEnemy mob = new MobEnemy(100, 100, 0, 10, 30);
        EliteEnemy elite = new EliteEnemy(100, 100, 0, 10, 30);
        SuperiorEnemy superior = new SuperiorEnemy(100, 100, 0, 10, 60);
        AceEnemy ace = new AceEnemy(100, 100, 3, 8, 90);
        BossEnemy boss = new BossEnemy(100, 100, 2, 0, 150);
        EnemyBullet bullet = new EnemyBullet(100, 100, 0, 5, 30);
        for (edu.hitsz.aircraftwar2026.prop.PropEventObserver o :
                new edu.hitsz.aircraftwar2026.prop.PropEventObserver[]{mob, elite, superior, ace, boss, bullet}) {
            publisher.registerObserver(o);
        }
        new BombProp(0, 0, 0, 5).getEffect(hero(), publisher);

        assertTrue(mob.notValid() && mob.isBombDestroyed());
        assertTrue(elite.notValid() && elite.isBombDestroyed());
        assertTrue(superior.notValid() && superior.isBombDestroyed());
        assertEquals(60, ace.getHp());
        assertFalse(ace.notValid());
        assertFalse(boss.notValid());
        assertEquals(150, boss.getHp());
        assertTrue(bullet.notValid());
    }

    @Test
    public void publisher_skipsInvalidObservers() {
        PropEventPublisher publisher = new PropEventPublisher();
        AceEnemy ace = new AceEnemy(100, 100, 3, 8, 90);
        publisher.registerObserver(ace);
        ace.vanish();
        publisher.notifyBombEvent();
        assertEquals(90, ace.getHp());
    }

    @Test
    public void publisher_registerIsIdempotentAndRemovable() {
        PropEventPublisher publisher = new PropEventPublisher();
        MobEnemy mob = new MobEnemy(0, 0, 0, 10, 30);
        publisher.registerObserver(mob);
        publisher.registerObserver(mob);
        assertEquals(1, publisher.getObserverCount());
        publisher.removeObserver(mob);
        assertEquals(0, publisher.getObserverCount());
    }

    // ---- 冰冻计时（帧计时，取代旧版 sleep 线程）----

    @Test
    public void elite_frozenFor4SecondsThenRecovers() {
        EliteEnemy elite = new EliteEnemy(100, 100, 0, 10, 30);
        new FrozenProp(0, 0, 0, 5).getEffect(hero(), publisherWith(elite));
        assertTrue(elite.isFrozen());
        assertEquals(0, elite.getSpeedY());
        tick(elite, 99);
        assertTrue(elite.isFrozen());
        tick(elite, 1);
        assertFalse(elite.isFrozen());
        assertEquals(10, elite.getSpeedY());
    }

    @Test
    public void superior_frozenFor3Seconds() {
        SuperiorEnemy superior = new SuperiorEnemy(100, 100, 2, 8, 60);
        new FrozenProp(0, 0, 0, 5).getEffect(hero(), publisherWith(superior));
        tick(superior, 74);
        assertTrue(superior.isFrozen());
        tick(superior, 1);
        assertFalse(superior.isFrozen());
    }

    @Test
    public void mob_frozenForever() {
        MobEnemy mob = new MobEnemy(100, 100, 0, 10, 30);
        new FrozenProp(0, 0, 0, 5).getEffect(hero(), publisherWith(mob));
        tick(mob, 10000);
        assertTrue(mob.isFrozen());
        assertEquals(0, mob.getSpeedY());
    }

    @Test
    public void ace_slowedForFiveSecondsThenRestored() {
        AceEnemy ace = new AceEnemy(100, 100, 3, 8, 90);
        new FrozenProp(0, 0, 0, 5).getEffect(hero(), publisherWith(ace));
        assertTrue(ace.isSlowed());
        assertEquals(4, ace.getSpeedY());
        assertEquals(1, ace.getSpeedX());
        tick(ace, 124);
        assertTrue(ace.isSlowed());
        tick(ace, 1);
        assertFalse(ace.isSlowed());
        assertEquals(8, ace.getSpeedY());
        assertEquals(3, ace.getSpeedX());
    }

    @Test
    public void enemyBullet_frozenFiveSecondsThenMovesAgain() {
        EnemyBullet bullet = new EnemyBullet(100, 100, 0, 5, 30);
        new FrozenProp(0, 0, 0, 5).getEffect(hero(), publisherWith(bullet));
        bullet.forward(BOUNDS);
        assertEquals(100, bullet.getLocationY());
        for (int i = 0; i < 125; i++) {
            bullet.tick();
        }
        assertFalse(bullet.isFrozen());
        bullet.forward(BOUNDS);
        assertEquals(105, bullet.getLocationY());
    }

    @Test
    public void unfreezeWhileSlowed_keepsHalfSpeed() {
        AceEnemy ace = new AceEnemy(100, 100, 3, 8, 90);
        ace.slow(AbstractAircraft.FOREVER);
        ace.freeze(AbstractAircraft.FOREVER);
        ace.unfreeze();
        assertEquals(4, ace.getSpeedY());
    }

    private static PropEventPublisher publisherWith(edu.hitsz.aircraftwar2026.prop.PropEventObserver observer) {
        PropEventPublisher publisher = new PropEventPublisher();
        publisher.registerObserver(observer);
        return publisher;
    }

    // ---- 掉落 ----

    @Test
    public void mobAndEliteNeverDropProps() {
        for (int i = 0; i < 200; i++) {
            assertTrue(new MobEnemy(0, 0, 0, 10, 30).dropProps().isEmpty());
            assertTrue(new EliteEnemy(0, 0, 0, 10, 30).dropProps().isEmpty());
        }
    }

    @Test
    public void bossAlwaysDropsThreeProps() {
        BossEnemy boss = new BossEnemy(100, 50, 2, 0, 150);
        List<BaseProp> props = boss.dropProps();
        assertEquals(3, props.size());
        assertEquals(110, props.get(0).getLocationX());
        assertEquals(100, props.get(1).getLocationX());
        assertEquals(90, props.get(2).getLocationX());
    }

    @Test
    public void superiorOnlyDropsFromItsPool() {
        SuperiorEnemy superior = new SuperiorEnemy(0, 0, 2, 8, 60);
        for (int i = 0; i < 300; i++) {
            for (BaseProp prop : superior.dropProps()) {
                assertTrue(prop.getType() != PropType.FROZEN);
            }
        }
    }
}
