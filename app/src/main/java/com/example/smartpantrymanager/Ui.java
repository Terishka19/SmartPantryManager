
package com.example.smartpantrymanager;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Shared design system for Smart Pantry Manager.
 *
 * Provides:
 * - Consistent application colours
 * - Density-independent measurements
 * - Reusable text and label components
 * - Rounded backgrounds
 * - Bottom navigation
 * - Ingredient quantity formatting
 */
public final class Ui {

    // ==========================================
    // COLOUR PALETTE
    // ==========================================

    public static final int GREEN = Color.rgb(29, 86, 62);

    public static final int DARK_GREEN = Color.rgb(21, 61, 45);

    public static final int MUTED = Color.rgb(107, 132, 117);

    public static final int BACKGROUND = Color.rgb(247, 249, 245);

    public static final int WHITE = Color.WHITE;

    public static final int SOFT_GREEN = Color.rgb(235, 242, 237);

    public static final int RED = Color.rgb(178, 75, 66);

    public static final int AMBER = Color.rgb(179, 122, 32);

    private Ui() {
        // Prevent this utility class from being instantiated.
    }

    // ==========================================
    // DIMENSION CONVERSION
    // ==========================================

    /**
     * Converts density-independent pixels to pixels.
     */
    public static int dp(Activity activity, float value) {

        return Math.round(
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        value,
                        activity.getResources().getDisplayMetrics()
                )
        );
    }

    // ==========================================
    // QUANTITY FORMATTING
    // ==========================================

    /**
     * Removes unnecessary decimal places.
     *
     * 2.0 becomes 2
     * 1.5 remains 1.5
     */
    public static String qty(double quantity) {

        if (Double.isFinite(quantity)
                && quantity == Math.floor(quantity)
                && quantity <= Long.MAX_VALUE
                && quantity >= Long.MIN_VALUE) {

            return String.valueOf((long) quantity);
        }

        return String.valueOf(quantity);
    }

    // ==========================================
    // REUSABLE TEXT COMPONENT
    // ==========================================

    public static TextView text(
            Activity activity,
            String content,
            int size,
            boolean bold
    ) {

        TextView textView = new TextView(activity);

        textView.setText(content);

        textView.setTextSize(
                TypedValue.COMPLEX_UNIT_SP,
                size
        );

        textView.setTextColor(DARK_GREEN);

        if (bold) {
            textView.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.BOLD
            );
        }

        int padding = dp(activity, 6);

        textView.setPadding(
                padding,
                padding,
                padding,
                padding
        );

        return textView;
    }

    // ==========================================
    // STANDARD TEXT LABEL
    // ==========================================

    /**
     * Creates a text label using font size
     * and bold formatting.
     */
    public static TextView label(
            Activity activity,
            String content,
            int size,
            boolean bold
    ) {

        TextView label = text(
                activity,
                content,
                size,
                bold
        );

        label.setTextColor(MUTED);

        return label;
    }

    // ==========================================
    // COLOURED STATUS LABEL
    // ==========================================

    /**
     * Creates coloured badges such as:
     * FRESH, USE SOON, EXPIRED and READY TO COOK.
     *
     * This overload accepts foreground and
     * background colour integers.
     */
    public static TextView label(
            Activity activity,
            String content,
            int foreground,
            int backgroundColour
    ) {

        TextView label = new TextView(activity);

        label.setText(content);
        label.setTextSize(11);

        label.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        label.setTextColor(foreground);
        label.setGravity(Gravity.CENTER);

        label.setPadding(
                dp(activity, 12),
                dp(activity, 7),
                dp(activity, 12),
                dp(activity, 7)
        );

        label.setBackground(
                background(backgroundColour, 12, activity)
        );

        return label;
    }

    // ==========================================
    // ROUNDED BACKGROUNDS
    // ==========================================

    /**
     * Creates a solid rounded background.
     */
    public static GradientDrawable rounded(
            Activity activity,
            int colour,
            float radiusDp
    ) {

        GradientDrawable drawable = new GradientDrawable();

        drawable.setShape(
                GradientDrawable.RECTANGLE
        );

        drawable.setColor(colour);

        drawable.setCornerRadius(
                dp(activity, radiusDp)
        );

        return drawable;
    }

    /**
     * Alternative method signature used by
     * MainActivity and RecipesActivity.
     */
    public static GradientDrawable background(
            int colour,
            int radiusDp,
            Activity activity
    ) {

        return rounded(activity, colour, radiusDp);
    }

    // ==========================================
    // BOTTOM NAVIGATION
    // ==========================================

    /**
     * Creates the main navigation bar.
     */
    public static void navigation(
            Activity activity,
            LinearLayout host
    ) {

        if (host == null) {
            return;
        }

        host.removeAllViews();

        LinearLayout bar = new LinearLayout(activity);

        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);

        bar.setPadding(
                dp(activity, 8),
                dp(activity, 8),
                dp(activity, 8),
                dp(activity, 8)
        );

        bar.setBackgroundColor(WHITE);

        navButton(
                activity,
                bar,
                "Pantry",
                MainActivity.class
        );

        navButton(
                activity,
                bar,
                "Recipes",
                RecipesActivity.class
        );

        navButton(
                activity,
                bar,
                "Settings",
                SettingsActivity.class
        );

        host.addView(
                bar,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );
    }

    // ==========================================
    // INDIVIDUAL NAVIGATION BUTTON
    // ==========================================

    private static void navButton(
            Activity activity,
            LinearLayout host,
            String title,
            Class<?> destination
    ) {

        Button button = new Button(activity);

        button.setText(title);
        button.setAllCaps(false);
        button.setTextSize(13);
        button.setSingleLine(true);
        button.setGravity(Gravity.CENTER);

        boolean selected =
                activity.getClass().equals(destination);

        int backgroundColour =
                selected ? GREEN : SOFT_GREEN;

        int textColour =
                selected ? WHITE : DARK_GREEN;

        button.setTextColor(textColour);

        button.setBackgroundTintList(
                ColorStateList.valueOf(backgroundColour)
        );

        button.setElevation(
                selected ? dp(activity, 3) : 0
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        0,
                        dp(activity, 56),
                        1f
                );

        params.setMargins(
                dp(activity, 3),
                0,
                dp(activity, 3),
                0
        );

        host.addView(button, params);

        button.setOnClickListener(v -> {

            if (activity.getClass().equals(destination)) {
                return;
            }

            Intent intent = new Intent(
                    activity,
                    destination
            );

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP
            );

            activity.startActivity(intent);
        });
    }
}
