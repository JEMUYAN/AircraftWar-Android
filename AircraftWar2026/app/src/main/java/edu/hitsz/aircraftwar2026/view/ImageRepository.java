package edu.hitsz.aircraftwar2026.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import edu.hitsz.aircraftwar2026.R;
import edu.hitsz.aircraftwar2026.basic.ImageKey;

import java.util.EnumMap;
import java.util.Map;

/**
 * 图片仓库：一次性解码并缓存全部 Bitmap，帧循环中只读取、不再 decode。
 * 逻辑层的 {@link ImageKey} 与 R.drawable 资源的映射只存在于这里。
 */
public class ImageRepository {

    private final Map<ImageKey, Bitmap> cache = new EnumMap<>(ImageKey.class);

    /**
     * 加载全部图片。
     *
     * @param context 任意 Context，内部取 ApplicationContext，避免持有 Activity
     */
    public void load(Context context) {
        Context app = context.getApplicationContext();
        // inScaled=false：按素材原始像素解码，不随屏幕密度缩放，
        // 否则 Bitmap 尺寸会与 ImageKey 中的逻辑尺寸不一致
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        for (ImageKey key : ImageKey.values()) {
            cache.put(key, BitmapFactory.decodeResource(app.getResources(), resourceOf(key), options));
        }
    }

    /** 取图；未加载时返回 null */
    public Bitmap get(ImageKey key) {
        return cache.get(key);
    }

    /** 释放全部 Bitmap */
    public void release() {
        cache.clear();
    }

    private static int resourceOf(ImageKey key) {
        switch (key) {
            case BG_EASY:
                return R.drawable.bg;
            case BG_NORMAL:
                return R.drawable.bg3;
            case BG_HARD:
                return R.drawable.bg5;
            case HERO:
                return R.drawable.hero;
            case MOB:
                return R.drawable.mob;
            case ELITE:
                return R.drawable.elite;
            case SUPERIOR:
                return R.drawable.eliteplus;
            case ACE:
                return R.drawable.elitepro;
            case BOSS:
                return R.drawable.boss;
            case HERO_BULLET:
                return R.drawable.bullet_hero;
            case ENEMY_BULLET:
                return R.drawable.bullet_enemy;
            case PROP_BLOOD:
                return R.drawable.prop_blood;
            case PROP_BOMB:
                return R.drawable.prop_bomb;
            case PROP_BULLET:
                return R.drawable.prop_bullet;
            case PROP_BULLETPLUS:
                return R.drawable.prop_bulletplus;
            case PROP_FREEZE:
                return R.drawable.prop_freeze;
            default:
                throw new IllegalArgumentException("未映射的图片: " + key);
        }
    }
}
