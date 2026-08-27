package com.example.animalmanagerapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.animalmanagerapp.adapter.AnimalAdapter;
import com.example.animalmanagerapp.db.DatabaseHelper;
import com.example.animalmanagerapp.model.Animal;

import java.util.List;

/**
 * Dashboard / home screen: shows quick totals and animals that haven't had
 * a health/feeding event logged recently, plus entry points to add or
 * browse animals.
 */
public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TextView tvTotalAnimals, tvTotalTypes, tvAllGood;
    private RecyclerView rvNeedsAttention;
    private AnimalAdapter adapter;

    private static final int OVERDUE_DAYS_THRESHOLD = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        tvTotalAnimals = findViewById(R.id.tvTotalAnimals);
        tvTotalTypes = findViewById(R.id.tvTotalTypes);
        tvAllGood = findViewById(R.id.tvAllGood);
        rvNeedsAttention = findViewById(R.id.rvNeedsAttention);

        Button btnAddAnimal = findViewById(R.id.btnAddAnimal);
        Button btnViewAnimals = findViewById(R.id.btnViewAnimals);

        rvNeedsAttention.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AnimalAdapter(new java.util.ArrayList<>(), dbHelper, animal -> {
            Intent intent = new Intent(MainActivity.this, AnimalDetailsActivity.class);
            intent.putExtra("animal_id", animal.getId());
            startActivity(intent);
        });
        rvNeedsAttention.setAdapter(adapter);

        btnAddAnimal.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddAnimalActivity.class)));

        btnViewAnimals.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AnimalListActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSummary();
    }

    private void loadSummary() {
        int totalAnimals = dbHelper.getTotalAnimalCount();
        int totalTypes = dbHelper.getDistinctTypeCount();
        tvTotalAnimals.setText(String.valueOf(totalAnimals));
        tvTotalTypes.setText(String.valueOf(totalTypes));

        List<Animal> needsAttention = dbHelper.getAnimalsNeedingAttention(OVERDUE_DAYS_THRESHOLD);
        adapter.updateData(needsAttention);

        if (needsAttention.isEmpty()) {
            tvAllGood.setVisibility(View.VISIBLE);
            rvNeedsAttention.setVisibility(View.GONE);
        } else {
            tvAllGood.setVisibility(View.GONE);
            rvNeedsAttention.setVisibility(View.VISIBLE);
        }
    }
}