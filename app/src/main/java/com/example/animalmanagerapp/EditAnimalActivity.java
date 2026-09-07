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

public class EditAnimalActivity extends AppCompatActivity {

    private static final String OTHER_OPTION = "Other (specify)";

    private Spinner spinnerAnimalType, spinnerSex;
    private EditText etCustomAnimalType, etVariety, etQuantity, etLayingCount, etTraysCollected,
            etTagNumber, etAge;
    private LinearLayout llPoultryFields;
    private Button btnDateAcquired, btnUpdateAnimal;

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
        btnUpdateAnimal = findViewById(R.id.btnUpdateAnimal);

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

    private void updatePoultryFieldsVisibility() {
        String selected = spinnerAnimalType.getSelectedItem() != null
                ? spinnerAnimalType.getSelectedItem().toString() : "";
        String relevant = OTHER_OPTION.equals(selected) ? etCustomAnimalType.getText().toString() : selected;
        llPoultryFields.setVisibility(ValidationUtils.isPoultryRelated(relevant) ? View.VISIBLE : View.GONE);
    }

    private void populateFields() {
        String[] types = getResources().getStringArray(R.array.animal_types);
        int matchIndex = -1;
        for (int i = 0; i < types.length; i++) {
            if (types[i].equalsIgnoreCase(existingAnimal.getAnimalType())) {
                matchIndex = i;
                break;
            }
        }
        if (matchIndex >= 0) {
            spinnerAnimalType.setSelection(matchIndex);
            etCustomAnimalType.setVisibility(View.GONE);
        } else {
            spinnerAnimalType.setSelection(types.length - 1);
            etCustomAnimalType.setVisibility(View.VISIBLE);
            etCustomAnimalType.setText(existingAnimal.getAnimalType());
        }

        etVariety.setText(existingAnimal.getVariety());
        etQuantity.setText(existingAnimal.getQuantity());
        etLayingCount.setText(existingAnimal.getLayingCount());
        etTraysCollected.setText(existingAnimal.getTraysCollected());
        etTagNumber.setText(existingAnimal.getTagNumber());
        etAge.setText(existingAnimal.getAge());

        updatePoultryFieldsVisibility();

        String[] sexOptions = getResources().getStringArray(R.array.sex_options);
        for (int i = 0; i < sexOptions.length; i++) {
            if (sexOptions[i].equalsIgnoreCase(existingAnimal.getSex())) {
                spinnerSex.setSelection(i);
                break;
            }
        }

        dateAcquiredIso = existingAnimal.getDateAcquired();
        btnDateAcquired.setText(DateUtils.toDisplayFormat(dateAcquiredIso));
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
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void updateAnimal() {
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

        existingAnimal.setTagNumber(tagNumber);
        existingAnimal.setAnimalType(animalType);
        existingAnimal.setVariety(variety);
        existingAnimal.setQuantity(quantity);
        existingAnimal.setDateAcquired(dateAcquiredIso);
        existingAnimal.setSex(spinnerSex.getSelectedItem().toString());
        existingAnimal.setAge(age);
        existingAnimal.setLayingCount(layingCount);
        existingAnimal.setTraysCollected(traysCollected);

        int rows = dbHelper.updateAnimal(existingAnimal);
        if (rows > 0) {
            Toast.makeText(this, "Animal updated", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Could not update animal. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}