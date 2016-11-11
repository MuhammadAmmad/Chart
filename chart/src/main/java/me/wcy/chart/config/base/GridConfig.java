package me.wcy.chart.config.base;

import android.graphics.Color;

import me.wcy.chart.ChartUtils;

public abstract class GridConfig {
    private long enterAnimationDuration = 600;

    private boolean isTouchable = true;

    private int gridLineColor = Color.LTGRAY;
    private int maxTitleCount = 5;

    private float textSize = ChartUtils.sp2px(12);
    private int textColor = Color.GRAY;

    public long getEnterAnimationDuration() {
        return enterAnimationDuration;
    }

    public void setEnterAnimationDuration(long enterAnimationDuration) {
        this.enterAnimationDuration = enterAnimationDuration;
    }

    public boolean isTouchable() {
        return isTouchable;
    }

    public void setTouchable(boolean touchable) {
        isTouchable = touchable;
    }

    public int getGridLineColor() {
        return gridLineColor;
    }

    public void setGridLineColor(int gridLineColor) {
        this.gridLineColor = gridLineColor;
    }

    public int getMaxTitleCount() {
        return maxTitleCount;
    }

    public void setMaxTitleCount(int maxTitleCount) {
        this.maxTitleCount = maxTitleCount;
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }
}
