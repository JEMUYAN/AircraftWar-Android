package edu.hitsz.aircraftwar2026.game;

import edu.hitsz.aircraftwar2026.basic.ImageKey;

/**
 * 游戏难度（枚举，取代旧版的难度字符串，优化清单 #7）。
 */
public enum Difficulty {
    EASY("easy", ImageKey.BG_EASY),
    NORMAL("normal", ImageKey.BG_NORMAL),
    HARD("hard", ImageKey.BG_HARD);

    private final String id;
    private final ImageKey background;

    Difficulty(String id, ImageKey background) {
        this.id = id;
        this.background = background;
    }

    /** 稳定的字符串标识（用于 Intent extra、存储等），不要随意修改 */
    public String getId() {
        return id;
    }

    /** 该难度的背景图 */
    public ImageKey getBackground() {
        return background;
    }

    /** 创建该难度的规则对象 */
    public DifficultyRules createRules() {
        switch (this) {
            case EASY:
                return new EasyRules();
            case NORMAL:
                return new NormalRules();
            case HARD:
                return new HardRules();
            default:
                throw new IllegalStateException("未知难度: " + this);
        }
    }

    /** 由标识解析难度，无法识别时返回 EASY */
    public static Difficulty fromId(String id) {
        for (Difficulty d : values()) {
            if (d.id.equals(id)) {
                return d;
            }
        }
        return EASY;
    }
}
