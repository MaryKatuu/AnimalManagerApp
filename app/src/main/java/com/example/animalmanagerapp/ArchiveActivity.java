package com.example.animalmanagerapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.animalmanagerapp.adapter.SoldAnimalAdapter;
import com.example.animalmanagerapp.db.DatabaseHelper;
import com.example.animalmanagerapp.model.Animal;

import java.util.List;

/**
 * Lists sold animals (the archive) and shows total sales revenue, so a
 * farmer can review sales history across seasons.
 */
public class ArchiveActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private RecyclerView rvSoldAnimals;
    private TextView tvTotalRevenue, tvEmptyState;
    private SoldAnimalAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive);

        dbHelper = new DatabaseHelper(this);
        rvSoldAnimals = findViewById(R.id.rvSoldAnimals);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        rvSoldAnimals.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SoldAnimalAdapter(new java.util.ArrayList<>(), animal -> {
            Intent intent = new Intent(ArchiveActivity.this, AnimalDetailsActivity.class);
            intent.putExtra("animal_id", animal.getId());
            startActivity(intent);
        });
        rvSoldAnimals.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadArchive();
    }

    private void loadArchive() {
        List<Animal> sold = dbHelper.getSoldAnimals(null);
        adapter.updateData(sold);

        if (sold.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvSoldAnimals.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvSoldAnimals.setVisibility(View.VISIBLE);
        }

        double totalRevenue = dbHelper.getTotalSalesRevenue();
        tvTotalRevenue.setText(String.format("KES %.2f", totalRevenue));
    }
}