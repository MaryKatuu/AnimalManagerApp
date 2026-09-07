package com.example.animalmanagerapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.animalmanagerapp.db.DatabaseHelper;
import com.example.animalmanagerapp.db.DateUtils;
import com.example.animalmanagerapp.db.ValidationUtils;
import com.example.animalmanagerapp.model.Animal;

import java.util.Calendar;

public class AddAnimalActivity extends AppCompatActivity {

    private static final String OTHER_OPTION = "Other (specify)";

    private Spinner spinnerAnimalType, spinnerSex;
    private EditText etCustomAnimalType, etVariety, etQuantity, etLayingCount, etTraysCollected,
            etTagNumber, etAge;
    private LinearLayout llPoultryFields;
    private Button btnDateAcquired, btnSaveAnimal;

    private String dateAcquiredIso = null;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_animal);

        dbHelper = new DatabaseHelper(this);

        spinnerAnimalType = findViewById(R.id.spinnerAnimalType);
        spinnerSex = findViewById(R.id.spinnerSex);
        etCustomAnimalType = findViewById(R.id.etCustomAnimalType);
        etVariety = findViewById(R.id.etVariety);
        etQuantity = findViewById(R.id.etQuantity);
        etLayingCount = findViewById(R.id.etLayingCount);
        etTraysCollected = findViewById(R.id.etTraysCollected);
        etTagNumber = findViewById(R.id.etTagNumber);
        etAge = findViewById(R.id.etAge);
        llPoultryFields = findViewById(R.id.llPoultryFields);
        btnDateAcquired = findViewById(R.id.btnDateAcquired);
        btnSaveAnimal = findViewById(R.id.btnSaveAnimal);

        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.animal_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAnimalType.setAdapter(typeAdapter);

        ArrayAdapter<CharSequence> sexAdapter = ArrayAdapter.createFromResource(this,
                R.array.sex_options, android.R.layout.simple_spinner_item);
        sexAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSex.setAdapter(sexAdapter);

        spinnerAnimalType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = spinnerAnimalType.getSelectedItem().toString();
                etCustomAnimalType.setVisibility(OTHER_OPTION.equals(selected) ? View.VISIBLE : View.GONE);
                updatePoultryFieldsVisibility();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        etCustomAnimalType.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePoultryFieldsVisibility();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        btnDateAcquired.setOnClickListener(v -> showDatePicker());
        btnSaveAnimal.setOnClickListener(v -> saveAnimal());
    }

    private void updatePoultryFieldsVisibility() {
        String selected = spinnerAnimalType.getSelectedItem() != null
                ? spinnerAnimalType.getSelectedItem().toString() : "";
        String relevant = OTHER_OPTION.equals(selected) ? etCustomAnimalType.getText().toString() : selected;
        llPoultryFields.setVisibility(ValidationUtils.isPoultryRelated(relevant) ? View.VISIBLE : View.GONE);
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    dateAcquiredIso = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    btnDateAcquired.setText(DateUtils.toDisplayFormat(dateAcquiredIso));
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void saveAnimal() {
        String selectedType = spinnerAnimalType.getSelectedItem().toString();
        String animalType;

        if (OTHER_OPTION.equals(selectedType)) {
            animalType = etCustomAnimalType.getText().toString().trim();
            if (TextUtils.isEmpty(animalType)) {
                etCustomAnimalType.setError("Please enter the animal type");
                etCustomAnimalType.requestFocus();
                return;
            }
            if (!ValidationUtils.containsLetter(animalType)) {
                etCustomAnimalType.setError("Animal type must include letters, not just numbers");
                etCustomAnimalType.requestFocus();
                return;
            }
        } else {
            animalType = selectedType;
        }

        String variety = etVariety.getText().toString().trim();
        String quantity = etQuantity.getText().toString().trim();
        String tagNumber = etTagNumber.getText().toString().trim();
        String age = etAge.getText().toString().trim();

        if (TextUtils.isEmpty(variety)) {
            etVariety.setError("Variety is required (enter 'Local' or 'Unknown' if unsure)");
            etVariety.requestFocus();
            return;
        }
        if (!ValidationUtils.containsLetter(variety)) {
            etVariety.setError("Variety must include letters, not just numbers");
            etVariety.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(quantity)) {
            etQuantity.setError("Please enter the total number of animals");
            etQuantity.requestFocus();
            return;
        }
        if (!quantity.matches("\\d+") || Integer.parseInt(quantity) < 1) {
            etQuantity.setError("Enter a whole number of 1 or more");
            etQuantity.requestFocus();
            return;
        }

        String layingCount = etLayingCount.getText().toString().trim();
        String traysCollected = etTraysCollected.getText().toString().trim();
        if (llPoultryFields.getVisibility() == View.VISIBLE) {
            if (!TextUtils.isEmpty(layingCount) && !layingCount.matches("\\d+")) {
                etLayingCount.setError("Enter a whole number");
                etLayingCount.requestFocus();
                return;
            }
            if (!TextUtils.isEmpty(traysCollected) && !traysCollected.matches("\\d+")) {
                etTraysCollected.setError("Enter a whole number");
                etTraysCollected.requestFocus();
                return;
            }
        } else {
            layingCount = "";
            traysCollected = "";
        }

        if (TextUtils.isEmpty(tagNumber)) {
            etTagNumber.setError("Tag number is required");
            etTagNumber.requestFocus();
            return;
        }
        if (!tagNumber.matches("\\d+")) {
            etTagNumber.setError("Tag number must contain digits only");
            etTagNumber.requestFocus();
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
        if (!ValidationUtils.containsLetter(age)) {
            etAge.setError("Please include a unit, e.g. 2 years");
            etAge.requestFocus();
            return;
        }

        Animal animal = new Animal();
        animal.setTagNumber(tagNumber);
        animal.setAnimalType(animalType);
        animal.setVariety(variety);
        animal.setQuantity(quantity);
        animal.setDateAcquired(dateAcquiredIso);
        animal.setSex(spinnerSex.getSelectedItem().toString());
        animal.setAge(age);
        animal.setLayingCount(layingCount);
        animal.setTraysCollected(traysCollected);

        long id = dbHelper.addAnimal(animal);
        if (id > 0) {
            Toast.makeText(this, "Animal saved", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Could not save animal. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}