package com.example.animalmanagerapp;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.animalmanagerapp.adapter.HealthEventAdapter;
import com.example.animalmanagerapp.db.DatabaseHelper;
import com.example.animalmanagerapp.db.DateUtils;
import com.example.animalmanagerapp.model.Animal;
import com.example.animalmanagerapp.model.HealthEvent;

import java.util.List;

/**
 * Shows full details for one animal plus its health/feeding event log, and
 * provides entry points to edit, delete, or log a new event.
 */
public class AnimalDetailsActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private long animalId;
    private Animal currentAnimal;

    private TextView tvTagNumber, tvTypeBreed, tvStatusBadge, tvDateAcquired, tvSex, tvAge, tvNoEvents;
    private RecyclerView rvEvents;
    private HealthEventAdapter eventAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animal_details);

        dbHelper = new DatabaseHelper(this);
        animalId = getIntent().getLongExtra("animal_id", -1);

        tvTagNumber = findViewById(R.id.tvTagNumber);
        tvTypeBreed = findViewById(R.id.tvTypeBreed);
        tvStatusBadge = findViewById(R.id.tvStatusBadge);
        tvDateAcquired = findViewById(R.id.tvDateAcquired);
        tvSex = findViewById(R.id.tvSex);
        tvAge = findViewById(R.id.tvAge);
        tvNoEvents = findViewById(R.id.tvNoEvents);
        rvEvents = findViewById(R.id.rvEvents);

        Button btnEditAnimal = findViewById(R.id.btnEditAnimal);
        Button btnDeleteAnimal = findViewById(R.id.btnDeleteAnimal);
        Button btnAddEvent = findViewById(R.id.btnAddEvent);

        rvEvents.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new HealthEventAdapter(new java.util.ArrayList<>());
        rvEvents.setAdapter(eventAdapter);

        btnEditAnimal.setOnClickListener(v -> {
            Intent intent = new Intent(AnimalDetailsActivity.this, EditAnimalActivity.class);
            intent.putExtra("animal_id", animalId);
            startActivity(intent);
        });

        btnDeleteAnimal.setOnClickListener(v -> confirmDelete());

        btnAddEvent.setOnClickListener(v -> {
            Intent intent = new Intent(AnimalDetailsActivity.this, AddHealthEventActivity.class);
            intent.putExtra("animal_id", animalId);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAnimalDetails();
        loadEvents();
    }

    private void loadAnimalDetails() {
        currentAnimal = dbHelper.getAnimal(animalId);
        if (currentAnimal == null) {
            Toast.makeText(this, "This animal record no longer exists.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvTagNumber.setText(currentAnimal.getTagNumber());
        tvTypeBreed.setText(currentAnimal.getTypeBreed());
        tvDateAcquired.setText("Acquired: " + DateUtils.toDisplayFormat(currentAnimal.getDateAcquired()));
        tvSex.setText("Sex: " + currentAnimal.getSex());
        tvAge.setText("Age: " + currentAnimal.getAge());

        HealthEvent lastEvent = dbHelper.getMostRecentEvent(animalId);
        String badgeText;
        int color;
        if (lastEvent == null) {
            badgeText = "No events logged yet";
            color = 0xFF9E9E9E;
        } else {
            int daysSince = DateUtils.daysSince(lastEvent.getEventDate());
            if (daysSince == Integer.MIN_VALUE) {
                badgeText = "Unknown";
                color = 0xFF9E9E9E;
            } else if (daysSince <= 7) {
                badgeText = daysSince + " day(s) since last event";
                color = 0xFF1B5E20;
            } else if (daysSince <= 30) {
                badgeText = daysSince + " day(s) since last event";
                color = 0xFFF9A825;
            } else {
                badgeText = daysSince + " day(s) since last event";
                color = 0xFFC62828;
            }
        }
        tvStatusBadge.setText(badgeText);
        GradientDrawable bg = (GradientDrawable) tvStatusBadge.getBackground().mutate();
        bg.setColor(color);
    }

    private void loadEvents() {
        List<HealthEvent> events = dbHelper.getEventsForAnimal(animalId);
        eventAdapter.updateData(events);

        if (events.isEmpty()) {
            tvNoEvents.setVisibility(View.VISIBLE);
            rvEvents.setVisibility(View.GONE);
        } else {
            tvNoEvents.setVisibility(View.GONE);
            rvEvents.setVisibility(View.VISIBLE);
        }
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete animal record")
                .setMessage("This will permanently delete \"" + currentAnimal.getTagNumber() +
                        "\" and all its logged health/feeding events. This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deleteAnimal(animalId);
                    Toast.makeText(this, "Animal deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}