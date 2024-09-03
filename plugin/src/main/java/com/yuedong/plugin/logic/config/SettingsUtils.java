package com.yuedong.plugin.logic.config;


import android.text.TextUtils;

import com.yuedong.plugin.logic.SdkApi;
import com.cosmos.beauty.module.beauty.AutoBeautyType;
import com.cosmos.beauty.module.beauty.MakeupType;
import com.cosmos.beauty.module.beauty.SimpleBeautyType;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 缓存/恢复美颜
 **/
public class SettingsUtils {
    private static Map<SimpleBeautyType, Float> simpleBeautyType = new HashMap<>();
    private static AutoBeautyType autoBeautyType;
    private static String makeupPath;
    private static float styleValue;
    private static float lutValue;
    private static String stickerPath;

    public static void recoverStatus(SdkApi api) {
        if (api == null) return;

        if (api.getBeautyModule() != null) {
            if (autoBeautyType != null && autoBeautyType != AutoBeautyType.AUTOBEAUTY_NULL) {
                api.getBeautyModule().setAutoBeauty(autoBeautyType);
            }
            for (Map.Entry<SimpleBeautyType, Float> entry : simpleBeautyType.entrySet()) {
                api.getBeautyModule().setValue(entry.getKey(), entry.getValue());
            }
        }

        if (!TextUtils.isEmpty(makeupPath) && api.getMakeupModule() != null) {
            api.getMakeupModule().addMakeup(makeupPath);
            api.getMakeupModule().setValue(MakeupType.MAKEUP_STYLE, styleValue);
            api.getMakeupModule().setValue(MakeupType.MAKEUP_LUT, lutValue);
        }

        if (!TextUtils.isEmpty(stickerPath) && api.getStickerModule() != null) {
            api.getStickerModule().addMaskModel(new File(stickerPath), maskModel -> {
            });
        }
    }

    public static void setSimpleBeautyValue(SimpleBeautyType type, float value) {
        if (value == 0) {
            simpleBeautyType.remove(type);
        } else if (value > 0) {
            simpleBeautyType.put(type, value);
        }
    }

    public static void setAutoBeauty(AutoBeautyType beautyType) {
        if (beautyType == AutoBeautyType.AUTOBEAUTY_NULL) {
            simpleBeautyType.clear();
        }
        autoBeautyType = beautyType;
    }

    public static void setMakeup(String path, float style_value, float lut_value) {
        makeupPath = path;
        styleValue = style_value;
        lutValue = lut_value;
    }

    public static void setSticker(String path) {
        stickerPath = path;
    }

    public static void clearMakeup() {
        makeupPath = null;
    }

    public static void clearSticker() {
        stickerPath = null;
    }
}
