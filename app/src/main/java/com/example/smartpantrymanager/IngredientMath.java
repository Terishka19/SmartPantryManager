
package com.example.smartpantrymanager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Utility methods for ingredient name normalisation,
 * measurement conversion and recipe matching.
 */
public final class IngredientMath {

    private static final Map<String, String> ALIASES;

    static {
        Map<String, String> aliases = new HashMap<>();

        // Vegetables
        aliases.put("tomatoes", "tomato");
        aliases.put("potatoes", "potato");
        aliases.put("onions", "onion");
        aliases.put("carrots", "carrot");

        // Fruit
        aliases.put("bananas", "banana");
        aliases.put("apples", "apple");

        // Other ingredients
        aliases.put("eggs", "egg");
        aliases.put("tortillas", "tortilla");
        aliases.put("garlic cloves", "garlic clove");
        aliases.put("garlic", "garlic clove");
        aliases.put("cheese slices", "cheese slice");
        aliases.put("bread slices", "bread");
        aliases.put("slices of bread", "bread");
        aliases.put("rolled oats", "oats");

        ALIASES = Collections.unmodifiableMap(aliases);
    }

    private IngredientMath() {
        // Prevent instantiation
    }

    /**
     * Normalise ingredient names for consistent matching.
     */
    public static String normalize(String name) {

        if (name == null) {
            return "";
        }

        String normalized = name.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ");

        String canonicalName = ALIASES.get(normalized);

        return canonicalName != null
                ? canonicalName
                : normalized;
    }

    /**
     * Identify the measurement category.
     */
    public static String group(String unit) {

        if (unit == null) {
            return "invalid";
        }

        switch (unit.trim().toLowerCase(Locale.ROOT)) {

            case "g":
            case "kg":
                return "mass";

            case "ml":
            case "l":
            case "tsp":
            case "tbsp":
                return "volume";

            case "pcs":
                return "count";

            default:
                return "invalid";
        }
    }

    /**
     * Convert measurements into their base units:
     * Mass = grams, Volume = millilitres.
     */
    public static double base(double amount, String unit) {

        if (!validUnit(unit)) {
            throw new IllegalArgumentException(
                    "Unsupported unit: " + unit
            );
        }

        switch (unit.trim().toLowerCase(Locale.ROOT)) {

            case "kg":
                return amount * 1000.0;

            case "l":
                return amount * 1000.0;

            case "tbsp":
                return amount * 15.0;

            case "tsp":
                return amount * 5.0;

            case "g":
            case "ml":
            case "pcs":
                return amount;

            default:
                throw new IllegalArgumentException(
                        "Unsupported unit: " + unit
                );
        }
    }

    /**
     * Check whether a measurement unit is supported.
     */
    public static boolean validUnit(String unit) {
        return !group(unit).equals("invalid");
    }
}
