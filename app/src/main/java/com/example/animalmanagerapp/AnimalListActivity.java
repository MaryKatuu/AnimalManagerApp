package com.example.animalmanagerapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.animalmanagerapp.adapter.AnimalAdapter;
import com.example.animalmanagerapp.db.DatabaseHelper;
import com.example.animalmanagerapp.model.Animal;

import java.util.List;

/**
 * Full animal list with a live search box that filters by tag number or
 * type/breed as the farmer types.
 */
public class AnimalListActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private RecyclerView rvAnimals;
    private EditText etSearch;
    private TextView tvEmptyState;
    private AnimalAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animal_list);

        dbHelper = new DatabaseHelper(this);
        rvAnimals = findViewById(R.id.rvAnimals);
        etSearch = findViewById(R.id.etSearch);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        rvAnimals.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AnimalAdapter(new java.util.ArrayList<>(), dbHelper, animal -> {
            Intent intent = new Intent(AnimalListActivity.this, AnimalDetailsActivity.class);
            intent.putExtra("animal_id", animal.getId());
            startActivity(intent);
        });
        rvAnimals.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadAnimals(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        BottomNavHelper.setup(bottomNav, this, R.id.nav_animals);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAnimals(etSearch.getText().toString());
    }

    private void loadAnimals(String searchTerm) {
        List<Animal> animals = dbHelper.getAllAnimals(searchTerm);
        adapter.updateData(animals);

        if (animals.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvAnimals.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvAnimals.setVisibility(View.VISIBLE);
        }
    }
}