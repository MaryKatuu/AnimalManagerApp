package com.example.animalmanagerapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.animalmanagerapp.adapter.AnimalAdapter;
import com.example.animalmanagerapp.auth.SessionManager;
import com.example.animalmanagerapp.db.DatabaseHelper;
import com.example.animalmanagerapp.model.Animal;

import java.util.List;

/**
 * Dashboard / Home tab: shows quick totals and animals that haven't had
 * a health/feeding event logged recently. Settings (report + logout) is
 * reached via the hamburger icon.
 */
public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private TextView tvTotalAnimals, tvAllGood;
    private RecyclerView rvNeedsAttention;
    private AnimalAdapter adapter;

    private static final int OVERDUE_DAYS_THRESHOLD = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        tvTotalAnimals = findViewById(R.id.tvTotalAnimals);
        tvAllGood = findViewById(R.id.tvAllGood);
        rvNeedsAttention = findViewById(R.id.rvNeedsAttention);

        Button btnAddAnimal = findViewById(R.id.btnAddAnimal);
        Button btnViewAnimals = findViewById(R.id.btnViewAnimals);
        ImageButton btnMenu = findViewById(R.id.btnMenu);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

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

        btnMenu.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SettingsActivity.class)));

        BottomNavHelper.setup(bottomNav, this, R.id.nav_home);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dbHelper != null) {
            loadSummary();
        }
    }

    private void loadSummary() {
        int totalAnimals = dbHelper.getTotalAnimalCount();
        tvTotalAnimals.setText(String.valueOf(totalAnimals));

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