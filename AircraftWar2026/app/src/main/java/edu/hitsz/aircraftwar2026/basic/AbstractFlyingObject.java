package edu.hitsz.aircraftwar2026.basic;

/**
 * 可飞行对象的基类（逻辑层，不依赖 android.*，不持有图片）。
 *
 * @author hitsz
 */
public abstract class AbstractFlyingObject {

    /** 图片中心位置坐标 */
    protected int locationX;
    protected int locationY;

    /** x、y 轴移动速度 */
    protected int speedX;
    protected int speedY;

    /** 有效（生存）标记，标记为 false 的对象会在后处理阶段被清除 */
    protected boolean isValid = true;

    public AbstractFlyingObject(int locationX, int locationY, int speedX, int speedY) {
        this.locationX = locationX;
        this.locationY = locationY;
        this.speedX = speedX;
        this.speedY = speedY;
    }

    /**
     * 对应的图片标识，渲染层据此取图，逻辑层据此取尺寸。
     */
    public abstract ImageKey getImageKey();

    /**
     * 根据速度移动；触碰横向边界时横向速度反向。
     *
     * @param bounds 世界边界
     */
    public void forward(Bounds bounds) {
        locationX += speedX;
        locationY += speedY;
        if (locationX <= 0 || locationX >= bounds.getWidth()) {
            speedX = -speedX;
        }
    }

    /**
     * 每个逻辑帧调用一次，用于推进冻结、道具效果等计时器（取代旧版的 sleep 线程）。
     */
    public void tick() {
    }

    /**
     * 碰撞判定时纵向范围的缩小倍数：飞机取 2（只判定中间一半高度），其余取 1。
     */
    protected int hitHeightDivisor() {
        return 1;
    }

    /**
     * 碰撞检测，当对方坐标进入我方范围，判定我方击中。
     * <br>
     * 非飞机对象区域：横向 [x - width/2, x + width/2]，纵向 [y - height/2, y + height/2]。
     * <br>
     * 飞机对象区域：横向 [x - width/2, x + width/2]，纵向 [y - height/4, y + height/4]。
     *
     * @param flyingObject 撞击对方
     * @return true: 我方被击中; false: 我方未被击中
     */
    public boolean crash(AbstractFlyingObject flyingObject) {
        int factor = this.hitHeightDivisor();
        int fFactor = flyingObject.hitHeightDivisor();

        int x = flyingObject.getLocationX();
        int y = flyingObject.getLocationY();
        int fWidth = flyingObject.getWidth();
        int fHeight = flyingObject.getHeight();

        return x + (fWidth + this.getWidth()) / 2 > locationX
                && x - (fWidth + this.getWidth()) / 2 < locationX
                && y + (fHeight / fFactor + this.getHeight() / factor) / 2 > locationY
                && y - (fHeight / fFactor + this.getHeight() / factor) / 2 < locationY;
    }

    public int getLocationX() {
        return locationX;
    }

    public int getLocationY() {
        return locationY;
    }

    public int getSpeedX() {
        return speedX;
    }

    public int getSpeedY() {
        return speedY;
    }

    public void setLocation(double locationX, double locationY) {
        this.locationX = (int) locationX;
        this.locationY = (int) locationY;
    }

    public int getWidth() {
        return getImageKey().getWidth();
    }

    public int getHeight() {
        return getImageKey().getHeight();
    }

    /** 标记为失效，下一次后处理时清除 */
    public void vanish() {
        isValid = false;
    }

    public boolean notValid() {
        return !this.isValid;
    }
}
