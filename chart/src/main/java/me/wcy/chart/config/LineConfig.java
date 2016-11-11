package me.wcy.chart.config;

import me.wcy.chart.ChartUtils;
import me.wcy.chart.config.base.GridConfig;

/**
 * Created by hzwangchenyan on 2016/10/10.
 */
public class LineConfig extends GridConfig {
    private float lineWidth = ChartUtils.dp2px(2);
    private boolean isShowShadow = true;
    private boolean isCurvedLine = false;

    public float getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
    }

    public boolean isShowShadow() {
        return isShowShadow;
    }

    public void setShowShadow(boolean showShadow) {
        isShowShadow = showShadow;
    }

    public boolean isCurvedLine() {
        return isCurvedLine;
    }

    public void setCurvedLine(boolean curvedLine) {
        isCurvedLine = curvedLine;
    }
}
