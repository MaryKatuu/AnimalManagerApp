package com.example.animalmanagerapp.db;

/**
 * Small validation helpers shared across the "add"/"edit" forms so a text
 * field meant for a name/label can't be saved as pure digits.
 */
public class ValidationUtils {

    /** True if the text contains at least one letter (a-z, A-Z). */
    public static boolean containsLetter(String text) {
        if (text == null) return false;
        for (int i = 0; i < text.length(); i++) {
            if (Character.isLetter(text.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /** True if the text is a poultry-related type (case-insensitive keyword match). */
    public static boolean isPoultryRelated(String text) {
        if (text == null) return false;
        String lower = text.toLowerCase();
        return lower.contains("poultry") || lower.contains("layer") || lower.contains("broiler")
                || lower.contains("duck") || lower.contains("turkey") || lower.contains("goose")
                || lower.contains("geese");
    }
}