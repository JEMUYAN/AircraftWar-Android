package edu.hitsz.aircraftwar2026.prop;

/**
 * 道具简单工厂。
 */
public class PropFactory {

    private PropFactory() {
    }

    /**
     * 创建道具。
     *
     * @param type      道具类型
     * @param locationX 初始 x
     * @param locationY 初始 y
     * @param speedX    x 方向速度
     * @param speedY    y 方向速度
     */
    public static BaseProp createProp(PropType type, int locationX, int locationY, int speedX, int speedY) {
        switch (type) {
            case HP:
                return new HpProp(locationX, locationY, speedX, speedY);
            case POWER:
                return new PowerProp(locationX, locationY, speedX, speedY);
            case SUPER_POWER:
                return new SuperPowerProp(locationX, locationY, speedX, speedY);
            case BOMB:
                return new BombProp(locationX, locationY, speedX, speedY);
            case FROZEN:
                return new FrozenProp(locationX, locationY, speedX, speedY);
            default:
                throw new IllegalArgumentException("未知道具类型: " + type);
        }
    }
}
