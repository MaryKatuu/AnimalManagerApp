package com.example.animalmanagerapp;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.animalmanagerapp.analytics.OutputAggregator;
import com.example.animalmanagerapp.analytics.OutputBarChartView;
import com.example.animalmanagerapp.db.DatabaseHelper;
import com.example.animalmanagerapp.model.Animal;
import com.example.animalmanagerapp.model.OutputLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Compares an animal's (or the whole farm's) production output across
 * Daily / Weekly / Monthly time buckets, with an optional filter by
 * animal type.
 */
public class YieldsActivity extends AppCompatActivity {

    private static final String ALL_TYPES = "All Types";
    private enum Period { DAILY, WEEKLY, MONTHLY }

    private DatabaseHelper dbHelper;
    private TextView tvPeriodDaily, tvPeriodWeekly, tvPeriodMonthly;
    private Spinner spinnerAnimalType;
    private OutputBarChartView chartOutput;

    private Period selectedPeriod = Period.DAILY;
    private String selectedType = ALL_TYPES;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yields);

        dbHelper = new DatabaseHelper(this);

        tvPeriodDaily = findViewById(R.id.tvPeriodDaily);
        tvPeriodWeekly = findViewById(R.id.tvPeriodWeekly);
        tvPeriodMonthly = findViewById(R.id.tvPeriodMonthly);
        spinnerAnimalType = findViewById(R.id.spinnerAnimalType);
        chartOutput = findViewById(R.id.chartOutput);

        tvPeriodDaily.setOnClickListener(v -> setPeriod(Period.DAILY));
        tvPeriodWeekly.setOnClickListener(v -> setPeriod(Period.WEEKLY));
        tvPeriodMonthly.setOnClickListener(v -> setPeriod(Period.MONTHLY));

        setupTypeSpinner();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        BottomNavHelper.setup(bottomNav, this, R.id.nav_yields);

        setPeriod(Period.DAILY);
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
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
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

        tvPeriodDaily.setTextColor(getResources().getColor(period == Period.DAILY ? R.color.white : R.color.text_primary));
        tvPeriodWeekly.setTextColor(getResources().getColor(period == Period.WEEKLY ? R.color.white : R.color.text_primary));
        tvPeriodMonthly.setTextColor(getResources().getColor(period == Period.MONTHLY ? R.color.white : R.color.text_primary));

        renderChart();
    }

    private void renderChart() {
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

        Map<String, Float> buckets;
        switch (selectedPeriod) {
            case WEEKLY:
                buckets = OutputAggregator.getWeeklyTotals(filtered, 6);
                break;
            case MONTHLY:
                buckets = OutputAggregator.getMonthlyTotals(filtered, 6);
                break;
            case DAILY:
            default:
                buckets = OutputAggregator.getDailyTotals(filtered, 7);
                break;
        }

        List<String> labels = new ArrayList<>(buckets.keySet());
        List<Float> values = new ArrayList<>(buckets.values());
        chartOutput.setData(labels, values);
    }
}