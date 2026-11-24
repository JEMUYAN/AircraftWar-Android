package edu.hitsz.aircraftwar2026.game;

import edu.hitsz.aircraftwar2026.aircraft.AbstractAircraft;

/**
 * 困难难度：敌机多且快，Boss 血量逐次递增，难度提升更快。
 */
public class HardRules extends DifficultyRules {

    private static final double MOB_PROB = 0.25;
    private static final double ELITE_PROB = 0.30;
    private static final double SUPERIOR_PROB = 0.25;
    // 王牌概率 = 0.20

    private static final int DIFFICULTY_INCREASE_INTERVAL = 200;
    private static final double ENEMY_CYCLE_DECREASE = 0.4;
    private static final double ENEMY_CYCLE_MIN = 15.0;
    private static final double ATTRIBUTE_MULTIPLIER_INCREASE = 0.03;
    private static final double SHOOT_CYCLE_INCREASE = 0.2;
    private static final double SHOOT_CYCLE_MAX = 25.0;

    /** Boss 每次召唤增加的血量 */
    private static final int BOSS_HP_INCREASE = 50;

    @Override
    public Difficulty getDifficulty() {
        return Difficulty.HARD;
    }

    @Override
    public void configure(DifficultyState state) {
        state.enemyMaxNumber = 7;
        state.enemySpawnCycle = 30;
        state.shootCycle = 15;
        state.bossScoreThreshold = 50;
        state.bossScoreIncrement = 100;
    }

    @Override
    public AbstractAircraft generateEnemy(int locX, int locY, DifficultyState state,
                                          double roll, double coin) {
        return spawnByProbability(locX, locY, state, roll, coin, MOB_PROB, ELITE_PROB, SUPERIOR_PROB);
    }

    @Override
    public int bossHp(int spawnCount) {
        return BOSS_BASE_HP + (spawnCount - 1) * BOSS_HP_INCREASE;
    }

    @Override
    public void increaseDifficulty(DifficultyState state) {
        if (state.difficultyCounter % DIFFICULTY_INCREASE_INTERVAL == 0 && state.difficultyCounter > 0) {
            if (state.enemySpawnCycle > ENEMY_CYCLE_MIN) {
                state.enemySpawnCycle -= ENEMY_CYCLE_DECREASE;
            }
            state.enemyAttributeMultiplier += ATTRIBUTE_MULTIPLIER_INCREASE;
            if (state.shootCycle < SHOOT_CYCLE_MAX) {
                state.shootCycle += SHOOT_CYCLE_INCREASE;
            }
        }
    }
}
