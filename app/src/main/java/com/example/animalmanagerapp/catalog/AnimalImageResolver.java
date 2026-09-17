package com.example.animalmanagerapp.catalog;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.ImageView;

import com.example.animalmanagerapp.R;

import java.io.File;

public class AnimalImageResolver {

    public static void applyAnimalImage(ImageView imageView, Context context, String animalType, String imagePath) {
        if (!TextUtils.isEmpty(imagePath)) {
            File file = new File(imagePath);
            if (file.exists()) {
                imageView.setImageURI(Uri.fromFile(file));
                return;
            }
        }
        imageView.setImageResource(typeIcon(animalType));
    }

    /** Checks for a specific per-type image first, falls back to category icon. */
    public static int typeIcon(String animalType) {
        if (animalType == null) return R.drawable.ic_cat_other_animal;

        switch (animalType) {
            case "Dairy Cattle": return R.drawable.dairy_cattle;
            case "Beef Cattle": return R.drawable.beef_cattle;
            case "Goats (Dairy)": return R.drawable.goat_dairy;
            case "Goats (Meat)": return R.drawable.goat_meat;
            case "Sheep": return R.drawable.sheep;
            case "Goat": return R.drawable.goat;
            case "Meat Goat": return R.drawable.meat_goat;
            case "Pigs": return R.drawable.pig;
            case "Poultry - Broilers": return R.drawable.poultry_broilers;
            case "Poultry - Layers": return R.drawable.poultry_layers;
            case "Bees (Beehives)": return R.drawable.bees_beehives;
            case "Poultry - Geese": return R.drawable.poultry_geese;
            case "Poultry - Kienyeji (Indigenous)": return R.drawable.poultry_kienyeji;
            case "Donkeys": return R.drawable.donkeys;
            case "Camels": return R.drawable.camels;
            case "Poultry - Turkeys": return R.drawable.poultry_turkeys;
            case "Horses": return R.drawable.horses;
            case "Rabbits": return R.drawable.rabbits;

        }
        // no specific photo for this type — use the shared category icon instead
        return categoryIcon(AnimalCatalog.categorize(animalType));
    }

    public static int categoryIcon(String category) {
        if (category == null) return R.drawable.ic_cat_other_animal;
        switch (category) {
            case AnimalCatalog.CATEGORY_CATTLE: return R.drawable.ic_cat_cattle;
            case AnimalCatalog.CATEGORY_GOATS_SHEEP: return R.drawable.ic_cat_goat_sheep;
            case AnimalCatalog.CATEGORY_PIGS: return R.drawable.ic_cat_pig;
            case AnimalCatalog.CATEGORY_POULTRY: return R.drawable.ic_cat_poultry;
            case AnimalCatalog.CATEGORY_WORKING: return R.drawable.ic_cat_working_animal;
            default: return R.drawable.ic_cat_other_animal;
        }
    }
}