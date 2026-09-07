package com.example.animalmanagerapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import java.util.Calendar;
import java.util.List;

public class AnimalDetailsActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private long animalId;
    private Animal currentAnimal;

    private TextView tvTagNumber, tvTypeBreed, tvStatusBadge, tvQuantity, tvDateAcquired,
            tvSex, tvAge, tvPoultryInfo, tvNoEvents;
    private Button btnSoldAction, btnUndoSold;
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
        tvQuantity = findViewById(R.id.tvQuantity);
        tvDateAcquired = findViewById(R.id.tvDateAcquired);
        tvSex = findViewById(R.id.tvSex);
        tvAge = findViewById(R.id.tvAge);
        tvPoultryInfo = findViewById(R.id.tvPoultryInfo);
        tvNoEvents = findViewById(R.id.tvNoEvents);
        rvEvents = findViewById(R.id.rvEvents);

        Button btnEditAnimal = findViewById(R.id.btnEditAnimal);
        Button btnDeleteAnimal = findViewById(R.id.btnDeleteAnimal);
        Button btnAddEvent = findViewById(R.id.btnAddEvent);
        btnSoldAction = findViewById(R.id.btnSoldAction);
        btnUndoSold = findViewById(R.id.btnUndoSold);

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

        btnSoldAction.setOnClickListener(v -> showSoldDialog());
        btnUndoSold.setOnClickListener(v -> confirmUndoSold());
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

        tvTagNumber.setText("Tag #" + currentAnimal.getTagNumber());
        String typeLine = currentAnimal.getAnimalType();
        if (!TextUtils.isEmpty(currentAnimal.getVariety())) {
            typeLine += " • " + currentAnimal.getVariety();
        }
        tvTypeBreed.setText(typeLine);
        tvQuantity.setText("Total number: " + (TextUtils.isEmpty(currentAnimal.getQuantity()) ? "1" : currentAnimal.getQuantity()));
        tvDateAcquired.setText("Acquired: " + DateUtils.toDisplayFormat(currentAnimal.getDateAcquired()));
        tvSex.setText("Sex: " + currentAnimal.getSex());
        tvAge.setText("Age: " + currentAnimal.getAge());

        boolean hasLaying = !TextUtils.isEmpty(currentAnimal.getLayingCount());
        boolean hasTrays = !TextUtils.isEmpty(currentAnimal.getTraysCollected());
        if (hasLaying || hasTrays) {
            tvPoultryInfo.setText("Currently laying: " + (hasLaying ? currentAnimal.getLayingCount() : "Not recorded") +
                    " | Trays collected: " + (hasTrays ? currentAnimal.getTraysCollected() : "Not recorded"));
            tvPoultryInfo.setVisibility(View.VISIBLE);
        } else {
            tvPoultryInfo.setVisibility(View.GONE);
        }

        String badgeText;
        int color;

        if (currentAnimal.isSold()) {
            String amountText = TextUtils.isEmpty(currentAnimal.getSaleAmount()) ? "not recorded" : "KES " + currentAnimal.getSaleAmount();
            badgeText = "Sold " + DateUtils.toDisplayFormat(currentAnimal.getSoldDate()) + " — " + amountText;
            color = 0xFF1B5E20;
            btnSoldAction.setText("Edit Sale Record");
            btnUndoSold.setVisibility(View.VISIBLE);
        } else {
            HealthEvent lastEvent = dbHelper.getMostRecentEvent(animalId);
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
                    color = 0xFFE65100;
                } else {
                    badgeText = daysSince + " day(s) since last event";
                    color = 0xFFB71C1C;
                }
            }
            btnSoldAction.setText("Mark as Sold");
            btnUndoSold.setVisibility(View.GONE);
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

    private void showSoldDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_mark_sold, null);
        Button btnDialogDate = dialogView.findViewById(R.id.btnDialogSoldDate);
        EditText etDialogAmount = dialogView.findViewById(R.id.etDialogSaleAmount);

        final String[] chosenDate = { currentAnimal.isSold() && !TextUtils.isEmpty(currentAnimal.getSoldDate())
                ? currentAnimal.getSoldDate() : DateUtils.todayIso() };
        btnDialogDate.setText(DateUtils.toDisplayFormat(chosenDate[0]));
        if (currentAnimal.isSold() && !TextUtils.isEmpty(currentAnimal.getSaleAmount())) {
            etDialogAmount.setText(currentAnimal.getSaleAmount());
        }

        btnDialogDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            if (DateUtils.isValidIsoDate(chosenDate[0])) {
                String[] parts = chosenDate[0].split("-");
                calendar.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]));
            }
            new DatePickerDialog(this, (view, year, month, day) -> {
                chosenDate[0] = String.format("%04d-%02d-%02d", year, month + 1, day);
                btnDialogDate.setText(DateUtils.toDisplayFormat(chosenDate[0]));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        new AlertDialog.Builder(this)
                .setTitle(currentAnimal.isSold() ? "Edit Sale Record" : "Mark as Sold")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String amount = etDialogAmount.getText().toString().trim();
                    if (TextUtils.isEmpty(amount)) {
                        Toast.makeText(this, "Please enter the sale amount", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!amount.matches("\\d+(\\.\\d+)?")) {
                        Toast.makeText(this, "Enter a valid amount, e.g. 15000", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    dbHelper.markAnimalSold(animalId, chosenDate[0], amount);
                    Toast.makeText(this, "Sale recorded", Toast.LENGTH_SHORT).show();
                    loadAnimalDetails();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmUndoSold() {
        new AlertDialog.Builder(this)
                .setTitle("Undo sold status")
                .setMessage("This will move the animal back to your active list and clear its recorded sale date and amount.")
                .setPositiveButton("Undo", (dialog, which) -> {
                    dbHelper.unmarkAnimalSold(animalId);
                    Toast.makeText(this, "Sold status undone", Toast.LENGTH_SHORT).show();
                    loadAnimalDetails();
                })
                .setNegativeButton("Cancel", null)
                .show();
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