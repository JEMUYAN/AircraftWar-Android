package edu.hitsz.aircraftwar2026.aircraft;

/**
 * 各机种的敌机工厂。每个工厂对应一种敌机，新增机种只需新增工厂，不改动调用方。
 */
public final class EnemyFactories {

    public static final EnemyFactory MOB = MobEnemy::new;
    public static final EnemyFactory ELITE = EliteEnemy::new;
    public static final EnemyFactory SUPERIOR = SuperiorEnemy::new;
    public static final EnemyFactory ACE = AceEnemy::new;
    public static final EnemyFactory BOSS = BossEnemy::new;

    private EnemyFactories() {
    }
}
