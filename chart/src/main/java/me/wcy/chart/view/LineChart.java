package me.wcy.chart.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import me.wcy.chart.R;
import me.wcy.chart.ChartUtils;
import me.wcy.chart.view.base.GridChart;
import me.wcy.chart.config.LineConfig;
import me.wcy.chart.data.GridData;

/**
 * Created by hzwangchenyan on 2016/10/8.
 */
public class LineChart extends GridChart {
    private Paint linePaint = new Paint();
    private Paint tapLinePaint = new Paint();
    private Paint pointPaint = new Paint();
    private Paint clearPaint = new Paint();
    private Paint shadowPaint = new Paint();

    private Path path = new Path();

    private RectF tipsRect = new RectF();
    private int tapPosition = -1;

    public LineChart(Context context) {
        this(context, null);
    }

    public LineChart(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LineChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setupPaints();
    }

    @Override
    protected LineConfig getConfig() {
        if (config == null) {
            config = new LineConfig();
        }
        return (LineConfig) config;
    }

    @Override
    protected void setupPaints() {
        super.setupPaints();

        // 曲线画笔
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(getConfig().getLineWidth());
        linePaint.setStrokeCap(Paint.Cap.ROUND);

        // 点击选中线画笔
        tapLinePaint.setAntiAlias(true);
        tapLinePaint.setColor(0xFF979797);
        tapLinePaint.setStyle(Paint.Style.STROKE);
        tapLinePaint.setStrokeWidth(ChartUtils.dp2px(1));

        // 圆点画笔
        pointPaint.setAntiAlias(true);

        // 擦除画笔
        clearPaint.setAntiAlias(true);
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        // 阴影画笔
        shadowPaint.setAntiAlias(true);
    }

    @Override
    protected void onConfig() {
        super.onConfig();

        tapPosition = -1;
    }

    @Override
    protected void drawDesc(Canvas canvas) {
        float pointRadius = ChartUtils.dp2px(3.5f);
        float spacing1 = ChartUtils.dp2px(5);
        float spacing2 = ChartUtils.dp2px(15);
        float descMaxLength = (pointRadius * 2 + spacing1) * dataList.get(0).getEntries().length +
                spacing2 * (dataList.get(0).getEntries().length - 1);
        for (GridData.Entry entry : dataList.get(0).getEntries()) {
            descMaxLength += textPaint.measureText(entry.getDesc());
        }

        float descStartX = (getWidth() - horizontalOffset - descMaxLength) / 2 + horizontalOffset;
        float descMidY = getHeight() - getTextHeight() / 2;

        textPaint.setTextAlign(Paint.Align.LEFT);
        for (GridData.Entry entry : dataList.get(0).getEntries()) {
            pointPaint.setColor(entry.getLineColor());
            textPaint.setColor(entry.getLineColor());
            canvas.drawCircle(descStartX + pointRadius, descMidY, pointRadius, pointPaint);

            descStartX += pointRadius * 2 + spacing1;
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
        for (int index = 0; index < dataList.get(0).getEntries().length; ++index) {
            drawLine(canvas, index);
        }

        // 绘制tips
        if (tapPosition != -1) {
            drawTips(canvas);
        }
    }

    private void drawLine(Canvas canvas, int index) {
        // 绘制曲线
        float currentX, currentY, nextX, nextY;
        path.reset();
        for (int i = firstRenderItem; i < lastRenderItem; i++) {
            currentX = getScaledItemWidth() * (i + 0.5f);
            currentY = getChartBottom() - (dataList.get(i).getEntries()[index].getValue() * getItemHeightRatio());
            nextX = getScaledItemWidth() * (i + 1 + 0.5f);
            nextY = getChartBottom() - (dataList.get(i + 1).getEntries()[index].getValue() * getItemHeightRatio());

            if (i == firstRenderItem) {
                path.moveTo(currentX, currentY);
            }

            if (getConfig().isCurvedLine()) {
                float middleX = (currentX + nextX) / 2;
                path.cubicTo(middleX, currentY, middleX, nextY, nextX, nextY);
            } else {
                path.lineTo(nextX, nextY);
            }
        }
        linePaint.setColor(dataList.get(0).getEntries()[index].getLineColor());
        canvas.drawPath(path, linePaint);

        // 绘制阴影
        if (getConfig().isShowShadow() && dataList.size() > 1) {
            int color = dataList.get(0).getEntries()[index].getLineColor();
            int alphaColor = Color.argb((int) (Color.alpha(color) * 0.2), Color.red(color), Color.green(color), Color.blue(color));
            shadowPaint.setColor(alphaColor);
            float lastRenderX = getScaledItemWidth() * (lastRenderItem + 0.5f);
            path.lineTo(lastRenderX, getChartBottom());
            path.lineTo(getScaledItemWidth() * (firstRenderItem + 0.5f), getChartBottom());
            path.lineTo(getScaledItemWidth() * (firstRenderItem + 0.5f), getChartBottom() - (dataList.get(firstRenderItem).getEntries()[0].getValue() * getItemHeightRatio()));
            canvas.drawPath(path, shadowPaint);
        }
    }

    private void drawTips(Canvas canvas) {
        GridData tapData = dataList.get(tapPosition);

        canvas.drawLine(getScaledItemWidth() * (tapPosition + 0.5f), getChartBottom() - tapData.getMaxValue() * getItemHeightRatio() - ChartUtils.dp2px(25),
                getScaledItemWidth() * (tapPosition + 0.5f), getChartBottom(), tapLinePaint);

        float pointX = getScaledItemWidth() * (tapPosition + 0.5f);
        for (GridData.Entry entry : tapData.getEntries()) {
            float pointY = getChartBottom() - (entry.getValue() * getItemHeightRatio());
            pointPaint.setColor(entry.getLineColor());
            canvas.drawCircle(pointX, pointY, ChartUtils.dp2px(4), clearPaint);
            canvas.drawCircle(pointX, pointY, ChartUtils.dp2px(3), pointPaint);
        }

        drawTipsBackground(canvas);
        drawTipsText(canvas);
    }

    private void drawTipsBackground(Canvas canvas) {
        float textVerticalSpacing = ChartUtils.dp2px(10);
        float tipsPointRadius = ChartUtils.dp2px(4);
        float spacing1 = ChartUtils.dp2px(6);
        float spacing2 = ChartUtils.dp2px(20);
        float tipsPadding = ChartUtils.dp2px(15);
        float tipsMargin = ChartUtils.dp2px(15);

        GridData tapData = dataList.get(tapPosition);

        float maxDescWidth = 0;
        float maxValueWidth = 0;
        for (GridData.Entry entry : tapData.getEntries()) {
            float descWidth = textPaint.measureText(entry.getDesc());
            float valueWidth = textPaint.measureText(String.valueOf(entry.getValue()));
            maxDescWidth = Math.max(maxDescWidth, descWidth);
            maxValueWidth = Math.max(maxValueWidth, valueWidth);
        }
        float maxItemWidth = tipsPointRadius * 2 + spacing1 + maxDescWidth + spacing2 + maxValueWidth;
        float tipsTitleWidth = textPaint.measureText(tapData.getTitle());

        float rectWidth = Math.max(maxItemWidth, tipsTitleWidth) + tipsPadding * 2;
        float rectHeight = (getTextHeight() + textVerticalSpacing) * tapData.getEntries().length + getTextHeight() + tipsPadding * 2;

        float rectLeft = getScaledItemWidth() * (tapPosition + 0.5f) + tipsMargin;
        float rectBottom = (getChartBottom() - tapData.getMaxValue() * getItemHeightRatio()) - tipsMargin;
        tipsRect.set(rectLeft, rectBottom - rectHeight, rectLeft + rectWidth, rectBottom);

        if (tipsRect.right + translateX > getChartWidth() - tipsMargin) {
            tipsRect.offsetTo(getScaledItemWidth() * (tapPosition + 0.5f) - tipsMargin - tipsRect.width(), tipsRect.top);
        }
        if (tipsRect.top < 0) {
            tipsRect.offsetTo(tipsRect.left, (getChartBottom() - tapData.getMaxValue() * getItemHeightRatio()) + tipsMargin);
        }

        Drawable drawable = getContext().getResources().getDrawable(R.drawable.card_bg);
        drawable.setBounds((int) tipsRect.left, (int) tipsRect.top, (int) tipsRect.right, (int) tipsRect.bottom);
        drawable.draw(canvas);
    }

    private void drawTipsText(Canvas canvas) {
        float textVerticalSpacing = ChartUtils.dp2px(10);
        float tipsPointRadius = ChartUtils.dp2px(4);
        float spacing1 = ChartUtils.dp2px(6);
        float spacing2 = ChartUtils.dp2px(20);
        float tipsPadding = ChartUtils.dp2px(15);

        GridData tapData = dataList.get(tapPosition);

        float maxDescWidth = 0;
        for (GridData.Entry entry : tapData.getEntries()) {
            float descWidth = textPaint.measureText(entry.getDesc());
            maxDescWidth = Math.max(maxDescWidth, descWidth);
        }

        float textY = tipsRect.top + tipsPadding + getTextHeight();
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setColor(0xFF222222);
        canvas.drawText(tapData.getTitle(), tipsRect.left + tipsPadding, textY - getTextOffsetY(), textPaint);
        for (GridData.Entry entry : tapData.getEntries()) {
            textY += getTextHeight() + textVerticalSpacing;
            float startX = tipsRect.left + tipsPadding;
            pointPaint.setColor(entry.getLineColor());
            canvas.drawCircle(startX + tipsPointRadius, textY - getTextHeight() / 2, tipsPointRadius, pointPaint);

            startX += tipsPointRadius * 2 + spacing1;
            textPaint.setColor(entry.getLineColor());
            canvas.drawText(entry.getDesc(), startX, textY - getTextOffsetY(), textPaint);

            startX += maxDescWidth + spacing2;
            canvas.drawText(String.valueOf((int) entry.getValue()), startX, textY - getTextOffsetY(), textPaint);
        }
    }

    @Override
    protected void onTouchStart() {
        super.onTouchStart();

        tapPosition = -1;
        invalidate();
    }

    @Override
    protected boolean onSingleTap(float x, float y) {
        PointF point = new PointF(x - horizontalOffset, y);
        if (point.x < 0 || point.y > getChartBottom() + getBottomTextHeight()) {
            return false;
        }
        RectF rect = new RectF();
        for (int i = 0; i < dataList.size(); i++) {
            float measuredX = getScaledItemWidth() * (i + 0.5f) + translateX;
            if (measuredX < 0 || (int) measuredX > (int) (getWidth() - horizontalOffset)) {
                // 超出屏幕不关心
                continue;
            }
            if (dataList.get(i).getEntries()[0].getValue() < 0) {
                // 不合法
                continue;
            }
            float radius = defaultItemWidth * 0.5f;
            rect.left = measuredX - radius;
            rect.top = 0;
            rect.right = measuredX + radius;
            rect.bottom = getChartBottom() + getBottomTextHeight();
            if (rect.contains(point.x, point.y)) {
                tapPosition = (tapPosition == i) ? -1 : i;
                invalidate();
                return true;
            }
        }
        return false;
    }

    @Override
    protected void calculateRenderRange() {
        // 计算绘制起止点，超出屏幕区域不绘制
        firstRenderItem = 0;
        lastRenderItem = dataList.size() - 1;
        for (int i = 0; i < dataList.size() - 1; i++) {
            float nextX = getScaledItemWidth() * (i + 1 + 0.5f);

            if (nextX + translateX > 0) {
                firstRenderItem = i;
                break;
            }
        }

        for (int i = firstRenderItem + 1; i < dataList.size(); i++) {
            float x = getScaledItemWidth() * (i + 0.5f);

            if (x + translateX >= getChartWidth()) {
                lastRenderItem = i;
                break;
            }
        }

        for (int i = firstRenderItem; i <= lastRenderItem; i++) {
            if (dataList.get(i).getEntries()[0].getValue() < 0) {
                lastRenderItem = i - 1;
            }
        }
    }
}
