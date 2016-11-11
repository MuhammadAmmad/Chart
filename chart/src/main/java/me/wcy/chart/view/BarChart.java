package me.wcy.chart.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import me.wcy.chart.ChartUtils;
import me.wcy.chart.view.base.GridChart;
import me.wcy.chart.config.BarConfig;
import me.wcy.chart.data.GridData;

public class BarChart extends GridChart {
    private Paint barPaint = new Paint();

    public BarChart(Context context) {
        this(context, null);
    }

    public BarChart(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BarChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setupPaints();
    }

    @Override
    protected BarConfig getConfig() {
        if (config == null) {
            config = new BarConfig();
        }
        return (BarConfig) config;
    }

    @Override
    protected void setupPaints() {
        super.setupPaints();

        // 柱状图画笔
        barPaint.setAntiAlias(true);
    }

    @Override
    protected void drawDesc(Canvas canvas) {
        float blockLength = getTextHeight() / 2;
        float spacing1 = ChartUtils.dp2px(5);
        float spacing2 = ChartUtils.dp2px(15);

        float descMaxLength = (blockLength + spacing1) * dataList.get(0).getEntries().length +
                spacing2 * (dataList.get(0).getEntries().length - 1);
        for (GridData.Entry entry : dataList.get(0).getEntries()) {
            descMaxLength += textPaint.measureText(entry.getDesc());
        }

        float descStartX = (getWidth() - horizontalOffset - descMaxLength) / 2 + horizontalOffset;

        textPaint.setTextAlign(Paint.Align.LEFT);
        for (GridData.Entry entry : dataList.get(0).getEntries()) {
            barPaint.setColor(entry.getLineColor());
            textPaint.setColor(entry.getLineColor());
            canvas.drawRect(descStartX, getHeight() - (getTextHeight() + blockLength) / 2,
                    descStartX + blockLength, getHeight() - (getTextHeight() - blockLength) / 2, barPaint);

            descStartX += blockLength + spacing1;
            canvas.drawText(entry.getDesc(), descStartX, getHeight() - getTextOffsetY(), textPaint);
            descStartX += textPaint.measureText(entry.getDesc()) + spacing2;
        }
    }

    @Override
    protected void drawTitle(Canvas canvas) {
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(getConfig().getTextColor());
        for (int i = 0; i < renderTitleList.size(); i++) {
            int index = renderTitleList.get(i);
            String title = dataList.get(index).getTitle();
            float drawX = getScaledItemWidth() * (index + 0.5f);
            canvas.drawText(title, drawX, getChartBottom() + getBottomTextHeight() - getTextOffsetY(), textPaint);
        }
    }

    @Override
    protected void drawContent(Canvas canvas) {
        // 绘制柱状图
        for (int i = firstRenderItem; i <= lastRenderItem; i++) {
            GridData.Entry[] entries = dataList.get(i).getEntries();
            // 设定间距为柱宽的1/3
            float spacing = getScaledItemWidth() / (entries.length * 4 + 1);
            float barWidth = spacing * 3;
            for (int j = 0; j < entries.length; j++) {
                barPaint.setColor(entries[j].getLineColor());
                float left = getScaledItemWidth() * i + spacing + (barWidth + spacing) * j;
                float right = left + barWidth;
                float bottom = getChartBottom();
                float top = bottom - entries[j].getValue() * getItemHeightRatio() * enterFraction;
                canvas.drawRect(left, top, right, bottom, barPaint);
            }
        }
    }

    @Override
    protected void calculateRenderRange() {
        firstRenderItem = 0;
        lastRenderItem = dataList.size() - 1;
        for (int i = 0; i < dataList.size(); i++) {
            if (getScaledItemWidth() * (i + 1) + translateX > 0) {
                firstRenderItem = i;
                break;
            }
        }

        for (int i = firstRenderItem; i < dataList.size(); i++) {
            if (getScaledItemWidth() * (i + 1) + translateX >= getChartWidth()) {
                lastRenderItem = i;
                break;
            }
        }
    }
}
