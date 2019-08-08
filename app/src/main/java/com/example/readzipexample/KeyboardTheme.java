package com.example.readzipexample;

import androidx.annotation.StyleRes;

public class KeyboardTheme {

    @StyleRes
    private final int mThemeResId;
    @StyleRes
    private final int mPopupThemeResId;
    @StyleRes
    private final int mIconsThemeResId;
    @StyleRes
    private final int mPopupIconsThemeResId;
    public final ReflectResource mReflectResource;
    int INVALID_RES_ID = 0;

    @Override
    public String toString() {
        return "KeyboardTheme{" +
                "mThemeResId=" + mThemeResId +
                ", mPopupThemeResId=" + mPopupThemeResId +
                ", mIconsThemeResId=" + mIconsThemeResId +
                ", mPopupIconsThemeResId=" + mPopupIconsThemeResId +
                ", mReflectResource=" + mReflectResource +
                '}';
    }

    public KeyboardTheme(
            ReflectResource reflectResource,
            @StyleRes int themeResId,
            @StyleRes int popupThemeResId,
            @StyleRes int iconsThemeResId,
            @StyleRes int popupIconsThemeResId) {
        mReflectResource = reflectResource;
        mThemeResId = themeResId;
        mPopupThemeResId = popupThemeResId == INVALID_RES_ID ? mThemeResId : popupThemeResId;
        mIconsThemeResId = iconsThemeResId;
        mPopupIconsThemeResId =
                popupIconsThemeResId == INVALID_RES_ID ? mIconsThemeResId : popupIconsThemeResId;
    }

    @StyleRes
    public int getThemeResId() {
        return mThemeResId;
    }

    @StyleRes
    public int getPopupThemeResId() {
        return mPopupThemeResId;
    }

    @StyleRes
    public int getIconsThemeResId() {
        return mIconsThemeResId;
    }

    @StyleRes
    public int getPopupIconsThemeResId() {
        return mPopupIconsThemeResId;
    }
}
