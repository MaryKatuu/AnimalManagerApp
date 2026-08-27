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
 * Form for adding a brand-new animal record. Date acquired is picked via
 * DatePickerDialog; sex is chosen from a Spinner; remaining fields are
 * validated before saving.
 */
public class AddAnimalActivity extends AppCompatActivity {

    private EditText etTagNumber, etTypeBreed, etAge;
    private Button btnDateAcquired, btnSaveAnimal;
    private Spinner spinnerSex;

    private String dateAcquiredIso = null;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_animal);

        dbHelper = new DatabaseHelper(this);

        etTagNumber = findViewById(R.id.etTagNumber);
        etTypeBreed = findViewById(R.id.etTypeBreed);
        etAge = findViewById(R.id.etAge);
        btnDateAcquired = findViewById(R.id.btnDateAcquired);
        btnSaveAnimal = findViewById(R.id.btnSaveAnimal);
        spinnerSex = findViewById(R.id.spinnerSex);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sex_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSex.setAdapter(adapter);

        btnDateAcquired.setOnClickListener(v -> showDatePicker());
        btnSaveAnimal.setOnClickListener(v -> saveAnimal());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
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

    private void saveAnimal() {
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

        Animal animal = new Animal();
        animal.setTagNumber(tagNumber);
        animal.setTypeBreed(typeBreed);
        animal.setDateAcquired(dateAcquiredIso);
        animal.setSex(spinnerSex.getSelectedItem().toString());
        animal.setAge(age);

        long id = dbHelper.addAnimal(animal);
        if (id > 0) {
            Toast.makeText(this, "Animal saved", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Could not save animal. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}