package com.example.animalmanagerapp.analytics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * A self-drawn grouped bar chart comparing several individual animals'
 * output across the same time buckets. Each animal gets its own colour;
 * one group of bars is drawn per time bucket (day/week/month/year).
 */
public class AnimalComparisonChartView extends View {

    public static final int[] SERIES_COLORS = {
            0xFF1B5E20, 0xFFE65100, 0xFF1565C0, 0xFF6A1B9A,
            0xFFC62828, 0xFF00838F, 0xFF9E9D24, 0xFF4E342E
    };

    private List<String> bucketLabels = new ArrayList<>();
    private List<List<Float>> seriesValues = new ArrayList<>(); // one list per animal, aligned to bucketLabels

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint emptyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final int barWidthPx;
    private final int barGapPx;
    private final int groupGapPx;
    private final int chartHeightPx;
    private final int labelTextSizePx;

    public AnimalComparisonChartView(Context context, AttributeSet attrs) {
        super(context, attrs);

        float density = context.getResources().getDisplayMetrics().density;
        barWidthPx = (int) (16 * density);
        barGapPx = (int) (3 * density);
        groupGapPx = (int) (20 * density);
        chartHeightPx = (int) (180 * density);
        labelTextSizePx = (int) (11 * density);

        barPaint.setStyle(Paint.Style.FILL);

        labelPaint.setColor(0xFF1A1A1A);
        labelPaint.setTextSize(labelTextSizePx);
        labelPaint.setTextAlign(Paint.Align.CENTER);

        axisPaint.setColor(0xFFBDBDBD);
        axisPaint.setStrokeWidth(2 * density);

        emptyPaint.setColor(0xFF1A1A1A);
        emptyPaint.setTextSize(labelTextSizePx);
        emptyPaint.setTextAlign(Paint.Align.CENTER);
    }

    /** seriesValues[i] is one animal's values, aligned 1:1 with bucketLabels. */
    public void setData(List<String> bucketLabels, List<List<Float>> seriesValues) {
        this.bucketLabels = bucketLabels != null ? bucketLabels : new ArrayList<>();
        this.seriesValues = seriesValues != null ? seriesValues : new ArrayList<>();
        requestLayout();
        invalidate();
    }

    private int groupWidthPx() {
        int seriesCount = Math.max(seriesValues.size(), 1);
        return seriesCount * barWidthPx + (seriesCount - 1) * barGapPx;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int bucketCount = Math.max(bucketLabels.size(), 1);
        int desiredWidth = bucketCount * (groupWidthPx() + groupGapPx) + groupGapPx;
        int minWidth = MeasureSpec.getSize(widthMeasureSpec);
        int width = Math.max(desiredWidth, minWidth);

        int desiredHeight = chartHeightPx + labelTextSizePx * 3;
        setMeasuredDimension(width, desiredHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        float axisY = chartHeightPx;

        canvas.drawLine(0, axisY, width, axisY, axisPaint);

        if (bucketLabels.isEmpty() || seriesValues.isEmpty()) {
            canvas.drawText("No output logged yet for these animals", width / 2f, chartHeightPx / 2f, emptyPaint);
            return;
        }

        float maxValue = 0f;
        for (List<Float> series : seriesValues) {
            for (float v : series) if (v > maxValue) maxValue = v;
        }
        if (maxValue <= 0f) maxValue = 1f;

        float x = groupGapPx;
        for (int bucketIndex = 0; bucketIndex < bucketLabels.size(); bucketIndex++) {
            float barX = x;
            for (int seriesIndex = 0; seriesIndex < seriesValues.size(); seriesIndex++) {
                List<Float> series = seriesValues.get(seriesIndex);
                float value = bucketIndex < series.size() ? series.get(bucketIndex) : 0f;
                float barHeight = (value / maxValue) * (chartHeightPx - labelTextSizePx);
                if (barHeight < 2) barHeight = 2;

                barPaint.setColor(SERIES_COLORS[seriesIndex % SERIES_COLORS.length]);
                RectF barRect = new RectF(barX, axisY - barHeight, barX + barWidthPx, axisY);
                canvas.drawRoundRect(barRect, 4, 4, barPaint);

                barX += barWidthPx + barGapPx;
            }

            canvas.drawText(bucketLabels.get(bucketIndex), x + groupWidthPx() / 2f,
                    axisY + labelTextSizePx + 10, labelPaint);

            x += groupWidthPx() + groupGapPx;
        }
    }
}