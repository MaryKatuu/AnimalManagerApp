package com.example.animalmanagerapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.animalmanagerapp.catalog.AnimalCatalog;
import com.example.animalmanagerapp.catalog.AnimalImageResolver;
import com.example.animalmanagerapp.db.DatabaseHelper;
import com.example.animalmanagerapp.db.DateUtils;
import com.example.animalmanagerapp.db.ValidationUtils;
import com.example.animalmanagerapp.model.Animal;

import java.util.Calendar;

public class EditAnimalActivity extends AppCompatActivity {

    private static final int REQUEST_BROWSE_ANIMALS = 200;

    private CardView cardSelectType;
    private ImageView ivSelectedTypeImage;
    private TextView tvSelectedTypeName;
    private Spinner spinnerSex;
    private EditText etVariety, etQuantity, etLayingCount, etTraysCollected, etTagNumber, etAge;
    private LinearLayout llPoultryFields;
    private Button btnDateAcquired, btnUpdateAnimal;

    private String selectedAnimalType = null;
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

        cardSelectType = findViewById(R.id.cardSelectType);
        ivSelectedTypeImage = findViewById(R.id.ivSelectedTypeImage);
        tvSelectedTypeName = findViewById(R.id.tvSelectedTypeName);
        spinnerSex = findViewById(R.id.spinnerSex);
        etVariety = findViewById(R.id.etVariety);
        etQuantity = findViewById(R.id.etQuantity);
        etLayingCount = findViewById(R.id.etLayingCount);
        etTraysCollected = findViewById(R.id.etTraysCollected);
        etTagNumber = findViewById(R.id.etTagNumber);
        etAge = findViewById(R.id.etAge);
        llPoultryFields = findViewById(R.id.llPoultryFields);
        btnDateAcquired = findViewById(R.id.btnDateAcquired);
        btnUpdateAnimal = findViewById(R.id.btnUpdateAnimal);

        ArrayAdapter<CharSequence> sexAdapter = ArrayAdapter.createFromResource(this,
                R.array.sex_options, android.R.layout.simple_spinner_item);
        sexAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSex.setAdapter(sexAdapter);

        existingAnimal = dbHelper.getAnimal(animalId);
        if (existingAnimal == null) {
            Toast.makeText(this, "This animal record no longer exists.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        populateFields();

        cardSelectType.setOnClickListener(v ->
                startActivityForResult(new Intent(EditAnimalActivity.this, BrowseAnimalsActivity.class), REQUEST_BROWSE_ANIMALS));

        btnDateAcquired.setOnClickListener(v -> showDatePicker());
        btnUpdateAnimal.setOnClickListener(v -> updateAnimal());
    }

    private void populateFields() {
        selectedAnimalType = existingAnimal.getAnimalType();
        tvSelectedTypeName.setText(selectedAnimalType);
        tvSelectedTypeName.setTextColor(getResources().getColor(R.color.text_primary));
        String overrideImage = dbHelper.getAnimalTypeImage(selectedAnimalType);
        AnimalImageResolver.applyAnimalImage(ivSelectedTypeImage, this, selectedAnimalType, overrideImage);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_BROWSE_ANIMALS && resultCode == RESULT_OK && data != null) {
            selectedAnimalType = data.getStringExtra(BrowseAnimalsActivity.EXTRA_TYPE_NAME);
            tvSelectedTypeName.setText(selectedAnimalType);
            String overrideImage = dbHelper.getAnimalTypeImage(selectedAnimalType);
            AnimalImageResolver.applyAnimalImage(ivSelectedTypeImage, this, selectedAnimalType, overrideImage);
            updatePoultryFieldsVisibility();
        }
    }

    private void updatePoultryFieldsVisibility() {
        boolean isPoultry = selectedAnimalType != null
                && AnimalCatalog.CATEGORY_POULTRY.equals(AnimalCatalog.categorize(selectedAnimalType));
        llPoultryFields.setVisibility(isPoultry ? View.VISIBLE : View.GONE);
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
        if (TextUtils.isEmpty(selectedAnimalType)) {
            Toast.makeText(this, "Please select an animal type", Toast.LENGTH_SHORT).show();
            return;
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
        existingAnimal.setAnimalType(selectedAnimalType);
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