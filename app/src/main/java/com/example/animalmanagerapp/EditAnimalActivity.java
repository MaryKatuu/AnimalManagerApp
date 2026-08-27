package com.example.animalmanagerapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.animalmanagerapp.db.DatabaseHelper;
import com.example.animalmanagerapp.db.DateUtils;
import com.example.animalmanagerapp.model.Animal;

import java.util.Calendar;

/**
 * Pre-fills the form with the existing animal record and saves changes
 * back to the same row. Validation mirrors AddAnimalActivity.
 */
public class EditAnimalActivity extends AppCompatActivity {

    private EditText etTagNumber, etTypeBreed, etAge;
    private Button btnDateAcquired, btnUpdateAnimal;
    private Spinner spinnerSex;

    private String dateAcquiredIso = null;
    private DatabaseHelper dbHelper;
    private long animalId;
    private Animal existingAnimal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_animal);

        dbHelper = new DatabaseHelper(this);
        animalId = getIntent().getLongExtra("animal_id", -1);

        etTagNumber = findViewById(R.id.etTagNumber);
        etTypeBreed = findViewById(R.id.etTypeBreed);
        etAge = findViewById(R.id.etAge);
        btnDateAcquired = findViewById(R.id.btnDateAcquired);
        btnUpdateAnimal = findViewById(R.id.btnUpdateAnimal);
        spinnerSex = findViewById(R.id.spinnerSex);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sex_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSex.setAdapter(adapter);

        existingAnimal = dbHelper.getAnimal(animalId);
        if (existingAnimal == null) {
            Toast.makeText(this, "This animal record no longer exists.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        populateFields();

        btnDateAcquired.setOnClickListener(v -> showDatePicker());
        btnUpdateAnimal.setOnClickListener(v -> updateAnimal());
    }

    private void populateFields() {
        etTagNumber.setText(existingAnimal.getTagNumber());
        etTypeBreed.setText(existingAnimal.getTypeBreed());
        etAge.setText(existingAnimal.getAge());

        dateAcquiredIso = existingAnimal.getDateAcquired();
        btnDateAcquired.setText(DateUtils.toDisplayFormat(dateAcquiredIso));

        String[] sexOptions = getResources().getStringArray(R.array.sex_options);
        for (int i = 0; i < sexOptions.length; i++) {
            if (sexOptions[i].equalsIgnoreCase(existingAnimal.getSex())) {
                spinnerSex.setSelection(i);
                break;
            }
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        if (DateUtils.isValidIsoDate(dateAcquiredIso)) {
            String[] parts = dateAcquiredIso.split("-");
            calendar.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]));
        }

        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    dateAcquiredIso = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    btnDateAcquired.setText(DateUtils.toDisplayFormat(dateAcquiredIso));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void updateAnimal() {
        String tagNumber = etTagNumber.getText().toString().trim();
        String typeBreed = etTypeBreed.getText().toString().trim();
        String age = etAge.getText().toString().trim();

        if (TextUtils.isEmpty(tagNumber)) {
            etTagNumber.setError("Tag number is required");
            etTagNumber.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(typeBreed)) {
            etTypeBreed.setError("Type / breed is required");
            etTypeBreed.requestFocus();
            return;
        }
        if (dateAcquiredIso == null) {
            Toast.makeText(this, "Please select a date acquired", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(age)) {
            etAge.setError("Age is required");
            etAge.requestFocus();
            return;
        }

        existingAnimal.setTagNumber(tagNumber);
        existingAnimal.setTypeBreed(typeBreed);
        existingAnimal.setDateAcquired(dateAcquiredIso);
        existingAnimal.setSex(spinnerSex.getSelectedItem().toString());
        existingAnimal.setAge(age);

        int rows = dbHelper.updateAnimal(existingAnimal);
        if (rows > 0) {
            Toast.makeText(this, "Animal updated", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Could not update animal. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}