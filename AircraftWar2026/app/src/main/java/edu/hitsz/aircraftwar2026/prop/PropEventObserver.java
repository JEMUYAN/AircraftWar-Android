package edu.hitsz.aircraftwar2026.prop;

/**
 * 道具事件观察者（观察者模式）。炸弹、冰冻道具生效时向所有观察者广播。
 */
public interface PropEventObserver {

    /** 炸弹事件 */
    void onBombEvent();

    /** 冰冻事件 */
    void onFrozenEvent();

    /** 观察者是否仍然有效；失效的观察者不再收到广播 */
    boolean isAlive();
}
