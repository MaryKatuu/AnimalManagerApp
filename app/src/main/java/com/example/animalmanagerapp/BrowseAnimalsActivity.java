package com.example.animalmanagerapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.animalmanagerapp.adapter.AnimalCatalogAdapter;
import com.example.animalmanagerapp.catalog.AnimalCatalog;
import com.example.animalmanagerapp.catalog.ImageStorageUtils;
import com.example.animalmanagerapp.db.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Category-filtered, searchable grid of animal types (built-in +
 * farmer-added custom ones), used to pick a type when adding/editing an
 * animal. Long-press to change a type's photo, or hide/forget a type.
 */
public class BrowseAnimalsActivity extends AppCompatActivity {

    public static final String EXTRA_TYPE_NAME = "type_name";

    private static final int REQUEST_PICK_PHOTO_FOR_TYPE = 500;
    private static final int REQUEST_ADD_CUSTOM_TYPE = 501;

    private DatabaseHelper dbHelper;
    private RecyclerView rvCatalog;
    private LinearLayout llCategoryFilters;
    private EditText etSearch;

    private AnimalCatalogAdapter adapter;
    private List<AnimalCatalogAdapter.Entry> allEntries;
    private String selectedCategory = "All";
    private String pendingImageTypeName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_animals);

        dbHelper = new DatabaseHelper(this);
        rvCatalog = findViewById(R.id.rvCatalog);
        llCategoryFilters = findViewById(R.id.llCategoryFilters);
        etSearch = findViewById(R.id.etSearch);
        Button btnAddCustomType = findViewById(R.id.btnAddCustomType);

        rvCatalog.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new AnimalCatalogAdapter(new ArrayList<>(), this::onTypeChosen, this::onTypeLongPressed);
        rvCatalog.setAdapter(adapter);

        buildCategoryFilters();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        btnAddCustomType.setOnClickListener(v ->
                startActivityForResult(new Intent(BrowseAnimalsActivity.this, AddCustomAnimalTypeActivity.class),
                        REQUEST_ADD_CUSTOM_TYPE));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllEntries();
        applyFilters();
    }

    private void loadAllEntries() {
        Set<String> hidden = dbHelper.getHiddenAnimalTypes();
        List<AnimalCatalogAdapter.Entry> combined = new ArrayList<>();

        for (String type : AnimalCatalog.getDefaultAnimalTypes()) {
            if (hidden.contains(type)) continue;
            String overrideImage = dbHelper.getAnimalTypeImage(type);
            combined.add(new AnimalCatalogAdapter.Entry(type, AnimalCatalog.categorize(type), overrideImage, false));
        }

        // Custom types are any type already used by a saved animal that isn't in the default list
        // or hidden set, or one just typed via "Add Custom Animal Type". We surface every type that
        // has an image override AND isn't a default/hidden type, plus anything the user just added.
        for (String type : getKnownCustomTypes()) {
            if (hidden.contains(type)) continue;
            if (AnimalCatalog.getDefaultAnimalTypes().contains(type)) continue;
            String overrideImage = dbHelper.getAnimalTypeImage(type);
            combined.add(new AnimalCatalogAdapter.Entry(type, AnimalCatalog.categorize(type), overrideImage, true));
        }

        allEntries = combined;
    }

    /** Custom type names come from two places: types already used by saved animals, and any type with a stored photo. */
    private Set<String> getKnownCustomTypes() {
        java.util.LinkedHashSet<String> types = new java.util.LinkedHashSet<>();
        for (com.example.animalmanagerapp.model.Animal animal : dbHelper.getAllAnimals(null)) {
            types.add(animal.getAnimalType());
        }
        for (com.example.animalmanagerapp.model.Animal animal : dbHelper.getSoldAnimals(null)) {
            types.add(animal.getAnimalType());
        }
        return types;
    }

    private void buildCategoryFilters() {
        llCategoryFilters.removeAllViews();
        List<String> categories = new ArrayList<>();
        categories.add("All");
        categories.addAll(AnimalCatalog.getCategories());

        for (String category : categories) {
            TextView pill = new TextView(this);
            pill.setText(category);
            pill.setTextColor(getResources().getColor(R.color.text_primary));
            pill.setBackgroundResource(R.drawable.filter_pill_background);
            pill.setPadding(28, 14, 28, 14);
            pill.setSelected(category.equals(selectedCategory));
            if (pill.isSelected()) pill.setTextColor(getResources().getColor(R.color.white));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 12, 0);
            pill.setLayoutParams(params);
            pill.setGravity(Gravity.CENTER);

            pill.setOnClickListener(v -> {
                selectedCategory = category;
                buildCategoryFilters();
                applyFilters();
            });

            llCategoryFilters.addView(pill);
        }
    }

    private void applyFilters() {
        if (allEntries == null) return;
        String searchTerm = etSearch.getText().toString().trim().toLowerCase();
        List<AnimalCatalogAdapter.Entry> filtered = new ArrayList<>();

        for (AnimalCatalogAdapter.Entry entry : allEntries) {
            boolean matchesCategory = "All".equals(selectedCategory) || entry.category.equals(selectedCategory);
            boolean matchesSearch = searchTerm.isEmpty() || entry.typeName.toLowerCase().contains(searchTerm);
            if (matchesCategory && matchesSearch) {
                filtered.add(entry);
            }
        }
        adapter.updateData(filtered);
    }

    private void onTypeChosen(String typeName, boolean isCustom) {
        Intent result = new Intent();
        result.putExtra(EXTRA_TYPE_NAME, typeName);
        setResult(RESULT_OK, result);
        finish();
    }

    private void onTypeLongPressed(String typeName, boolean isCustom) {
        List<String> options = new ArrayList<>();
        options.add("Change Photo");
        options.add(isCustom ? "Forget This Type" : "Hide from List");

        new AlertDialog.Builder(this)
                .setTitle(typeName)
                .setItems(options.toArray(new String[0]), (dialog, which) -> {
                    String chosen = options.get(which);
                    if ("Change Photo".equals(chosen)) {
                        pendingImageTypeName = typeName;
                        pickPhotoForType();
                    } else if ("Hide from List".equals(chosen) || "Forget This Type".equals(chosen)) {
                        confirmHideType(typeName);
                    }
                })
                .show();
    }

    private void pickPhotoForType() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_PICK_PHOTO_FOR_TYPE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_PHOTO_FOR_TYPE && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedUri = data.getData();
            if (selectedUri != null && pendingImageTypeName != null) {
                String savedPath = ImageStorageUtils.copyToInternalStorage(this, selectedUri);
                if (savedPath != null) {
                    dbHelper.setAnimalTypeImage(pendingImageTypeName, savedPath);
                    Toast.makeText(this, "Photo updated", Toast.LENGTH_SHORT).show();
                    loadAllEntries();
                    applyFilters();
                } else {
                    Toast.makeText(this, "Could not load that photo. Please try another.", Toast.LENGTH_SHORT).show();
                }
            }
            pendingImageTypeName = null;
        } else if (requestCode == REQUEST_ADD_CUSTOM_TYPE && resultCode == Activity.RESULT_OK && data != null) {
            String typeName = data.getStringExtra("type_name");
            if (typeName != null) {
                Intent result = new Intent();
                result.putExtra(EXTRA_TYPE_NAME, typeName);
                setResult(RESULT_OK, result);
                finish();
            }
        }
    }

    private void confirmHideType(String typeName) {
        new AlertDialog.Builder(this)
                .setTitle("Hide from List")
                .setMessage("Hide \"" + typeName + "\" from this picker? Any animal records already " +
                        "saved under this type are kept.")
                .setPositiveButton("Hide", (dialog, which) -> {
                    dbHelper.hideAnimalType(typeName);
                    Toast.makeText(this, "Type hidden", Toast.LENGTH_SHORT).show();
                    loadAllEntries();
                    applyFilters();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}