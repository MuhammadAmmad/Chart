package me.wcy.chart.view.base;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PorterDuff;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ScrollView;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.List;

import me.wcy.chart.ChartUtils;
import me.wcy.chart.config.base.GridConfig;
import me.wcy.chart.data.GridData;
import me.wcy.chart.gesture.ChartGestureDetector;
import me.wcy.chart.gesture.ChartGestureListener;

/**
 * Created by hzwangchenyan on 2016/10/9.
 */
public abstract class GridChart extends View {
    private static final int TEXT_MARGIN = ChartUtils.dp2px(5);
    private static final int ROW_COUNT = 5;
    private static final int MAX_SCALE = 3;
    private static final float FLING_MIN_VELOCITY_X = 200;

    private Paint solidLinePaint = new Paint();
    private Paint dashLinePaint = new Paint();
    protected TextPaint textPaint = new TextPaint();
    protected Paint.FontMetrics fontMetrics;
    private Path linePath = new Path();

    private ValueAnimator enterAnimator;
    protected float enterFraction = 1;
    private ValueAnimator doubleTapAnimator;
    private ValueAnimator flingAnimator;
    private Scroller flingScroller;

    private ScrollView scrollView;
    protected GridConfig config;
    private ChartGestureDetector gestureDetector;

    protected List<GridData> dataList = new ArrayList<>();
    protected List<Integer> renderTitleList = new ArrayList<>();

    protected float defaultItemWidth;
    private float itemHeight;
    private int gridHeight;
    protected float horizontalOffset;

    protected int firstRenderItem;
    protected int lastRenderItem;

    protected float translateX;
    private float scaleFocusX;
    private float scaleValue;

    private boolean hasLayout = false;
    private boolean isNeedAnimation = false;

    public GridChart(Context context) {
        this(context, null);
    }

    public GridChart(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GridChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        config = getConfig();

        gestureDetector = new ChartGestureDetector(getContext());
        gestureDetector.setGestureListener(gestureListener);

        enterAnimator = ValueAnimator.ofFloat(0, 1);
        enterAnimator.setDuration(config.getEnterAnimationDuration());
        enterAnimator.setInterpolator(new LinearInterpolator());
        enterAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                enterFraction = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
    }

    public void setDataList(List<GridData> dataList, boolean showAnimation) {
        if (enterAnimator.isRunning()) {
            enterAnimator.end();
        }

        this.dataList.clear();
        this.dataList.addAll(dataList);

        if (hasLayout) {
            onConfig();
            if (showAnimation) {
                enterAnimator.start();
            } else {
                invalidate();
            }
        } else {
            isNeedAnimation = showAnimation;
        }
    }

    public void setScrollView(ScrollView scrollView) {
        this.scrollView = scrollView;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!hasLayout) {
            hasLayout = true;
            onConfig();

            if (isNeedAnimation) {
                enterAnimator.start();
            }
        }
    }

    protected void onConfig() {
        translateX = 0;
        scaleValue = 1;
        gridHeight = calculateGridHeight();
        itemHeight = (getChartBottom() - getTextHeight()) / ROW_COUNT;
        horizontalOffset = textPaint.measureText(String.valueOf(gridHeight * 5)) + TEXT_MARGIN;
        defaultItemWidth = getChartWidth() / dataList.size();

        calculateRenderRange();
        calculateRenderTitle();
    }

    protected void setupPaints() {
        // 文字画笔
        textPaint.setAntiAlias(true);
        textPaint.setColor(config.getTextColor());
        textPaint.setTextSize(config.getTextSize());
        fontMetrics = textPaint.getFontMetrics();

        // 虚线画笔
        dashLinePaint.reset();
        dashLinePaint.setColor(config.getGridLineColor());
        dashLinePaint.setStyle(Paint.Style.STROKE);
        dashLinePaint.setStrokeWidth(1);
        // 设置虚线的间隔和点的长度
        float dot = ChartUtils.dp2px(1);
        PathEffect effects = new DashPathEffect(new float[]{dot, dot}, 1);
        dashLinePaint.setPathEffect(effects);

        // 实线画笔
        solidLinePaint.setColor(config.getGridLineColor());
        solidLinePaint.setStyle(Paint.Style.STROKE);
        solidLinePaint.setStrokeWidth(ChartUtils.dp2px(1));
    }

    private int calculateGridHeight() {
        float max = 0;
        for (GridData data : dataList) {
            max = Math.max(max, data.getMaxValue());
        }

        int gridHeight = (int) Math.ceil(max / 5);
        gridHeight = (gridHeight == 0) ? 1 : gridHeight;

        if (gridHeight > 5) {
            gridHeight = (int) Math.ceil((float) gridHeight / 5) * 5;
        }
        return gridHeight;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (dataList.isEmpty()) {
            canvas.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR);
            return;
        }

        drawGridLine(canvas);
        drawGridText(canvas);
        drawDesc(canvas);

        int count = canvas.save();
        canvas.clipRect(0, 0, getWidth() * enterFraction, getHeight());

        canvas.save();
        canvas.translate(horizontalOffset + translateX, 0);
        drawTitle(canvas);
        canvas.restore();

        canvas.save();
        canvas.translate(horizontalOffset, 0);
        canvas.clipRect(0, 0, getChartWidth(), getHeight());
        canvas.translate(translateX, 0);
        drawContent(canvas);
        canvas.restore();

        canvas.restoreToCount(count);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouch(event);
    }

    private void drawGridLine(Canvas canvas) {
        linePath.reset();
        linePath.moveTo(horizontalOffset, getChartBottom() - itemHeight * 5);
        linePath.lineTo(horizontalOffset, getChartBottom());
        linePath.lineTo(getWidth(), getChartBottom());
        canvas.drawPath(linePath, solidLinePaint);

        linePath.reset();
        for (int i = 1; i <= ROW_COUNT; i++) {
            linePath.moveTo(horizontalOffset, getChartBottom() - itemHeight * i);
            linePath.lineTo(getWidth(), getChartBottom() - itemHeight * i);
        }
        canvas.drawPath(linePath, dashLinePaint);
    }

    private void drawGridText(Canvas canvas) {
        // 绘制水平文字
        textPaint.setColor(config.getTextColor());
        textPaint.setTextAlign(Paint.Align.RIGHT);
        for (int i = 0; i <= ROW_COUNT; i++) {
            String text = String.valueOf(gridHeight * i);
            canvas.drawText(text, horizontalOffset - TEXT_MARGIN, getChartBottom() - itemHeight * i +
                    getTextHeight() / 2 - getTextOffsetY(), textPaint);
        }
    }

    private void calculateRenderTitle() {
        int firstVisiblePoint = 0;
        int lastVisiblePoint = dataList.size() - 1;
        for (int i = 0; i < dataList.size(); i++) {
            float currentX = getScaledItemWidth() * i;
            if (currentX + translateX >= 0) {
                firstVisiblePoint = i;
                break;
            }
        }

        for (int i = firstVisiblePoint; i < dataList.size(); i++) {
            float nextX = getScaledItemWidth() * (i + 1);
            if ((int) (nextX + translateX) > (int) getChartWidth()) {
                lastVisiblePoint = i;
                break;
            }
        }

        renderTitleList.clear();
        int count = lastVisiblePoint - firstVisiblePoint + 1;
        int maxCount = getMaxTitle();
        if (count < maxCount) {
            for (int i = firstVisiblePoint; i <= lastVisiblePoint; i++) {
                renderTitleList.add(i);
            }
        } else {
            float interval = (count - 1f) / (maxCount - 1);
            for (int i = 0; i < maxCount; i++) {
                int index = (int) Math.floor(firstVisiblePoint + interval * i);
                renderTitleList.add(index);
            }
        }
    }

    protected abstract GridConfig getConfig();

    protected float getTextHeight() {
        return fontMetrics.descent - fontMetrics.ascent;
    }

    protected float getChartBottom() {
        return getHeight() - getDescHeight() - getBottomTextHeight();
    }

    protected float getBottomTextHeight() {
        return getTextHeight() * 1.5f;
    }

    protected float getDescHeight() {
        return getTextHeight() + ChartUtils.dp2px(10);
    }

    protected float getChartWidth() {
        return getWidth() - horizontalOffset;
    }

    protected float getChartMeasuredWidth() {
        return getScaledItemWidth() * dataList.size();
    }

    protected float getScaledItemWidth() {
        return defaultItemWidth * scaleValue;
    }

    protected float getItemHeightRatio() {
        return itemHeight / gridHeight;
    }

    protected float getTextOffsetY() {
        return fontMetrics.descent;
    }

    protected int getMaxTitle() {
        if (config.getMaxTitleCount() < 0) {
            return dataList.size();
        } else {
            return config.getMaxTitleCount();
        }
    }

    protected abstract void drawTitle(Canvas canvas);

    protected abstract void drawDesc(Canvas canvas);

    protected abstract void drawContent(Canvas canvas);

    protected abstract void calculateRenderRange();

    private ChartGestureListener gestureListener = new ChartGestureListener() {
        @Override
        public boolean onDown(float x, float y) {
            onTouchStart();
            return config.isTouchable();
        }

        @Override
        public boolean onSingleTap(float x, float y) {
            return GridChart.this.onSingleTap(x, y);
        }

        @Override
        public boolean onDoubleTap(float x, float y) {
            float newScaleFocusX = x - horizontalOffset;
            doubleTap(newScaleFocusX);
            return true;
        }

        @Override
        public boolean onScroll(float distanceX, float distanceY) {
            if (Math.abs(distanceX) > Math.abs(distanceY)) {
                requestDisallowInterceptTouchEvent();
            }

            // 平滑处理
            float offset = -distanceX * 0.5f;
            return updateTranslateX(translateX + offset);
        }

        @Override
        public boolean onFling(float velocityX, float velocityY) {
            return fling(velocityX);
        }

        @Override
        public boolean onScaleBegin(float focusX, float focusY) {
            requestDisallowInterceptTouchEvent();

            scaleFocusX = focusX - horizontalOffset;
            return true;
        }

        @Override
        public boolean onScale(float factorX, float factorY) {
            return scale(factorX);
        }
    };

    protected void onTouchStart() {
        stopFling();
    }

    protected boolean onSingleTap(float x, float y) {
        return false;
    }

    private boolean scale(float factorX) {
        // 平滑处理
        factorX = 1 + (factorX - 1) * 0.5f;
        float newScaleValue = scaleValue * factorX;
        if (newScaleValue < 1) {
            newScaleValue = 1;
        } else if (newScaleValue > MAX_SCALE) {
            newScaleValue = MAX_SCALE;
        }

        return updateScale(newScaleValue);
    }

    private boolean fling(float velocityX) {
        if (Math.abs(velocityX) < FLING_MIN_VELOCITY_X) {
            return false;
        }

        flingScroller = new Scroller(getContext());
        flingScroller.fling((int) translateX, 0, (int) velocityX, 0, (int) (getChartWidth() - getChartMeasuredWidth()), 0, 0, 0);
        flingAnimator = ValueAnimator.ofInt(0, 1);
        // 由Scroller去控制有没有滚动完
        flingAnimator.setDuration(flingScroller.getDuration())
                .addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        if (flingScroller != null) {
                            if (flingScroller.computeScrollOffset()) {
                                updateTranslateX(flingScroller.getCurrX());
                            } else {
                                flingScroller = null;
                                flingAnimator = null;
                            }
                        }
                    }
                });
        flingAnimator.start();
        return true;
    }

    private void stopFling() {
        if (flingAnimator != null && flingAnimator.isRunning()) {
            flingAnimator.cancel();
        }
    }

    private void doubleTap(float newScaleFocusX) {
        if (doubleTapAnimator != null && doubleTapAnimator.isRunning()) {
            doubleTapAnimator.end();
        }

        scaleFocusX = newScaleFocusX;

        float target = (scaleValue == 1) ? 2 : 1;
        doubleTapAnimator = ValueAnimator.ofFloat(scaleValue, target);
        doubleTapAnimator.setDuration(300);
        doubleTapAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                updateScale((Float) animation.getAnimatedValue());
            }
        });
        doubleTapAnimator.start();
    }

    private boolean updateScale(float newScaleValue) {
        if (newScaleValue == scaleValue) {
            return false;
        }

        float offset = (translateX - scaleFocusX) * (newScaleValue - scaleValue) / scaleValue;
        translateX += offset;

        if (translateX > 0) {
            translateX = 0;
        } else if (getChartMeasuredWidth() + translateX < getChartWidth()) {
            translateX = getChartWidth() - getChartMeasuredWidth();
        }

        scaleValue = newScaleValue;

        calculateRenderRange();
        calculateRenderTitle();

        invalidate();
        return true;
    }

    private boolean updateTranslateX(float newTranslateX) {
        if (newTranslateX > 0) {
            newTranslateX = 0;
        } else if (getChartMeasuredWidth() + newTranslateX < getChartWidth()) {
            newTranslateX = getChartWidth() - getChartMeasuredWidth();
        }

        if (newTranslateX != translateX) {
            translateX = newTranslateX;

            calculateRenderRange();
            calculateRenderTitle();

            invalidate();
            return true;
        } else {
            return false;
        }
    }

    private void requestDisallowInterceptTouchEvent() {
        if (scrollView != null) {
            scrollView.requestDisallowInterceptTouchEvent(true);
        }
    }
}
