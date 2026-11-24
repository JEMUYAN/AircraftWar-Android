package edu.hitsz.aircraftwar2026.game;

import edu.hitsz.aircraftwar2026.aircraft.AbstractAircraft;
import edu.hitsz.aircraftwar2026.aircraft.HeroAircraft;
import edu.hitsz.aircraftwar2026.basic.Bounds;
import edu.hitsz.aircraftwar2026.basic.ImageKey;
import edu.hitsz.aircraftwar2026.bullet.BaseBullet;
import edu.hitsz.aircraftwar2026.prop.BaseProp;
import edu.hitsz.aircraftwar2026.prop.PropEventPublisher;

import java.util.LinkedList;
import java.util.List;

/**
 * 一局游戏的全部状态：实体列表、分数、Boss 状态。仅数据，不含流程。
 */
public class GameWorld {

    /** Boss 状态（取代旧版松散的 hasBoss 布尔变量） */
    public enum BossState {
        ABSENT, ACTIVE
    }

    private final Bounds bounds;
    private final HeroAircraft hero;
    private final List<AbstractAircraft> enemies = new LinkedList<>();
    private final List<BaseBullet> heroBullets = new LinkedList<>();
    private final List<BaseBullet> enemyBullets = new LinkedList<>();
    private final List<BaseProp> props = new LinkedList<>();
    private final PropEventPublisher publisher = new PropEventPublisher();

    private int score = 0;
    private BossState bossState = BossState.ABSENT;

    public GameWorld(Bounds bounds) {
        this.bounds = bounds;
        this.hero = new HeroAircraft(
                bounds.getWidth() / 2,
                bounds.getHeight() - ImageKey.HERO.getHeight(),
                0, 0, HeroAircraft.DEFAULT_HP);
    }

    public Bounds getBounds() {
        return bounds;
    }

    public HeroAircraft getHero() {
        return hero;
    }

    public List<AbstractAircraft> getEnemies() {
        return enemies;
    }

    public List<BaseBullet> getHeroBullets() {
        return heroBullets;
    }

    public List<BaseBullet> getEnemyBullets() {
        return enemyBullets;
    }

    public List<BaseProp> getProps() {
        return props;
    }

    public PropEventPublisher getPublisher() {
        return publisher;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int delta) {
        score += delta;
    }

    public BossState getBossState() {
        return bossState;
    }

    public void setBossState(BossState bossState) {
        this.bossState = bossState;
    }

    public boolean isHeroDead() {
        return hero.getHp() <= 0;
    }
}
