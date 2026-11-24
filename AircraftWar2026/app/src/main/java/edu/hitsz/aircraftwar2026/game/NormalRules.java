package edu.hitsz.aircraftwar2026.game;

import edu.hitsz.aircraftwar2026.aircraft.AbstractAircraft;

/**
 * 普通难度：出现 Boss；随时间缩短敌机生成周期并提升敌机属性。
 */
public class NormalRules extends DifficultyRules {

    private static final double MOB_PROB = 0.40;
    private static final double ELITE_PROB = 0.30;
    private static final double SUPERIOR_PROB = 0.20;
    // 王牌概率 = 0.10

    /** 难度提升间隔（帧） */
    private static final int DIFFICULTY_INCREASE_INTERVAL = 300;
    private static final double ENEMY_CYCLE_DECREASE = 0.4;
    private static final double ENEMY_CYCLE_MIN = 20.0;
    private static final double ATTRIBUTE_MULTIPLIER_INCREASE = 0.02;

    @Override
    public Difficulty getDifficulty() {
        return Difficulty.NORMAL;
    }

    @Override
    public void configure(DifficultyState state) {
        state.enemyMaxNumber = 5;
        state.enemySpawnCycle = 40;
        state.shootCycle = 20;
        state.bossScoreThreshold = 50;
        state.bossScoreIncrement = 100;
    }

    @Override
    public AbstractAircraft generateEnemy(int locX, int locY, DifficultyState state,
                                          double roll, double coin) {
        return spawnByProbability(locX, locY, state, roll, coin, MOB_PROB, ELITE_PROB, SUPERIOR_PROB);
    }

    @Override
    public void increaseDifficulty(DifficultyState state) {
        if (state.difficultyCounter % DIFFICULTY_INCREASE_INTERVAL == 0 && state.difficultyCounter > 0) {
            if (state.enemySpawnCycle > ENEMY_CYCLE_MIN) {
                state.enemySpawnCycle -= ENEMY_CYCLE_DECREASE;
                state.enemyAttributeMultiplier += ATTRIBUTE_MULTIPLIER_INCREASE;
            }
        }
    }
}
