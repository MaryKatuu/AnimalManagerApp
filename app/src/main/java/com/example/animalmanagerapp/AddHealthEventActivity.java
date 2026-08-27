package com.example.animalmanagerapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.animalmanagerapp.db.DatabaseHelper;
import com.example.animalmanagerapp.db.DateUtils;
import com.example.animalmanagerapp.model.Animal;
import com.example.animalmanagerapp.model.HealthEvent;

import java.util.Calendar;

/**
 * Logs a single health/feeding event (vaccination, deworming, feeding
 * schedule, treatment, etc.) against the animal the user came from.
 */
public class AddHealthEventActivity extends AppCompatActivity {

    private Spinner spinnerEventType;
    private Button btnEventDate, btnSaveEvent;
    private EditText etNotes;
    private TextView tvFormTitle;

    private String eventDateIso = null;
    private DatabaseHelper dbHelper;
    private long animalId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_health_event);

        dbHelper = new DatabaseHelper(this);
        animalId = getIntent().getLongExtra("animal_id", -1);

        spinnerEventType = findViewById(R.id.spinnerEventType);
        btnEventDate = findViewById(R.id.btnEventDate);
        btnSaveEvent = findViewById(R.id.btnSaveEvent);
        etNotes = findViewById(R.id.etNotes);
        tvFormTitle = findViewById(R.id.tvFormTitle);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.event_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEventType.setAdapter(adapter);

        Animal animal = dbHelper.getAnimal(animalId);
        if (animal != null) {
            tvFormTitle.setText("Log Event — " + animal.getTagNumber());
        }

        btnEventDate.setOnClickListener(v -> showDatePicker());
        btnSaveEvent.setOnClickListener(v -> saveEvent());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    eventDateIso = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    btnEventDate.setText(DateUtils.toDisplayFormat(eventDateIso));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void saveEvent() {
        if (animalId == -1) {
            Toast.makeText(this, "Missing animal reference. Please go back and try again.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (eventDateIso == null) {
            Toast.makeText(this, "Please select a date for this event", Toast.LENGTH_SHORT).show();
            return;
        }

        String eventType = spinnerEventType.getSelectedItem().toString();
        String notes = etNotes.getText().toString().trim();

        HealthEvent event = new HealthEvent();
        event.setAnimalId(animalId);
        event.setEventType(eventType);
        event.setEventDate(eventDateIso);
        event.setNotes(notes);

        long id = dbHelper.addEvent(event);
        if (id > 0) {
            Toast.makeText(this, "Event logged", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Could not save event. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}