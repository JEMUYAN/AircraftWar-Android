package edu.hitsz.aircraftwar2026.game;

import edu.hitsz.aircraftwar2026.aircraft.AbstractAircraft;

/**
 * 简单难度：不出 Boss，难度不随时间提升。
 */
public class EasyRules extends DifficultyRules {

    private static final double MOB_PROB = 0.50;
    private static final double ELITE_PROB = 0.30;
    private static final double SUPERIOR_PROB = 0.15;
    // 王牌概率 = 1 - 0.50 - 0.30 - 0.15 = 0.05

    @Override
    public Difficulty getDifficulty() {
        return Difficulty.EASY;
    }

    @Override
    public void configure(DifficultyState state) {
        state.enemyMaxNumber = 5;
        state.enemySpawnCycle = 50;
        state.shootCycle = 25;
        // 阈值取最大值，永远不会触发 Boss
        state.bossScoreThreshold = Integer.MAX_VALUE;
    }

    @Override
    public AbstractAircraft generateEnemy(int locX, int locY, DifficultyState state,
                                          double roll, double coin) {
        return spawnByProbability(locX, locY, state, roll, coin, MOB_PROB, ELITE_PROB, SUPERIOR_PROB);
    }
}
