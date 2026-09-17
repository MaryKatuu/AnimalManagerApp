package com.example.animalmanagerapp.catalog;

import java.util.Arrays;
import java.util.List;

/**
 * Category definitions for animal types, and the logic that assigns
 * each built-in animal type to a category (used for filtering and
 * fallback icons in the Browse Animals picker).
 */
public class AnimalCatalog {

    public static final String CATEGORY_CATTLE = "Cattle";
    public static final String CATEGORY_GOATS_SHEEP = "Goats & Sheep";
    public static final String CATEGORY_PIGS = "Pigs";
    public static final String CATEGORY_POULTRY = "Poultry";
    public static final String CATEGORY_WORKING = "Working Animals";
    public static final String CATEGORY_OTHER = "Other";

    public static List<String> getCategories() {
        return Arrays.asList(CATEGORY_CATTLE, CATEGORY_GOATS_SHEEP, CATEGORY_PIGS,
                CATEGORY_POULTRY, CATEGORY_WORKING, CATEGORY_OTHER);
    }

    /** Returns the built-in animal types, same list as arrays.xml's animal_types (minus "Other (specify)"). */
    public static List<String> getDefaultAnimalTypes() {
        return Arrays.asList(
                "Dairy Cattle", "Beef Cattle",
                "Goats (Dairy)", "Goats (Meat)", "Sheep",
                "Pigs",
                "Poultry - Broilers", "Poultry - Layers", "Poultry - Kienyeji (Indigenous)",
                "Poultry - Ducks", "Poultry - Turkeys", "Poultry - Geese",
                "Camels", "Donkeys", "Horses",
                "Rabbits", "Bees (Beehives)"
        );
    }

    /** Assigns a category to any animal type — built-in or custom — by keyword matching. */
    public static String categorize(String animalType) {
        if (animalType == null) return CATEGORY_OTHER;
        String lower = animalType.toLowerCase();

        if (lower.contains("cattle") || lower.contains("cow") || lower.contains("bull")) {
            return CATEGORY_CATTLE;
        }
        if (lower.contains("goat") || lower.contains("sheep")) {
            return CATEGORY_GOATS_SHEEP;
        }
        if (lower.contains("pig")) {
            return CATEGORY_PIGS;
        }
        if (lower.contains("poultry") || lower.contains("chicken") || lower.contains("layer")
                || lower.contains("broiler") || lower.contains("duck") || lower.contains("turkey")
                || lower.contains("goose") || lower.contains("geese")) {
            return CATEGORY_POULTRY;
        }
        if (lower.contains("camel") || lower.contains("donkey") || lower.contains("horse")) {
            return CATEGORY_WORKING;
        }
        return CATEGORY_OTHER;
    }
}