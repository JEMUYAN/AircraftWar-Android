package edu.hitsz.aircraftwar2026.prop;

import java.util.ArrayList;
import java.util.List;

/**
 * 道具事件发布者（被观察者/Subject）：管理观察者的注册与注销，并广播道具事件。
 */
public class PropEventPublisher {

    private final List<PropEventObserver> observers = new ArrayList<>();

    /** 注册观察者（重复注册无效） */
    public void registerObserver(PropEventObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /** 注销观察者 */
    public void removeObserver(PropEventObserver observer) {
        observers.remove(observer);
    }

    /** 广播炸弹事件 */
    public void notifyBombEvent() {
        // 复制一份，避免回调中修改列表
        for (PropEventObserver observer : new ArrayList<>(observers)) {
            if (observer.isAlive()) {
                observer.onBombEvent();
            }
        }
    }

    /** 广播冰冻事件 */
    public void notifyFrozenEvent() {
        for (PropEventObserver observer : new ArrayList<>(observers)) {
            if (observer.isAlive()) {
                observer.onFrozenEvent();
            }
        }
    }

    public int getObserverCount() {
        return observers.size();
    }
}
