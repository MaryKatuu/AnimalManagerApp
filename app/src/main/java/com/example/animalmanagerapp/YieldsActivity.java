package com.example.animalmanagerapp;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.animalmanagerapp.analytics.AnimalComparisonChartView;
import com.example.animalmanagerapp.analytics.OutputAggregator;
import com.example.animalmanagerapp.analytics.OutputBarChartView;
import com.example.animalmanagerapp.db.DatabaseHelper;
import com.example.animalmanagerapp.model.Animal;
import com.example.animalmanagerapp.model.OutputLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Compares animal production output over time. Two modes:
 * "Farm Totals" sums output across all animals (optionally filtered by
 * type); "Compare Animals" shows one bar series per individual animal
 * of the selected type, so e.g. five dairy cows' milk output can be
 * compared side by side. Daily / Weekly / Monthly / Yearly buckets.
 */
public class YieldsActivity extends AppCompatActivity {

    private static final String ALL_TYPES = "All Types";
    private enum Period { DAILY, WEEKLY, MONTHLY, YEARLY }
    private enum Mode { TOTALS, COMPARE }

    private DatabaseHelper dbHelper;
    private TextView tvPeriodDaily, tvPeriodWeekly, tvPeriodMonthly, tvPeriodYearly;
    private TextView tvModeTotals, tvModeCompare;
    private TextView tvCompareHint;
    private Spinner spinnerAnimalType;
    private FrameLayout scrollTotalsParent;
    private View scrollTotals, scrollCompare;
    private OutputBarChartView chartOutput;
    private AnimalComparisonChartView chartCompare;
    private LinearLayout llAnimalLegend;

    private Period selectedPeriod = Period.DAILY;
    private Mode selectedMode = Mode.TOTALS;
    private String selectedType = ALL_TYPES;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yields);

        dbHelper = new DatabaseHelper(this);

        tvPeriodDaily = findViewById(R.id.tvPeriodDaily);
        tvPeriodWeekly = findViewById(R.id.tvPeriodWeekly);
        tvPeriodMonthly = findViewById(R.id.tvPeriodMonthly);
        tvPeriodYearly = findViewById(R.id.tvPeriodYearly);
        tvModeTotals = findViewById(R.id.tvModeTotals);
        tvModeCompare = findViewById(R.id.tvModeCompare);
        tvCompareHint = findViewById(R.id.tvCompareHint);
        spinnerAnimalType = findViewById(R.id.spinnerAnimalType);
        scrollTotals = findViewById(R.id.scrollTotals);
        scrollCompare = findViewById(R.id.scrollCompare);
        chartOutput = findViewById(R.id.chartOutput);
        chartCompare = findViewById(R.id.chartCompare);
        llAnimalLegend = findViewById(R.id.llAnimalLegend);

        tvPeriodDaily.setOnClickListener(v -> setPeriod(Period.DAILY));
        tvPeriodWeekly.setOnClickListener(v -> setPeriod(Period.WEEKLY));
        tvPeriodMonthly.setOnClickListener(v -> setPeriod(Period.MONTHLY));
        tvPeriodYearly.setOnClickListener(v -> setPeriod(Period.YEARLY));

        tvModeTotals.setOnClickListener(v -> setMode(Mode.TOTALS));
        tvModeCompare.setOnClickListener(v -> setMode(Mode.COMPARE));

        setupTypeSpinner();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        BottomNavHelper.setup(bottomNav, this, R.id.nav_yields);

        setPeriod(Period.DAILY);
        setMode(Mode.TOTALS);
    }

    private void setupTypeSpinner() {
        List<String> types = new ArrayList<>();
        types.add(ALL_TYPES);
        types.addAll(dbHelper.getDistinctAnimalTypes());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAnimalType.setAdapter(adapter);

        spinnerAnimalType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedType = types.get(position);
                renderChart();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void setPeriod(Period period) {
        selectedPeriod = period;

        tvPeriodDaily.setSelected(period == Period.DAILY);
        tvPeriodWeekly.setSelected(period == Period.WEEKLY);
        tvPeriodMonthly.setSelected(period == Period.MONTHLY);
        tvPeriodYearly.setSelected(period == Period.YEARLY);

        tvPeriodDaily.setTextColor(getResources().getColor(period == Period.DAILY ? R.color.white : R.color.text_primary));
        tvPeriodWeekly.setTextColor(getResources().getColor(period == Period.WEEKLY ? R.color.white : R.color.text_primary));
        tvPeriodMonthly.setTextColor(getResources().getColor(period == Period.MONTHLY ? R.color.white : R.color.text_primary));
        tvPeriodYearly.setTextColor(getResources().getColor(period == Period.YEARLY ? R.color.white : R.color.text_primary));

        renderChart();
    }

    private void setMode(Mode mode) {
        selectedMode = mode;

        tvModeTotals.setSelected(mode == Mode.TOTALS);
        tvModeCompare.setSelected(mode == Mode.COMPARE);
        tvModeTotals.setTextColor(getResources().getColor(mode == Mode.TOTALS ? R.color.white : R.color.text_primary));
        tvModeCompare.setTextColor(getResources().getColor(mode == Mode.COMPARE ? R.color.white : R.color.text_primary));

        scrollTotals.setVisibility(mode == Mode.TOTALS ? View.VISIBLE : View.GONE);
        scrollCompare.setVisibility(mode == Mode.COMPARE ? View.VISIBLE : View.GONE);
        llAnimalLegend.setVisibility(mode == Mode.COMPARE ? View.VISIBLE : View.GONE);

        renderChart();
    }

    private void renderChart() {
        if (selectedMode == Mode.TOTALS) {
            renderTotalsChart();
        } else {
            renderCompareChart();
        }
    }

    private void renderTotalsChart() {
        tvCompareHint.setVisibility(View.GONE);

        List<OutputLog> allLogs = dbHelper.getAllOutputLogs();
        List<OutputLog> filtered = new ArrayList<>();

        if (ALL_TYPES.equals(selectedType)) {
            filtered = allLogs;
        } else {
            for (OutputLog log : allLogs) {
                Animal animal = dbHelper.getAnimal(log.getAnimalId());
                if (animal != null && selectedType.equals(animal.getAnimalType())) {
                    filtered.add(log);
                }
            }
        }

        Map<String, Float> buckets = bucketsFor(filtered);
        List<String> labels = new ArrayList<>(buckets.keySet());
        List<Float> values = new ArrayList<>(buckets.values());
        chartOutput.setData(labels, values);
    }

    private void renderCompareChart() {
        llAnimalLegend.removeAllViews();

        if (ALL_TYPES.equals(selectedType)) {
            tvCompareHint.setVisibility(View.VISIBLE);
            chartCompare.setData(new ArrayList<>(), new ArrayList<>());
            return;
        }
        tvCompareHint.setVisibility(View.GONE);

        List<Animal> animals = dbHelper.getAnimalsByType(selectedType);
        if (animals.isEmpty()) {
            chartCompare.setData(new ArrayList<>(), new ArrayList<>());
            return;
        }

        List<String> sharedLabels = null;
        List<List<Float>> seriesValues = new ArrayList<>();

        for (Animal animal : animals) {
            List<OutputLog> animalLogs = dbHelper.getOutputLogsForAnimal(animal.getId());
            Map<String, Float> buckets = bucketsFor(animalLogs);

            if (sharedLabels == null) {
                sharedLabels = new ArrayList<>(buckets.keySet());
            }
            seriesValues.add(new ArrayList<>(buckets.values()));

            addLegendRow(animal, sumValues(buckets));
        }

        chartCompare.setData(sharedLabels, seriesValues);
    }

    private Map<String, Float> bucketsFor(List<OutputLog> logs) {
        switch (selectedPeriod) {
            case WEEKLY:
                return OutputAggregator.getWeeklyTotals(logs, 6);
            case MONTHLY:
                return OutputAggregator.getMonthlyTotals(logs, 6);
            case YEARLY:
                return OutputAggregator.getYearlyTotals(logs, 5);
            case DAILY:
            default:
                return OutputAggregator.getDailyTotals(logs, 7);
        }
    }

    private float sumValues(Map<String, Float> buckets) {
        float total = 0f;
        for (Float v : buckets.values()) total += v;
        return total;
    }

    private void addLegendRow(Animal animal, float total) {
        int index = llAnimalLegend.getChildCount();
        int color = AnimalComparisonChartView.SERIES_COLORS[index % AnimalComparisonChartView.SERIES_COLORS.length];

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.topMargin = (int) (6 * getResources().getDisplayMetrics().density);
        row.setLayoutParams(rowParams);

        View swatch = new View(this);
        int swatchSize = (int) (14 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams swatchParams = new LinearLayout.LayoutParams(swatchSize, swatchSize);
        swatchParams.rightMargin = (int) (8 * getResources().getDisplayMetrics().density);
        swatch.setLayoutParams(swatchParams);
        swatch.setBackgroundColor(color);
        row.addView(swatch);

        TextView label = new TextView(this);
        String totalText = (total == Math.floor(total)) ? String.valueOf((long) total) : String.valueOf(total);
        label.setText("Tag #" + animal.getTagNumber() + " (" + animal.getVariety() + ") — Total: " + totalText);
        label.setTextColor(getResources().getColor(R.color.text_primary));
        label.setTextSize(13);
        row.addView(label);

        llAnimalLegend.addView(row);
    }
}