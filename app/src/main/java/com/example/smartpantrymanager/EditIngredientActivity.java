
package com.example.smartpantrymanager;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditIngredientActivity extends Activity {

    private DatabaseHelper db;

    private EditText name;
    private EditText qty;
    private EditText expiry;
    private Spinner unit;

    private long id;

    private static final String[] UNITS = {
            "pcs", "g", "kg", "ml", "l", "tsp", "tbsp"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit);

        // Initialise database connection
        db = new DatabaseHelper(this);

        // Connect Java variables to XML components
        name = findViewById(R.id.nameInput);
        qty = findViewById(R.id.qtyInput);
        expiry = findViewById(R.id.expiryInput);
        unit = findViewById(R.id.unitInput);

        // Populate measurement units
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                UNITS
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        unit.setAdapter(adapter);

        // Determine whether adding or editing an ingredient
        id = getIntent().getLongExtra("id", 0L);

        TextView heading = findViewById(R.id.heading);

        heading.setText(
                id > 0 ? "Edit ingredient" : "Add ingredient"
        );

        // Load existing ingredient data when editing
        if (id > 0) {

            Ingredient existing = db.getIngredient(id);

            if (existing == null) {
                Toast.makeText(
                        this,
                        "Ingredient not found",
                        Toast.LENGTH_SHORT
                ).show();

                finish();
                return;
            }

            name.setText(existing.name);
            qty.setText(Ui.qty(existing.quantity));

            expiry.setText(
                    existing.expiry == null ? "" : existing.expiry
            );

            // Select the ingredient's existing unit
            for (int i = 0; i < UNITS.length; i++) {

                if (UNITS[i].equals(existing.unit)) {
                    unit.setSelection(i);
                    break;
                }
            }
        }

        // Configure the expiry date picker
        expiry.setFocusable(false);

        expiry.setOnClickListener(v -> showDatePicker());

        // Clear an optional expiry date
        findViewById(R.id.clearDate).setOnClickListener(
                v -> {
                    expiry.setText("");
                    expiry.setError(null);
                }
        );

        // Save ingredient
        findViewById(R.id.saveButton).setOnClickListener(
                v -> save()
        );
    }

    // -------------------------------------------------
    // DISPLAY DATE PICKER
    // -------------------------------------------------

    private void showDatePicker() {

        Calendar calendar = Calendar.getInstance();

        // Reopen the picker at the previously chosen date
        String currentDate = expiry.getText().toString().trim();

        if (!currentDate.isEmpty()) {

            SimpleDateFormat formatter =
                    new SimpleDateFormat(
                            "yyyy-MM-dd",
                            Locale.ROOT
                    );

            formatter.setLenient(false);

            try {
                Date parsedDate = formatter.parse(currentDate);

                if (parsedDate != null) {
                    calendar.setTime(parsedDate);
                }

            } catch (ParseException e) {
                // Fall back to today's date
            }
        }

        DatePickerDialog datePicker = new DatePickerDialog(
                this,
                (view, year, month, day) -> {

                    String selectedDate = String.format(
                            Locale.ROOT,
                            "%04d-%02d-%02d",
                            year,
                            month + 1,
                            day
                    );

                    expiry.setText(selectedDate);
                    expiry.setError(null);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePicker.show();
    }

    // -------------------------------------------------
    // VALIDATE AND SAVE INGREDIENT
    // -------------------------------------------------

    private void save() {

        // 1. Validate ingredient name
        String title = name.getText().toString().trim();

        if (title.isEmpty()) {
            name.setError("Enter an ingredient name");
            name.requestFocus();
            return;
        }

        if (title.length() > 70) {
            name.setError("Use 70 characters or fewer");
            name.requestFocus();
            return;
        }

        // 2. Validate ingredient quantity
        String rawQuantity =
                qty.getText().toString().trim();

        double amount;

        try {
            amount = Double.parseDouble(rawQuantity);

        } catch (NumberFormatException e) {

            qty.setError("Enter a valid quantity");
            qty.requestFocus();
            return;
        }

        if (!Double.isFinite(amount)
                || amount <= 0
                || amount > 1000000) {

            qty.setError(
                    "Enter a positive quantity up to 1,000,000"
            );

            qty.requestFocus();
            return;
        }

        // 3. Validate measurement unit
        Object selectedItem = unit.getSelectedItem();

        if (selectedItem == null) {

            Toast.makeText(
                    this,
                    "Please select a measurement unit",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String selectedUnit = selectedItem.toString();

        // Pieces must be whole numbers
        if (selectedUnit.equals("pcs")
                && amount != Math.floor(amount)) {

            qty.setError("Pieces must be a whole number");
            qty.requestFocus();
            return;
        }

        // 4. Validate optional expiry date
        String date =
                expiry.getText().toString().trim();

        if (!date.isEmpty()) {

            SimpleDateFormat formatter =
                    new SimpleDateFormat(
                            "yyyy-MM-dd",
                            Locale.ROOT
                    );

            formatter.setLenient(false);

            try {

                Date parsedDate = formatter.parse(date);

                if (parsedDate == null
                        || !formatter.format(parsedDate)
                        .equals(date)) {

                    expiry.setError(
                            "Use a valid YYYY-MM-DD date"
                    );

                    Toast.makeText(
                            this,
                            "Invalid expiry date",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }

            } catch (ParseException e) {

                expiry.setError(
                        "Use a valid YYYY-MM-DD date"
                );

                Toast.makeText(
                        this,
                        "Invalid expiry date",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }
        }

        // 5. Save ingredient to SQLite database
        try {

            db.saveIngredient(
                    id,
                    title,
                    amount,
                    selectedUnit,
                    date
            );

            // 6. Inform user of success
            String message = id > 0
                    ? "Ingredient updated successfully"
                    : "Ingredient added successfully";

            Toast.makeText(
                    this,
                    message,
                    Toast.LENGTH_SHORT
            ).show();

            // Return to the pantry screen
            finish();

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Unable to save ingredient. Please try again.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (db != null) {
            db.close();
        }

        super.onDestroy();
    }
}
